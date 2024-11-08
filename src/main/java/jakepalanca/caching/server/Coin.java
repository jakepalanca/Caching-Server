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

    private String id; // Renamed from coinId to align with Jackson's naming conventions
    private String symbol;
    private String name;
    private String image;
    private Double currentPrice;
    private Long marketCap;
    private Integer marketCapRank;
    private Long fullyDilutedValuation;
    private Long totalVolume;
    private Double high24h;
    private Double low24h;
    private Double priceChange24h;
    private Double priceChangePercentage24h;
    private Double priceChangePercentage1h;
    private Double priceChangePercentage7d;
    private Double priceChangePercentage14d;
    private Double priceChangePercentage30d;
    private Double priceChangePercentage200d;
    private Double priceChangePercentage1y;
    private Long marketCapChange24h;
    private Double marketCapChangePercentage24h;
    private Double circulatingSupply;
    private Double totalSupply;
    private Double maxSupply;
    private Double ath;
    private Double athChangePercentage;
    private String athDate;
    private Double atl;
    private Double atlChangePercentage;
    private String atlDate;
    private Roi roi;
    private String lastUpdated;
    private String lowerCaseName;
    private String lowerCaseSymbol;

    /**
     * Default constructor for deserialization purposes.
     */
    public Coin() {
        // Default constructor
    }

    // Getters and Setters with @JsonProperty annotations

    @JsonProperty("id")
    public String getId() { // Renamed from getCoinId()
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) { // Renamed from setCoinId()
        this.id = id;
    }

    @JsonProperty("symbol")
    public String getSymbol() {
        return symbol;
    }

    @JsonProperty("symbol")
    public void setSymbol(String symbol) {
        this.symbol = symbol;
        this.lowerCaseSymbol = (symbol != null) ? symbol.toLowerCase() : "";
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
        this.lowerCaseName = (name != null) ? name.toLowerCase() : "";
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
    public Double getCurrentPrice() {
        return currentPrice;
    }

    @JsonProperty("current_price")
    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    @JsonProperty("market_cap")
    public Long getMarketCap() {
        return marketCap;
    }

    @JsonProperty("market_cap")
    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    @JsonProperty("market_cap_rank")
    public Integer getMarketCapRank() {
        return marketCapRank;
    }

    @JsonProperty("market_cap_rank")
    public void setMarketCapRank(Integer marketCapRank) {
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
    public Long getTotalVolume() {
        return totalVolume;
    }

    @JsonProperty("total_volume")
    public void setTotalVolume(Long totalVolume) {
        this.totalVolume = totalVolume;
    }

    @JsonProperty("high_24h")
    public Double getHigh24h() {
        return high24h;
    }

    @JsonProperty("high_24h")
    public void setHigh24h(Double high24h) {
        this.high24h = high24h;
    }

    @JsonProperty("low_24h")
    public Double getLow24h() {
        return low24h;
    }

    @JsonProperty("low_24h")
    public void setLow24h(Double low24h) {
        this.low24h = low24h;
    }

    @JsonProperty("price_change_24h")
    public Double getPriceChange24h() {
        return priceChange24h;
    }

    @JsonProperty("price_change_24h")
    public void setPriceChange24h(Double priceChange24h) {
        this.priceChange24h = priceChange24h;
    }

    @JsonProperty("price_change_percentage_24h")
    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    @JsonProperty("price_change_percentage_24h")
    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    @JsonProperty("price_change_percentage_1h_in_currency")
    public Double getPriceChangePercentage1h() {
        return priceChangePercentage1h;
    }

    @JsonProperty("price_change_percentage_1h_in_currency")
    public void setPriceChangePercentage1h(Double priceChangePercentage1h) {
        this.priceChangePercentage1h = priceChangePercentage1h;
    }

    @JsonProperty("price_change_percentage_7d_in_currency")
    public Double getPriceChangePercentage7d() {
        return priceChangePercentage7d;
    }

    @JsonProperty("price_change_percentage_7d_in_currency")
    public void setPriceChangePercentage7d(Double priceChangePercentage7d) {
        this.priceChangePercentage7d = priceChangePercentage7d;
    }

    @JsonProperty("price_change_percentage_14d_in_currency")
    public Double getPriceChangePercentage14d() {
        return priceChangePercentage14d;
    }

    @JsonProperty("price_change_percentage_14d_in_currency")
    public void setPriceChangePercentage14d(Double priceChangePercentage14d) {
        this.priceChangePercentage14d = priceChangePercentage14d;
    }

    @JsonProperty("price_change_percentage_30d_in_currency")
    public Double getPriceChangePercentage30d() {
        return priceChangePercentage30d;
    }

    @JsonProperty("price_change_percentage_30d_in_currency")
    public void setPriceChangePercentage30d(Double priceChangePercentage30d) {
        this.priceChangePercentage30d = priceChangePercentage30d;
    }

    @JsonProperty("price_change_percentage_200d_in_currency")
    public Double getPriceChangePercentage200d() {
        return priceChangePercentage200d;
    }

    @JsonProperty("price_change_percentage_200d_in_currency")
    public void setPriceChangePercentage200d(Double priceChangePercentage200d) {
        this.priceChangePercentage200d = priceChangePercentage200d;
    }

    @JsonProperty("price_change_percentage_1y_in_currency")
    public Double getPriceChangePercentage1y() {
        return priceChangePercentage1y;
    }

    @JsonProperty("price_change_percentage_1y_in_currency")
    public void setPriceChangePercentage1y(Double priceChangePercentage1y) {
        this.priceChangePercentage1y = priceChangePercentage1y;
    }

    @JsonProperty("market_cap_change_24h")
    public Long getMarketCapChange24h() {
        return marketCapChange24h;
    }

    @JsonProperty("market_cap_change_24h")
    public void setMarketCapChange24h(Long marketCapChange24h) {
        this.marketCapChange24h = marketCapChange24h;
    }

    @JsonProperty("market_cap_change_percentage_24h")
    public Double getMarketCapChangePercentage24h() {
        return marketCapChangePercentage24h;
    }

    @JsonProperty("market_cap_change_percentage_24h")
    public void setMarketCapChangePercentage24h(Double marketCapChangePercentage24h) {
        this.marketCapChangePercentage24h = marketCapChangePercentage24h;
    }

    @JsonProperty("circulating_supply")
    public Double getCirculatingSupply() {
        return circulatingSupply;
    }

    @JsonProperty("circulating_supply")
    public void setCirculatingSupply(Double circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    @JsonProperty("total_supply")
    public Double getTotalSupply() {
        return totalSupply;
    }

    @JsonProperty("total_supply")
    public void setTotalSupply(Double totalSupply) {
        this.totalSupply = totalSupply;
    }

    @JsonProperty("max_supply")
    public Double getMaxSupply() {
        return maxSupply;
    }

    @JsonProperty("max_supply")
    public void setMaxSupply(Double maxSupply) {
        this.maxSupply = maxSupply;
    }

    @JsonProperty("ath")
    public Double getAth() {
        return ath;
    }

    @JsonProperty("ath")
    public void setAth(Double ath) {
        this.ath = ath;
    }

    @JsonProperty("ath_change_percentage")
    public Double getAthChangePercentage() {
        return athChangePercentage;
    }

    @JsonProperty("ath_change_percentage")
    public void setAthChangePercentage(Double athChangePercentage) {
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
    public Double getAtl() {
        return atl;
    }

    @JsonProperty("atl")
    public void setAtl(Double atl) {
        this.atl = atl;
    }

    @JsonProperty("atl_change_percentage")
    public Double getAtlChangePercentage() {
        return atlChangePercentage;
    }

    @JsonProperty("atl_change_percentage")
    public void setAtlChangePercentage(Double atlChangePercentage) {
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
     * Overrides the {@code equals} method to compare coins based on their {@code id}.
     *
     * @param o the object to compare with
     * @return {@code true} if the {@code id}s are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coin coin = (Coin) o;

        return Objects.equals(id, coin.id);
    }

    /**
     * Overrides the {@code hashCode} method to generate hash based on {@code id}.
     *
     * @return the hash code based on {@code id}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Not necessary in JSON response
    String getLowerCaseName() {
        return lowerCaseName;
    }

    // Not necessary in JSON response
    String getLowerCaseSymbol() {
        return lowerCaseSymbol;
    }
}
