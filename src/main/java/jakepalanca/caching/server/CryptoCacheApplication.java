package jakepalanca.caching.server;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * The {@code CryptoCacheApplication} class is the entry point for the CryptoCache API server.
 * It initializes the necessary components such as the {@link CoinCache}, {@link BubbleService},
 * and sets up the {@link Javalin} server with various API endpoints.
 *
 * <p>The server provides endpoints for retrieving cryptocurrency data in different formats, including:</p>
 * <ul>
 *   <li>Bubble charts</li>
 *   <li>Top 100 coins</li>
 *   <li>Coin search</li>
 * </ul>
 *
 * <p>The application also schedules a Quartz job to periodically update the {@link CoinCache}
 * with the latest data from the CoinGecko API.</p>
 */
public class CryptoCacheApplication {

    private static final BubbleService bubbleService = new BubbleService(); // Initialize BubbleService

    /**
     * The main method that serves as the entry point to the application.
     * It initializes the CoinCache, sets up the Javalin server, and schedules a job to update the cache.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        CoinCache coinCache = new CoinCache(false);

        Javalin app = createServer(coinCache);
        app.start("0.0.0.0", 8080); // Start the server on the default port

        scheduleCoinUpdateJob(coinCache);
    }

    /**
     * Creates and configures the {@link Javalin} server, setting up API routes and handlers.
     *
     * <p>The server provides the following endpoints:</p>
     * <ul>
     *   <li>{@code /health} - Health check endpoint</li>
     *   <li>{@code /bubbles/list} - Returns a list of bubbles based on the provided parameters</li>
     *   <li>{@code /coins} - Returns all coins in the cache</li>
     *   <li>{@code /top100} - Returns the top 100 coins in the cache</li>
     *   <li>{@code /search} - Performs a search for coins based on a query string</li>
     * </ul>
     *
     * @param coinCache the {@link CoinCache} instance used to serve coin data
     * @return the configured {@link Javalin} server instance
     */
    public static Javalin createServer(CoinCache coinCache) {
        // Initialize CoinCache and CoinGeckoClient
        CoinGeckoClient coinGeckoClient = new CoinGeckoClient();

        // Create and configure the Javalin server
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson()); // Using Jackson for JSON serialization
        });

        app.get("/health", ctx -> {
            ctx.status(200).result("OK");
        });

        // Define routes
        app.get("/bubbles/list", ctx -> {
            String idsParam = ctx.queryParam("ids");
            String dataType = ctx.queryParam("data_type");
            Optional<String> timeInterval = Optional.ofNullable(ctx.queryParam("time_interval"));
            String xHeightChartViewParam = ctx.queryParam("x_height_chart_view");
            String yWidthChartViewParam = ctx.queryParam("y_width_chart_view");

            if (!validateInputs(idsParam, dataType, timeInterval, xHeightChartViewParam, yWidthChartViewParam)) {
                ctx.status(400).result("Bad Request: Invalid input parameters.");
                return;
            }

            double xHeightScreenDouble = Double.parseDouble(xHeightChartViewParam);
            int xHeightScreen = (int) xHeightScreenDouble;

            double yWidthScreenDouble = Double.parseDouble(yWidthChartViewParam);
            int yWidthScreen = (int) yWidthScreenDouble;

            List<String> coinIds = Arrays.asList(idsParam.split(","));
            List<Coin> coins = coinCache.getCoinsByIds(coinIds);

            List<Bubble> bubbles = bubbleService.createBubbles(coins, dataType, timeInterval.orElse(null), xHeightScreen, yWidthScreen);

            ctx.json(bubbles);
        });

        app.get("/coins", ctx -> {
            if (coinCache == null || coinCache.getAllCoins().isEmpty()) {
                ctx.status(500).result("Coin cache is empty or null");
            } else {
                ctx.contentType("application/json");
                ctx.json(coinCache.getAllCoins());
            }
        });

        app.get("/top100", ctx -> {
            if (coinCache == null || coinCache.getTop100Coins().isEmpty()) {
                ctx.status(500).result("Coin cache is empty or null");
            } else {
                ctx.contentType("application/json");
                ctx.json(coinCache.getTop100Coins());
            }
        });

        app.get("/search", ctx -> {
            String query = ctx.queryParam("query");
            if (query != null) {
                ctx.contentType("application/json");
                ctx.json(coinCache.searchCoins(query));
            } else {
                ctx.status(400).result("Bad Request: Must provide a query.");
            }
        });

        return app; // Return the configured Javalin instance
    }

    /**
     * Schedules a Quartz job that periodically updates the {@link CoinCache} with the latest data from CoinGecko.
     * The job runs every 15 seconds by default.
     *
     * @param coinCache the {@link CoinCache} instance to be updated
     */
    private static void scheduleCoinUpdateJob(CoinCache coinCache) {
        try {
            System.out.println("Scheduling CoinUpdateJob...");
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("coinCache", coinCache);
            jobDataMap.put("coinGeckoClient", new CoinGeckoClient());

            JobDetail job = newJob(CoinUpdateJob.class)
                    .setJobData(jobDataMap)
                    .build();

            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(15) // Runs every 15 seconds
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(job, trigger);
            System.out.println("CoinUpdateJob scheduled to run every 15 seconds.");
        } catch (SchedulerException e) {
            System.err.println("Failed to schedule CoinUpdateJob: " + e.getMessage());
            e.printStackTrace();
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
     * @param xHeightScreenParam the height of the screen in pixels
     * @param yWidthScreenParam  the width of the screen in pixels
     * @return                   {@code true} if all inputs are valid; {@code false} otherwise
     */
    private static boolean validateInputs(String idsParam, String dataType, Optional<String> timeInterval, String xHeightScreenParam, String yWidthScreenParam) {
        if (idsParam == null || idsParam.isEmpty()) return false;
        if (dataType == null || dataType.isEmpty() || !BubbleService.VALID_DATA_TYPES.contains(dataType)) return false;

        if (dataType.equals("price_change")) {
            if (!timeInterval.isPresent() || !BubbleService.VALID_TIME_INTERVALS.contains(timeInterval.get())) {
                return false;
            }
        } else if (timeInterval.isPresent()) {
            return false;
        }

        if (xHeightScreenParam == null || yWidthScreenParam == null) return false;

        try {
            parseInt(xHeightScreenParam);
            parseInt(yWidthScreenParam);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
