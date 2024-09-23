// ----- CoinGeckoClient.java -----
package jakepalanca.caching.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code CoinGeckoClient} class provides methods to fetch cryptocurrency data from the CoinGecko API.
 * This client supports both demo and pro modes, controlled via environment variables.
 *
 * <p>The client retrieves data in batches of 250 coins, with a delay between requests to avoid rate limits.</p>
 *
 * <p><strong>Environment Variables:</strong></p>
 * <ul>
 * <li>{@code COINGECKO_API_KEY} - The API key for accessing CoinGecko's API (used in demo mode).</li>
 * <li>{@code DEMO_MODE} - A boolean flag indicating whether to use the demo API or the pro API.</li>
 * <li>{@code COINGECKO_REQUEST_DELAY_MS} - The delay between requests in milliseconds.</li>
 * </ul>
 */
public class CoinGeckoClient {

    private static final Logger logger = LoggerFactory.getLogger(CoinGeckoClient.class);

    private static final String DEMO_API_URL = System.getenv().getOrDefault("COINGECKO_DEMO_API_URL", "https://api.coingecko.com/api/v3/coins/markets");
    private static final String PRO_API_URL = System.getenv().getOrDefault("COINGECKO_PRO_API_URL", "https://pro-api.coingecko.com/api/v3/coins/markets");
    private static final String PARAMETERS = System.getenv().getOrDefault("COINGECKO_API_PARAMETERS", "?vs_currency=usd&order=market_cap_desc&per_page=250&sparkline=false&price_change_percentage=1h%2C24h%2C7d%2C14d%2C30d%2C200d%2C1y&locale=en&precision=full");
    private static final int REQUEST_DELAY_MS = Integer.parseInt(System.getenv().getOrDefault("COINGECKO_REQUEST_DELAY_MS", "15000"));

    // Retrieve the API key and demo mode flag from environment variables
    private static final String API_KEY = System.getenv("COINGECKO_API_KEY");
    private static final boolean DEMO_MODE = Boolean.parseBoolean(System.getenv().getOrDefault("DEMO_MODE", "true"));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient client;

    /**
     * Constructs a new {@code CoinGeckoClient} instance.
     * This client is used to interact with the CoinGecko API to fetch cryptocurrency data.
     */
    public CoinGeckoClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(Duration.ofSeconds(30)))
                .build();
        this.client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Fetches the top coins from CoinGecko API in batches of 250 coins each.
     * This method fetches a total of 1000 coins, divided into 4 batches, with a delay between each request.
     *
     * @return a list of {@link Coin} objects representing the top coins
     * @throws IOException    if an I/O error occurs during the HTTP request
     * @throws ParseException if an error occurs while parsing the response
     */
    public List<Coin> fetchTopCoins() throws IOException, ParseException {
        List<Coin> allCoins = new ArrayList<>();
        logger.info("Starting to fetch top coins from CoinGecko...");

        for (int i = 1; i <= 4; i++) { // Loop to fetch batches of 250 coins each
            String baseUrl = DEMO_MODE ? DEMO_API_URL : PRO_API_URL;
            String url = baseUrl + PARAMETERS + "&page=" + i;
            logger.info("Fetching batch {} from URL: {}", i, url);

            List<Coin> batchCoins = fetchBatch(url, i);
            allCoins.addAll(batchCoins);

            // Sleep for a constant interval between requests
            try {
                logger.info("Sleeping for {} milliseconds before fetching the next batch...", REQUEST_DELAY_MS);
                Thread.sleep(REQUEST_DELAY_MS);
            } catch (InterruptedException e) {
                logger.error("Sleep interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Finished fetching all coins.");
        return allCoins;
    }

    private List<Coin> fetchBatch(String url, int batchNumber) throws IOException, ParseException {
        HttpGet request = new HttpGet(url);
        request.addHeader("accept", "application/json");

        if (DEMO_MODE) {
            // Add the API key header only in demo mode
            request.addHeader("x-cg-demo-api-key", API_KEY);
        }

        HttpClientResponseHandler<List<Coin>> responseHandler = getListHttpClientResponseHandler(batchNumber);

        int retryCount = 0;
        int maxRetries = 3;
        long retryDelay = 2000; // Initial retry delay in milliseconds

        while (true) {
            return client.execute(request, responseHandler);
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
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.info("Successfully fetched batch {}", batchNumber);
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
        return objectMapper.readValue(responseBody, new TypeReference<List<Coin>>() {
        });
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
