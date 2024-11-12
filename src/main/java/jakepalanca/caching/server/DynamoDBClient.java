package jakepalanca.caching.server;

import jakepalanca.common.Coin;
import jakepalanca.common.Roi;
import jakepalanca.common.SparklineIn7d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles interactions with DynamoDB for storing and retrieving Coin data.
 */
public class DynamoDBClient {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);
    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Coins-en-USD";
    private static final int BATCH_SIZE = 25; // DynamoDB's maximum for batch write
    private static final long BATCH_DELAY_MS = 1000; // Delay between batches

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
        } catch (Exception e) {
            logger.error("Failed to initialize DynamoDBClient: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Saves or updates a list of Coins in the DynamoDB table using batch write operations.
     *
     * @param coins the list of Coin objects to be saved or updated
     */
    public void saveOrUpdateCoins(List<Coin> coins) {
        if (coins == null || coins.isEmpty()) {
            logger.warn("No coins provided to save or update.");
            return;
        }

        logger.info("Starting saveOrUpdateCoins for {} coins.", coins.size());
        List<WriteRequest> writeRequests = new ArrayList<>();

        for (Coin coin : coins) {
            if (coin == null) {
                logger.warn("Encountered a null Coin object. Skipping.");
                continue;
            }

            Map<String, AttributeValue> itemValues = mapCoinToAttributeValues(coin);
            if (itemValues == null || itemValues.isEmpty()) {
                logger.warn("Mapped itemValues are empty for coin ID: {}. Skipping.", coin.getId());
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

                // Delay between batches to avoid throttling
                try {
                    Thread.sleep(BATCH_DELAY_MS);
                } catch (InterruptedException e) {
                    logger.error("Interrupted during batch delay: {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // Execute any remaining write requests
        if (!writeRequests.isEmpty()) {
            executeBatchWrite(writeRequests);
        }

        logger.info("Completed saveOrUpdateCoins for {} coins.", coins.size());
    }

    /**
     * Maps a Coin object to DynamoDB AttributeValue map.
     *
     * @param coin the Coin object to map
     * @return a map of attribute names to AttributeValue
     */
    private Map<String, AttributeValue> mapCoinToAttributeValues(Coin coin) {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        try {
            itemValues.put("id", AttributeValue.builder().s(coin.getId()).build());

            if (coin.getSymbol() != null) {
                itemValues.put("symbol", AttributeValue.builder().s(coin.getSymbol()).build());
            }
            if (coin.getName() != null) {
                itemValues.put("name", AttributeValue.builder().s(coin.getName()).build());
            }
            if (coin.getImage() != null) {
                itemValues.put("image", AttributeValue.builder().s(coin.getImage()).build());
            }
            if (coin.getAthDate() != null) {
                itemValues.put("ath_date", AttributeValue.builder().s(coin.getAthDate()).build());
            }
            if (coin.getAtlDate() != null) {
                itemValues.put("atl_date", AttributeValue.builder().s(coin.getAtlDate()).build());
            }
            if (coin.getLastUpdated() != null) {
                itemValues.put("last_updated", AttributeValue.builder().s(coin.getLastUpdated()).build());
            }

            if (coin.getCurrentPrice() != null) {
                itemValues.put("current_price", AttributeValue.builder().n(String.valueOf(coin.getCurrentPrice())).build());
            }
            if (coin.getMarketCap() != null) {
                itemValues.put("market_cap", AttributeValue.builder().n(String.valueOf(coin.getMarketCap())).build());
            }
            if (coin.getMarketCapRank() != null) {
                itemValues.put("market_cap_rank", AttributeValue.builder().n(String.valueOf(coin.getMarketCapRank())).build());
            }
            if (coin.getFullyDilutedValuation() != null) {
                itemValues.put("fully_diluted_valuation", AttributeValue.builder().n(String.valueOf(coin.getFullyDilutedValuation())).build());
            }
            if (coin.getTotalVolume() != null) {
                itemValues.put("total_volume", AttributeValue.builder().n(String.valueOf(coin.getTotalVolume())).build());
            }
            if (coin.getHigh24h() != null) {
                itemValues.put("high_24h", AttributeValue.builder().n(String.valueOf(coin.getHigh24h())).build());
            }
            if (coin.getLow24h() != null) {
                itemValues.put("low_24h", AttributeValue.builder().n(String.valueOf(coin.getLow24h())).build());
            }

            // Handle ROI if present
            Roi roi = coin.getRoi();
            if (roi != null) {
                if (roi.getCurrency() != null) {
                    itemValues.put("roi_currency", AttributeValue.builder().s(roi.getCurrency()).build());
                }
                if (roi.getTimes() != null) {
                    itemValues.put("roi_times", AttributeValue.builder().n(String.valueOf(roi.getTimes())).build());
                }
                if (roi.getPercentage() != null) {
                    itemValues.put("roi_percentage", AttributeValue.builder().n(String.valueOf(roi.getPercentage())).build());
                }
            }

            // Handle Sparkline data
            SparklineIn7d sparkline = coin.getSparklineIn7d();
            if (sparkline != null && sparkline.getPrice() != null) {
                List<AttributeValue> sparklinePrices = sparkline.getPrice().stream()
                        .map(price -> AttributeValue.builder().n(String.valueOf(price)).build())
                        .collect(Collectors.toList());

                itemValues.put("sparkline_in_7d", AttributeValue.builder().l(sparklinePrices).build());
            }

        } catch (Exception e) {
            logger.error("Error mapping Coin ID {} to AttributeValues: {}", coin.getId(), e.getMessage(), e);
            return null;
        }

        return itemValues;
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

        BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
                .requestItems(Map.of(TABLE_NAME, writeRequests))
                .build();

        try {
            BatchWriteItemResponse response = dynamoDbClient.batchWriteItem(batchWriteItemRequest);
            logger.info("Batch write executed. Unprocessed items count: {}", response.unprocessedItems().size());

            // Handle unprocessed items
            if (!response.unprocessedItems().isEmpty()) {
                logger.warn("There are {} unprocessed items. Retrying...", response.unprocessedItems().values().stream().mapToInt(List::size).sum());
                // Implement retry logic as needed
            }
        } catch (DynamoDbException e) {
            logger.error("DynamoDBException during batch write: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected exception during batch write: {}", e.getMessage(), e);
        }
    }

    // The rest of the DynamoDBClient remains unchanged
    // ...

    /**
     * Closes the DynamoDB client.
     */
    public void close() {
        if (dynamoDbClient != null) {
            dynamoDbClient.close();
            logger.info("DynamoDBClient closed successfully.");
        }
    }
}
