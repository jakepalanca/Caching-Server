package jakepalanca.cryptocache.javalin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a Coin object with various attributes related to a cryptocurrency.
 * The Coin class implements {@code Serializable} to allow instances of this class
 * to be serialized, enabling them to be easily stored or transmitted.
 *
 * <p>This class is typically used to store data retrieved from an API (such as CoinGecko)
 * and to encapsulate information like the coin's name, symbol, price, and various other
 * financial metrics.</p>
 *
 * <p><strong>Note:</strong> The {@code @JsonIgnoreProperties} annotation is used to ignore any
 * unknown JSON properties during deserialization, which ensures compatibility with API changes.</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * Coin coin = new Coin("bitcoin", "BTC", "Bitcoin", "https://image.url", 50000.0,
 *                      1000000000L, 1, 1200000000L, 100000000L, 55000.0,
 *                      48000.0, 2000.0, 4.0, 0.5, 3.0, 6.0, 10.0,
 *                      8.0, 50000000L, 5.0, 21000000.0, 18000000.0,
 *                      21000000.0, 65000.0, -5.0, "2021-04-14",
 *                      3000.0, -90.0, "2017-12-15", null,
 *                      "2024-01-01T00:00:00Z");
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coin implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the coin (e.g., "bitcoin").
     */
    private String id;

    /**
     * The ticker symbol of the coin (e.g., "BTC").
     */
    private String symbol;

    /**
     * The name of the coin (e.g., "Bitcoin").
     */
    private String name;

    /**
     * The URL of the coin's image (e.g., "https://example.com/bitcoin.png").
     */
    private String image;

    /**
     * The current price of the coin in USD.
     */
    private double currentPrice;

    /**
     * The market capitalization of the coin in USD.
     */
    private long marketCap;

    /**
     * The market capitalization rank of the coin (e.g., 1 for the top coin by market cap).
     */
    private int marketCapRank;

    /**
     * The fully diluted valuation of the coin in USD, which is the market cap assuming all coins are in circulation.
     */
    private Long fullyDilutedValuation;

    /**
     * The total trading volume of the coin in the last 24 hours in USD.
     */
    private long totalVolume;

    /**
     * The highest price of the coin in the last 24 hours in USD.
     */
    private double high24h;

    /**
     * The lowest price of the coin in the last 24 hours in USD.
     */
    private double low24h;

    /**
     * The price change of the coin in the last 24 hours in USD.
     */
    private double priceChange24h;

    /**
     * The percentage price change of the coin in the last 24 hours.
     */
    private double priceChangePercentage24h;

    /**
     * The percentage price change of the coin in the last 1 hour.
     */
    private double priceChangePercentage1h;

    /**
     * The percentage price change of the coin in the last 7 days.
     */
    private double priceChangePercentage7d;

    /**
     * The percentage price change of the coin in the last 14 days.
     */
    private double priceChangePercentage14d;

    /**
     * The percentage price change of the coin in the last 30 days.
     */
    private double priceChangePercentage30d;

    /**
     * The percentage price change of the coin in the last 200 days.
     */
    private double priceChangePercentage200d;

    /**
     * The percentage price change of the coin in the last 1 year.
     */
    private double priceChangePercentage1y;

    /**
     * The market capitalization change of the coin in the last 24 hours in USD.
     */
    private long marketCapChange24h;

    /**
     * The market capitalization percentage change of the coin in the last 24 hours.
     */
    private double marketCapChangePercentage24h;

    /**
     * The circulating supply of the coin, which is the number of coins currently in circulation.
     */
    private double circulatingSupply;

    /**
     * The total supply of the coin, which is the total number of coins that currently exist.
     */
    private double totalSupply;

    /**
     * The maximum supply of the coin, which is the maximum number of coins that will ever exist.
     */
    private double maxSupply;

    /**
     * The all-time high price of the coin in USD.
     */
    private double ath;

    /**
     * The percentage change from the all-time high price of the coin.
     */
    private double athChangePercentage;

    /**
     * The date when the all-time high price was reached (e.g., "2021-04-14").
     */
    private String athDate;

    /**
     * The all-time low price of the coin in USD.
     */
    private double atl;

    /**
     * The percentage change from the all-time low price of the coin.
     */
    private double atlChangePercentage;

    /**
     * The date when the all-time low price was reached (e.g., "2018-12-15").
     */
    private String atlDate;

    /**
     * The return on investment (ROI) for the coin. This may be null if not available.
     */
    private Object roi;

    /**
     * The last update timestamp for the coin data (e.g., "2024-08-25T00:00:00Z").
     */
    private String lastUpdated;


    /**
     * Constructs a new {@code Coin} object with all fields initialized.
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
     */
    public Coin(String id, String symbol, String name, String image, double currentPrice, long marketCap, int marketCapRank,
                Long fullyDilutedValuation, long totalVolume, double high24h, double low24h, double priceChange24h,
                double priceChangePercentage24h, double priceChangePercentage1h, double priceChangePercentage7d,
                double priceChangePercentage14d, double priceChangePercentage30d, double priceChangePercentage200d,
                double priceChangePercentage1y, long marketCapChange24h, double marketCapChangePercentage24h,
                double circulatingSupply, double totalSupply, double maxSupply, double ath, double athChangePercentage,
                String athDate, double atl, double atlChangePercentage, String atlDate, Object roi, String lastUpdated) {

        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.marketCapRank = marketCapRank;
        this.fullyDilutedValuation = fullyDilutedValuation;
        this.totalVolume = totalVolume;
        this.high24h = high24h;
        this.low24h = low24h;
        this.priceChange24h = priceChange24h;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.priceChangePercentage1h = priceChangePercentage1h;
        this.priceChangePercentage7d = priceChangePercentage7d;
        this.priceChangePercentage14d = priceChangePercentage14d;
        this.priceChangePercentage30d = priceChangePercentage30d;
        this.priceChangePercentage200d = priceChangePercentage200d;
        this.priceChangePercentage1y = priceChangePercentage1y;
        this.marketCapChange24h = marketCapChange24h;
        this.marketCapChangePercentage24h = marketCapChangePercentage24h;
        this.circulatingSupply = circulatingSupply;
        this.totalSupply = totalSupply;
        this.maxSupply = maxSupply;
        this.ath = ath;
        this.athChangePercentage = athChangePercentage;
        this.athDate = athDate;
        this.atl = atl;
        this.atlChangePercentage = atlChangePercentage;
        this.atlDate = atlDate;
        this.roi = roi;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Default constructor for creating a new {@code Coin} object with no initial values.
     * Useful for cases where a Coin object needs to be created before its data is available.
     */
    public Coin() {
        // Default constructor
    }

    // Getters and Setters

    /**
     * Gets the unique identifier of the coin.
     *
     * @return the unique identifier of the coin
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the coin.
     *
     * @param id the unique identifier of the coin
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the ticker symbol of the coin.
     *
     * @return the ticker symbol of the coin
     */
    @JsonProperty("symbol")
    public String getSymbol() {
        return symbol;
    }

    /**
     * Sets the ticker symbol of the coin.
     *
     * @param symbol the ticker symbol of the coin
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the name of the coin.
     *
     * @return the name of the coin
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the coin.
     *
     * @param name the name of the coin
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the URL of the coin's image.
     *
     * @return the URL of the coin's image
     */
    @JsonProperty("image")
    public String getImage() {
        return image;
    }

    /**
     * Sets the URL of the coin's image.
     *
     * @param image the URL of the coin's image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the current price of the coin.
     *
     * @return the current price of the coin
     */
    @JsonProperty("current_price")
    public double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the current price of the coin.
     *
     * @param currentPrice the current price of the coin
     */
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    /**
     * Gets the market capitalization of the coin.
     *
     * @return the market capitalization of the coin
     */
    @JsonProperty("market_cap")
    public long getMarketCap() {
        return marketCap;
    }

    /**
     * Sets the market capitalization of the coin.
     *
     * @param marketCap the market capitalization of the coin
     */
    public void setMarketCap(long marketCap) {
        this.marketCap = marketCap;
    }

    /**
     * Gets the market capitalization rank of the coin.
     *
     * @return the market capitalization rank of the coin
     */
    @JsonProperty("market_cap_rank")
    public int getMarketCapRank() {
        return marketCapRank;
    }

    /**
     * Sets the market capitalization rank of the coin.
     *
     * @param marketCapRank the market capitalization rank of the coin
     */
    public void setMarketCapRank(int marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    /**
     * Gets the fully diluted valuation of the coin.
     *
     * @return the fully diluted valuation of the coin
     */
    @JsonProperty("fully_diluted_valuation")
    public Long getFullyDilutedValuation() {
        return fullyDilutedValuation;
    }

    /**
     * Sets the fully diluted valuation of the coin.
     *
     * @param fullyDilutedValuation the fully diluted valuation of the coin
     */
    public void setFullyDilutedValuation(Long fullyDilutedValuation) {
        this.fullyDilutedValuation = fullyDilutedValuation;
    }

    /**
     * Gets the total volume of the coin.
     *
     * @return the total volume of the coin
     */
    @JsonProperty("total_volume")
    public long getTotalVolume() {
        return totalVolume;
    }

    /**
     * Sets the total volume of the coin.
     *
     * @param totalVolume the total volume of the coin
     */
    public void setTotalVolume(long totalVolume) {
        this.totalVolume = totalVolume;
    }

    /**
     * Gets the highest price in the last 24 hours.
     *
     * @return the highest price in the last 24 hours
     */
    @JsonProperty("high_24h")
    public double getHigh24h() {
        return high24h;
    }

    /**
     * Sets the highest price in the last 24 hours.
     *
     * @param high24h the highest price in the last 24 hours
     */
    public void setHigh24h(double high24h) {
        this.high24h = high24h;
    }

    /**
     * Gets the lowest price in the last 24 hours.
     *
     * @return the lowest price in the last 24 hours
     */
    @JsonProperty("low_24h")
    public double getLow24h() {
        return low24h;
    }

    /**
     * Sets the lowest price in the last 24 hours.
     *
     * @param low24h the lowest price in the last 24 hours
     */
    public void setLow24h(double low24h) {
        this.low24h = low24h;
    }

    /**
     * Gets the price change in the last 24 hours.
     *
     * @return the price change in the last 24 hours
     */
    @JsonProperty("price_change_24h")
    public double getPriceChange24h() {
        return priceChange24h;
    }

    /**
     * Sets the price change in the last 24 hours.
     *
     * @param priceChange24h the price change in the last 24 hours
     */
    public void setPriceChange24h(double priceChange24h) {
        this.priceChange24h = priceChange24h;
    }

    /**
     * Gets the price change percentage in the last 24 hours.
     *
     * @return the price change percentage in the last 24 hours
     */
    @JsonProperty("price_change_percentage_24h")
    public double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    /**
     * Sets the price change percentage in the last 24 hours.
     *
     * @param priceChangePercentage24h the price change percentage in the last 24 hours
     */
    public void setPriceChangePercentage24h(double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    /**
     * Gets the price change percentage in the last 1 hour.
     *
     * @return the price change percentage in the last 1 hour
     */
    @JsonProperty("price_change_percentage_1h_in_currency")
    public double getPriceChangePercentage1h() {
        return priceChangePercentage1h;
    }

    /**
     * Sets the price change percentage in the last 1 hour.
     *
     * @param priceChangePercentage1h the price change percentage in the last 1 hour
     */
    public void setPriceChangePercentage1h(double priceChangePercentage1h) {
        this.priceChangePercentage1h = priceChangePercentage1h;
    }

    /**
     * Gets the price change percentage in the last 7 days.
     *
     * @return the price change percentage in the last 7 days
     */
    @JsonProperty("price_change_percentage_7d_in_currency")
    public double getPriceChangePercentage7d() {
        return priceChangePercentage7d;
    }

    /**
     * Sets the price change percentage in the last 7 days.
     *
     * @param priceChangePercentage7d the price change percentage in the last 7 days
     */
    public void setPriceChangePercentage7d(double priceChangePercentage7d) {
        this.priceChangePercentage7d = priceChangePercentage7d;
    }

    /**
     * Gets the price change percentage in the last 14 days.
     *
     * @return the price change percentage in the last 14 days
     */
    @JsonProperty("price_change_percentage_14d_in_currency")
    public double getPriceChangePercentage14d() {
        return priceChangePercentage14d;
    }

    /**
     * Sets the price change percentage in the last 14 days.
     *
     * @param priceChangePercentage14d the price change percentage in the last 14 days
     */
    public void setPriceChangePercentage14d(double priceChangePercentage14d) {
        this.priceChangePercentage14d = priceChangePercentage14d;
    }

    /**
     * Gets the price change percentage in the last 30 days.
     *
     * @return the price change percentage in the last 30 days
     */
    @JsonProperty("price_change_percentage_30d_in_currency")
    public double getPriceChangePercentage30d() {
        return priceChangePercentage30d;
    }

    /**
     * Sets the price change percentage in the last 30 days.
     *
     * @param priceChangePercentage30d the price change percentage in the last 30 days
     */
    public void setPriceChangePercentage30d(double priceChangePercentage30d) {
        this.priceChangePercentage30d = priceChangePercentage30d;
    }

    /**
     * Gets the price change percentage in the last 200 days.
     *
     * @return the price change percentage in the last 200 days
     */
    @JsonProperty("price_change_percentage_200d_in_currency")
    public double getPriceChangePercentage200d() {
        return priceChangePercentage200d;
    }

    /**
     * Sets the price change percentage in the last 200 days.
     *
     * @param priceChangePercentage200d the price change percentage in the last 200 days
     */
    public void setPriceChangePercentage200d(double priceChangePercentage200d) {
        this.priceChangePercentage200d = priceChangePercentage200d;
    }

    /**
     * Gets the price change percentage in the last 1 year.
     *
     * @return the price change percentage in the last 1 year
     */
    @JsonProperty("price_change_percentage_1y_in_currency")
    public double getPriceChangePercentage1y() {
        return priceChangePercentage1y;
    }

    /**
     * Sets the price change percentage in the last 1 year.
     *
     * @param priceChangePercentage1y the price change percentage in the last 1 year
     */
    public void setPriceChangePercentage1y(double priceChangePercentage1y) {
        this.priceChangePercentage1y = priceChangePercentage1y;
    }

    /**
     * Gets the market capitalization change in the last 24 hours.
     *
     * @return the market capitalization change in the last 24 hours
     */
    @JsonProperty("market_cap_change_24h")
    public long getMarketCapChange24h() {
        return marketCapChange24h;
    }

    /**
     * Sets the market capitalization change in the last 24 hours.
     *
     * @param marketCapChange24h the market capitalization change in the last 24 hours
     */
    public void setMarketCapChange24h(long marketCapChange24h) {
        this.marketCapChange24h = marketCapChange24h;
    }

    /**
     * Gets the market capitalization change percentage in the last 24 hours.
     *
     * @return the market capitalization change percentage in the last 24 hours
     */
    @JsonProperty("market_cap_change_percentage_24h")
    public double getMarketCapChangePercentage24h() {
        return marketCapChangePercentage24h;
    }

    /**
     * Sets the market capitalization change percentage in the last 24 hours.
     *
     * @param marketCapChangePercentage24h the market capitalization change percentage in the last 24 hours
     */
    public void setMarketCapChangePercentage24h(double marketCapChangePercentage24h) {
        this.marketCapChangePercentage24h = marketCapChangePercentage24h;
    }

    /**
     * Gets the circulating supply of the coin.
     *
     * @return the circulating supply of the coin
     */
    @JsonProperty("circulating_supply")
    public double getCirculatingSupply() {
        return circulatingSupply;
    }

    /**
     * Sets the circulating supply of the coin.
     *
     * @param circulatingSupply the circulating supply of the coin
     */
    public void setCirculatingSupply(double circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    /**
     * Gets the total supply of the coin.
     *
     * @return the total supply of the coin
     */
    @JsonProperty("total_supply")
    public double getTotalSupply() {
        return totalSupply;
    }

    /**
     * Sets the total supply of the coin.
     *
     * @param totalSupply the total supply of the coin
     */
    public void setTotalSupply(double totalSupply) {
        this.totalSupply = totalSupply;
    }

    /**
     * Gets the maximum supply of the coin.
     *
     * @return the maximum supply of the coin
     */
    @JsonProperty("max_supply")
    public double getMaxSupply() {
        return maxSupply;
    }

    /**
     * Sets the maximum supply of the coin.
     *
     * @param maxSupply the maximum supply of the coin
     */
    public void setMaxSupply(double maxSupply) {
        this.maxSupply = maxSupply;
    }

    /**
     * Gets the all-time high price of the coin.
     *
     * @return the all-time high price of the coin
     */
    @JsonProperty("ath")
    public double getAth() {
        return ath;
    }

    /**
     * Sets the all-time high price of the coin.
     *
     * @param ath the all-time high price of the coin
     */
    public void setAth(double ath) {
        this.ath = ath;
    }

    /**
     * Gets the percentage change from the all-time high.
     *
     * @return the percentage change from the all-time high
     */
    @JsonProperty("ath_change_percentage")
    public double getAthChangePercentage() {
        return athChangePercentage;
    }

    /**
     * Sets the percentage change from the all-time high.
     *
     * @param athChangePercentage the percentage change from the all-time high
     */
    public void setAthChangePercentage(double athChangePercentage) {
        this.athChangePercentage = athChangePercentage;
    }

    /**
     * Gets the date when the all-time high was reached.
     *
     * @return the date when the all-time high was reached
     */
    @JsonProperty("ath_date")
    public String getAthDate() {
        return athDate;
    }

    /**
     * Sets the date when the all-time high was reached.
     *
     * @param athDate the date when the all-time high was reached
     */
    public void setAthDate(String athDate) {
        this.athDate = athDate;
    }

    /**
     * Gets the all-time low price of the coin.
     *
     * @return the all-time low price of the coin
     */
    @JsonProperty("atl")
    public double getAtl() {
        return atl;
    }

    /**
     * Sets the all-time low price of the coin.
     *
     * @param atl the all-time low price of the coin
     */
    public void setAtl(double atl) {
        this.atl = atl;
    }

    /**
     * Gets the percentage change from the all-time low.
     *
     * @return the percentage change from the all-time low
     */
    @JsonProperty("atl_change_percentage")
    public double getAtlChangePercentage() {
        return atlChangePercentage;
    }

    /**
     * Sets the percentage change from the all-time low.
     *
     * @param atlChangePercentage the percentage change from the all-time low
     */
    public void setAtlChangePercentage(double atlChangePercentage) {
        this.atlChangePercentage = atlChangePercentage;
    }

    /**
     * Gets the date when the all-time low was reached.
     *
     * @return the date when the all-time low was reached
     */
    @JsonProperty("atl_date")
    public String getAtlDate() {
        return atlDate;
    }

    /**
     * Sets the date when the all-time low was reached.
     *
     * @param atlDate the date when the all-time low was reached
     */
    public void setAtlDate(String atlDate) {
        this.atlDate = atlDate;
    }

    /**
     * Gets the return on investment for the coin. The ROI value is an optional field and may be null.
     *
     * @return the return on investment for the coin, or null if not applicable
     */
    @JsonProperty("roi")
    public Object getRoi() {
        return roi;
    }

    /**
     * Sets the return on investment for the coin.
     *
     * @param roi the return on investment for the coin, or null if not applicable
     */
    public void setRoi(Object roi) {
        this.roi = roi;
    }

    /**
     * Gets the last update timestamp for the coin data.
     *
     * @return the last update timestamp for the coin data
     */
    @JsonProperty("last_updated")
    public String getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets the last update timestamp for the coin data.
     *
     * @param lastUpdated the last update timestamp for the coin data
     */
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
