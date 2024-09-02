package jakepalanca.cryptocache.javalin;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.List;

/**
 * The {@code CoinUpdateJob} class is a Quartz job that fetches the latest cryptocurrency data
 * from the CoinGecko API and updates the {@link CoinCache} with the retrieved data.
 *
 * <p>This job is intended to be scheduled at regular intervals to ensure the coin cache remains
 * up-to-date with the latest market data.</p>
 *
 * <p>The {@code CoinUpdateJob} relies on two dependencies that are passed via the Quartz {@link JobExecutionContext}:
 * <ul>
 *   <li>{@code coinCache} - The {@link CoinCache} instance to be updated.</li>
 *   <li>{@code coinGeckoClient} - The {@link CoinGeckoClient} instance used to fetch the latest coin data.</li>
 * </ul>
 */
public class CoinUpdateJob implements Job {

    /**
     * Constructs a new {@code CoinUpdateJob} instance.
     * This job is responsible for updating the coin cache with the latest data from the CoinGecko API.
     */
    public CoinUpdateJob() {
        // Default constructor
    }

    /**
     * Executes the job to update the coin cache by fetching the latest data from CoinGecko.
     *
     * <p>This method is triggered by the Quartz scheduler and performs the following steps:
     * <ol>
     *   <li>Fetches the {@code CoinCache} and {@code CoinGeckoClient} instances from the job's context.</li>
     *   <li>Fetches the top coins from the CoinGecko API using the {@code CoinGeckoClient}.</li>
     *   <li>Updates the {@code CoinCache} with the fetched data.</li>
     *   <li>Handles any exceptions that occur during the process by logging the error and rethrowing it as a {@link JobExecutionException}.</li>
     * </ol>
     *
     * @param context the Quartz {@link JobExecutionContext} which provides access to the job's runtime environment
     * @throws JobExecutionException if an error occurs during the job execution
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("CoinUpdateJob triggered. Starting to update the coin cache...");
        CoinCache coinCache = (CoinCache) context.getJobDetail().getJobDataMap().get("coinCache");
        CoinGeckoClient coinGeckoClient = (CoinGeckoClient) context.getJobDetail().getJobDataMap().get("coinGeckoClient");

        // Fetch coins in batches and update cache
        try {
            List<Coin> allCoins = coinGeckoClient.fetchTopCoins();
            coinCache.updateCache(allCoins);
            System.out.println("Coin cache successfully updated.");
        } catch (IOException | ParseException e) {
            System.err.println("Error occurred while updating the coin cache: " + e.getMessage());
            e.printStackTrace();
            throw new JobExecutionException(e);
        }
    }
}
