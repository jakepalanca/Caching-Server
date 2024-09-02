package jakepalanca.cryptocache.javalin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link BubbleService} class.
 */
public class BubbleServiceTest {

    /**
     * Tests the {@code createBubbles} method for market capitalization.
     * Bubbles should be created and colored blue as market cap is not a percentage-based data type.
     */
    @Test
    public void testCreateBubblesForMarketCap() {
        BubbleService bubbleService = new BubbleService();

        Coin mockCoin = new Coin("bitcoin", "btc", "Bitcoin", "https://example.com/bitcoin.png", 50000.0, 1000000000L, 1,
                1100000000L, 500000000L, 55000.0, 45000.0, 500.0, 1.0, 0.5, 7.0, 10.0, 20.0, 15.0, 25.0,
                50000000L, 1.0, 20000000.0, 21000000.0, 21000000.0, 60000.0, -20.0, "2021-04-14", 3000.0,
                1500.0, "2018-12-15", null, "2024-08-25T00:00:00Z");

        // Create a list of mock coins
        List<Coin> coins = List.of(mockCoin);

        // Call createBubbles with mock data
        List<Bubble> bubbles = bubbleService.createBubbles(coins, "market_cap", Optional.empty(), 1080, 1920);

        // Validate the output
        assertThat(bubbles).isNotEmpty();
        assertThat(bubbles.get(0).getRadius()).isGreaterThan(0);
        assertThat(bubbles.get(0).getColor()).isEqualTo("#0000FF"); // Non-percentage based, should be blue
    }

    /**
     * Tests the {@code createBubbles} method for total volume.
     * Bubbles should be created and colored blue as total volume is not a percentage-based data type.
     */
    @Test
    public void testCreateBubblesForTotalVolume() {
        BubbleService bubbleService = new BubbleService();

        Coin mockCoin = new Coin("ethereum", "eth", "Ethereum", "https://example.com/ethereum.png", 4000.0, 500000000L, 2,
                600000000L, 200000000L, 4200.0, 3800.0, 100.0, 2.0, 1.0, 5.0, 7.0, 10.0, 8.0, 12.0,
                20000000L, 2.0, 10000000.0, 12000000.0, 12000000.0, 4500.0, -10.0, "2021-05-10", 1000.0,
                500.0, "2019-01-01", null, "2024-08-25T00:00:00Z");

        List<Coin> coins = List.of(mockCoin);

        List<Bubble> bubbles = bubbleService.createBubbles(coins, "total_volume", Optional.empty(), 1080, 1920);

        // Validate the output
        assertThat(bubbles).isNotEmpty();
        assertThat(bubbles.get(0).getRadius()).isGreaterThan(0);
        assertThat(bubbles.get(0).getColor()).isEqualTo("#0000FF"); // Non-percentage based, should be blue
    }

    /**
     * Tests the {@code createBubbles} method for price change with a specific time interval.
     * Bubbles should be colored red if the price change is negative.
     */
    @Test
    public void testCreateBubblesForPriceChange() {
        BubbleService bubbleService = new BubbleService();

        Coin mockCoin = new Coin("dogecoin", "doge", "Dogecoin", "https://example.com/dogecoin.png", 0.25, 50000000L, 5,
                60000000L, 20000000L, 0.3, 0.2, 0.05, 0.5, 0.4, 1.5, 2.0, 3.0, 2.5, 3.5, 10000000L,
                0.6, 5000000.0, 6000000.0, 6000000.0, 0.35, 30.0, "2021-05-08", 0.01, 100.0, "2018-12-15",
                null, "2024-08-25T00:00:00Z");

        List<Coin> coins = List.of(mockCoin);

        // Use a time interval that will return a positive price change
        List<Bubble> bubbles = bubbleService.createBubbles(coins, "price_change", Optional.of("24h"), 1080, 1920);

        // The color should be green (#00FF00) because of the positive price change
        assertThat(bubbles).isNotEmpty();
        assertThat(bubbles.get(0).getRadius()).isGreaterThan(0);
        assertThat(bubbles.get(0).getColor()).isEqualTo("#00FF00"); // Green for positive price change
    }


