package jakepalanca.caching.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakepalanca.common.Coin;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code CoinGeckoClient} class provides methods to interact with the CoinGecko API.
 * It fetches cryptocurrency data, handling batching and rate limits.
 *
 * <p>This client supports fetching the top coins by market capitalization and fetching coins by a list of IDs.
 * It handles batching of requests and introduces delays between batches to comply with API rate limits.</p>
 */
public class CoinGeckoClient {

    private static final Logger logger = LoggerFactory.getLogger(CoinGeckoClient.class);

    // Base URL and Endpoints
    private static final String BASE_URL = "https://api.coingecko.com/api/v3";
    private static final String COINS_MARKETS_ENDPOINT = "/coins/markets";

    // Constants for configuration
    private static final String LOCALIZATION = "en"; // Change as needed
    private static final String VS_CURRENCY = "usd"; // Change as needed

    // API Parameters
    private static final String PARAMETERS = String.format(
            "?vs_currency=%s&order=market_cap_desc&per_page=250&sparkline=true" +
                    "&price_change_percentage=1h,24h,7d,14d,30d,200d,1y&locale=%s&precision=full",
            VS_CURRENCY, LOCALIZATION);

    // Delay between API requests in milliseconds to respect rate limits
    public static final int REQUEST_DELAY_MS = 15000;

