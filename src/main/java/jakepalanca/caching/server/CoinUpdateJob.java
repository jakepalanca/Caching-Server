// ----- CoinUpdateJob.java -----
package jakepalanca.caching.server;

import org.apache.hc.core5.http.ParseException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * The {@code CoinUpdateJob} class is a Quartz job that fetches the latest cryptocurrency data
 * from the CoinGecko API and updates the {@link CoinCache} with the retrieved data.
 */
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
        logger.info("CoinUpdateJob triggered. Starting to update the coin cache...");
        CoinCache coinCache = (CoinCache) context.getJobDetail().getJobDataMap().get("coinCache");
        CoinGeckoClient coinGeckoClient = (CoinGeckoClient) context.getJobDetail().getJobDataMap().get("coinGeckoClient");

        if (coinCache == null || coinGeckoClient == null) {
            logger.error("Dependencies not found in JobDataMap.");
            throw new JobExecutionException("Missing dependencies in JobDataMap.");
        }

        // Fetch coins in batches and update cache
        try {
            List<Coin> allCoins = coinGeckoClient.fetchTopCoins();
            coinCache.updateCache(allCoins);
            logger.info("Coin cache successfully updated.");
        } catch (IOException | ParseException e) {
            logger.error("Error occurred while updating the coin cache: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
