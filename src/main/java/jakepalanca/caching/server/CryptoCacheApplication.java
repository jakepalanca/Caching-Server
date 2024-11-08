// ----- main/java/jakepalanca/caching/server/CryptoCacheApplication.java -----
package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.json.JavalinJackson;

import jakepalanca.common.Bubble;
import jakepalanca.common.Coin;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


/**
 * The {@code CryptoCacheApplication} class serves as the entry point to the application.
 * It initializes the CoinCache, sets up the Javalin server,
 * and schedules a job to update the cache periodically.
 *
 * <p>The server provides the following endpoints:</p>
 * <ul>
 *   <li>{@code /health} - Health check endpoint</li>
 *   <li>{@code /v1/bubbles/list} - Returns a list of bubbles based on the provided parameters</li>
 *   <li>{@code /v1/coins/all} - Returns all coins in the cache</li>
 *   <li>{@code /v1/coins/top100} - Returns the top 100 coins in the cache</li>
 *   <li>{@code /v1/coins/search} - Performs a search for coins based on a query string</li>
 * </ul>
 */
public class CryptoCacheApplication {

    private static final Logger logger = LoggerFactory.getLogger(CryptoCacheApplication.class);
    private static final BubbleService bubbleService = new BubbleService(); // Initialize BubbleService

