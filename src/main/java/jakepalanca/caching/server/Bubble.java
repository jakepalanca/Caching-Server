// ----- Bubble.java -----
package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakepalanca.circlepacker.Packable;

import java.util.UUID;

/**
 * Represents a Bubble object used for visualizing data, encapsulating a Coin and its positioning.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bubble implements Packable {

    private final UUID packableId;
    private final Coin coin;
    private double x;      // Made mutable by removing 'final'
    private double y;      // Made mutable by removing 'final'
    private double radius; // Made mutable by removing 'final'
    private String color;
    private double radiusRatio;

    /**
     * Constructs a new Bubble with the specified attributes.
     *
     * @param coin   the Coin associated with this Bubble
     * @param x      the x-coordinate of the Bubble's position
     * @param y      the y-coordinate of the Bubble's position
     * @param radius the radius of the Bubble
     * @param color  the color of the Bubble
     */
    @JsonCreator
    public Bubble(
            @JsonProperty("coin") Coin coin,
            @JsonProperty("x") double x,
            @JsonProperty("y") double y,
            @JsonProperty("radius") double radius,
            @JsonProperty("color") String color) {
        this.packableId = UUID.randomUUID();
        this.coin = coin;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    /**
     * Returns the unique identifier of the Bubble.
     *
     * @return the UUID of the Bubble
     */
    @Override
    @JsonProperty("id")
    public UUID getId() {
        return packableId;
    }

    /**
     * Returns the radius ratio of the Bubble.
     *
     * @return the radius ratio
     */
    @Override
    @JsonProperty("radiusRatio")
    public double getRadiusRatio() {
        return this.radiusRatio;
    }

    /**
     * Sets the radius ratio of the Bubble.
     *
     * @param radiusRatio the new radius ratio
     */
    @Override
    public void setRadiusRatio(double radiusRatio) {
        this.radiusRatio = radiusRatio;
    }

    /**
     * Sets the radius of the Bubble.
     *
     * @param radius the new radius
     */
    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Sets the x-coordinate of the Bubble.
     *
     * @param x the new x-coordinate
     */
    @Override
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the Bubble.
     *
     * @param y the new y-coordinate
     */
    @Override
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the Coin associated with this Bubble.
     *
     * @return the Coin object
     */
    @JsonProperty("coin")
    public Coin getCoin() {
        return coin;
    }

    /**
     * Returns the x-coordinate of the Bubble's position.
     *
     * @return the x-coordinate
     */
    @Override
    @JsonProperty("x")
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the Bubble's position.
     *
     * @return the y-coordinate
     */
    @Override
    @JsonProperty("y")
    public double getY() {
        return y;
    }

    /**
     * Returns the radius of the Bubble.
     *
     * @return the radius
     */
    @JsonProperty("radius")
    public double getRadius() {
        return radius;
    }

    /**
     * Returns the color of the Bubble.
     *
     * @return the color as a String
     */
    @JsonProperty("color")
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of the Bubble.
     *
     * @param color the new color
     */
    @JsonProperty("color")
    public void setColor(String color) {
        this.color = color;
    }
}
