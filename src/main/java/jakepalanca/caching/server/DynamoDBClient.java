package jakepalanca.caching.server;

import jakepalanca.common.Coin;
import jakepalanca.common.Roi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBClient {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);
    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Coins";

    public DynamoDBClient() {
        // Initialize the DynamoDbClient
        dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1) // Replace with your AWS region
                .build();

        // Create table if it doesn't exist
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try {
            // List existing tables using the DynamoDbClient
            ListTablesResponse listTablesResponse = dynamoDbClient.listTables();
            List<String> existingTables = listTablesResponse.tableNames();

            if (!existingTables.contains(TABLE_NAME)) {
                logger.info("Creating DynamoDB table: {}", TABLE_NAME);

                CreateTableRequest request = CreateTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .keySchema(KeySchemaElement.builder()
                                .attributeName("id")
                                .keyType(KeyType.HASH)
                                .build())
                        .attributeDefinitions(AttributeDefinition.builder()
                                .attributeName("id")
                                .attributeType(ScalarAttributeType.S)
                                .build())
                        .provisionedThroughput(ProvisionedThroughput.builder()
                                .readCapacityUnits(5L)
                                .writeCapacityUnits(5L)
                                .build())
                        .build();

                DynamoDbWaiter dbWaiter = dynamoDbClient.waiter();
                CreateTableResponse createTableResponse = dynamoDbClient.createTable(request);
                WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(r -> r.tableName(TABLE_NAME));
                waiterResponse.matched().response().ifPresent(table -> logger.info("Table '{}' created successfully.", TABLE_NAME));
            } else {
                logger.info("Table '{}' already exists.", TABLE_NAME);
            }
        } catch (Exception e) {
            logger.error("Error creating table '{}': {}", TABLE_NAME, e.getMessage(), e);
        }
    }

    /**
     * Saves or updates a list of Coins in the DynamoDB table.
     *
     * @param coins the list of Coin objects to be saved or updated
     */
    public void saveOrUpdateCoins(List<Coin> coins) {
        if (coins == null || coins.isEmpty()) {
            logger.warn("No coins provided to save or update.");
            return;
        }

        for (Coin coin : coins) {
            saveOrUpdateCoin(coin);
        }
    }

    /**
     * Saves or updates a single Coin in the DynamoDB table.
     *
     * @param coin the Coin object to be saved or updated
     */
    public void saveOrUpdateCoin(Coin coin) {
        if (coin == null) {
            logger.warn("Attempted to save a null Coin object. Operation aborted.");
            return;
        }

        try {
            Map<String, AttributeValue> itemValues = new HashMap<>();
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
            if (coin.getPriceChange24hInCurrency() != null) {
                itemValues.put("price_change_24h_in_currency", AttributeValue.builder().n(String.valueOf(coin.getPriceChange24hInCurrency())).build());
            }
            if (coin.getPriceChangePercentage24h() != null) {
                itemValues.put("price_change_percentage_24h", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage24h())).build());
            }
            if (coin.getPriceChangePercentage1h() != null) {
                itemValues.put("price_change_percentage_1h", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage1h())).build());
            }
            if (coin.getPriceChangePercentage7d() != null) {
                itemValues.put("price_change_percentage_7d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage7d())).build());
            }
            if (coin.getPriceChangePercentage14d() != null) {
                itemValues.put("price_change_percentage_14d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage14d())).build());
            }
            if (coin.getPriceChangePercentage30d() != null) {
                itemValues.put("price_change_percentage_30d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage30d())).build());
            }
            if (coin.getPriceChangePercentage200d() != null) {
                itemValues.put("price_change_percentage_200d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage200d())).build());
            }
            if (coin.getPriceChangePercentage1y() != null) {
                itemValues.put("price_change_percentage_1y", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage1y())).build());
            }
            if (coin.getMarketCapChange24hInCurrency() != null) {
                itemValues.put("market_cap_change_24h_in_currency", AttributeValue.builder().n(String.valueOf(coin.getMarketCapChange24hInCurrency())).build());
            }
            if (coin.getMarketCapChangePercentage24h() != null) {
                itemValues.put("market_cap_change_percentage_24h", AttributeValue.builder().n(String.valueOf(coin.getMarketCapChangePercentage24h())).build());
            }
            if (coin.getCirculatingSupply() != null) {
                itemValues.put("circulating_supply", AttributeValue.builder().n(String.valueOf(coin.getCirculatingSupply())).build());
            }
            if (coin.getTotalSupply() != null) {
                itemValues.put("total_supply", AttributeValue.builder().n(String.valueOf(coin.getTotalSupply())).build());
            }
            if (coin.getMaxSupply() != null) {
                itemValues.put("max_supply", AttributeValue.builder().n(String.valueOf(coin.getMaxSupply())).build());
            }
            if (coin.getAth() != null) {
                itemValues.put("ath", AttributeValue.builder().n(String.valueOf(coin.getAth())).build());
            }
            if (coin.getAthChangePercentage() != null) {
                itemValues.put("ath_change_percentage", AttributeValue.builder().n(String.valueOf(coin.getAthChangePercentage())).build());
            }
            if (coin.getAtl() != null) {
                itemValues.put("atl", AttributeValue.builder().n(String.valueOf(coin.getAtl())).build());
            }
            if (coin.getAtlChangePercentage() != null) {
                itemValues.put("atl_change_percentage", AttributeValue.builder().n(String.valueOf(coin.getAtlChangePercentage())).build());
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

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(itemValues)
                    .build();

            PutItemResponse putItemResponse = dynamoDbClient.putItem(putItemRequest);
            logger.info("Successfully saved/updated coin with ID: {}", coin.getId());

        } catch (Exception e) {
            logger.error("Failed to save/update coin with ID {}: {}", coin.getId(), e.getMessage(), e);
        }
    }

    /**
     * Retrieves all coins from the DynamoDB table.
     *
     * @return a list of Coin objects
     */
    public List<Coin> getAllCoins() {
        List<Coin> coins = new ArrayList<>();
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResponse.items();

            for (Map<String, AttributeValue> item : items) {
                coins.add(itemToCoin(item));
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve coins: {}", e.getMessage(), e);
        }
        return coins;
    }

    /**
     * Converts a DynamoDB item map to a Coin object.
     *
     * @param item the DynamoDB item map
     * @return the corresponding Coin object
     */
    private Coin itemToCoin(Map<String, AttributeValue> item) {
        Coin coin = new Coin();
        coin.setId(item.get("id").s());
        coin.setSymbol(item.get("symbol") != null ? item.get("symbol").s() : null);
        coin.setName(item.get("name") != null ? item.get("name").s() : null);
        coin.setImage(item.get("image") != null ? item.get("image").s() : null);
        coin.setCurrentPrice(item.get("current_price") != null ? Double.valueOf(item.get("current_price").n()) : null);
        coin.setMarketCap(item.get("market_cap") != null ? Long.valueOf(item.get("market_cap").n()) : null);
        coin.setMarketCapRank(item.get("market_cap_rank") != null ? Integer.valueOf(item.get("market_cap_rank").n()) : null);
        coin.setFullyDilutedValuation(item.get("fully_diluted_valuation") != null ? Long.valueOf(item.get("fully_diluted_valuation").n()) : null);
        coin.setTotalVolume(item.get("total_volume") != null ? Long.valueOf(item.get("total_volume").n()) : null);
        coin.setHigh24h(item.get("high_24h") != null ? Double.valueOf(item.get("high_24h").n()) : null);
        coin.setLow24h(item.get("low_24h") != null ? Double.valueOf(item.get("low_24h").n()) : null);
        coin.setPriceChange24hInCurrency(item.get("price_change_24h_in_currency") != null ? Double.valueOf(item.get("price_change_24h_in_currency").n()) : null);
        coin.setPriceChangePercentage24h(item.get("price_change_percentage_24h") != null ? Double.valueOf(item.get("price_change_percentage_24h").n()) : null);
        coin.setPriceChangePercentage1h(item.get("price_change_percentage_1h") != null ? Double.valueOf(item.get("price_change_percentage_1h").n()) : null);
        coin.setPriceChangePercentage7d(item.get("price_change_percentage_7d") != null ? Double.valueOf(item.get("price_change_percentage_7d").n()) : null);
        coin.setPriceChangePercentage14d(item.get("price_change_percentage_14d") != null ? Double.valueOf(item.get("price_change_percentage_14d").n()) : null);
        coin.setPriceChangePercentage30d(item.get("price_change_percentage_30d") != null ? Double.valueOf(item.get("price_change_percentage_30d").n()) : null);
        coin.setPriceChangePercentage200d(item.get("price_change_percentage_200d") != null ? Double.valueOf(item.get("price_change_percentage_200d").n()) : null);
        coin.setPriceChangePercentage1y(item.get("price_change_percentage_1y") != null ? Double.valueOf(item.get("price_change_percentage_1y").n()) : null);
        coin.setMarketCapChange24hInCurrency(item.get("market_cap_change_24h_in_currency") != null ? Long.valueOf(item.get("market_cap_change_24h_in_currency").n()) : null);
        coin.setMarketCapChangePercentage24h(item.get("market_cap_change_percentage_24h") != null ? Double.valueOf(item.get("market_cap_change_percentage_24h").n()) : null);
        coin.setCirculatingSupply(item.get("circulating_supply") != null ? Double.valueOf(item.get("circulating_supply").n()) : null);
        coin.setTotalSupply(item.get("total_supply") != null ? Double.valueOf(item.get("total_supply").n()) : null);
        coin.setMaxSupply(item.get("max_supply") != null ? Double.valueOf(item.get("max_supply").n()) : null);
        coin.setAth(item.get("ath") != null ? Double.valueOf(item.get("ath").n()) : null);
        coin.setAthChangePercentage(item.get("ath_change_percentage") != null ? Double.valueOf(item.get("ath_change_percentage").n()) : null);
        coin.setAthDate(item.get("ath_date") != null ? item.get("ath_date").s() : null);
        coin.setAtl(item.get("atl") != null ? Double.valueOf(item.get("atl").n()) : null);
        coin.setAtlChangePercentage(item.get("atl_change_percentage") != null ? Double.valueOf(item.get("atl_change_percentage").n()) : null);
        coin.setAtlDate(item.get("atl_date") != null ? item.get("atl_date").s() : null);
        coin.setLastUpdated(item.get("last_updated") != null ? item.get("last_updated").s() : null);

        // Handle ROI if present
        if (item.containsKey("roi_currency") || item.containsKey("roi_times") || item.containsKey("roi_percentage")) {
            Roi roi = new Roi();
            roi.setCurrency(item.get("roi_currency") != null ? item.get("roi_currency").s() : null);
            roi.setTimes(item.get("roi_times") != null ? Double.valueOf(item.get("roi_times").n()) : null);
            roi.setPercentage(item.get("roi_percentage") != null ? Double.valueOf(item.get("roi_percentage").n()) : null);
            coin.setRoi(roi);
        }

        return coin;
    }
}