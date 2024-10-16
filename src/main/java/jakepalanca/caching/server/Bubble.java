// ----- Bubble.java -----
package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakepalanca.circlepacker.Packable;

import java.util.UUID;

/**
 * Represents a Bubble object that contains a Coin and additional properties
 * specific to the Bubble, such as its x and y coordinates, radius, and color.
 *
 * <p>This class is used to represent visual bubbles in a bubble chart that
 * correspond to cryptocurrency data retrieved from the CoinGecko API.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bubble implements Packable {

    /**
     * The unique identifier for packing purposes.
     */
    private final UUID packableId;

    /**
     * The Coin associated with this Bubble.
     */
    private final Coin coin;

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
     * The color of the bubble, "green", "red", "grey" or "blue".
     */
    private String color;

    /**
     * The normalized radius ratio for packing.
     */
    private double radiusRatio;

    /**
     * Constructs a new {@code Bubble} object with the specified properties.
     *
     * @param coin    the {@link Coin} object associated with this bubble
     * @param x       the x-coordinate of the bubble in the chart
     * @param y       the y-coordinate of the bubble in the chart
     * @param radius  the radius of the bubble in the chart
     * @param color   the color of the bubble
     */
    public Bubble(Coin coin, double x, double y, double radius, String color) {
        this.packableId = UUID.randomUUID();
        this.coin = coin;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    /**
     * Gets the unique identifier for packing.
     *
     * @return the UUID of this bubble
     */
    @Override
    public UUID getId() {
        return packableId;
    }

    /**
     * Gets the normalized radius ratio.
     *
     * @return the radius ratio
     */
    @Override
    public double getRadiusRatio() {
        return this.radiusRatio;
    }

    /**
     * Sets the normalized radius ratio.
     *
     * @param radiusRatio the radius ratio to set
     */
    @Override
    public void setRadiusRatio(double radiusRatio) {
        this.radiusRatio = radiusRatio;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    // Getters and Setters for Bubble-specific fields

    @JsonProperty("x")
    public double getX() {
        return x;
    }

    @JsonProperty("y")
    public double getY() {
        return y;
    }

    @JsonProperty("radius")
    public double getRadius() {
        return radius;
    }

    @JsonProperty("color")
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Gets the associated Coin object.
     *
     * @return the Coin associated with this bubble
     */
    public Coin getCoin() {
        return coin;
    }
}