    /**
     * Tests the {@code createBubbles} method for rank data type.
     * Bubbles should be created and colored blue as rank is not a percentage-based data type.
     */
    @Test
    public void testCreateBubblesForRank() {
        BubbleService bubbleService = new BubbleService();

        Coin mockCoin = new Coin("litecoin", "ltc", "Litecoin", "https://example.com/litecoin.png", 200.0, 200000000L, 3,
                250000000L, 100000000L, 220.0, 180.0, 20.0, 1.5, 1.2, 5.0, 8.0, 15.0, 10.0, 20.0,
                10000000L, 2.0, 5000000.0, 6000000.0, 6000000.0, 250.0, -15.0, "2021-06-10", 50.0, 300.0,
                "2018-12-15", null, "2024-08-25T00:00:00Z");

        List<Coin> coins = List.of(mockCoin);

        List<Bubble> bubbles = bubbleService.createBubbles(coins, "rank", Optional.empty(), 1080, 1920);

        // Validate the output
        assertThat(bubbles).isNotEmpty();
        assertThat(bubbles.get(0).getRadius()).isGreaterThan(0);
        assertThat(bubbles.get(0).getColor()).isEqualTo("#0000FF"); // Non-percentage based, should be blue
    }

    /**
     * Tests the {@code createBubbles} method with an empty coin list.
     * This test ensures that the method handles an empty input list correctly by returning an empty bubble list.
     */
    @Test
    public void testCreateBubblesWithEmptyCoinList() {
        BubbleService bubbleService = new BubbleService();

        List<Coin> coins = List.of(); // Empty list

        List<Bubble> bubbles = bubbleService.createBubbles(coins, "market_cap", Optional.empty(), 1080, 1920);

        // Validate the output
        assertThat(bubbles).isEmpty();
    }

    /**
     * Tests the {@code createBubbles} method with different screen sizes.
     * Bubbles should be created and colored blue as the data type is non-percentage-based.
     */
    @Test
    public void testCreateBubblesForSmallScreenSize() {
        BubbleService bubbleService = new BubbleService();

        Coin mockCoin = new Coin("ripple", "xrp", "Ripple", "https://example.com/ripple.png", 1.0, 500000000L, 4,
                600000000L, 200000000L, 1.2, 0.8, 0.1, 1.0, 0.9, 2.0, 3.0, 5.0, 4.0, 6.0,
                10000000L, 0.5, 5000000.0, 6000000.0, 6000000.0, 1.5, -5.0, "2021-07-10", 0.2, 50.0,
                "2018-12-15", null, "2024-08-25T00:00:00Z");

        List<Coin> coins = List.of(mockCoin);

        List<Bubble> bubbles = bubbleService.createBubbles(coins, "market_cap", Optional.empty(), 480, 320);

        // Validate the output
        assertThat(bubbles).isNotEmpty();
        assertThat(bubbles.get(0).getRadius()).isGreaterThan(0);
        assertThat(bubbles.get(0).getColor()).isEqualTo("#0000FF"); // Non-percentage based, should be blue
    }

    /**
     * Tests the {@code createBubbles} method with invalid data type.
     * This test ensures that bubbles are not created when an invalid data type is provided.
     */
    @Test
    public void testCreateBubblesWithInvalidDataType() {
        BubbleService bubbleService = new BubbleService();

        Coin mockCoin = new Coin("monero", "xmr", "Monero", "https://example.com/monero.png", 300.0, 100000000L, 10,
                120000000L, 50000000L, 320.0, 280.0, 15.0, 2.0, 1.8, 4.5, 6.0, 9.0, 7.5, 10.0,
                5000000L, 1.5, 2000000.0, 2200000.0, 2200000.0, 350.0, -10.0, "2021-08-01", 150.0, 200.0,
                "2018-12-15", null, "2024-08-25T00:00:00Z");

        List<Coin> coins = List.of(mockCoin);

        // Test with an invalid data type
        List<Bubble> bubbles = bubbleService.createBubbles(coins, "invalid_data_type", Optional.empty(), 1080, 1920);

        // Validate the output
        assertThat(bubbles).isEmpty(); // Should not create bubbles with an invalid data type
    }
}