    /**
     * The main method that serves as the entry point to the application.
     * It initializes the CoinCache, sets up the Javalin server,
     * and schedules a job to update the cache.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        // Define the directory where the cache will be stored
        String cacheDirectory = "~/cache/";

        // Create a production CoinCache instance
        CoinCache coinCache = new CoinCache(cacheDirectory, false);

        // Proceed with the rest of your application logic
        System.out.println("CoinCache initialized for production use.");

        // Initialize CoinGeckoClient
        CoinGeckoClient coinGeckoClient = new CoinGeckoClient();

        // Create and configure the Javalin server
        Javalin app = createServer(coinCache);
        app.start("0.0.0.0", 8080); // Start the server on the default port
        logger.info("Javalin server started on port 8080.");

        // Schedule the CoinUpdateJob
        scheduleCoinUpdateJob(coinCache, coinGeckoClient);
    }

    /**
     * Creates and configures the {@link Javalin} server, setting up API routes and handlers.
     *
     * <p>The server provides the following endpoints:</p>
     * <ul>
     *   <li>{@code /health} - Health check endpoint</li>
     *   <li>{@code /v1/bubbles/list} - Returns a list of bubbles based on the provided parameters</li>
     *   <li>{@code /v1/coins/all} - Returns all coins in the cache</li>
     *   <li>{@code /v1/coins/top100} - Returns the top 100 coins in the cache</li>
     *   <li>{@code /v1/coins/search} - Performs a search for coins based on a query string</li>
     * </ul>
     *
     * @param coinCache the {@link CoinCache} instance used to serve coin data
     * @return the configured {@link Javalin} server instance
     */
    public static Javalin createServer(CoinCache coinCache) {

        // Create and configure your ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Configure visibility to use fields
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // Disable features as needed
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Create a JavalinJackson instance
        JavalinJackson jackson = new JavalinJackson();

        // Use updateMapper to configure the ObjectMapper
        jackson.updateMapper(mapper -> {
            // Apply the same configurations to 'mapper'
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            // Register modules if necessary
            // mapper.registerModule(new JavaTimeModule());
        });

        // Create and configure the Javalin server
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(jackson);
        });

        // Global Exception Handling
        app.exception(HttpResponseException.class, (e, ctx) -> {
            ErrorResponse errorResponse = new ErrorResponse(
                    String.valueOf(e.getStatus()),
                    e.getMessage(),
                    null
            );
            ctx.status(e.getStatus()).json(errorResponse);
            logger.error("HTTP error {}: {}", e.getStatus(), e.getMessage());
        });

        app.exception(Exception.class, (e, ctx) -> {
            ErrorResponse errorResponse = new ErrorResponse(
                    "500",
                    "Internal Server Error",
                    e.getMessage()
            );
            ctx.status(500).json(errorResponse);
            logger.error("Unhandled exception: {}", e.getMessage(), e);
        });

        // Health check endpoint
        app.get("/health", ctx -> {
            ctx.status(200).result("OK");
            logger.debug("Health check endpoint accessed.");
        });

        // Define routes
        app.get("/v1/bubbles/list", ctx -> {
            String idsParam = ctx.queryParam("ids");
            String dataType = ctx.queryParam("data_type");
            Optional<String> timeInterval = Optional.ofNullable(ctx.queryParam("time_interval"));
            String chartWidthParam = ctx.queryParam("chart_width");
            String chartHeightParam = ctx.queryParam("chart_height");

            if (!validateInputs(idsParam, dataType, timeInterval, chartWidthParam, chartHeightParam)) {
                throw new HttpResponseException(400, "Bad Request: Invalid input parameters.");
            }

            int chartWidth = Integer.parseInt(chartWidthParam);
            int chartHeight = Integer.parseInt(chartHeightParam);

            List<String> coinIds = Arrays.asList(idsParam.split(","));
            List<Coin> coins = coinCache.getCoinsByIds(coinIds);

            if (coins.isEmpty()) {
                throw new HttpResponseException(404, "No coins found for the provided IDs.");
            }

            List<Bubble> bubbles = bubbleService.createBubbles(coins, dataType, timeInterval.orElse(null), chartWidth, chartHeight);
            ctx.json(bubbles);
        });


        app.get("/v1/coins/all", ctx -> {
            List<Coin> allCoins = coinCache.getAllCoins();
            if (allCoins.isEmpty()) {
                throw new HttpResponseException(500, "Coin cache is empty.");
            } else {
                ctx.json(allCoins);
                logger.info("Returned {} coins for /v1/coins/all.", allCoins.size());
            }
        });

        app.get("/v1/coins/top100", ctx -> {
            List<Coin> topCoins = coinCache.getTop100Coins();
            if (topCoins.isEmpty()) {
                throw new HttpResponseException(500, "Coin cache is empty.");
            } else {
                ctx.json(topCoins);
                logger.info("Returned top 100 coins for /v1/coins/top100.");
            }
        });

        app.get("/v1/coins/search", ctx -> {
            String query = ctx.queryParam("query");
            if (query != null && !query.trim().isEmpty()) {
                List<Coin> searchResults = coinCache.searchCoins(query);
                if (searchResults.isEmpty()) {
                    throw new HttpResponseException(404, "No coins found matching the query.");
                }
                ctx.json(searchResults);
                logger.info("Search for '{}' returned {} results.", query, searchResults.size());
            } else {
                throw new HttpResponseException(400, "Bad Request: Must provide a non-empty query.");
            }
        });

        return app; // Return the configured Javalin instance
    }

    /**
     * Schedules a Quartz job that periodically updates the {@link CoinCache} with the latest data from CoinGecko.
     * The job runs every 30 seconds by default, configurable via the {@code COIN_UPDATE_INTERVAL_SECONDS} environment variable.
     *
     * @param coinCache       the {@link CoinCache} instance to be updated
     * @param coinGeckoClient the {@link CoinGeckoClient} instance used to fetch data
     */
    private static void scheduleCoinUpdateJob(CoinCache coinCache, CoinGeckoClient coinGeckoClient) {
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
            jobDataMap.put("coinCache", coinCache);
            jobDataMap.put("coinGeckoClient", coinGeckoClient);

            JobDetail job = newJob(CoinUpdateJob.class)
                    .setJobData(jobDataMap)
                    .withIdentity("coinUpdateJob", "group1")
                    .build();

            // Fetch interval from environment variable or default to 30 seconds
            int intervalInSeconds = Integer.parseInt(System.getenv().getOrDefault("COIN_UPDATE_INTERVAL_SECONDS", "30"));

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(simpleSchedule()
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

    /**
     * Validates the input parameters for the bubbles API endpoint.
     *
     * <p>Ensures that required parameters are present, that the data type is valid,
     * and that numeric parameters can be parsed correctly.</p>
     *
     * @param idsParam           the comma-separated list of coin IDs
     * @param dataType           the data type for bubble creation (e.g., "market_cap", "price_change")
     * @param timeInterval       the optional time interval for price change calculations
     * @param chartHeightParam the height of the screen in pixels
     * @param chartWidthParam  the width of the screen in pixels
     * @return {@code true} if all inputs are valid; {@code false} otherwise
     */
    private static boolean validateInputs(String idsParam, String dataType, Optional<String> timeInterval, String chartWidthParam, String chartHeightParam) {
        if (idsParam == null || idsParam.trim().isEmpty()) {
            logger.warn("Validation failed: 'ids' parameter is missing or empty.");
            return false;
        }

        if (dataType == null || dataType.trim().isEmpty() || !BubbleService.VALID_DATA_TYPES.contains(dataType)) {
            logger.warn("Validation failed: 'data_type' parameter is invalid or missing.");
            return false;
        }

        // Allow timeInterval to be provided for any data type
        if (timeInterval.isPresent() && !BubbleService.VALID_TIME_INTERVALS.contains(timeInterval.get())) {
            logger.warn("Validation failed: 'time_interval' parameter is invalid.");
            return false;
        }

        if (chartWidthParam == null || chartHeightParam == null) {
            logger.warn("Validation failed: Chart dimension parameters are missing.");
            return false;
        }

        try {
            int chartWidth = Integer.parseInt(chartWidthParam);
            int chartHeight = Integer.parseInt(chartHeightParam);
            if (chartWidth <= 0 || chartHeight <= 0) {
                logger.warn("Validation failed: Chart dimensions must be positive integers.");
                return false;
            }
        } catch (NumberFormatException e) {
            logger.warn("Validation failed: Chart dimensions must be valid integers.");
            return false;
        }

        return true;
    }

}
