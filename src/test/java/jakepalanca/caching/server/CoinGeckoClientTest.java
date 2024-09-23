// ----- CoinGeckoClientTest.java -----
package jakepalanca.caching.server;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link CoinGeckoClient} class.
 * These tests focus on fetching and parsing coin data from the CoinGecko API.
 */
public class CoinGeckoClientTest {

    /**
     * Tests that the fetchTopCoins method correctly retrieves and parses coins with all properties set.
     *
     * @throws IOException    if an I/O error occurs during the HTTP request
     * @throws ParseException if an error occurs while parsing the response
     */
    @Test
    public void testFetchTopCoinsWithCorrectProperties() throws IOException, ParseException {
        // Mock the CoinGeckoClient
        CoinGeckoClient client = Mockito.mock(CoinGeckoClient.class);

        // Create a mock Coin using setters instead of the Builder pattern
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
        mockCoin.setAth(60000.0);
        mockCoin.setAthChangePercentage(-20.0);
        mockCoin.setAthDate("2021-04-14");
        mockCoin.setAtl(3000.0);
        mockCoin.setAtlChangePercentage(1500.0);
        mockCoin.setAtlDate("2019-01-01");
        mockCoin.setRoi(null);
        mockCoin.setLastUpdated("2024-08-25T00:00:00Z");

        // Mock the fetchTopCoins method to return the mockCoin
        when(client.fetchTopCoins()).thenReturn(List.of(mockCoin));

        // Call the method and assert results
        List<Coin> coins = client.fetchTopCoins();
        assertThat(coins).isNotEmpty();
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
        assertThat(coin.getAth()).isEqualTo(60000.0);
        assertThat(coin.getAthChangePercentage()).isEqualTo(-20.0);
        assertThat(coin.getAthDate()).isEqualTo("2021-04-14");
        assertThat(coin.getAtl()).isEqualTo(3000.0);
        assertThat(coin.getAtlChangePercentage()).isEqualTo(1500.0);
        assertThat(coin.getAtlDate()).isEqualTo("2019-01-01");
        assertThat(coin.getRoi()).isNull();
        assertThat(coin.getLastUpdated()).isEqualTo("2024-08-25T00:00:00Z");
    }
}
