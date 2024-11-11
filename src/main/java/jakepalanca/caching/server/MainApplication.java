package jakepalanca.caching.server;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class MainApplication {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    public static void main(String[] args) {

        // Initialize CoinGeckoClient
        CoinGeckoClient coinGeckoClient = new CoinGeckoClient();

        // Initialize DynamoDBClient
        DynamoDBClient dynamoDBClient = new DynamoDBClient();

        // Schedule the CoinUpdateJob
        scheduleCoinUpdateJob(dynamoDBClient, coinGeckoClient);
    }

    private static void scheduleCoinUpdateJob(DynamoDBClient dynamoDBClient, CoinGeckoClient coinGeckoClient) {
        try {
            logger.info("Scheduling CoinUpdateJob...");

            // Define Quartz properties for a single-threaded scheduler
            Properties props = new Properties();
            props.setProperty("org.quartz.scheduler.instanceName", "SingleThreadScheduler");
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty("org.quartz.threadPool.threadCount", "1"); // Single thread
            props.setProperty("org.quartz.threadPool.threadPriority", "5");
            props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore"); // In-memory job store

            // Initialize the SchedulerFactory with the properties
            StdSchedulerFactory factory = new StdSchedulerFactory(props);
            Scheduler scheduler = factory.getScheduler();
            scheduler.start();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("dynamoDBClient", dynamoDBClient);
            jobDataMap.put("coinGeckoClient", coinGeckoClient);

            JobDetail job = newJob(CoinUpdateJob.class)
                    .setJobData(jobDataMap)
                    .withIdentity("coinUpdateJob", "group1")
                    .build();

            // Fetch interval from environment variable or default to 300 seconds
            int intervalInSeconds = Integer.parseInt(System.getenv().getOrDefault("COIN_UPDATE_INTERVAL_SECONDS", "300"));

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(intervalInSeconds)
                            .repeatForever())
                    .withIdentity("coinUpdateTrigger", "group1")
                    .build();

            scheduler.scheduleJob(job, trigger);
            logger.info("CoinUpdateJob scheduled to run every {} seconds.", intervalInSeconds);
        } catch (SchedulerException e) {
            logger.error("Failed to schedule CoinUpdateJob: {}", e.getMessage(), e);
        }
    }
}
