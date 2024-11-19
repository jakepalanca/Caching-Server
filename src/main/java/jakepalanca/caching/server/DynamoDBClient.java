package jakepalanca.caching.server;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Handles interactions with DynamoDB for storing and retrieving Coin data.
 */
public class DynamoDBClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);
    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Coins-en-USD";
    private static final int BATCH_SIZE = 25; // DynamoDB's maximum for batch write

    // Introduce RateLimiter to control write rate
    private final RateLimiter rateLimiter;

    /**
     * Constructs a new {@code DynamoDBClient} and initializes the DynamoDB connection.
     */
    public DynamoDBClient() {
        logger.debug("Initializing DynamoDBClient.");
        try {
            // Initialize the DynamoDbClient
            dynamoDbClient = DynamoDbClient.builder()
                    .region(Region.US_EAST_1) // Replace with your AWS region
                    .build();
            logger.info("DynamoDBClient initialized successfully with region US_EAST_1.");

            // Initialize RateLimiter based on provisioned write capacity
            String writeCapacityEnv = System.getenv("DYNAMODB_WRITE_CAPACITY_UNITS");
            double writeCapacityUnits;
            if (writeCapacityEnv != null) {
                try {
                    writeCapacityUnits = Double.parseDouble(writeCapacityEnv);
                    logger.debug("DYNAMODB_WRITE_CAPACITY_UNITS set to {} from environment variable.", writeCapacityUnits);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid DYNAMODB_WRITE_CAPACITY_UNITS value '{}'. Defaulting to 50.", writeCapacityEnv);
                    writeCapacityUnits = 50;
                }
            } else {
                writeCapacityUnits = 50;
                logger.debug("DYNAMODB_WRITE_CAPACITY_UNITS not set. Defaulting to {}.", writeCapacityUnits);
            }
            this.rateLimiter = RateLimiter.create(writeCapacityUnits);
            logger.info("RateLimiter initialized with {} permits per second.", writeCapacityUnits);

        } catch (Exception e) {
            logger.error("Failed to initialize DynamoDBClient: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Saves or updates a list of coins in the DynamoDB table using batch write operations.
     *
     * @param coins the list of coin data maps to be saved or updated
     */
    public void saveOrUpdateCoins(List<Map<String, Object>> coins) {
        if (coins == null || coins.isEmpty()) {
            logger.warn("No coins provided to save or update.");
            return;
        }

        logger.info("Starting saveOrUpdateCoins for {} coins.", coins.size());
        List<WriteRequest> writeRequests = new ArrayList<>();

        for (Map<String, Object> coinData : coins) {
            if (coinData == null) {
                logger.warn("Encountered a null coin data map. Skipping.");
                continue;
            }

            Map<String, AttributeValue> itemValues = mapCoinToAttributeValues(coinData);
            if (itemValues == null || itemValues.isEmpty()) {
                logger.warn("Mapped itemValues are empty for coin data. Skipping.");
                continue;
            }

            PutRequest putRequest = PutRequest.builder()
                    .item(itemValues)
                    .build();

            WriteRequest writeRequest = WriteRequest.builder()
                    .putRequest(putRequest)
                    .build();

            writeRequests.add(writeRequest);

            // If batch size reached, execute the batch
            if (writeRequests.size() == BATCH_SIZE) {
                executeBatchWrite(new ArrayList<>(writeRequests));
                writeRequests.clear();
            }
        }

        // Execute any remaining write requests
        if (!writeRequests.isEmpty()) {
            executeBatchWrite(writeRequests);
        }

        logger.info("Completed saveOrUpdateCoins for {} coins.", coins.size());
    }

    /**
     * Maps a coin data map to DynamoDB AttributeValue map.
     *
     * @param coinData the coin data map to map
     * @return a map of attribute names to AttributeValue
     */
    private Map<String, AttributeValue> mapCoinToAttributeValues(Map<String, Object> coinData) {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        try {
            for (Map.Entry<String, Object> entry : coinData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                AttributeValue attributeValue = convertToAttributeValue(value);
                if (attributeValue != null) {
                    itemValues.put(key, attributeValue);
                }
            }
        } catch (Exception e) {
            logger.error("Error mapping coin data to AttributeValues: {}", e.getMessage(), e);
            return null;
        }

        return itemValues;
    }

    /**
     * Converts an Object to DynamoDB AttributeValue.
     *
     * @param value the object to convert
     * @return the AttributeValue representation
     */
    private AttributeValue convertToAttributeValue(Object value) {
        if (value == null) {
            return AttributeValue.builder().nul(true).build();
        } else if (value instanceof String) {
            return AttributeValue.builder().s((String) value).build();
        } else if (value instanceof Number) {
            return AttributeValue.builder().n(value.toString()).build();
        } else if (value instanceof Boolean) {
            return AttributeValue.builder().bool((Boolean) value).build();
        } else if (value instanceof List) {
            List<AttributeValue> attrValues = ((List<?>) value).stream()
                    .map(this::convertToAttributeValue)
                    .collect(Collectors.toList());
            return AttributeValue.builder().l(attrValues).build();
        } else if (value instanceof Map) {
            Map<String, AttributeValue> mapValues = ((Map<?, ?>) value).entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toString(),
                            e -> convertToAttributeValue(e.getValue())
                    ));
            return AttributeValue.builder().m(mapValues).build();
        } else {
            logger.warn("Unsupported attribute type: {}", value.getClass().getName());
            return null;
        }
    }

    /**
     * Executes a batch write operation to DynamoDB.
     *
     * @param writeRequests the list of WriteRequest objects
     */
    private void executeBatchWrite(List<WriteRequest> writeRequests) {
        if (writeRequests == null || writeRequests.isEmpty()) {
            logger.warn("No write requests to execute in batch.");
            return;
        }

        Map<String, List<WriteRequest>> requestItems = new HashMap<>();
        requestItems.put(TABLE_NAME, writeRequests);

        BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
                .requestItems(requestItems)
                .build();

        int retryCount = 0;
        int maxRetries = 5;

        while (retryCount < maxRetries) {
            // Acquire permits from rate limiter
            rateLimiter.acquire(writeRequests.size());

            try {
                BatchWriteItemResponse response = dynamoDbClient.batchWriteItem(batchWriteItemRequest);
                logger.info("Batch write executed. Unprocessed items count: {}", response.unprocessedItems().size());

                if (response.unprocessedItems().isEmpty()) {
                    logger.info("All items processed successfully.");
                    break;
                } else {
                    logger.warn("Retrying unprocessed items...");
                    batchWriteItemRequest = BatchWriteItemRequest.builder()
                            .requestItems(response.unprocessedItems())
                            .build();
                    retryCount++;
                    TimeUnit.SECONDS.sleep((long) Math.pow(2, retryCount)); // Exponential backoff
                }
            } catch (DynamoDbException e) {
                logger.error("DynamoDBException during batch write: {}", e.getMessage(), e);
                break;
            } catch (InterruptedException e) {
                logger.error("Interrupted during batch write: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Unexpected exception during batch write: {}", e.getMessage(), e);
                break;
            }
        }

        if (retryCount == maxRetries) {
            logger.error("Failed to process items after {} retries.", maxRetries);
        }
    }

    /**
     * Closes the DynamoDB client.
     */
    @Override
    public void close() {
        if (dynamoDbClient != null) {
            dynamoDbClient.close();
            logger.info("DynamoDBClient closed successfully.");
        }
    }
}