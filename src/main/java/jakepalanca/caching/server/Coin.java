// ----- Coin.java -----
package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

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
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coin implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String coinId;
    private String symbol;
    private String name;
    private String image;
    private double currentPrice;
    private long marketCap;
    private int marketCapRank;
    private long fullyDilutedValuation;
    private long totalVolume;
    private double high24h;
    private double low24h;
    private double priceChange24h;
    private double priceChangePercentage24h;
    private double priceChangePercentage1h;
    private double priceChangePercentage7d;
    private double priceChangePercentage14d;
    private double priceChangePercentage30d;
    private double priceChangePercentage200d;
    private double priceChangePercentage1y;
    private long marketCapChange24h;
    private double marketCapChangePercentage24h;
    private double circulatingSupply;
    private double totalSupply;
    private double maxSupply;
    private double ath;
    private double athChangePercentage;
    private String athDate;
    private double atl;
    private double atlChangePercentage;
    private String atlDate;
    private Roi roi;
    private String lastUpdated;

    /**
     * Default constructor for deserialization purposes.
     */
    public Coin() {
        // Default constructor
    }

    // Getters and Setters with @JsonProperty annotations

    @JsonProperty("id")
    public String getCoinId() {
        return coinId;
    }

    @JsonProperty("id")
    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    @JsonProperty("symbol")
    public String getSymbol() {
        return symbol;
    }

    @JsonProperty("symbol")
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("image")
    public String getImage() {
        return image;
    }

    @JsonProperty("image")
    public void setImage(String image) {
        this.image = image;
    }

    @JsonProperty("current_price")
    public double getCurrentPrice() {
        return currentPrice;
    }

    @JsonProperty("current_price")
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    @JsonProperty("market_cap")
    public long getMarketCap() {
        return marketCap;
    }

    @JsonProperty("market_cap")
    public void setMarketCap(long marketCap) {
        this.marketCap = marketCap;
    }

    @JsonProperty("market_cap_rank")
    public int getMarketCapRank() {
        return marketCapRank;
    }

    @JsonProperty("market_cap_rank")
    public void setMarketCapRank(int marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    @JsonProperty("fully_diluted_valuation")
    public Long getFullyDilutedValuation() {
        return fullyDilutedValuation;
    }

    @JsonProperty("fully_diluted_valuation")
    public void setFullyDilutedValuation(Long fullyDilutedValuation) {
        this.fullyDilutedValuation = fullyDilutedValuation;
    }

    @JsonProperty("total_volume")
    public long getTotalVolume() {
        return totalVolume;
    }

    @JsonProperty("total_volume")
    public void setTotalVolume(long totalVolume) {
        this.totalVolume = totalVolume;
    }

    @JsonProperty("high_24h")
    public double getHigh24h() {
        return high24h;
    }

    @JsonProperty("high_24h")
    public void setHigh24h(double high24h) {
        this.high24h = high24h;
    }

    @JsonProperty("low_24h")
    public double getLow24h() {
        return low24h;
    }

    @JsonProperty("low_24h")
    public void setLow24h(double low24h) {
        this.low24h = low24h;
    }

    @JsonProperty("price_change_24h")
    public double getPriceChange24h() {
        return priceChange24h;
    }

    @JsonProperty("price_change_24h")
    public void setPriceChange24h(double priceChange24h) {
        this.priceChange24h = priceChange24h;
    }

    @JsonProperty("price_change_percentage_24h")
    public double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    @JsonProperty("price_change_percentage_24h")
    public void setPriceChangePercentage24h(double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    @JsonProperty("price_change_percentage_1h_in_currency")
    public double getPriceChangePercentage1h() {
        return priceChangePercentage1h;
    }

    @JsonProperty("price_change_percentage_1h_in_currency")
    public void setPriceChangePercentage1h(double priceChangePercentage1h) {
        this.priceChangePercentage1h = priceChangePercentage1h;
    }

    @JsonProperty("price_change_percentage_7d_in_currency")
    public double getPriceChangePercentage7d() {
        return priceChangePercentage7d;
    }

    @JsonProperty("price_change_percentage_7d_in_currency")
    public void setPriceChangePercentage7d(double priceChangePercentage7d) {
        this.priceChangePercentage7d = priceChangePercentage7d;
    }

    @JsonProperty("price_change_percentage_14d_in_currency")
    public double getPriceChangePercentage14d() {
        return priceChangePercentage14d;
    }

    @JsonProperty("price_change_percentage_14d_in_currency")
    public void setPriceChangePercentage14d(double priceChangePercentage14d) {
        this.priceChangePercentage14d = priceChangePercentage14d;
    }

    @JsonProperty("price_change_percentage_30d_in_currency")
    public double getPriceChangePercentage30d() {
        return priceChangePercentage30d;
    }

    @JsonProperty("price_change_percentage_30d_in_currency")
    public void setPriceChangePercentage30d(double priceChangePercentage30d) {
        this.priceChangePercentage30d = priceChangePercentage30d;
    }

    @JsonProperty("price_change_percentage_200d_in_currency")
    public double getPriceChangePercentage200d() {
        return priceChangePercentage200d;
    }

    @JsonProperty("price_change_percentage_200d_in_currency")
    public void setPriceChangePercentage200d(double priceChangePercentage200d) {
        this.priceChangePercentage200d = priceChangePercentage200d;
    }

    @JsonProperty("price_change_percentage_1y_in_currency")
    public double getPriceChangePercentage1y() {
        return priceChangePercentage1y;
    }

    @JsonProperty("price_change_percentage_1y_in_currency")
    public void setPriceChangePercentage1y(double priceChangePercentage1y) {
        this.priceChangePercentage1y = priceChangePercentage1y;
    }

    @JsonProperty("market_cap_change_24h")
    public long getMarketCapChange24h() {
        return marketCapChange24h;
    }

    @JsonProperty("market_cap_change_24h")
    public void setMarketCapChange24h(long marketCapChange24h) {
        this.marketCapChange24h = marketCapChange24h;
    }

    @JsonProperty("market_cap_change_percentage_24h")
    public double getMarketCapChangePercentage24h() {
        return marketCapChangePercentage24h;
    }

    @JsonProperty("market_cap_change_percentage_24h")
    public void setMarketCapChangePercentage24h(double marketCapChangePercentage24h) {
        this.marketCapChangePercentage24h = marketCapChangePercentage24h;
    }

    @JsonProperty("circulating_supply")
    public double getCirculatingSupply() {
        return circulatingSupply;
    }

    @JsonProperty("circulating_supply")
    public void setCirculatingSupply(double circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    @JsonProperty("total_supply")
    public double getTotalSupply() {
        return totalSupply;
    }

    @JsonProperty("total_supply")
    public void setTotalSupply(double totalSupply) {
        this.totalSupply = totalSupply;
    }

    @JsonProperty("max_supply")
    public double getMaxSupply() {
        return maxSupply;
    }

    @JsonProperty("max_supply")
    public void setMaxSupply(double maxSupply) {
        this.maxSupply = maxSupply;
    }

    @JsonProperty("ath")
    public double getAth() {
        return ath;
    }

    @JsonProperty("ath")
    public void setAth(double ath) {
        this.ath = ath;
    }

    @JsonProperty("ath_change_percentage")
    public double getAthChangePercentage() {
        return athChangePercentage;
    }

    @JsonProperty("ath_change_percentage")
    public void setAthChangePercentage(double athChangePercentage) {
        this.athChangePercentage = athChangePercentage;
    }

    @JsonProperty("ath_date")
    public String getAthDate() {
        return athDate;
    }

    @JsonProperty("ath_date")
    public void setAthDate(String athDate) {
        this.athDate = athDate;
    }

    @JsonProperty("atl")
    public double getAtl() {
        return atl;
    }

    @JsonProperty("atl")
    public void setAtl(double atl) {
        this.atl = atl;
    }

    @JsonProperty("atl_change_percentage")
    public double getAtlChangePercentage() {
        return atlChangePercentage;
    }

    @JsonProperty("atl_change_percentage")
    public void setAtlChangePercentage(double atlChangePercentage) {
        this.atlChangePercentage = atlChangePercentage;
    }

    @JsonProperty("atl_date")
    public String getAtlDate() {
        return atlDate;
    }

    @JsonProperty("atl_date")
    public void setAtlDate(String atlDate) {
        this.atlDate = atlDate;
    }

    @JsonProperty("roi")
    public Roi getRoi() {
        return roi;
    }

    @JsonProperty("roi")
    public void setRoi(Roi roi) {
        this.roi = roi;
    }

    @JsonProperty("last_updated")
    public String getLastUpdated() {
        return lastUpdated;
    }

    @JsonProperty("last_updated")
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Overrides the {@code equals} method to compare coins based on their {@code coinId}.
     *
     * @param o the object to compare with
     * @return {@code true} if the {@code coinId}s are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coin coin = (Coin) o;

        return Objects.equals(coinId, coin.coinId);
    }

    /**
     * Overrides the {@code hashCode} method to generate hash based on {@code coinId}.
     *
     * @return the hash code based on {@code coinId}
     */
    @Override
    public int hashCode() {
        return Objects.hash(coinId);
    }
}
