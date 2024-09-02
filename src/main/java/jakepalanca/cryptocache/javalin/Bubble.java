package jakepalanca.cryptocache.javalin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Bubble object that extends the Coin class.
 * The Bubble class includes additional properties specific to the Bubble,
 * such as its x and y coordinates, radius, and color.
 *
 * <p>This class is used to represent visual bubbles in a bubble chart that
 * correspond to cryptocurrency data retrieved from the CoinGecko API.</p>
 *
 * <p><strong>Note:</strong> The {@code Bubble} class inherits all properties of {@code Coin} and
 * adds additional fields necessary for graphical representation.</p>
 *
 * <p>The {@code @JsonIgnoreProperties} annotation is used to ignore any unknown
 * JSON properties during deserialization, ensuring forward compatibility.</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * Bubble bubble = new Bubble("bitcoin", "BTC", "Bitcoin", "https://image.url", 50000.0,
 *                            1000000000L, 1, 1200000000L, 100000000L, 55000.0,
 *                            48000.0, 2000.0, 4.0, 0.5, 3.0, 6.0, 10.0,
 *                            8.0, 50000000L, 5.0, 21000000.0, 18000000.0,
 *                            21000000.0, 65000.0, -5.0, "2021-04-14",
 *                            3000.0, -90.0, "2017-12-15", null,
 *                            "2024-01-01T00:00:00Z", 100.0, 200.0, 25.0, "#00FF00");
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bubble extends Coin {

    /**
     * The x-coordinate of the bubble in the chart.
     */
    private double x;

    /**
     * The y-coordinate of the bubble in the chart.
     */
    private double y;

    /**
     * The radius of the bubble in the chart.
     */
    private double radius;

    /**
     * The color of the bubble (e.g., "#00FF00" for green).
     */
    private String color;

    /**
     * Constructs a new {@code Bubble} object with all fields initialized.
     *
     * @param id                       the unique identifier of the coin
     * @param symbol                   the ticker symbol of the coin
     * @param name                     the name of the coin
     * @param image                    the URL of the coin's image
     * @param currentPrice             the current price of the coin
     * @param marketCap                the market capitalization of the coin
     * @param marketCapRank            the market capitalization rank of the coin
     * @param fullyDilutedValuation    the fully diluted valuation of the coin
     * @param totalVolume              the total volume of the coin
     * @param high24h                  the highest price in the last 24 hours
     * @param low24h                   the lowest price in the last 24 hours
     * @param priceChange24h           the price change in the last 24 hours
     * @param priceChangePercentage24h the price change percentage in the last 24 hours
     * @param priceChangePercentage1h  the price change percentage in the last 1 hour
     * @param priceChangePercentage7d  the price change percentage in the last 7 days
     * @param priceChangePercentage14d the price change percentage in the last 14 days
     * @param priceChangePercentage30d the price change percentage in the last 30 days
     * @param priceChangePercentage200d the price change percentage in the last 200 days
     * @param priceChangePercentage1y  the price change percentage in the last 1 year
     * @param marketCapChange24h       the market capitalization change in the last 24 hours
     * @param marketCapChangePercentage24h the market capitalization change percentage in the last 24 hours
     * @param circulatingSupply        the circulating supply of the coin
     * @param totalSupply              the total supply of the coin
     * @param maxSupply                the maximum supply of the coin
     * @param ath                      the all-time high price of the coin
     * @param athChangePercentage      the percentage change from the all-time high
     * @param athDate                  the date when the all-time high was reached
     * @param atl                      the all-time low price of the coin
     * @param atlChangePercentage      the percentage change from the all-time low
     * @param atlDate                  the date when the all-time low was reached
     * @param roi                      the return on investment for the coin (may be null)
     * @param lastUpdated              the last update timestamp for the coin data
     * @param x                        the x-coordinate of the bubble in the chart
     * @param y                        the y-coordinate of the bubble in the chart
     * @param radius                   the radius of the bubble in the chart
     * @param color                    the color of the bubble (e.g., "#00FF00" for green)
     */
    public Bubble(String id, String symbol, String name, String image, double currentPrice, long marketCap,
                  int marketCapRank, Long fullyDilutedValuation, long totalVolume, double high24h, double low24h,
                  double priceChange24h, double priceChangePercentage24h, double priceChangePercentage1h,
                  double priceChangePercentage7d, double priceChangePercentage14d, double priceChangePercentage30d,
                  double priceChangePercentage200d, double priceChangePercentage1y, long marketCapChange24h,
                  double marketCapChangePercentage24h, double circulatingSupply, double totalSupply, double maxSupply,
                  double ath, double athChangePercentage, String athDate, double atl, double atlChangePercentage,
                  String atlDate, Object roi, String lastUpdated, double x, double y, double radius, String color) {

        // Call the superclass constructor to initialize Coin fields
        super(id, symbol, name, image, currentPrice, marketCap, marketCapRank, fullyDilutedValuation, totalVolume,
                high24h, low24h, priceChange24h, priceChangePercentage24h, priceChangePercentage1h,
                priceChangePercentage7d, priceChangePercentage14d, priceChangePercentage30d,
                priceChangePercentage200d, priceChangePercentage1y, marketCapChange24h, marketCapChangePercentage24h,
                circulatingSupply, totalSupply, maxSupply, ath, athChangePercentage, athDate, atl, atlChangePercentage,
                atlDate, roi, lastUpdated);

        // Initialize Bubble-specific fields
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    /**
     * Constructs a new {@code Bubble} object with only the essential fields initialized.
     * This constructor can be used when the full set of coin details is not needed.
     *
     * @param id     the unique identifier of the coin
     * @param symbol the ticker symbol of the coin
     * @param name   the name of the coin
     * @param x      the x-coordinate of the bubble in the chart
     * @param y      the y-coordinate of the bubble in the chart
     * @param radius the radius of the bubble in the chart
     * @param color  the color of the bubble (e.g., "#00FF00" for green)
     */
    public Bubble(String id, String symbol, String name, int x, int y, double radius, String color) {
        this.setId(id);
        this.setSymbol(symbol);
        this.setName(name);
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    /**
     * Gets the x-coordinate of the bubble.
     *
     * @return the x-coordinate of the bubble in the chart
     */
    @JsonProperty("x")
    public double getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the bubble.
     *
     * @param x the x-coordinate of the bubble in the chart
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the y-coordinate of the bubble.
     *
     * @return the y-coordinate of the bubble in the chart
     */
    @JsonProperty("y")
    public double getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of the bubble.
     *
     * @param y the y-coordinate of the bubble in the chart
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the radius of the bubble.
     *
     * @return the radius of the bubble in the chart
     */
    @JsonProperty("radius")
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the bubble.
     *
     * @param radius the radius of the bubble in the chart
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Gets the color of the bubble.
     *
     * @return the color of the bubble (e.g., "#00FF00" for green)
     */
    @JsonProperty("color")
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of the bubble.
     *
     * @param color the color of the bubble (e.g., "#00FF00" for green)
     */
    public void setColor(String color) {
        this.color = color;
    }
}
