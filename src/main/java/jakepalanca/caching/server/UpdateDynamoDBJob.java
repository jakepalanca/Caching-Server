package jakepalanca.caching.server;

import jakepalanca.common.Coin;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * The {@code UpdateDynamoDBJob} class defines the job to update DynamoDB with fetched coin data.
 */
@DisallowConcurrentExecution
public class UpdateDynamoDBJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(UpdateDynamoDBJob.class);

    public UpdateDynamoDBJob() {
        // Default constructor
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("UpdateDynamoDBJob triggered. Starting DynamoDB update...");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        DynamoDBClient dynamoDBClient = (DynamoDBClient) dataMap.get("dynamoDBClient");
        BlockingQueue<List<Coin>> queue = (BlockingQueue<List<Coin>>) dataMap.get("coinQueue");

        if (dynamoDBClient == null || queue == null) {
            logger.error("Dependencies not found in JobDataMap.");
            throw new JobExecutionException("Missing dependencies in JobDataMap.");
        }

        try {
            // Poll the queue with a timeout to prevent blocking indefinitely
            List<Coin> coinsToUpdate = queue.poll(5, java.util.concurrent.TimeUnit.SECONDS);
            if (coinsToUpdate != null && !coinsToUpdate.isEmpty()) {
                logger.info("Dequeued {} coins for DynamoDB update.", coinsToUpdate.size());
                dynamoDBClient.saveOrUpdateCoins(coinsToUpdate);
                logger.info("DynamoDB update completed for {} coins.", coinsToUpdate.size());
            } else {
                logger.info("No coins to update at this time.");
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while polling the queue: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new JobExecutionException(e);
        } catch (Exception e) {
            logger.error("Error updating DynamoDB: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
