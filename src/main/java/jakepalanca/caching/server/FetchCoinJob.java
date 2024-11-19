package jakepalanca.caching.server;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * The {@code FetchCoinJob} class defines the job to fetch coin data from CoinGecko API.
 */
@DisallowConcurrentExecution
public class FetchCoinJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(FetchCoinJob.class);

    public FetchCoinJob() {
        // Default constructor
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("FetchCoinJob triggered. Starting to fetch coin data...");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        CoinGeckoClient coinGeckoClient = (CoinGeckoClient) dataMap.get("coinGeckoClient");
        BlockingQueue<List<Map<String, Object>>> queue = (BlockingQueue<List<Map<String, Object>>>) dataMap.get("coinQueue");

        if (coinGeckoClient == null || queue == null) {
            logger.error("Dependencies not found in JobDataMap.");
            throw new JobExecutionException("Missing dependencies in JobDataMap.");
        }

        try {
            // Get number of batches from environment variable
            String numberOfBatchesEnv = System.getenv("COINGECKO_NUMBER_OF_BATCHES");
            int numberOfBatches;
            if (numberOfBatchesEnv != null) {
                try {
                    numberOfBatches = Integer.parseInt(numberOfBatchesEnv);
                    logger.debug("COINGECKO_NUMBER_OF_BATCHES set to {} from environment variable.", numberOfBatches);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid COINGECKO_NUMBER_OF_BATCHES value '{}'. Defaulting to 1.", numberOfBatchesEnv);
                    numberOfBatches = 1;
                }
            } else {
                numberOfBatches = 1;
                logger.debug("COINGECKO_NUMBER_OF_BATCHES not set. Defaulting to {}.", numberOfBatches);
            }

            // Fetch top coins
            List<Map<String, Object>> topCoins = coinGeckoClient.fetchTopCoins(numberOfBatches);
            logger.info("Fetched {} top coins.", topCoins.size());

            // Data sanitization before enqueuing
            List<Map<String, Object>> sanitizedCoins = topCoins.stream()
                    .map(this::sanitizeCoinData)
                    .collect(Collectors.toList());

            // Enqueue the sanitized coins for DynamoDB update
            queue.put(sanitizedCoins);
            logger.info("Enqueued {} coins for DynamoDB update.", sanitizedCoins.size());

        } catch (IOException | ParseException | InterruptedException e) {
            logger.error("Error fetching coin data: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        } catch (Exception e) {
            logger.error("Unexpected error during FetchCoinJob execution: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private Map<String, Object> sanitizeCoinData(Map<String, Object> coinData) {
        // Similar sanitization logic as in DynamoDBClient
        // Ensure numeric fields are numbers or null
        String[] numericFields = {
                "current_price", "market_cap", "market_cap_rank", "fully_diluted_valuation",
                "total_volume", "high_24h", "low_24h", "price_change_24h",
                "price_change_percentage_24h", "price_change_percentage_24h_in_currency",
                "price_change_percentage_1h_in_currency", "price_change_percentage_7d_in_currency",
                "price_change_percentage_14d_in_currency", "price_change_percentage_30d_in_currency",
                "price_change_percentage_200d_in_currency", "price_change_percentage_1y_in_currency",
                "market_cap_change_24h", "market_cap_change_percentage_24h", "circulating_supply",
                "total_supply", "max_supply", "ath", "ath_change_percentage", "atl",
                "atl_change_percentage"
        };

        for (String field : numericFields) {
            Object value = coinData.get(field);
            if (!(value instanceof Number)) {
                if (value instanceof String) {
                    try {
                        coinData.put(field, Double.parseDouble((String) value));
                    } catch (NumberFormatException e) {
                        coinData.put(field, null);
                    }
                } else {
                    coinData.put(field, null);
                }
            }
        }

        // Handle sparkline_in_7d field, ensure it's a List<Double>
        Object sparkline = coinData.get("sparkline_in_7d");
        if (sparkline instanceof Map) {
            Map<?, ?> sparklineMap = (Map<?, ?>) sparkline;
            Object priceList = sparklineMap.get("price");
            if (priceList instanceof List) {
                coinData.put("sparkline_in_7d", priceList);
            } else {
                coinData.put("sparkline_in_7d", null);
            }
        } else if (!(sparkline instanceof List)) {
            coinData.put("sparkline_in_7d", null);
        }

        // Additional field sanitization as needed

        return coinData;
    }
}
