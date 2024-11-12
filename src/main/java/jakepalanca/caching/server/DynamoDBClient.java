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
     * Saves or updates a list of Coins in the DynamoDB table.
     *
     * @param coins the list of Coin objects to be saved or updated
     */
    public void saveOrUpdateCoins(List<Coin> coins) {
        if (coins == null || coins.isEmpty()) {
            logger.warn("No coins provided to save or update.");
            return;
        }

        logger.info("Starting saveOrUpdateCoins for {} coins.", coins.size());
        for (Coin coin : coins) {
            saveOrUpdateCoin(coin);
        }
        logger.info("Completed saveOrUpdateCoins for {} coins.", coins.size());
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

        logger.debug("Preparing to save/update coin with ID: {}", coin.getId());

        try {
            Map<String, AttributeValue> itemValues = new HashMap<>();
            itemValues.put("id", AttributeValue.builder().s(coin.getId()).build());
            logger.debug("Set 'id' attribute for coin: {}", coin.getId());

            if (coin.getSymbol() != null) {
                itemValues.put("symbol", AttributeValue.builder().s(coin.getSymbol()).build());
                logger.debug("Set 'symbol' attribute: {}", coin.getSymbol());
            }
            if (coin.getName() != null) {
                itemValues.put("name", AttributeValue.builder().s(coin.getName()).build());
                logger.debug("Set 'name' attribute: {}", coin.getName());
            }
            if (coin.getImage() != null) {
                itemValues.put("image", AttributeValue.builder().s(coin.getImage()).build());
                logger.debug("Set 'image' attribute: {}", coin.getImage());
            }
            if (coin.getAthDate() != null) {
                itemValues.put("ath_date", AttributeValue.builder().s(coin.getAthDate()).build());
                logger.debug("Set 'ath_date' attribute: {}", coin.getAthDate());
            }
            if (coin.getAtlDate() != null) {
                itemValues.put("atl_date", AttributeValue.builder().s(coin.getAtlDate()).build());
                logger.debug("Set 'atl_date' attribute: {}", coin.getAtlDate());
            }
            if (coin.getLastUpdated() != null) {
                itemValues.put("last_updated", AttributeValue.builder().s(coin.getLastUpdated()).build());
                logger.debug("Set 'last_updated' attribute: {}", coin.getLastUpdated());
            }

            if (coin.getCurrentPrice() != null) {
                itemValues.put("current_price", AttributeValue.builder().n(String.valueOf(coin.getCurrentPrice())).build());
                logger.debug("Set 'current_price' attribute: {}", coin.getCurrentPrice());
            }
            if (coin.getMarketCap() != null) {
                itemValues.put("market_cap", AttributeValue.builder().n(String.valueOf(coin.getMarketCap())).build());
                logger.debug("Set 'market_cap' attribute: {}", coin.getMarketCap());
            }
            if (coin.getMarketCapRank() != null) {
                itemValues.put("market_cap_rank", AttributeValue.builder().n(String.valueOf(coin.getMarketCapRank())).build());
                logger.debug("Set 'market_cap_rank' attribute: {}", coin.getMarketCapRank());
            }
            if (coin.getFullyDilutedValuation() != null) {
                itemValues.put("fully_diluted_valuation", AttributeValue.builder().n(String.valueOf(coin.getFullyDilutedValuation())).build());
                logger.debug("Set 'fully_diluted_valuation' attribute: {}", coin.getFullyDilutedValuation());
            }
            if (coin.getTotalVolume() != null) {
                itemValues.put("total_volume", AttributeValue.builder().n(String.valueOf(coin.getTotalVolume())).build());
                logger.debug("Set 'total_volume' attribute: {}", coin.getTotalVolume());
            }
            if (coin.getHigh24h() != null) {
                itemValues.put("high_24h", AttributeValue.builder().n(String.valueOf(coin.getHigh24h())).build());
                logger.debug("Set 'high_24h' attribute: {}", coin.getHigh24h());
            }
            if (coin.getLow24h() != null) {
                itemValues.put("low_24h", AttributeValue.builder().n(String.valueOf(coin.getLow24h())).build());
                logger.debug("Set 'low_24h' attribute: {}", coin.getLow24h());
            }
            if (coin.getPriceChange24h() != null) {
                itemValues.put("price_change_24h", AttributeValue.builder().n(String.valueOf(coin.getPriceChange24h())).build());
                logger.debug("Set 'price_change_24h' attribute: {}", coin.getPriceChange24h());
            }
            if (coin.getPriceChangePercentage24h() != null) {
                itemValues.put("price_change_percentage_24h", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage24h())).build());
                logger.debug("Set 'price_change_percentage_24h' attribute: {}", coin.getPriceChangePercentage24h());
            }
            if (coin.getPriceChangePercentage1h() != null) {
                itemValues.put("price_change_percentage_1h", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage1h())).build());
                logger.debug("Set 'price_change_percentage_1h' attribute: {}", coin.getPriceChangePercentage1h());
            }
            if (coin.getPriceChangePercentage7d() != null) {
                itemValues.put("price_change_percentage_7d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage7d())).build());
                logger.debug("Set 'price_change_percentage_7d' attribute: {}", coin.getPriceChangePercentage7d());
            }
            if (coin.getPriceChangePercentage14d() != null) {
                itemValues.put("price_change_percentage_14d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage14d())).build());
                logger.debug("Set 'price_change_percentage_14d' attribute: {}", coin.getPriceChangePercentage14d());
            }
            if (coin.getPriceChangePercentage30d() != null) {
                itemValues.put("price_change_percentage_30d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage30d())).build());
                logger.debug("Set 'price_change_percentage_30d' attribute: {}", coin.getPriceChangePercentage30d());
            }
            if (coin.getPriceChangePercentage200d() != null) {
                itemValues.put("price_change_percentage_200d", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage200d())).build());
                logger.debug("Set 'price_change_percentage_200d' attribute: {}", coin.getPriceChangePercentage200d());
            }
            if (coin.getPriceChangePercentage1y() != null) {
                itemValues.put("price_change_percentage_1y", AttributeValue.builder().n(String.valueOf(coin.getPriceChangePercentage1y())).build());
                logger.debug("Set 'price_change_percentage_1y' attribute: {}", coin.getPriceChangePercentage1y());
            }
            if (coin.getMarketCapChange24h() != null) {
                itemValues.put("market_cap_change_24h", AttributeValue.builder().n(String.valueOf(coin.getMarketCapChange24h())).build());
                logger.debug("Set 'market_cap_change_24h' attribute: {}", coin.getMarketCapChange24h());
            }
            if (coin.getMarketCapChangePercentage24h() != null) {
                itemValues.put("market_cap_change_percentage_24h", AttributeValue.builder().n(String.valueOf(coin.getMarketCapChangePercentage24h())).build());
                logger.debug("Set 'market_cap_change_percentage_24h' attribute: {}", coin.getMarketCapChangePercentage24h());
            }
            if (coin.getCirculatingSupply() != null) {
                itemValues.put("circulating_supply", AttributeValue.builder().n(String.valueOf(coin.getCirculatingSupply())).build());
                logger.debug("Set 'circulating_supply' attribute: {}", coin.getCirculatingSupply());
            }
            if (coin.getTotalSupply() != null) {
                itemValues.put("total_supply", AttributeValue.builder().n(String.valueOf(coin.getTotalSupply())).build());
                logger.debug("Set 'total_supply' attribute: {}", coin.getTotalSupply());
            }
            if (coin.getMaxSupply() != null) {
                itemValues.put("max_supply", AttributeValue.builder().n(String.valueOf(coin.getMaxSupply())).build());
                logger.debug("Set 'max_supply' attribute: {}", coin.getMaxSupply());
            }
            if (coin.getAth() != null) {
                itemValues.put("ath", AttributeValue.builder().n(String.valueOf(coin.getAth())).build());
                logger.debug("Set 'ath' attribute: {}", coin.getAth());
            }
            if (coin.getAthChangePercentage() != null) {
                itemValues.put("ath_change_percentage", AttributeValue.builder().n(String.valueOf(coin.getAthChangePercentage())).build());
                logger.debug("Set 'ath_change_percentage' attribute: {}", coin.getAthChangePercentage());
            }
            if (coin.getAtl() != null) {
                itemValues.put("atl", AttributeValue.builder().n(String.valueOf(coin.getAtl())).build());
                logger.debug("Set 'atl' attribute: {}", coin.getAtl());
            }
            if (coin.getAtlChangePercentage() != null) {
                itemValues.put("atl_change_percentage", AttributeValue.builder().n(String.valueOf(coin.getAtlChangePercentage())).build());
                logger.debug("Set 'atl_change_percentage' attribute: {}", coin.getAtlChangePercentage());
            }

            // Handle ROI if present
            Roi roi = coin.getRoi();
            if (roi != null) {
                if (roi.getCurrency() != null) {
                    itemValues.put("roi_currency", AttributeValue.builder().s(roi.getCurrency()).build());
                    logger.debug("Set 'roi_currency' attribute: {}", roi.getCurrency());
                }
                if (roi.getTimes() != null) {
                    itemValues.put("roi_times", AttributeValue.builder().n(String.valueOf(roi.getTimes())).build());
                    logger.debug("Set 'roi_times' attribute: {}", roi.getTimes());
                }
                if (roi.getPercentage() != null) {
                    itemValues.put("roi_percentage", AttributeValue.builder().n(String.valueOf(roi.getPercentage())).build());
                    logger.debug("Set 'roi_percentage' attribute: {}", roi.getPercentage());
                }
            }

            // Handle Sparkline data
            SparklineIn7d sparkline = coin.getSparklineIn7d();
            if (sparkline != null && sparkline.getPrice() != null) {
                List<AttributeValue> sparklinePrices = sparkline.getPrice().stream()
                        .map(price -> AttributeValue.builder().n(String.valueOf(price)).build())
                        .collect(Collectors.toList());

                itemValues.put("sparkline_in_7d", AttributeValue.builder().l(sparklinePrices).build());
                logger.debug("Set 'sparkline_in_7d' attribute with {} price points.", sparklinePrices.size());
            }

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(itemValues)
                    .build();
            logger.debug("Constructed PutItemRequest for coin ID: {}", coin.getId());

            PutItemResponse putItemResponse = dynamoDbClient.putItem(putItemRequest);
            logger.info("Successfully saved/updated coin with ID: {}. Consumed Capacity: {}", coin.getId(), putItemResponse.consumedCapacity());
        } catch (DynamoDbException e) {
            logger.error("DynamoDBException while saving/updating coin with ID {}: {}", coin.getId(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected exception while saving/updating coin with ID {}: {}", coin.getId(), e.getMessage(), e);
        }
    }

    /**
     * Retrieves all coins from the DynamoDB table.
     *
     * @return a list of Coin objects
     */
    public List<Coin> getAllCoins() {
        logger.info("Starting getAllCoins to retrieve all coins from DynamoDB.");
        List<Coin> coins = new ArrayList<>();
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();
            logger.debug("Constructed ScanRequest for table: {}", TABLE_NAME);

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResponse.items();
            logger.info("Scan completed. Retrieved {} items from DynamoDB.", items.size());

            for (Map<String, AttributeValue> item : items) {
                Coin coin = itemToCoin(item);
                coins.add(coin);
                logger.debug("Converted DynamoDB item to Coin object with ID: {}", coin.getId());
            }
        } catch (DynamoDbException e) {
            logger.error("DynamoDBException while retrieving coins: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected exception while retrieving coins: {}", e.getMessage(), e);
        }
        logger.info("Completed getAllCoins. Total coins retrieved: {}", coins.size());
        return coins;
    }

    /**
     * Converts a DynamoDB item map to a Coin object.
     *
     * @param item the DynamoDB item map
     * @return the corresponding Coin object
     */
    private Coin itemToCoin(Map<String, AttributeValue> item) {
        logger.debug("Converting DynamoDB item to Coin object.");
        Coin coin = new Coin();
        try {
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
            coin.setPriceChange24h(item.get("price_change_24h") != null ? Double.valueOf(item.get("price_change_24h").n()) : null);
            coin.setPriceChangePercentage24h(item.get("price_change_percentage_24h") != null ? Double.valueOf(item.get("price_change_percentage_24h").n()) : null);
            coin.setPriceChangePercentage1h(item.get("price_change_percentage_1h") != null ? Double.valueOf(item.get("price_change_percentage_1h").n()) : null);
            coin.setPriceChangePercentage7d(item.get("price_change_percentage_7d") != null ? Double.valueOf(item.get("price_change_percentage_7d").n()) : null);
            coin.setPriceChangePercentage14d(item.get("price_change_percentage_14d") != null ? Double.valueOf(item.get("price_change_percentage_14d").n()) : null);
            coin.setPriceChangePercentage30d(item.get("price_change_percentage_30d") != null ? Double.valueOf(item.get("price_change_percentage_30d").n()) : null);
            coin.setPriceChangePercentage200d(item.get("price_change_percentage_200d") != null ? Double.valueOf(item.get("price_change_percentage_200d").n()) : null);
            coin.setPriceChangePercentage1y(item.get("price_change_percentage_1y") != null ? Double.valueOf(item.get("price_change_percentage_1y").n()) : null);
            coin.setMarketCapChange24h(item.get("market_cap_change_24h") != null ? Double.valueOf(item.get("market_cap_change_24h").n()) : null);
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
                logger.debug("Set ROI for coin ID: {}", coin.getId());
            }

            // Handle Sparkline data
            if (item.containsKey("sparkline_in_7d")) {
                SparklineIn7d sparkline = new SparklineIn7d();
                List<AttributeValue> priceList = item.get("sparkline_in_7d").l();
                List<Double> prices = priceList.stream()
                        .map(attributeValue -> Double.valueOf(attributeValue.n()))
                        .collect(Collectors.toList());
                sparkline.setPrice(prices);
                coin.setSparklineIn7d(sparkline);
                logger.debug("Set Sparkline data with {} price points for coin ID: {}", prices.size(), coin.getId());
            }

            logger.debug("Conversion completed for coin ID: {}", coin.getId());
        } catch (Exception e) {
            logger.error("Error converting DynamoDB item to Coin object: {}", e.getMessage(), e);
        }

        return coin;
    }
}
