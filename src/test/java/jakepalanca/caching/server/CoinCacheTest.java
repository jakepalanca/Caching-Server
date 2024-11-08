// ----- CoinCacheTest.java -----
package jakepalanca.caching.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link CoinCache} class.
 * These tests focus on cache functionality, ensuring that the cache can update, retrieve, and store coins correctly.
 */
public class CoinCacheTest {

    private static final Logger logger = LoggerFactory.getLogger(CoinCacheTest.class);

    private CoinCache coinCache;
    private String testCacheDir;
    private String testCachePath;

    /**
     * Initializes the test environment by creating a new CoinCache instance and setting up the test cache directory.
     */
    @BeforeEach
    public void setup() {
        // Set up a separate cache directory for testing
        testCacheDir = "./test-cache/";
        File dir = new File(testCacheDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            logger.info("Test cache directory created: {}", created);
        }

        // Determine the test cache file path
        testCachePath = testCacheDir + "coinCache-test.ser";

        // Initialize CoinCache for testing
        coinCache = new CoinCache(testCacheDir, true);

        // Ensure the cache file is clean before each test
        coinCache.emptyCacheFile();
    }

    /**
     * Cleans up the test environment by deleting the test cache file and directory.
     */
    @AfterEach
    public void cleanup() {
        // Delete the test cache file
        File cacheFile = new File(testCachePath);
        if (cacheFile.exists()) {
            boolean deleted = cacheFile.delete();
            logger.info("Test cache file deleted: {}", deleted);
        }

        // Delete the test cache directory
        File dir = new File(testCacheDir);
        if (dir.exists()) {
            boolean deleted = dir.delete();
            logger.info("Test cache directory deleted: {}", deleted);
        }
    }

    /**
     * Tests that the cache is initially empty upon creation if no cache file exists.
     */
    @Test
    public void testInitialCacheIsEmpty() {
        logger.info("Running testInitialCacheIsEmpty");

        List<Coin> coins = coinCache.getAllCoins();
        assertThat(coins).isEmpty();  // Cache should be empty initially
    }

    /**
     * Tests updating the cache with a list of new coins.
     * Verifies that coins are added correctly and saved to the cache file.
     */
    @Test
    public void testUpdateCacheAddsCoins() {
        logger.info("Running testUpdateCacheAddsCoins");

        // Ensure the cache file does not exist before the test
        File cacheFile = new File(testCachePath);
        if (cacheFile.exists()) {
            boolean deleted = cacheFile.delete();
            logger.info("Existing cache file deleted: {}", deleted);
        }

        // Add some test coins to the cache
        List<Coin> testCoins = List.of(
                createCoin("bitcoin", "btc", "Bitcoin", 50000.0, 1),
                createCoin("ethereum", "eth", "Ethereum", 3000.0, 2)
        );

        coinCache.updateCache(testCoins);

        // Check if the cache file is created
        assertThat(cacheFile.exists())
                .as("Checking if the cache file exists at " + cacheFile.getAbsolutePath())
                .isTrue();

        // Verify the cache contains the added coins
        List<Coin> coins = coinCache.getAllCoins();
        assertThat(coins).hasSize(2);

        // Sort coins by market cap rank to ensure consistent order
        coins.sort(Comparator.comparingInt(Coin::getMarketCapRank));

        assertThat(coins.get(0).getId()).isEqualTo("bitcoin");
        assertThat(coins.get(1).getId()).isEqualTo("ethereum");

        logger.info("Test completed successfully, cache file and contents are as expected.");
    }

    /**
     * Tests that the cache correctly updates existing coins without adding duplicates.
     */
    @Test
    public void testUpdateCacheRemovesDuplicates() {
        logger.info("Running testUpdateCacheRemovesDuplicates");

        // Add initial coins
        List<Coin> initialCoins = List.of(
                createCoin("bitcoin", "btc", "Bitcoin", 50000.0, 1)
        );
        coinCache.updateCache(initialCoins);

        // Add a duplicate coin with updated data
        List<Coin> updatedCoins = List.of(
                createCoin("bitcoin", "btc", "Bitcoin", 51000.0, 1)  // Updated price
        );
        coinCache.updateCache(updatedCoins);

        // Verify that "bitcoin" has been updated but only one instance remains
        List<Coin> coins = coinCache.getAllCoins();
        assertThat(coins).hasSize(1);  // Ensure no duplicates
        assertThat(coins.get(0).getCurrentPrice()).isEqualTo(51000.0);  // Verify the coin was updated
    }

    /**
     * Tests that coins outside the top 1000 are retained in the cache after an update.
     */
    @Test
    public void testRetainsCoinsOutsideTop1000() {
        logger.info("Running testRetainsCoinsOutsideTop1000");

        // Add a large number of coins (1001 coins)
        List<Coin> initialCoins = createSampleCoins(1001);
        coinCache.updateCache(initialCoins);

        // Add new coins to simulate an update (top 1000 coins)
        List<Coin> newCoins = createSampleCoins(1000);
        coinCache.updateCache(newCoins);

        // Verify that there are still 1001 coins in the cache (existing coins should remain)
        List<Coin> coins = coinCache.getAllCoins();
        assertThat(coins).hasSize(1001);
    }

    /**
     * Helper method to create a list of dummy {@link Coin} objects for testing.
     *
     * @param count the number of coins to create
     * @return a list of dummy coins
     */
    private List<Coin> createSampleCoins(int count) {
        List<Coin> coins = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            coins.add(createCoin(
                    "coin" + i, "sym" + i, "Coin " + i, 1000.0 * i, i
            ));
        }
        return coins;
    }

    /**
     * Helper method to create a {@link Coin} object with minimal necessary fields.
     *
     * @param coinId          the unique identifier for the coin
     * @param symbol          the symbol of the coin
     * @param name            the name of the coin
     * @param currentPrice    the current price of the coin
     * @param marketCapRank   the market capitalization rank of the coin
     * @return                a {@link Coin} object
     */
    private Coin createCoin(String coinId, String symbol, String name, double currentPrice, int marketCapRank) {
        Coin coin = new Coin();
        coin.setId(coinId);
        coin.setSymbol(symbol);
        coin.setName(name);
        coin.setImage("https://example.com/" + coinId + ".png");
        coin.setCurrentPrice(currentPrice);
        coin.setMarketCapRank(marketCapRank);
        coin.setMarketCap(1000000L * marketCapRank);
        coin.setTotalVolume(500000L * marketCapRank);
        coin.setPriceChangePercentage24h(1.0 * marketCapRank);
        coin.setPriceChangePercentage1h(0.5 * marketCapRank);
        coin.setPriceChangePercentage7d(7.0 * marketCapRank);
        coin.setLastUpdated("2024-08-25T00:00:00Z");
        return coin;
    }
}
