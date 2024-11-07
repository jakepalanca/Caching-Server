package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakepalanca.circlepacker.Packable;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bubble implements Packable {

    private final UUID packableId;
    private final Coin coin;
    private final double x;
    private final double y;
    private final double radius;
    private final String color;
    private double radiusRatio;

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

    @Override
    public UUID getId() {
        return packableId;
    }

    @Override
    public double getRadiusRatio() {
        return this.radiusRatio;
    }

    @Override
    public void setRadiusRatio(double radiusRatio) {
        this.radiusRatio = radiusRatio;
    }

    @Override
    public void setRadius(double radius) {
        // This method is required by the Packable interface
    }

    @Override
    public void setX(double x) {
        // This method is required by the Packable interface
    }

    @Override
    public void setY(double y) {
        // This method is required by the Packable interface
    }

    public Coin getCoin() {
        return coin;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public String getColor() {
        return color;
    }
}
