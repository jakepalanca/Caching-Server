// ----- CoinCacheTest.java -----
package jakepalanca.caching.server;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link CoinCache} class.
 * These tests focus on cache functionality, ensuring that the cache can update, retrieve, and store coins correctly.
 */
public class CoinCacheTest {

    private CoinCache coinCache;

    /**
     * Initializes the test environment by creating a new CoinCache instance and deleting any existing cache file.
     */
    @BeforeEach
    public void setup() {
        coinCache = new CoinCache(true);
        coinCache.emptyCacheFile(); // Ensure cache is deleted before the test
    }

    @AfterEach
    public void cleanup() throws IOException {
        coinCache.emptyCacheFile(); // Ensure cache is clean before each test
    }

    /**
     * Tests that the cache is initially empty upon creation if no cache file exists.
     */
    @Test
    public void testInitialCacheIsEmpty() {
        coinCache.emptyCacheFile();
        List<Coin> coins = coinCache.getAllCoins();
        Assertions.assertThat(coins).isEmpty();  // Cache should be empty initially
    }

    /**
     * Tests updating the cache with a list of new coins.
     * Verifies that coins are added correctly and saved to the cache file.
     */
    @Test
    public void testUpdateCacheAddsCoins() {
        System.out.println("Starting testUpdateCacheAddsCoins");

        // Define the expected cache file path for easier debugging
        String cacheFilePath = "coinCache-test.ser";
        File cacheFile = new File(cacheFilePath);

        // Clear existing cache file if it exists for a clean test environment
        if (cacheFile.exists()) {
            boolean deleted = cacheFile.delete();
            System.out.println("Existing cache file deleted: " + deleted);
        }

        // Add some test coins to the cache
        List<Coin> testCoins = List.of(
                createCoin("bitcoin", "btc", "Bitcoin", 50000.0, 1000000000L, 1, 1100000000L, 500000000L, 55000.0, 45000.0,
                        500.0, 1.0, 0.5, 7.0, 10.0, 20.0, 15.0, 25.0, 50000000L, 1.0, 20000000.0, 21000000.0, 21000000.0,
                        60000.0, -20.0, "2021-04-14", 3000.0, 1500.0, "2018-12-15", "2024-08-25T00:00:00Z"),
                createCoin("ethereum", "eth", "Ethereum", 3000.0, 400000000L, 2, 500000000L, 100000000L, 3500.0, 2500.0,
                        50.0, 1.5, 0.2, 8.0, 11.0, 25.0, 14.0, 30.0, 40000000L, 2.5, 8000000.0, 10000000.0, 12000000.0,
                        4500.0, -25.0, "2022-04-14", 1000.0, 1300.0, "2019-12-15", "2024-08-25T00:00:00Z")
        );

        System.out.println("Test coins added to cache");

        coinCache.updateCache(testCoins);
        System.out.println("Cache update completed");

        // Check if the cache file is created
        if (!cacheFile.exists()) {
            System.out.println("Cache file not found at expected location: " + cacheFile.getAbsolutePath());
        }

        assertThat(cacheFile.exists())
                .as("Checking if the cache file exists at " + cacheFile.getAbsolutePath())
                .isTrue();

        // Verify the cache contains the added coins
        List<Coin> coins = coinCache.getAllCoins();
        Assertions.assertThat(coins).hasSize(2);

        // Sort coins by market cap rank to ensure consistent order
        coins.sort(Comparator.comparingInt(Coin::getMarketCapRank));

        assertThat(coins.get(0).getCoinId()).isEqualTo("bitcoin");
        assertThat(coins.get(1).getCoinId()).isEqualTo("ethereum");

        System.out.println("Test completed successfully, cache file and contents are as expected.");
    }

    /**
     * Tests that the cache correctly updates existing coins without adding duplicates.
     */
    @Test
    public void testUpdateCacheRemovesDuplicates() {
        // Add initial coins
        List<Coin> initialCoins = List.of(
                createCoin("bitcoin", "btc", "Bitcoin", 50000.0, 1000000000L, 1, 1100000000L, 500000000L, 55000.0, 45000.0,
                        500.0, 1.0, 0.5, 7.0, 10.0, 20.0, 15.0, 25.0, 50000000L, 1.0, 20000000.0, 21000000.0, 21000000.0,
                        60000.0, -20.0, "2021-04-14", 3000.0, 1500.0, "2018-12-15", "2024-08-25T00:00:00Z")
        );
        coinCache.updateCache(initialCoins);

        // Add a duplicate coin with updated data
        List<Coin> updatedCoins = List.of(
                createCoin("bitcoin", "btc", "Bitcoin", 51000.0, 1100000000L, 1, 1200000000L, 550000000L, 56000.0, 46000.0,
                        510.0, 1.1, 0.6, 7.5, 10.5, 21.0, 16.0, 26.0, 51000000L, 1.2, 21000000.0, 22000000.0, 22000000.0,
                        61000.0, -21.0, "2021-05-14", 3100.0, 1600.0, "2019-12-16", "2024-09-01T00:00:00Z")
        );
        coinCache.updateCache(updatedCoins);

        // Verify that "bitcoin" has been updated but only one instance remains
        List<Coin> coins = coinCache.getAllCoins();
        Assertions.assertThat(coins).hasSize(1);  // Ensure no duplicates
        assertThat(coins.get(0).getCurrentPrice()).isEqualTo(51000.0);  // Verify the coin was updated
    }

