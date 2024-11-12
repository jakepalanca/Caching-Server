package jakepalanca.caching.server;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

        // Initialize a thread-safe queue for inter-job communication
        BlockingQueue<List<Map<String, Object>>> coinQueue = new LinkedBlockingQueue<>();

        // Initialize Scheduler
        Scheduler scheduler = initializeScheduler();

        // Add shutdown hook to close clients and scheduler
        final CoinGeckoClient finalCoinGeckoClient = coinGeckoClient;
        final DynamoDBClient finalDynamoDBClient = dynamoDBClient;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Closing clients and stopping scheduler.");
            try {
                if (scheduler != null) {
                    scheduler.shutdown(true);
                    logger.info("Scheduler shut down successfully.");
                }
            } catch (SchedulerException e) {
                logger.error("Error shutting down scheduler: {}", e.getMessage(), e);
            }
            if (finalCoinGeckoClient != null) {
                finalCoinGeckoClient.close();
            }
            if (finalDynamoDBClient != null) {
                finalDynamoDBClient.close();
            }
            logger.info("Clients closed successfully.");
        }));

        // Schedule FetchCoinJob
        scheduleFetchCoinJob(coinGeckoClient, coinQueue, scheduler);

        // Schedule UpdateDynamoDBJob
        scheduleUpdateDynamoDBJob(dynamoDBClient, coinQueue, scheduler);

        logger.info("MainApplication started successfully.");
    }

    private static Scheduler initializeScheduler() {
        logger.info("Initializing Quartz Scheduler...");

        try {
            // Define Quartz properties
            Properties props = new Properties();
            props.setProperty("org.quartz.scheduler.instanceName", "MainScheduler");
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty("org.quartz.threadPool.threadCount", "2"); // Adjust as needed
            props.setProperty("org.quartz.threadPool.threadPriority", "5");
            props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

            // Initialize the SchedulerFactory with the properties
            StdSchedulerFactory factory = new StdSchedulerFactory(props);
            Scheduler scheduler = factory.getScheduler();
            scheduler.start();
            logger.info("Quartz Scheduler started successfully.");

            return scheduler;
        } catch (SchedulerException e) {
            logger.error("Failed to initialize Scheduler: {}", e.getMessage(), e);
            throw new RuntimeException("Scheduler initialization failed", e);
        }
    }

    /**
     * Schedules the FetchCoinJob using Quartz Scheduler.
     *
     * @param coinGeckoClient the CoinGeckoClient instance
     * @param coinQueue       the shared BlockingQueue for passing coin data
     * @param scheduler       the Quartz Scheduler instance
     */
    private static void scheduleFetchCoinJob(CoinGeckoClient coinGeckoClient, BlockingQueue<List<Map<String, Object>>> coinQueue, Scheduler scheduler) {
        logger.info("Scheduling FetchCoinJob...");

        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("coinGeckoClient", coinGeckoClient);
            jobDataMap.put("coinQueue", coinQueue);

            JobDetail job = newJob(FetchCoinJob.class)
                    .setJobData(jobDataMap)
                    .withIdentity("fetchCoinJob", "group1")
                    .build();
            logger.debug("FetchCoinJob detail created with JobDataMap.");

            int intervalInSeconds = getIntervalFromEnv("FETCH_COIN_INTERVAL_SECONDS", 60);

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds)
                            .repeatForever())
                    .withIdentity("fetchCoinTrigger", "group1")
                    .build();
            logger.debug("Trigger for FetchCoinJob created with interval {} seconds.", intervalInSeconds);

            scheduler.scheduleJob(job, trigger);
            logger.info("FetchCoinJob scheduled to run every {} seconds.", intervalInSeconds);
        } catch (SchedulerException e) {
            logger.error("Failed to schedule FetchCoinJob: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to schedule FetchCoinJob", e);
        }
    }

    /**
     * Schedules the UpdateDynamoDBJob using Quartz Scheduler.
     *
     * @param dynamoDBClient the DynamoDBClient instance
     * @param coinQueue      the shared BlockingQueue for receiving coin data
     * @param scheduler      the Quartz Scheduler instance
     */
    private static void scheduleUpdateDynamoDBJob(DynamoDBClient dynamoDBClient, BlockingQueue<List<Map<String, Object>>> coinQueue, Scheduler scheduler) {
        logger.info("Scheduling UpdateDynamoDBJob...");

        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("dynamoDBClient", dynamoDBClient);
            jobDataMap.put("coinQueue", coinQueue);

            JobDetail job = newJob(UpdateDynamoDBJob.class)
                    .setJobData(jobDataMap)
                    .withIdentity("updateDynamoDBJob", "group1")
                    .build();
            logger.debug("UpdateDynamoDBJob detail created with JobDataMap.");

            int intervalInSeconds = getIntervalFromEnv("UPDATE_DYNAMODB_INTERVAL_SECONDS", 30);

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds)
                            .repeatForever())
                    .withIdentity("updateDynamoDBTrigger", "group1")
                    .build();
            logger.debug("Trigger for UpdateDynamoDBJob created with interval {} seconds.", intervalInSeconds);

            scheduler.scheduleJob(job, trigger);
            logger.info("UpdateDynamoDBJob scheduled to run every {} seconds.", intervalInSeconds);
        } catch (SchedulerException e) {
            logger.error("Failed to schedule UpdateDynamoDBJob: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to schedule UpdateDynamoDBJob", e);
        }
    }

    /**
     * Helper method to get interval from environment variable or default value.
     *
     * @param envVarName      the name of the environment variable
     * @param defaultInterval the default interval in seconds
     * @return the interval in seconds
     */
    private static int getIntervalFromEnv(String envVarName, int defaultInterval) {
        String intervalEnv = System.getenv(envVarName);
        int intervalInSeconds;
        if (intervalEnv != null) {
            try {
                intervalInSeconds = Integer.parseInt(intervalEnv);
                logger.debug("{} set to {} seconds from environment variable.", envVarName, intervalInSeconds);
            } catch (NumberFormatException e) {
                logger.warn("Invalid {} value '{}'. Defaulting to {} seconds.", envVarName, intervalEnv, defaultInterval);
                intervalInSeconds = defaultInterval;
            }
        } else {
            intervalInSeconds = defaultInterval;
            logger.debug("{} not set. Defaulting to {} seconds.", envVarName, defaultInterval);
        }
        return intervalInSeconds;
    }
}
