package jakepalanca.caching.server;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

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
            // Fetch top coins
            List<Map<String, Object>> topCoins = coinGeckoClient.fetchTopCoins(4);
            logger.info("Fetched {} top coins.", topCoins.size());

            // Enqueue the fetched coins for DynamoDB update
            queue.put(topCoins);
            logger.info("Enqueued {} coins for DynamoDB update.", topCoins.size());

        } catch (IOException | ParseException | InterruptedException e) {
            logger.error("Error fetching coin data: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        } catch (Exception e) {
            logger.error("Unexpected error during FetchCoinJob execution: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
