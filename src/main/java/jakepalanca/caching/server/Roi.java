// ----- Roi.java -----
package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Represents the Return on Investment (ROI) data for a coin.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Roi(double times, String currency, double percentage) implements Serializable {

    public Roi(@JsonProperty("times") double times,
               @JsonProperty("currency") String currency,
               @JsonProperty("percentage") double percentage) {
        this.times = times;
        this.currency = currency;
        this.percentage = percentage;
    }

    @Override
    @JsonProperty("times")
    public double times() {
        return times;
    }

    @Override
    @JsonProperty("currency")
    public String currency() {
        return currency;
    }

    @Override
    @JsonProperty("percentage")
    public double percentage() {
        return percentage;
    }
}
