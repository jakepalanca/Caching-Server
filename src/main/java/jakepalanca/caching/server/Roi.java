// ----- Roi.java -----
package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Represents the Return on Investment (ROI) data for a coin.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Roi implements Serializable {

    private final double times;
    private final String currency;
    private final double percentage;

    public Roi(@JsonProperty("times") double times,
               @JsonProperty("currency") String currency,
               @JsonProperty("percentage") double percentage) {
        this.times = times;
        this.currency = currency;
        this.percentage = percentage;
    }

    @JsonProperty("times")
    public double getTimes() {
        return times;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("percentage")
    public double getPercentage() {
        return percentage;
    }
}