    /**
     * Tests that coins outside the top 1000 are retained in the cache after an update.
     */
    @Test
    public void testRetainsCoinsOutsideTop1000() {
        // Add a large number of coins (1001 coins)
        List<Coin> initialCoins = createSampleCoins(1001);
        coinCache.updateCache(initialCoins);

        // Add new coins to simulate an update
        List<Coin> newCoins = createSampleCoins(500);  // Simulate top 500 coins
        coinCache.updateCache(newCoins);

        // Verify that there are still 1001 coins in the cache (existing 1001 coins should remain)
        List<Coin> coins = coinCache.getAllCoins();
        Assertions.assertThat(coins).hasSize(1001);
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
                    "coin" + i, "sym" + i, "Coin " + i, 1000.0 * i, 1000000L * i, i, 1100000L * i, 500000L * i,
                    1200.0 * i, 900.0 * i, 10.0 * i, 1.0 * i, 0.5 * i, 7.0 * i, 10.0 * i, 20.0 * i, 15.0 * i, 25.0 * i,
                    50000000L * i, 1.0 * i, 20000000.0 * i, 21000000.0 * i, 21000000.0 * i, 60000.0 * i, -20.0 * i,
                    "2021-04-14", 3000.0 * i, 1500.0 * i, "2018-12-15", "2024-08-25T00:00:00Z"
            ));
        }
        return coins;
    }

    /**
     * Helper method to create a {@link Coin} object.
     */
    private Coin createCoin(String coinId, String symbol, String name, double currentPrice, long marketCap, int marketCapRank,
                            long fullyDilutedValuation, long totalVolume, double high24h, double low24h, double priceChange24h,
                            double priceChangePercentage24h, double priceChangePercentage1h, double priceChangePercentage7d,
                            double priceChangePercentage14d, double priceChangePercentage30d, double priceChangePercentage200d,
                            double priceChangePercentage1y, long marketCapChange24h, double marketCapChangePercentage24h,
                            double circulatingSupply, double totalSupply, double maxSupply, double ath, double athChangePercentage,
                            String athDate, double atl, double atlChangePercentage, String atlDate, String lastUpdated) {
        Coin coin = new Coin();
        coin.setCoinId(coinId);
        coin.setSymbol(symbol);
        coin.setName(name);
        coin.setImage("https://example.com/" + coinId + ".png");
        coin.setCurrentPrice(currentPrice);
        coin.setMarketCap(marketCap);
        coin.setMarketCapRank(marketCapRank);
        coin.setFullyDilutedValuation(fullyDilutedValuation);
        coin.setTotalVolume(totalVolume);
        coin.setHigh24h(high24h);
        coin.setLow24h(low24h);
        coin.setPriceChange24h(priceChange24h);
        coin.setPriceChangePercentage24h(priceChangePercentage24h);
        coin.setPriceChangePercentage1h(priceChangePercentage1h);
        coin.setPriceChangePercentage7d(priceChangePercentage7d);
        coin.setPriceChangePercentage14d(priceChangePercentage14d);
        coin.setPriceChangePercentage30d(priceChangePercentage30d);
        coin.setPriceChangePercentage200d(priceChangePercentage200d);
        coin.setPriceChangePercentage1y(priceChangePercentage1y);
        coin.setMarketCapChange24h(marketCapChange24h);
        coin.setMarketCapChangePercentage24h(marketCapChangePercentage24h);
        coin.setCirculatingSupply(circulatingSupply);
        coin.setTotalSupply(totalSupply);
        coin.setMaxSupply(maxSupply);
        coin.setAth(ath);
        coin.setAthChangePercentage(athChangePercentage);
        coin.setAthDate(athDate);
        coin.setAtl(atl);
        coin.setAtlChangePercentage(atlChangePercentage);
        coin.setAtlDate(atlDate);
        coin.setLastUpdated(lastUpdated);
        return coin;
    }
}
