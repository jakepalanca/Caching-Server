package jakepalanca.caching.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@code CoinGeckoClient} class provides methods to interact with the CoinGecko API.
 * It fetches cryptocurrency data, handling batching and rate limits.
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
    private static final String API_KEY = System.getProperty("COINGECKO_API_KEY");

    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code CoinGeckoClient} instance with default configurations.
     */
    public CoinGeckoClient() {
        logger.debug("Initializing CoinGeckoClient with default configurations.");
        this.client = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        logger.debug("CoinGeckoClient initialized successfully.");
    }

    /**
     * Fetches a specified number of batches of top coins from CoinGecko API.
     *
     * @param numberOfBatches the number of 250-coin batches to fetch
     * @return a list of {@link Map<String, Object>} representing the fetched coins
     * @throws IOException    if an I/O error occurs during the HTTP request
     * @throws ParseException if an error occurs while parsing the response
     */
    public List<Map<String, Object>> fetchTopCoins(int numberOfBatches) throws IOException, ParseException {
        logger.info("Starting fetchTopCoins with {} batches.", numberOfBatches);
        List<Map<String, Object>> allCoins = new ArrayList<>();

        for (int i = 1; i <= numberOfBatches; i++) {
            String url = BASE_URL + COINS_MARKETS_ENDPOINT + PARAMETERS + "&page=" + i;
            logger.info("Fetching top coins batch {}/{}.", i, numberOfBatches);

            List<Map<String, Object>> batchCoins = fetchBatch(url, i);
            logger.debug("Batch {}/{} fetched {} coins.", i, numberOfBatches, batchCoins.size());
            allCoins.addAll(batchCoins);

            // Sleep between batches to respect rate limits
            if (i < numberOfBatches) {
                try {
                    logger.info("Sleeping for {} ms before next batch...", REQUEST_DELAY_MS);
                    Thread.sleep(REQUEST_DELAY_MS);
                } catch (InterruptedException e) {
                    logger.error("Sleep interrupted: {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during top coins fetching", e);
                }
            }
        }

        logger.info("Completed fetchTopCoins. Total top coins fetched: {}", allCoins.size());
        return allCoins;
    }

    /**
     * Fetches a single batch of coins from the specified URL.
     *
     * @param url         the API endpoint to fetch data from
     * @param batchNumber the current batch number for logging
     * @return a list of {@link Map<String, Object>}
     * @throws IOException    if an I/O error occurs
     * @throws ParseException if parsing fails
     */
    private List<Map<String, Object>> fetchBatch(String url, int batchNumber) throws IOException, ParseException {
        logger.debug("Preparing HTTP GET request for batch {}: {}", batchNumber, url);
        HttpGet request = new HttpGet(url);
        request.addHeader("accept", "application/json");

        // Add API key header if available
        if (API_KEY != null && !API_KEY.isEmpty()) {
            request.addHeader("x-cg-pro-api-key", API_KEY);
            logger.debug("Added API key header to request for batch {}.", batchNumber);
        } else {
            logger.warn("API key is not set. Proceeding without API key for batch {}.", batchNumber);
        }

        HttpClientResponseHandler<List<Map<String, Object>>> responseHandler = getListHttpClientResponseHandler(batchNumber);

        int retryCount = 0;
        int maxRetries = 3;
        long retryDelay = 2000; // Initial retry delay in milliseconds

        while (true) {
            try {
                logger.debug("Executing HTTP request for batch {}.", batchNumber);
                List<Map<String, Object>> coins = client.execute(request, responseHandler);
                logger.debug("HTTP request successful for batch {}.", batchNumber);
                return coins;
            } catch (HttpHostConnectException e) {
                // Handle connection refused exception
                retryCount++;
                logger.warn("Connection refused. Attempt {} failed for batch {}: {}", retryCount, batchNumber, e.getMessage());
                if (retryCount >= maxRetries) {
                    logger.error("Max retries reached for batch {}. Throwing exception.", batchNumber);
                    throw e;
                }
                try {
                    logger.info("Retrying batch {} after {} ms delay.", batchNumber, retryDelay);
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    logger.error("Sleep interrupted during retry: {}", ie.getMessage(), ie);
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
                    logger.info("Retrying batch {} after {} ms delay.", batchNumber, retryDelay);
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    logger.error("Sleep interrupted during retry: {}", ie.getMessage(), ie);
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
     * @return a {@link HttpClientResponseHandler} that returns a list of {@link Map<String, Object>}
     */
    private @NotNull HttpClientResponseHandler<List<Map<String, Object>>> getListHttpClientResponseHandler(int batchNumber) {

        return response -> {
            int statusCode = response.getCode();
            logger.debug("Received HTTP status code {} for batch {}.", statusCode, batchNumber);
            if (statusCode == 200) {
                String responseBody = org.apache.hc.core5.http.io.entity.EntityUtils.toString(response.getEntity());
                logger.info("Successfully fetched batch {}.", batchNumber);
                return parseCoins(responseBody);
            } else {
                logger.error("Failed to fetch coins for batch {}, HTTP Code: {}", batchNumber, statusCode);
                throw new IOException("HTTP response code: " + statusCode);
            }
        };
    }

    /**
     * Parses the JSON response body into a list of {@link Map<String, Object>} using Jackson.
     *
     * @param responseBody the JSON response body as a string
     * @return a list of {@link Map<String, Object>} parsed from the JSON response
     * @throws IOException if an error occurs while parsing the JSON
     */
    private List<Map<String, Object>> parseCoins(String responseBody) throws IOException {
        logger.debug("Parsing JSON response into Map<String, Object>.");
        List<Map<String, Object>> coins = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
        logger.debug("Parsed {} coins from JSON response.", coins.size());
        return coins;
    }

    /**
     * Closes the HTTP client and releases resources.
     *
     * @throws IOException if an error occurs while closing the client
     */
    public void close() throws IOException {
        logger.info("Closing CoinGeckoClient HTTP client.");
        client.close();
        logger.info("CoinGeckoClient HTTP client closed successfully.");
    }
}
