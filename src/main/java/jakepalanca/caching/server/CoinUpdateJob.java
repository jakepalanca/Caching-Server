// ----- CoinUpdateJob.java -----
package jakepalanca.caching.server;

import org.apache.hc.core5.http.ParseException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
// Other imports...

/**
 * The {@code CoinUpdateJob} class implements the Quartz {@link Job} interface
 * and is responsible for periodically updating the coin cache by fetching
 * the latest data from the CoinGecko API.
 *
 * <p>This job fetches the top coins and updates any remaining coins in the cache
 * that are beyond the top list. It ensures that the coin cache stays up-to-date
 * with the latest market data.</p>
 */
@DisallowConcurrentExecution
public class CoinUpdateJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(CoinUpdateJob.class);

    /**
     * Constructs a new {@code CoinUpdateJob} instance.
     */
    public CoinUpdateJob() {
        // Default constructor
    }

    /**
     * Executes the job to update the coin cache by fetching the latest data from CoinGecko.
     *
     * @param context the Quartz {@link JobExecutionContext} which provides access to the job's runtime environment
     * @throws JobExecutionException if an error occurs during the job execution
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("CoinUpdateJob triggered. Starting coin cache update...");
        CoinCache coinCache = (CoinCache) context.getJobDetail().getJobDataMap().get("coinCache");
        CoinGeckoClient coinGeckoClient = (CoinGeckoClient) context.getJobDetail().getJobDataMap().get("coinGeckoClient");

        if (coinCache == null || coinGeckoClient == null) {
            logger.error("Dependencies not found in JobDataMap.");
            throw new JobExecutionException("Missing dependencies in JobDataMap.");
        }

        try {
            // Step 1: Fetch Top 1000 Coins (4 batches of 250)
            logger.info("Fetching top 1000 coins in batches...");
            List<Coin> topCoins = coinGeckoClient.fetchTopCoins(4);

            // Step 2: Identify Remaining Coins in Cache (beyond top 1000)
            List<Coin> currentCacheCoins = coinCache.getAllCoins();
            Set<String> topCoinIds = topCoins.stream()
                    .map(Coin::getCoinId)
                    .collect(Collectors.toSet());

            List<String> remainingCoinIds = currentCacheCoins.stream()
                    .map(Coin::getCoinId)
                    .filter(id -> !topCoinIds.contains(id))
                    .collect(Collectors.toList());

            logger.info("Found {} coins to update beyond the top 1000.", remainingCoinIds.size());

            // Step 3: Fetch Remaining Coins in Batches of 250 with delays
            List<Coin> remainingCoins = new ArrayList<>();
            if (!remainingCoinIds.isEmpty()) {
                int totalBatches = (int) Math.ceil((double) remainingCoinIds.size() / 250);
                logger.info("Fetching remaining coins in {} batches...", totalBatches);

                for (int i = 0; i < totalBatches; i++) {
                    int fromIndex = i * 250;
                    int toIndex = Math.min(fromIndex + 250, remainingCoinIds.size());
                    List<String> batchIds = remainingCoinIds.subList(fromIndex, toIndex);

                    logger.info("Fetching batch {}/{} for remaining coins.", i + 1, totalBatches);
                    List<Coin> batchCoins = coinGeckoClient.fetchCoinsByIds(batchIds);
                    remainingCoins.addAll(batchCoins);

                    // Sleep between batches to respect rate limits
                    if (i < totalBatches - 1) {
                        logger.info("Sleeping for {} ms before next batch...", CoinGeckoClient.REQUEST_DELAY_MS);
                        try {
                            Thread.sleep(CoinGeckoClient.REQUEST_DELAY_MS);
                        } catch (InterruptedException e) {
                            logger.error("Sleep interrupted: {}", e.getMessage());
                            Thread.currentThread().interrupt();
                            throw new JobExecutionException("Interrupted during batch fetching", e);
                        }
                    }
                }
                logger.info("Fetched {} additional coins.", remainingCoins.size());
            }

            // Combine Top Coins and Remaining Coins
            List<Coin> allFetchedCoins = new ArrayList<>(topCoins);
            allFetchedCoins.addAll(remainingCoins);

            // Step 4: Update the Cache
            coinCache.updateCache(allFetchedCoins);
            logger.info("Coin cache updated. Total coins in cache: {}", coinCache.getAllCoins().size());

        } catch (IOException | java.text.ParseException e) {
            logger.error("Error updating coin cache: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