    // API Key (Ensure this is set securely, e.g., via environment variables)
    private static final String API_KEY = System.getenv("COINGECKO_API_KEY"); // Set this environment variable

    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code CoinGeckoClient} instance with default configurations.
     */
    public CoinGeckoClient() {
        this.client = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Fetches coins by a list of IDs in batches of 250, respecting the API rate limits.
     *
     * @param coinIds the list of coin IDs to fetch
     * @return a list of {@link Coin} objects
     * @throws IOException    if an I/O error occurs during the HTTP request
     * @throws ParseException if an error occurs while parsing the response
     */
    public List<Coin> fetchCoinsByIds(List<String> coinIds) throws IOException, ParseException {
        List<Coin> allCoins = new ArrayList<>();
        int totalBatches = (int) Math.ceil((double) coinIds.size() / 250);
        logger.info("Fetching coins by IDs in {} batches...", totalBatches);

        for (int i = 0; i < totalBatches; i++) {
            int fromIndex = i * 250;
            int toIndex = Math.min(fromIndex + 250, coinIds.size());
            List<String> batchIds = coinIds.subList(fromIndex, toIndex);

            String idsParam = String.join(",", batchIds);
            String url = BASE_URL + COINS_MARKETS_ENDPOINT + PARAMETERS + "&ids=" + idsParam;
            int batchNumber = i + 1;
            logger.info("Fetching batch {}/{} for IDs.", batchNumber, totalBatches);

            List<Coin> batchCoins = fetchBatch(url, batchNumber);
            allCoins.addAll(batchCoins);

            // Sleep between batches to respect rate limits
            if (i < totalBatches - 1) {
                try {
                    logger.info("Sleeping for {} ms before next batch...", REQUEST_DELAY_MS);
                    Thread.sleep(REQUEST_DELAY_MS);
                } catch (InterruptedException e) {
                    logger.error("Sleep interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during batch fetching", e);
                }
            }
        }

        return allCoins;
    }

    /**
     * Fetches a specified number of batches of top coins from CoinGecko API.
     *
     * @param numberOfBatches the number of 250-coin batches to fetch
     * @return a list of {@link Coin} objects representing the fetched coins
     * @throws IOException    if an I/O error occurs during the HTTP request
     * @throws ParseException if an error occurs while parsing the response
     */
    public List<Coin> fetchTopCoins(int numberOfBatches) throws IOException, ParseException {
        List<Coin> allCoins = new ArrayList<>();
        logger.info("Fetching top coins in {} batches...", numberOfBatches);

        for (int i = 1; i <= numberOfBatches; i++) {
            String url = BASE_URL + COINS_MARKETS_ENDPOINT + PARAMETERS + "&page=" + i;
            logger.info("Fetching top coins batch {}/{}.", i, numberOfBatches);

            List<Coin> batchCoins = fetchBatch(url, i);
            allCoins.addAll(batchCoins);

            // Sleep between batches to respect rate limits
            if (i < numberOfBatches) {
                try {
                    logger.info("Sleeping for {} ms before next batch...", REQUEST_DELAY_MS);
                    Thread.sleep(REQUEST_DELAY_MS);
                } catch (InterruptedException e) {
                    logger.error("Sleep interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during top coins fetching", e);
                }
            }
        }

        logger.info("Finished fetching top coins.");
        return allCoins;
    }

    /**
     * Fetches a single batch of coins from the specified URL.
     *
     * @param url         the API endpoint to fetch data from
     * @param batchNumber the current batch number for logging
     * @return a list of {@link Coin} objects
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if parsing fails
     */
    private List<Coin> fetchBatch(String url, int batchNumber) throws IOException, ParseException {
        HttpGet request = new HttpGet(url);
        request.addHeader("accept", "application/json");

        // Add API key header if available
        if (API_KEY != null && !API_KEY.isEmpty()) {
            request.addHeader("x-cg-demo-api-key", API_KEY);
        }

        HttpClientResponseHandler<List<Coin>> responseHandler = getListHttpClientResponseHandler(batchNumber);

        int retryCount = 0;
        int maxRetries = 3;
        long retryDelay = 2000; // Initial retry delay in milliseconds

        while (true) {
            try {
                return client.execute(request, responseHandler);
            } catch (HttpHostConnectException e) {
                // Handle connection refused exception
                retryCount++;
                logger.warn("Connection refused. Attempt {} failed for batch {}: {}", retryCount, batchNumber, e.getMessage());
                if (retryCount >= maxRetries) {
                    logger.error("Max retries reached for batch {}. Throwing exception.", batchNumber);
                    throw e;
                }
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    logger.error("Sleep interrupted during retry: {}", ie.getMessage());
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry", ie);
                }
                retryDelay *= 2; // Exponential backoff
            } catch (IOException e) {
                retryCount++;
                logger.warn("Attempt {} failed for batch {}: {}", retryCount, batchNumber, e.getMessage());
                if (retryCount >= maxRetries) {
                    logger.error("Max retries reached for batch {}. Throwing exception.", batchNumber);
                    throw e;
                }
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    logger.error("Sleep interrupted during retry: {}", ie.getMessage());
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry", ie);
                }
                retryDelay *= 2; // Exponential backoff
            }
        }
    }

    /**
     * Creates a response handler to process the HTTP response and parse the list of coins.
     * The response handler checks for a successful HTTP status code (200) and parses the response body into a list of coins.
     *
     * @param batchNumber the batch number for logging purposes
     * @return a {@link HttpClientResponseHandler} that returns a list of {@link Coin} objects
     */
    private @NotNull HttpClientResponseHandler<List<Coin>> getListHttpClientResponseHandler(int batchNumber) {

        return response -> {
            int statusCode = response.getCode();
            if (statusCode == 200) {
                String responseBody = org.apache.hc.core5.http.io.entity.EntityUtils.toString(response.getEntity());
                logger.info("Successfully fetched batch {}.", batchNumber);
                return parseCoins(responseBody);
            } else {
                logger.error("Failed to fetch coins, HTTP Code: {}", statusCode);
                throw new IOException("HTTP response code: " + statusCode);
            }
        };
    }

    /**
     * Parses the JSON response body into a list of {@link Coin} objects using Jackson.
     *
     * @param responseBody the JSON response body as a string
     * @return a list of {@link Coin} objects parsed from the JSON response
     * @throws IOException if an error occurs while parsing the JSON
     */
    private List<Coin> parseCoins(String responseBody) throws IOException {
        return objectMapper.readValue(responseBody, new TypeReference<List<Coin>>() {});
    }

    /**
     * Closes the HTTP client and releases resources.
     *
     * @throws IOException if an error occurs while closing the client
     */
    public void close() throws IOException {
        client.close();
    }
}
