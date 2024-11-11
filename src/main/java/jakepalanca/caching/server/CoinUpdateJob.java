package jakepalanca.caching.server;

import jakepalanca.common.Coin;
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

@DisallowConcurrentExecution
public class CoinUpdateJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(CoinUpdateJob.class);

    public CoinUpdateJob() {
        // Default constructor
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("CoinUpdateJob triggered. Starting coin data update...");
        DynamoDBClient dynamoDBClient = (DynamoDBClient) context.getJobDetail().getJobDataMap().get("dynamoDBClient");
        CoinGeckoClient coinGeckoClient = (CoinGeckoClient) context.getJobDetail().getJobDataMap().get("coinGeckoClient");

        if (dynamoDBClient == null || coinGeckoClient == null) {
            logger.error("Dependencies not found in JobDataMap.");
            throw new JobExecutionException("Missing dependencies in JobDataMap.");
        }

        try {
            // Step 1: Fetch Top 1000 Coins (4 batches of 250)
            logger.info("Fetching top 1000 coins in batches...");
            List<Coin> topCoins = coinGeckoClient.fetchTopCoins(4);

            // Step 2: Get all coins currently in DynamoDB
            List<Coin> currentCoins = dynamoDBClient.getAllCoins();

            // Step 3: Identify coins not in the top 1000 but present in DynamoDB
            Set<String> topCoinIds = topCoins.stream()
                    .map(Coin::getId)
                    .collect(Collectors.toSet());

            List<String> remainingCoinIds = currentCoins.stream()
                    .map(Coin::getId)
                    .filter(id -> !topCoinIds.contains(id))
                    .collect(Collectors.toList());

            logger.info("Found {} coins to update beyond the top 1000.", remainingCoinIds.size());

            // Step 4: Fetch Remaining Coins in Batches of 250 with delays
            List<Coin> remainingCoins = new ArrayList<>();
            if (!remainingCoinIds.isEmpty()) {
                List<Coin> fetchedRemainingCoins = coinGeckoClient.fetchCoinsByIds(remainingCoinIds);
                remainingCoins.addAll(fetchedRemainingCoins);
            }

            // Combine Top Coins and Remaining Coins
            List<Coin> allFetchedCoins = new ArrayList<>(topCoins);
            allFetchedCoins.addAll(remainingCoins);

            // Step 5: Update DynamoDB with the latest coin data
            dynamoDBClient.saveOrUpdateCoins(allFetchedCoins);
            logger.info("Coin data updated in DynamoDB. Total coins updated: {}", allFetchedCoins.size());

        } catch (IOException | java.text.ParseException e) {
            logger.error("Error updating coin data: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
