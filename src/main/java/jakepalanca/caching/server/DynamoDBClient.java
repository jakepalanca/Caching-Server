// ----- DynamoDBClient.java -----
package jakepalanca.caching.server;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

import jakepalanca.common.Coin;
import jakepalanca.common.Roi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DynamoDBClient {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClient.class);
    private final AmazonDynamoDB client;
    private final DynamoDB dynamoDB;
    private final Table coinTable;
    private static final String TABLE_NAME = "Coins";

    public DynamoDBClient() {
        // Initialize the AmazonDynamoDB client
        client = AmazonDynamoDBClientBuilder.standard()
                // If you're running on EC2 with appropriate IAM role, you can omit credentials
                .withRegion("us-east-1") // Replace with your AWS region
                .build();

        this.dynamoDB = new DynamoDB(client);

        // Create table if it doesn't exist
        createTableIfNotExists();

        this.coinTable = dynamoDB.getTable(TABLE_NAME);
    }

    private void createTableIfNotExists() {
        try {
            // List existing tables using the AmazonDynamoDB client
            ListTablesResult listTablesResult = client.listTables();
            List<String> existingTables = listTablesResult.getTableNames();

            if (!existingTables.contains(TABLE_NAME)) {
                logger.info("Creating DynamoDB table: {}", TABLE_NAME);

                List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
                attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("S"));

                List<KeySchemaElement> keySchema = new ArrayList<>();
                keySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH)); // Partition key

                CreateTableRequest request = new CreateTableRequest()
                        .withTableName(TABLE_NAME)
                        .withKeySchema(keySchema)
                        .withAttributeDefinitions(attributeDefinitions)
                        .withProvisionedThroughput(new ProvisionedThroughput()
                                .withReadCapacityUnits(5L)
                                .withWriteCapacityUnits(5L));

                Table table = dynamoDB.createTable(request);
                table.waitForActive();
                logger.info("Table '{}' created successfully.", TABLE_NAME);
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
            // Initialize the DynamoDB Item with the primary key
            Item item = new Item()
                    .withPrimaryKey("id", coin.getId());

            // Add non-null String attributes
            if (coin.getSymbol() != null) {
                item.withString("symbol", coin.getSymbol());
            }
            if (coin.getName() != null) {
                item.withString("name", coin.getName());
            }
            if (coin.getImage() != null) {
                item.withString("image", coin.getImage());
            }
            if (coin.getAthDate() != null) {
                item.withString("ath_date", coin.getAthDate());
            }
            if (coin.getAtlDate() != null) {
                item.withString("atl_date", coin.getAtlDate());
            }
            if (coin.getLastUpdated() != null) {
                item.withString("last_updated", coin.getLastUpdated());
            }

            // Add non-null Number attributes
            if (coin.getCurrentPrice() != null) {
                item.withNumber("current_price", coin.getCurrentPrice());
            }
            if (coin.getMarketCap() != null) {
                item.withNumber("market_cap", coin.getMarketCap());
            }
            if (coin.getMarketCapRank() != null) {
                item.withNumber("market_cap_rank", coin.getMarketCapRank());
            }
            if (coin.getFullyDilutedValuation() != null) {
                item.withNumber("fully_diluted_valuation", coin.getFullyDilutedValuation());
            }
            if (coin.getTotalVolume() != null) {
                item.withNumber("total_volume", coin.getTotalVolume());
            }
            if (coin.getHigh24h() != null) {
                item.withNumber("high_24h", coin.getHigh24h());
            }
            if (coin.getLow24h() != null) {
                item.withNumber("low_24h", coin.getLow24h());
            }
            if (coin.getPriceChange24hInCurrency() != null) {
                item.withNumber("price_change_24h_in_currency", coin.getPriceChange24hInCurrency());
            }
            if (coin.getPriceChangePercentage24h() != null) {
                item.withNumber("price_change_percentage_24h", coin.getPriceChangePercentage24h());
            }
            if (coin.getPriceChangePercentage1h() != null) {
                item.withNumber("price_change_percentage_1h", coin.getPriceChangePercentage1h());
            }
            if (coin.getPriceChangePercentage7d() != null) {
                item.withNumber("price_change_percentage_7d", coin.getPriceChangePercentage7d());
            }
            if (coin.getPriceChangePercentage14d() != null) {
                item.withNumber("price_change_percentage_14d", coin.getPriceChangePercentage14d());
            }
            if (coin.getPriceChangePercentage30d() != null) {
                item.withNumber("price_change_percentage_30d", coin.getPriceChangePercentage30d());
            }
            if (coin.getPriceChangePercentage200d() != null) {
                item.withNumber("price_change_percentage_200d", coin.getPriceChangePercentage200d());
            }
            if (coin.getPriceChangePercentage1y() != null) {
                item.withNumber("price_change_percentage_1y", coin.getPriceChangePercentage1y());
            }
            if (coin.getMarketCapChange24hInCurrency() != null) {
                item.withNumber("market_cap_change_24h_in_currency", coin.getMarketCapChange24hInCurrency());
            }
            if (coin.getMarketCapChangePercentage24h() != null) {
                item.withNumber("market_cap_change_percentage_24h", coin.getMarketCapChangePercentage24h());
            }
            if (coin.getCirculatingSupply() != null) {
                item.withNumber("circulating_supply", coin.getCirculatingSupply());
            }
            if (coin.getTotalSupply() != null) {
                item.withNumber("total_supply", coin.getTotalSupply());
            }
            if (coin.getMaxSupply() != null) {
                item.withNumber("max_supply", coin.getMaxSupply());
            }
            if (coin.getAth() != null) {
                item.withNumber("ath", coin.getAth());
            }
            if (coin.getAthChangePercentage() != null) {
                item.withNumber("ath_change_percentage", coin.getAthChangePercentage());
            }
            if (coin.getAtl() != null) {
                item.withNumber("atl", coin.getAtl());
            }
            if (coin.getAtlChangePercentage() != null) {
                item.withNumber("atl_change_percentage", coin.getAtlChangePercentage());
            }

            // Handle ROI if present
            Roi roi = coin.getRoi();
            if (roi != null) {
                if (roi.getCurrency() != null) {
                    item.withString("roi_currency", roi.getCurrency());
                }
                if (roi.getTimes() != null) {
                    item.withNumber("roi_times", roi.getTimes());
                }
                if (roi.getPercentage() != null) {
                    item.withNumber("roi_percentage", roi.getPercentage());
                }
            }

            // Create a PutItemSpec with the item
            PutItemSpec putItemSpec = new PutItemSpec().withItem(item);

            // Execute the put operation
            PutItemOutcome outcome = coinTable.putItem(putItemSpec);

            // Log the successful save/update
            logger.info("Successfully saved/updated coin with ID: {}", coin.getId());

        } catch (Exception e) {
            // Log the error with coin ID and exception details
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
        ItemCollection<ScanOutcome> items = coinTable.scan();

        for (Item item : items) {
            Coin coin = itemToCoin(item);
            coins.add(coin);
        }

        return coins;
    }

    /**
     * Retrieves coins by a list of IDs from the DynamoDB table.
     *
     * @param ids the list of coin IDs to retrieve
     * @return a list of Coin objects
     */
    public List<Coin> getCoinsByIds(List<String> ids) {
        List<Coin> coins = new ArrayList<>();
        for (String id : ids) {
            Item item = coinTable.getItem("id", id);
            if (item != null) {
                Coin coin = itemToCoin(item);
                coins.add(coin);
            }
        }
        return coins;
    }

    /**
     * Converts a DynamoDB Item to a Coin object.
     *
     * @param item the DynamoDB Item
     * @return the corresponding Coin object
     */
    private Coin itemToCoin(Item item) {
        Coin coin = new Coin();
        coin.setId(item.getString("id"));
        coin.setSymbol(item.getString("symbol"));
        coin.setName(item.getString("name"));
        coin.setImage(item.getString("image"));
        coin.setCurrentPrice((item.getNumber("current_price") != null) ? item.getNumber("current_price").doubleValue() : null);
        coin.setMarketCap((item.getNumber("market_cap") != null) ? item.getNumber("market_cap").longValue() : null);
        coin.setMarketCapRank((item.getNumber("market_cap_rank") != null) ? item.getNumber("market_cap_rank").intValue() : null);
        coin.setFullyDilutedValuation((item.getNumber("fully_diluted_valuation") != null) ? item.getNumber("fully_diluted_valuation").longValue() : null);
        coin.setTotalVolume((item.getNumber("total_volume") != null) ? item.getNumber("total_volume").longValue() : null);
        coin.setHigh24h((item.getNumber("high_24h") != null) ? item.getNumber("high_24h").doubleValue() : null);
        coin.setLow24h((item.getNumber("low_24h") != null) ? item.getNumber("low_24h").doubleValue() : null);
        coin.setPriceChange24hInCurrency((item.getNumber("price_change_24h_in_currency") != null) ? item.getNumber("price_change_24h_in_currency").doubleValue() : null);
        coin.setPriceChangePercentage24h((item.getNumber("price_change_percentage_24h") != null) ? item.getNumber("price_change_percentage_24h").doubleValue() : null);
        coin.setPriceChangePercentage1h((item.getNumber("price_change_percentage_1h") != null) ? item.getNumber("price_change_percentage_1h").doubleValue() : null);
        coin.setPriceChangePercentage7d((item.getNumber("price_change_percentage_7d") != null) ? item.getNumber("price_change_percentage_7d").doubleValue() : null);
        coin.setPriceChangePercentage14d((item.getNumber("price_change_percentage_14d") != null) ? item.getNumber("price_change_percentage_14d").doubleValue() : null);
        coin.setPriceChangePercentage30d((item.getNumber("price_change_percentage_30d") != null) ? item.getNumber("price_change_percentage_30d").doubleValue() : null);
        coin.setPriceChangePercentage200d((item.getNumber("price_change_percentage_200d") != null) ? item.getNumber("price_change_percentage_200d").doubleValue() : null);
        coin.setPriceChangePercentage1y((item.getNumber("price_change_percentage_1y") != null) ? item.getNumber("price_change_percentage_1y").doubleValue() : null);
        coin.setMarketCapChange24hInCurrency((item.getNumber("market_cap_change_24h_in_currency") != null) ? item.getNumber("market_cap_change_24h_in_currency").longValue() : null);
        coin.setMarketCapChangePercentage24h((item.getNumber("market_cap_change_percentage_24h") != null) ? item.getNumber("market_cap_change_percentage_24h").doubleValue() : null);
        coin.setCirculatingSupply((item.getNumber("circulating_supply") != null) ? item.getNumber("circulating_supply").doubleValue() : null);
        coin.setTotalSupply((item.getNumber("total_supply") != null) ? item.getNumber("total_supply").doubleValue() : null);
        coin.setMaxSupply((item.getNumber("max_supply") != null) ? item.getNumber("max_supply").doubleValue() : null);
        coin.setAth((item.getNumber("ath") != null) ? item.getNumber("ath").doubleValue() : null);
        coin.setAthChangePercentage((item.getNumber("ath_change_percentage") != null) ? item.getNumber("ath_change_percentage").doubleValue() : null);
        coin.setAthDate(item.getString("ath_date"));
        coin.setAtl((item.getNumber("atl") != null) ? item.getNumber("atl").doubleValue() : null);
        coin.setAtlChangePercentage((item.getNumber("atl_change_percentage") != null) ? item.getNumber("atl_change_percentage").doubleValue() : null);
        coin.setAtlDate(item.getString("atl_date"));

        // Handle ROI if present
        if (item.hasAttribute("roi_currency") || item.hasAttribute("roi_times") || item.hasAttribute("roi_percentage")) {
            Roi roi = new Roi();
            roi.setCurrency(item.getString("roi_currency"));
            roi.setTimes((item.getNumber("roi_times") != null) ? item.getNumber("roi_times").doubleValue() : null);
            roi.setPercentage((item.getNumber("roi_percentage") != null) ? item.getNumber("roi_percentage").doubleValue() : null);
            coin.setRoi(roi);
        }

        coin.setLastUpdated(item.getString("last_updated"));

        return coin;
    }
}
