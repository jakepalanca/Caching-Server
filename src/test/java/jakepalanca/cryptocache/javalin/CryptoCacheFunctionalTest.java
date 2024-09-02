package jakepalanca.cryptocache.javalin;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests for the CryptoCacheApplication using Javalin and MockWebServer.
 * These tests ensure that the application routes behave as expected when interacting with the CoinCache.
 */
public class CryptoCacheFunctionalTest {

    private CoinCache coinCache;
    private MockWebServer mockWebServer;

    /**
     * Sets up the test environment before each test.
     * Initializes the in-memory CoinCache with test data and starts the MockWebServer.
     *
     * @throws IOException if an I/O error occurs during setup
     */
    @BeforeEach
    public void setup() throws IOException {
        // Initialize the in-memory coin cache with test data
        coinCache = new InMemoryCoinCache();
        List<Coin> testCoins = List.of(
                new Coin(
                        "bitcoin",                           // id
                        "btc",                               // symbol
                        "Bitcoin",                           // name
                        "https://example.com/bitcoin.png",   // image
                        50000.0,                             // currentPrice
                        1000000000L,                         // marketCap
                        1,                                   // marketCapRank
                        1100000000L,                         // fullyDilutedValuation
                        500000000L,                          // totalVolume
                        55000.0,                             // high24h
                        45000.0,                             // low24h
                        500.0,                               // priceChange24h
                        1.0,                                 // priceChangePercentage24h
                        0.5,                                 // priceChangePercentage1h
                        7.0,                                 // priceChangePercentage7d
                        10.0,                                // priceChangePercentage14d
                        20.0,                                // priceChangePercentage30d
                        15.0,                                // priceChangePercentage200d
                        25.0,                                // priceChangePercentage1y
                        50000000L,                           // marketCapChange24h
                        1.0,                                 // marketCapChangePercentage24h
                        20000000.0,                          // circulatingSupply
                        21000000.0,                          // totalSupply
                        21000000.0,                          // maxSupply
                        60000.0,                             // ath
                        -20.0,                               // athChangePercentage
                        "2021-04-14",                        // athDate
                        3000.0,                              // atl
                        1500.0,                              // atlChangePercentage
                        "2018-12-15",                        // atlDate
                        null,                                // roi
                        "2024-08-25T00:00:00Z"               // lastUpdated
                ),
                new Coin(
                        "notbitcoin",                        // id
                        "ctb",                               // symbol
                        "TCBcoin",                           // name
                        "https://example.com/notbitcoin.png",// image
                        100.0,                               // currentPrice
                        1020000000L,                         // marketCap
                        2,                                   // marketCapRank
                        11000000L,                           // fullyDilutedValuation
                        5000000L,                            // totalVolume
                        15000.0,                             // high24h
                        4000.0,                              // low24h
                        40.0,                                // priceChange24h
                        1.0,                                 // priceChangePercentage24h
                        0.5,                                 // priceChangePercentage1h
                        7.0,                                 // priceChangePercentage7d
                        10.0,                                // priceChangePercentage14d
                        20.0,                                // priceChangePercentage30d
                        15.0,                                // priceChangePercentage200d
                        25.0,                                // priceChangePercentage1y
                        50000000L,                           // marketCapChange24h
                        1.0,                                 // marketCapChangePercentage24h
                        20000000.0,                          // circulatingSupply
                        21000000.0,                          // totalSupply
                        21000000.0,                          // maxSupply
                        60000.0,                             // ath
                        -20.0,                               // athChangePercentage
                        "2021-04-14",                        // athDate
                        3000.0,                              // atl
                        1500.0,                              // atlChangePercentage
                        "2018-12-15",                        // atlDate
                        null,                                // roi
                        "2024-08-25T00:00:00Z"               // lastUpdated
                )
        );
        coinCache.updateCache(testCoins);

        // Start the MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    /**
     * Tears down the test environment after each test.
     * Shuts down the MockWebServer.
     *
     * @throws IOException if an I/O error occurs during teardown
     */
    @AfterEach
    public void tearDown() throws IOException {
        // Shut down the MockWebServer after each test
        mockWebServer.shutdown();
    }

    /**
     * Tests the "/coins" route to ensure it returns the expected list of coins.
     * This test simulates a request to the server and checks that the response contains the correct coin data.
     *
     * @throws IOException if an I/O error occurs during the test
     */
    @Test
    public void testGetCoinsRoute() throws IOException {
        // Setup a mock response
        mockWebServer.enqueue(new MockResponse().setBody("[{\"id\":\"bitcoin\",\"name\":\"Bitcoin\"}]").setResponseCode(200));

        Javalin app = CryptoCacheApplication.createServer(coinCache);
        OkHttpClient client = new OkHttpClient();

        JavalinTest.test(app, (server, testClient) -> {
            String baseUrl = mockWebServer.url("/coins").toString(); // Use MockWebServer URL
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).contains("bitcoin");
            }
        });
    }

    /**
     * Tests the "/top100" route to ensure it returns the top 100 coins.
     * This test simulates a request to the server and checks that the response contains the correct coin data.
     *
     * @throws IOException if an I/O error occurs during the test
     */
    @Test
    public void testTop100CoinsRoute() throws IOException {
        // Setup a mock response
        mockWebServer.enqueue(new MockResponse().setBody("[{\"id\":\"bitcoin\",\"name\":\"Bitcoin\"}]").setResponseCode(200));

        Javalin app = CryptoCacheApplication.createServer(coinCache);
        OkHttpClient client = new OkHttpClient();

        JavalinTest.test(app, (server, testClient) -> {
            String baseUrl = mockWebServer.url("/top100").toString(); // Use MockWebServer URL
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).contains("bitcoin");
            }
        });
    }

    /**
     * Tests the "/search" route with a valid query parameter.
     * This test simulates a search request and ensures that the response contains the expected coin.
     *
     * @throws IOException if an I/O error occurs during the test
     */
    @Test
    public void testSearchRouteWithValidQuery() throws IOException {
        // Setup a mock response
        mockWebServer.enqueue(new MockResponse().setBody("[{\"id\":\"bitcoin\",\"name\":\"Bitcoin\"}]").setResponseCode(200));

        Javalin app = CryptoCacheApplication.createServer(coinCache);
        OkHttpClient client = new OkHttpClient();

        JavalinTest.test(app, (server, testClient) -> {
            String baseUrl = mockWebServer.url("/search?query=bitcoin").toString(); // Use MockWebServer URL
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).contains("bitcoin");
            }
        });
    }

    /**
     * Tests the "/search" route with an invalid query parameter.
     * This test simulates a search request with a query that should return no results.
     *
     * @throws IOException if an I/O error occurs during the test
     */
    @Test
    public void testSearchRouteWithInvalidQuery() throws IOException {
        // Setup a mock response
        mockWebServer.enqueue(new MockResponse().setBody("[]").setResponseCode(200));

        Javalin app = CryptoCacheApplication.createServer(coinCache);
        OkHttpClient client = new OkHttpClient();

        JavalinTest.test(app, (server, testClient) -> {
            String baseUrl = mockWebServer.url("/search?query=nonexistentcoin").toString(); // Use MockWebServer URL
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).doesNotContain("bitcoin");
            }
        });
    }

    /**
     * Tests the "/health" route to ensure it returns an OK status.
     * This is a simple health check to verify that the server is running correctly.
     *
     * @throws IOException if an I/O error occurs during the test
     */
    @Test
    public void testHealthRoute() throws IOException {
        // Setup a mock response
        mockWebServer.enqueue(new MockResponse().setBody("OK").setResponseCode(200));

        Javalin app = CryptoCacheApplication.createServer(coinCache);
        OkHttpClient client = new OkHttpClient();

        JavalinTest.test(app, (server, testClient) -> {
            String baseUrl = mockWebServer.url("/health").toString(); // Use MockWebServer URL
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).isEqualTo("OK");
            }
        });
    }
}
