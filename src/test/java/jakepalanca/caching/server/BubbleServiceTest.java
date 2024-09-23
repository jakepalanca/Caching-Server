// ----- BubbleServiceTest.java -----
package jakepalanca.caching.server;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link BubbleService} class.
 * These tests focus on verifying the correctness of bubble creation based on cryptocurrency data.
 */
public class BubbleServiceTest {

    /**
     * Tests that bubbles are created with correct properties, including positive radius ratios and accurate color assignments.
     */
    @Test
    public void testCreateBubblesWithCorrectProperties() {
        // Create sample coins with varying market cap values
        List<Coin> coins = new ArrayList<>();

        // Coin with the highest market cap
        Coin maxCapCoin = new Coin();
        maxCapCoin.setCoinId("coinMax");
        maxCapCoin.setSymbol("symMax");
        maxCapCoin.setName("Coin Max");
        maxCapCoin.setImage("https://example.com/coinMax.png");
        maxCapCoin.setCurrentPrice(10000.0);
        maxCapCoin.setMarketCap(5000000L); // Highest market cap
        maxCapCoin.setMarketCapRank(1);
        maxCapCoin.setFullyDilutedValuation(6000000L);
        maxCapCoin.setTotalVolume(3000000L);
        maxCapCoin.setHigh24h(10500.0);
        maxCapCoin.setLow24h(9500.0);
        maxCapCoin.setPriceChange24h(500.0);
        maxCapCoin.setPriceChangePercentage24h(5.0);
        maxCapCoin.setPriceChangePercentage1h(0.5);
        maxCapCoin.setPriceChangePercentage7d(7.0);
        maxCapCoin.setPriceChangePercentage14d(14.0);
        maxCapCoin.setPriceChangePercentage30d(30.0);
        maxCapCoin.setPriceChangePercentage200d(20.0);
        maxCapCoin.setPriceChangePercentage1y(100.0);
        maxCapCoin.setMarketCapChange24h(250000L);
        maxCapCoin.setMarketCapChangePercentage24h(5.0);
        maxCapCoin.setCirculatingSupply(500000.0);
        maxCapCoin.setTotalSupply(1000000.0);
        maxCapCoin.setMaxSupply(1500000.0);
        maxCapCoin.setAth(12000.0);
        maxCapCoin.setAthChangePercentage(-10.0);
        maxCapCoin.setAthDate("2021-01-01");
        maxCapCoin.setAtl(1000.0);
        maxCapCoin.setAtlChangePercentage(50.0);
        maxCapCoin.setAtlDate("2020-01-01");
        maxCapCoin.setRoi(null);
        maxCapCoin.setLastUpdated("2024-08-25T00:00:00Z");

        coins.add(maxCapCoin);

        // Additional coins with varying market cap values
        for (int i = 1; i <= 9; i++) {
            Coin coin = new Coin();
            coin.setCoinId("coin" + i);
            coin.setSymbol("sym" + i);
            coin.setName("Coin " + i);
            coin.setImage("https://example.com/coin" + i + ".png");
            coin.setCurrentPrice(1000.0 * i);
            coin.setMarketCap(1000000L * i); // Varying market cap
            coin.setMarketCapRank(i + 1);
            coin.setFullyDilutedValuation(1100000L * i);
            coin.setTotalVolume(500000L * i);
            coin.setHigh24h(1200.0 * i);
            coin.setLow24h(900.0 * i);
            coin.setPriceChange24h(10.0 * i);
            coin.setPriceChangePercentage24h(1.0 * i);
            coin.setPriceChangePercentage1h(0.5 * i);
            coin.setPriceChangePercentage7d(7.0 * i);
            coin.setPriceChangePercentage14d(10.0 * i);
            coin.setPriceChangePercentage30d(20.0 * i);
            coin.setPriceChangePercentage200d(15.0 * i);
            coin.setPriceChangePercentage1y(25.0 * i);
            coin.setMarketCapChange24h(500000L * i);
            coin.setMarketCapChangePercentage24h(1.0 * i);
            coin.setCirculatingSupply(200000.0 * i);
            coin.setTotalSupply(210000.0 * i);
            coin.setMaxSupply(220000.0 * i);
            coin.setAth(6000.0 * i);
            coin.setAthChangePercentage(-2.0 * i);
            coin.setAthDate("2021-04-14");
            coin.setAtl(3000.0 * i);
            coin.setAtlChangePercentage(1500.0 * i);
            coin.setAtlDate("2018-12-15");
            coin.setRoi(null);
            coin.setLastUpdated("2024-08-25T00:00:00Z");
            coins.add(coin);
        }

        BubbleService bubbleService = new BubbleService();
        List<Bubble> bubbles = bubbleService.createBubbles(coins, "market_cap", null, 800, 600);

        // Assertions to ensure bubbles have positive radius ratios
        for (Bubble bubble : bubbles) {
            assertThat(bubble.getRadiusRatio()).isGreaterThan(0);
            assertThat(bubble.getRadius()).isGreaterThan(0);
            assertThat(bubble.getColor()).isNotNull();
        }

        // Find the bubble corresponding to the coin with the highest market cap
        Bubble targetBubble = bubbles.stream()
                .filter(bubble -> bubble.getCoin().getCoinId().equals("coinMax"))
                .findFirst()
                .orElse(null);

        // Verify that the target bubble exists
        assertThat(targetBubble).isNotNull();

        // Verify that the color of the target bubble is "blue"
        assertThat(targetBubble.getColor()).isEqualTo("blue");
    }
}
