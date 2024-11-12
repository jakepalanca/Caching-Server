package jakepalanca.caching.server;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * The main entry point of the application. Initializes clients and schedules jobs.
 */
public class MainApplication {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    public static void main(String[] args) {
        logger.info("Starting MainApplication.");

        // Initialize CoinGeckoClient
        CoinGeckoClient coinGeckoClient = null;
        try {
            logger.debug("Initializing CoinGeckoClient.");
            coinGeckoClient = new CoinGeckoClient();
            logger.info("CoinGeckoClient initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize CoinGeckoClient: {}", e.getMessage(), e);
            System.exit(1);
        }

        // Initialize DynamoDBClient
        DynamoDBClient dynamoDBClient = null;
        try {
            logger.debug("Initializing DynamoDBClient.");
            dynamoDBClient = new DynamoDBClient();
            logger.info("DynamoDBClient initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize DynamoDBClient: {}", e.getMessage(), e);
            if (coinGeckoClient != null) {
                try {
                    coinGeckoClient.close();
                } catch (Exception closeEx) {
                    logger.warn("Failed to close CoinGeckoClient during shutdown: {}", closeEx.getMessage(), closeEx);
                }
            }
            System.exit(1);
        }

        // Schedule the CoinUpdateJob
        scheduleCoinUpdateJob(dynamoDBClient, coinGeckoClient);

        logger.info("MainApplication started successfully.");
    }

    /**
     * Schedules the CoinUpdateJob using Quartz Scheduler.
     *
     * @param dynamoDBClient  the DynamoDBClient instance
     * @param coinGeckoClient the CoinGeckoClient instance
     */
    private static void scheduleCoinUpdateJob(DynamoDBClient dynamoDBClient, CoinGeckoClient coinGeckoClient) {
        logger.info("Scheduling CoinUpdateJob...");

        try {
            // Define Quartz properties for a single-threaded scheduler
            Properties props = new Properties();
            props.setProperty("org.quartz.scheduler.instanceName", "SingleThreadScheduler");
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty("org.quartz.threadPool.threadCount", "1"); // Single thread
            props.setProperty("org.quartz.threadPool.threadPriority", "5");
            props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore"); // In-memory job store

            // Initialize the SchedulerFactory with the properties
            logger.debug("Initializing Quartz Scheduler with custom properties.");
            StdSchedulerFactory factory = new StdSchedulerFactory(props);
            Scheduler scheduler = factory.getScheduler();
            scheduler.start();
            logger.info("Quartz Scheduler started successfully.");

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("dynamoDBClient", dynamoDBClient);
            jobDataMap.put("coinGeckoClient", coinGeckoClient);

            JobDetail job = newJob(CoinUpdateJob.class)
                    .setJobData(jobDataMap)
                    .withIdentity("coinUpdateJob", "group1")
                    .build();
            logger.debug("CoinUpdateJob detail created with JobDataMap.");

            // Fetch interval from environment variable or default to 30 seconds
            String intervalEnv = System.getenv("COIN_UPDATE_INTERVAL_SECONDS");
            int intervalInSeconds;
            if (intervalEnv != null) {
                try {
                    intervalInSeconds = Integer.parseInt(intervalEnv);
                    logger.debug("COIN_UPDATE_INTERVAL_SECONDS set to {} seconds from environment variable.", intervalInSeconds);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid COIN_UPDATE_INTERVAL_SECONDS value '{}'. Defaulting to 30 seconds.", intervalEnv);
                    intervalInSeconds = 30;
                }
            } else {
                intervalInSeconds = 30;
                logger.debug("COIN_UPDATE_INTERVAL_SECONDS not set. Defaulting to {} seconds.", intervalInSeconds);
            }

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds)
                            .repeatForever())
                    .withIdentity("coinUpdateTrigger", "group1")
                    .build();
            logger.debug("Trigger for CoinUpdateJob created with interval {} seconds.", intervalInSeconds);

            scheduler.scheduleJob(job, trigger);
            logger.info("CoinUpdateJob scheduled to run every {} seconds.", intervalInSeconds);
        } catch (SchedulerException e) {
            logger.error("Failed to schedule CoinUpdateJob: {}", e.getMessage(), e);
            throw new RuntimeException("Scheduler initialization failed", e);
        }
    }
}
