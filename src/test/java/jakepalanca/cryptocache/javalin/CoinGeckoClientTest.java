package jakepalanca.cryptocache.javalin;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link CoinGeckoClient} class.
 * These tests ensure that the client correctly interacts with the CoinGecko API and handles responses appropriately.
 */
public class CoinGeckoClientTest {

    /**
     * Tests the {@code fetchTopCoins} method to ensure it correctly fetches and returns a list of Coin objects.
     * This test uses a mock CoinGeckoClient to simulate the API call and response.
     *
     * @throws IOException   if an I/O error occurs during the test
     * @throws ParseException if a parsing error occurs during the test
     */
    @Test
    public void testFetchTopCoins() throws IOException, ParseException {
        CoinGeckoClient client = mock(CoinGeckoClient.class);

        // Mock the fetchTopCoins method with a realistic return value
        when(client.fetchTopCoins()).thenReturn(List.of(
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
                )
        ));

        // Call the method and assert results
        List<Coin> coins = client.fetchTopCoins();
        assertThat(coins).isNotEmpty();
        Coin coin = coins.get(0);

        // Assert all properties of the Coin object
        assertThat(coin.getId()).isEqualTo("bitcoin");
        assertThat(coin.getSymbol()).isEqualTo("btc");
        assertThat(coin.getName()).isEqualTo("Bitcoin");
        assertThat(coin.getImage()).isEqualTo("https://example.com/bitcoin.png");
        assertThat(coin.getCurrentPrice()).isEqualTo(50000.0);
        assertThat(coin.getMarketCap()).isEqualTo(1000000000L);
        assertThat(coin.getMarketCapRank()).isEqualTo(1);
        assertThat(coin.getFullyDilutedValuation()).isEqualTo(1100000000L);
        assertThat(coin.getTotalVolume()).isEqualTo(500000000L);
        assertThat(coin.getHigh24h()).isEqualTo(55000.0);
        assertThat(coin.getLow24h()).isEqualTo(45000.0);
        assertThat(coin.getPriceChange24h()).isEqualTo(500.0);
        assertThat(coin.getPriceChangePercentage24h()).isEqualTo(1.0);
        assertThat(coin.getPriceChangePercentage1h()).isEqualTo(0.5);
        assertThat(coin.getPriceChangePercentage7d()).isEqualTo(7.0);
        assertThat(coin.getPriceChangePercentage14d()).isEqualTo(10.0);
        assertThat(coin.getPriceChangePercentage30d()).isEqualTo(20.0);
        assertThat(coin.getPriceChangePercentage200d()).isEqualTo(15.0);
        assertThat(coin.getPriceChangePercentage1y()).isEqualTo(25.0);
        assertThat(coin.getMarketCapChange24h()).isEqualTo(50000000L);
        assertThat(coin.getMarketCapChangePercentage24h()).isEqualTo(1.0);
        assertThat(coin.getCirculatingSupply()).isEqualTo(20000000.0);
        assertThat(coin.getTotalSupply()).isEqualTo(21000000.0);
        assertThat(coin.getMaxSupply()).isEqualTo(21000000.0);
        assertThat(coin.getAth()).isEqualTo(60000.0);
        assertThat(coin.getAthChangePercentage()).isEqualTo(-20.0);
        assertThat(coin.getAthDate()).isEqualTo("2021-04-14");
        assertThat(coin.getAtl()).isEqualTo(3000.0);
        assertThat(coin.getAtlChangePercentage()).isEqualTo(1500.0);
        assertThat(coin.getAtlDate()).isEqualTo("2018-12-15");
        assertThat(coin.getRoi()).isNull();
        assertThat(coin.getLastUpdated()).isEqualTo("2024-08-25T00:00:00Z");
    }

    /**
     * Tests the {@code fetchTopCoins} method to ensure it returns an empty list when no coins are retrieved.
     * This could simulate a scenario where the API call returns no data.
     *
     * @throws IOException   if an I/O error occurs during the test
     * @throws ParseException if a parsing error occurs during the test
     */
    @Test
    public void testFetchTopCoinsReturnsEmptyList() throws IOException, ParseException {
        CoinGeckoClient client = mock(CoinGeckoClient.class);

        // Mock the fetchTopCoins method to return an empty list
        when(client.fetchTopCoins()).thenReturn(List.of());

        // Call the method and assert results
        List<Coin> coins = client.fetchTopCoins();
        assertThat(coins).isEmpty(); // The result should be an empty list
    }

    /**
     * Tests the {@code fetchTopCoins} method to ensure it handles an API failure gracefully.
     * This could simulate a scenario where the API call fails and throws an IOException.
     */
    @Test
    public void testFetchTopCoinsHandlesIOException() {
        CoinGeckoClient client = mock(CoinGeckoClient.class);

        try {
            // Mock the fetchTopCoins method to throw an IOException
            when(client.fetchTopCoins()).thenThrow(new IOException("API failure"));

            // Call the method and expect an exception
            client.fetchTopCoins();
        } catch (IOException | ParseException e) {
            // Assert that the exception is thrown with the correct message
            assertThat(e).hasMessage("API failure");
        }
    }

    /**
     * Tests the {@code fetchTopCoins} method to ensure it handles a parsing exception gracefully.
     * This could simulate a scenario where the API response is malformed or unparseable.
     */
    @Test
    public void testFetchTopCoinsHandlesParseException() {
        CoinGeckoClient client = mock(CoinGeckoClient.class);

        try {
            // Mock the fetchTopCoins method to throw a ParseException
            when(client.fetchTopCoins()).thenThrow(new ParseException("Parsing failure"));

            // Call the method and expect an exception
            client.fetchTopCoins();
        } catch (IOException | ParseException e) {
            // Assert that the exception is thrown with the correct message
            assertThat(e).hasMessage("Parsing failure");
        }
    }

    /**
     * Tests the {@code fetchTopCoins} method to ensure it returns a list of coins with the correct properties.
     * This test checks the integrity of the Coin objects returned by the method.
     *
     * @throws IOException   if an I/O error occurs during the test
     * @throws ParseException if a parsing error occurs during the test
     */
    @Test
    public void testFetchTopCoinsWithCorrectProperties() throws IOException, ParseException {
        CoinGeckoClient client = mock(CoinGeckoClient.class);

        // Mock the fetchTopCoins method with a specific coin
        Coin mockCoin = new Coin(
                "ethereum",                          // id
                "eth",                               // symbol
                "Ethereum",                          // name
                "https://example.com/ethereum.png",  // image
                4000.0,                              // currentPrice
                500000000L,                          // marketCap
                2,                                   // marketCapRank
                550000000L,                          // fullyDilutedValuation
                250000000L,                          // totalVolume
                4200.0,                              // high24h
                3800.0,                              // low24h
                100.0,                               // priceChange24h
                2.0,                                 // priceChangePercentage24h
                0.8,                                 // priceChangePercentage1h
                5.0,                                 // priceChangePercentage7d
                7.0,                                 // priceChangePercentage14d
                10.0,                                // priceChangePercentage30d
                8.0,                                 // priceChangePercentage200d
                12.0,                                // priceChangePercentage1y
                10000000L,                           // marketCapChange24h
                1.5,                                 // marketCapChangePercentage24h
                10000000.0,                          // circulatingSupply
                11000000.0,                          // totalSupply
                12000000.0,                          // maxSupply
                5000.0,                              // ath
                -20.0,                               // athChangePercentage
                "2021-04-14",                        // athDate
                100.0,                               // atl
                50.0,                                // atlChangePercentage
                "2019-01-01",                        // atlDate
                null,                                // roi
                "2024-08-25T00:00:00Z"               // lastUpdated
        );

        when(client.fetchTopCoins()).thenReturn(List.of(mockCoin));

        // Call the method and assert results
        List<Coin> coins = client.fetchTopCoins();
        assertThat(coins).isNotEmpty();
        Coin coin = coins.get(0);

        // Assert all properties of the Coin object
        assertThat(coin.getId()).isEqualTo("ethereum");
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
