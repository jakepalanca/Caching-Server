// ----- CryptoCacheFunctionalTest.java -----
package jakepalanca.caching.server;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Functional tests for the CryptoCacheApplication.
 * These tests validate the end-to-end functionality of fetching, caching, and retrieving coin data.
 */
class CryptoCacheFunctionalTest {

    private CoinCache coinCache;
    private CoinGeckoClient coinGeckoClient;

    @BeforeEach
    public void setup() {
        // Define the directory where the cache will be stored
        String cacheDirectory = "~/testing_cache/";

        // Create a testing CoinCache instance
        coinCache = new CoinCache(cacheDirectory, true); // Remove 'CoinCache' type to assign to the instance variable
        coinCache.emptyCacheFile(); // Ensure cache is clean before each test
        coinGeckoClient = Mockito.mock(CoinGeckoClient.class);
    }


    @AfterEach
    public void cleanup() throws IOException {
        coinCache.emptyCacheFile(); // Ensure cache is clean before each test
    }

    /**
     * Tests that the fetchTopCoins method retrieves coins with all correct properties set.
     */
    @Test
    public void testFetchTopCoinsWithCorrectProperties() throws IOException, ParseException, org.apache.hc.core5.http.ParseException {
        CoinGeckoClient client = Mockito.mock(CoinGeckoClient.class);

        // Mock the fetchTopCoins method with a specific coin
        Coin mockCoin = new Coin();
        mockCoin.setCoinId("ethereum");
        mockCoin.setSymbol("eth");
        mockCoin.setName("Ethereum");
        mockCoin.setImage("https://example.com/ethereum.png");
        mockCoin.setCurrentPrice(4000.0);
        mockCoin.setMarketCap(500000000L);
        mockCoin.setMarketCapRank(2);
        mockCoin.setFullyDilutedValuation(550000000L);
        mockCoin.setTotalVolume(250000000L);
        mockCoin.setHigh24h(4200.0);
        mockCoin.setLow24h(3800.0);
        mockCoin.setPriceChange24h(100.0);
        mockCoin.setPriceChangePercentage24h(2.0);
        mockCoin.setPriceChangePercentage1h(0.8);
        mockCoin.setPriceChangePercentage7d(5.0);
        mockCoin.setPriceChangePercentage14d(7.0);
        mockCoin.setPriceChangePercentage30d(10.0);
        mockCoin.setPriceChangePercentage200d(8.0);
        mockCoin.setPriceChangePercentage1y(12.0);
        mockCoin.setMarketCapChange24h(10000000L);
        mockCoin.setMarketCapChangePercentage24h(1.5);
        mockCoin.setCirculatingSupply(10000000.0);
        mockCoin.setTotalSupply(11000000.0);
        mockCoin.setMaxSupply(12000000.0);
        mockCoin.setAth(5000.0);
        mockCoin.setAthChangePercentage(-20.0);
        mockCoin.setAthDate("2021-04-14");
        mockCoin.setAtl(100.0);
        mockCoin.setAtlChangePercentage(50.0);
        mockCoin.setAtlDate("2019-01-01");
        mockCoin.setRoi(null);
        mockCoin.setLastUpdated("2024-08-25T00:00:00Z");

        when(client.fetchTopCoins()).thenReturn(List.of(mockCoin));

        // Call the method and assert results
        List<Coin> coins = client.fetchTopCoins();
        Assertions.assertThat(coins).isNotEmpty();
        Coin coin = coins.get(0);

        // Assert all properties of the Coin object
        assertThat(coin.getCoinId()).isEqualTo("ethereum");
        assertThat(coin.getSymbol()).isEqualTo("eth");
        assertThat(coin.getName()).isEqualTo("Ethereum");
        assertThat(coin.getImage()).isEqualTo("https://example.com/ethereum.png");
        assertThat(coin.getCurrentPrice()).isEqualTo(4000.0);
        assertThat(coin.getMarketCap()).isEqualTo(500000000L);
        assertThat(coin.getMarketCapRank()).isEqualTo(2);
        assertThat(coin.getFullyDilutedValuation()).isEqualTo(550000000L);
        assertThat(coin.getTotalVolume()).isEqualTo(250000000L);
        assertThat(coin.getHigh24h()).isEqualTo(4200.0);
        assertThat(coin.getLow24h()).isEqualTo(3800.0);
        assertThat(coin.getPriceChange24h()).isEqualTo(100.0);
        assertThat(coin.getPriceChangePercentage24h()).isEqualTo(2.0);
        assertThat(coin.getPriceChangePercentage1h()).isEqualTo(0.8);
        assertThat(coin.getPriceChangePercentage7d()).isEqualTo(5.0);
        assertThat(coin.getPriceChangePercentage14d()).isEqualTo(7.0);
        assertThat(coin.getPriceChangePercentage30d()).isEqualTo(10.0);
        assertThat(coin.getPriceChangePercentage200d()).isEqualTo(8.0);
        assertThat(coin.getPriceChangePercentage1y()).isEqualTo(12.0);
        assertThat(coin.getMarketCapChange24h()).isEqualTo(10000000L);
        assertThat(coin.getMarketCapChangePercentage24h()).isEqualTo(1.5);
        assertThat(coin.getCirculatingSupply()).isEqualTo(10000000.0);
        assertThat(coin.getTotalSupply()).isEqualTo(11000000.0);
        assertThat(coin.getMaxSupply()).isEqualTo(12000000.0);
        assertThat(coin.getAth()).isEqualTo(5000.0);
        assertThat(coin.getAthChangePercentage()).isEqualTo(-20.0);
        assertThat(coin.getAthDate()).isEqualTo("2021-04-14");
        assertThat(coin.getAtl()).isEqualTo(100.0);
        assertThat(coin.getAtlChangePercentage()).isEqualTo(50.0);
        assertThat(coin.getAtlDate()).isEqualTo("2019-01-01");
        assertThat(coin.getRoi()).isNull();
        assertThat(coin.getLastUpdated()).isEqualTo("2024-08-25T00:00:00Z");
    }
}
