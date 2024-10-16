package jakepalanca.caching.server;

import jakepalanca.circlepacker.Packable;
import jakepalanca.circlepacker.Packing;
import jakepalanca.circlepacker.PackingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for creating visual bubbles from cryptocurrency data.
 */
public class BubbleService {

    private static final Logger logger = LoggerFactory.getLogger(BubbleService.class);

    static final List<String> VALID_DATA_TYPES = List.of(
            "market_cap", "total_volume", "price_change", "rank",
            "market_cap_change_percentage_24hr", "total_supply"
    );
    static final List<String> VALID_TIME_INTERVALS = List.of(
            "1h", "24h", "7d", "14d", "30d", "200d", "1y"
    );
    private static final double DEFAULT_NORMALIZED_VALUE = 0.5; // Fixed ratio for identical values
    private static final int MAX_PACKING_ITERATIONS = 1000;
    private static final double MIN_RADIUS = 5.0; // Minimum radius for bubbles

    /**
     * Creates a list of {@code Bubble} objects from the given list of {@code Coin} objects.
     *
     * @param coins        the list of {@code Coin} objects to be converted into bubbles
     * @param dataType     the data type for calculating bubble size
     * @param timeInterval the time interval for data types that require it
     * @param screenHeight the height of the screen for positioning bubbles
     * @param screenWidth  the width of the screen for positioning bubbles
     * @return             a list of {@code Bubble} objects
     */
    public List<Bubble> createBubbles(List<Coin> coins, String dataType, String timeInterval, int screenHeight, int screenWidth) {
        if (!VALID_DATA_TYPES.contains(dataType)) {
            logger.warn("Invalid data type provided: {}", dataType);
            return new ArrayList<>();
        }

        if (coins.isEmpty()) {
            logger.warn("Coin list is empty. No bubbles to create.");
            return new ArrayList<>();
        }

        Pair<Double, Double> minMax = getMinAndMaxValues(coins, dataType, timeInterval);
        double minValue = minMax.getFirst();
        double maxValue = minMax.getSecond();

        double epsilon = 1e-9; // Small constant to handle precision issues
        boolean identicalValues = Math.abs(maxValue - minValue) < epsilon;

        if (identicalValues) {
            logger.info("Max and min values are identical or very close, distributing normalized values evenly.");
        }

        List<Bubble> bubbles = new ArrayList<>();

        // If values are identical, distribute normalized values evenly
        List<Double> normalizedValues;
        if (identicalValues) {
            normalizedValues = distributeNormalizedValuesEvenly(coins.size());
        } else {
            normalizedValues = coins.stream()
                    .map(coin -> normalizeValue(getValueForDataType(coin, dataType, timeInterval), minValue, maxValue))
                    .collect(Collectors.toList());
        }

        for (int i = 0; i < coins.size(); i++) {
            Coin coin = coins.get(i);
            double normalizedValue = normalizedValues.get(i);

            // Ensure normalizedValue is positive and greater than zero
            if (normalizedValue <= 0) {
                normalizedValue = epsilon; // Assign a small positive value
                logger.debug("Normalized value for coin {} was non-positive. Assigned epsilon: {}", coin.getCoinId(), epsilon);
            }

            Bubble bubble = new Bubble(
                    coin,
                    0, // Initial x-coordinate (will be set by packing)
                    0, // Initial y-coordinate (will be set by packing)
                    0, // Initial radius (will be set by packing)
                    "blue" // Default color, will be updated
            );
            bubble.setRadiusRatio(normalizedValue);

            // Assign color based on data type and value
            bubble.setColor(getColorForCoin(coin, dataType, timeInterval));

            bubbles.add(bubble);
        }

        // Perform circle packing
        PackingResult<Packable> packingResult = Packing.packCircles(screenWidth, screenHeight, new ArrayList<>(bubbles), MAX_PACKING_ITERATIONS);

        // Extract packed bubbles with updated positions and radii

        return packingResult.getPackables().stream()
                .map(packable -> (Bubble) packable)
                .collect(Collectors.toList());
    }

    /**
     * Distributes normalized values evenly when all data values are identical.
     *
     * @param size the number of coins/bubbles
     * @return     a list of normalized values
     */
    private List<Double> distributeNormalizedValuesEvenly(int size) {
        List<Double> normalizedValues = new ArrayList<>();
        double increment = 1.0 / size;
        for (int i = 0; i < size; i++) {
            normalizedValues.add(MIN_RADIUS + increment * i);
        }
        return normalizedValues;
    }

    /**
     * Determines the color of a bubble based on the data type and its value.
     *
     * @param coin         the associated {@link Coin}
     * @param dataType     the data type used for bubble sizing
     * @param timeInterval the time interval if applicable
     * @return             the color in HEX format
     */
    private String getColorForCoin(Coin coin, String dataType, String timeInterval) {
        getValueForDataType(coin, dataType, timeInterval);

        if (dataType.equals("price_change")) {
            // Use timeInterval to get the appropriate price change percentage
            double priceChange = getPriceChangeForInterval(coin, timeInterval);
            if (priceChange > 0) {
                return "green"; // Green for positive changes
            } else if (priceChange < 0) {
                return "red"; // Red for negative changes
            } else {
                return "grey"; // Grey for neutral/zero change
            }
        } else if (dataType.equals("total_volume") || dataType.equals("market_cap")) {
            // Normalize the value for color intensity
            return "blue";
        } else {
            return "grey"; // Grey for unknown data types
        }
    }

    /**
     * Normalizes a value based on the provided min and max values.
     *
     * @param value    the value to normalize
     * @param minValue the minimum value in the dataset
     * @param maxValue the maximum value in the dataset
     * @return the normalized value
     */
    private double normalizeValue(double value, double minValue, double maxValue) {
        double epsilon = 1e-9; // Small constant to handle precision issues
        if (Math.abs(maxValue - minValue) < epsilon) {
            return DEFAULT_NORMALIZED_VALUE;
        }

        // Adjust normalization for 'rank' where lower values are better
        double normalized;
        if ("rank".equalsIgnoreCase(value + "")) {
            normalized = 1 - ((value - minValue) / (maxValue - minValue));
        } else {
            normalized = (value - minValue) / (maxValue - minValue);
        }

        // Ensure the normalized value is within [0, 1]
        normalized = Math.max(0, Math.min(1, normalized));

        // Scale the normalized value to a suitable radius
        double radius = MIN_RADIUS + normalized * (100 - MIN_RADIUS); // Adjust 100 to max desired radius

        logger.debug("Normalized value: {}, Radius: {}", normalized, radius);
        return radius;
    }

    /**
     * Retrieves the minimum and maximum values for the specified data type across all coins.
     *
     * @param coins        the list of coins
     * @param dataType     the data type to evaluate
     * @param timeInterval the time interval if applicable
     * @return             a {@link Pair} containing min and max values
     */
    private Pair<Double, Double> getMinAndMaxValues(List<Coin> coins, String dataType, String timeInterval) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Coin coin : coins) {
            double value = getValueForDataType(coin, dataType, timeInterval);
            if (value < min) min = value;
            if (value > max) max = value;
        }

        // Handle cases where min or max values are not updated (e.g., empty coin list)
        if (Double.isInfinite(min)) min = 0;
        if (Double.isInfinite(max)) max = 0;

        logger.debug("Data Type: {}, Min Value: {}, Max Value: {}", dataType, min, max);
        return new Pair<>(min, max);
    }

    /**
     * Retrieves the value of a specific data type for a given coin.
     *
     * @param coin         the {@link Coin} object
     * @param dataType     the data type to retrieve
     * @param timeInterval the time interval if applicable
     * @return             the value corresponding to the data type
     */
    private double getValueForDataType(Coin coin, String dataType, String timeInterval) {
        return switch (dataType) {
            case "market_cap" -> coin.getMarketCap();
            case "total_volume" -> coin.getTotalVolume();
            case "price_change" -> getPriceChangeForInterval(coin, timeInterval);
            case "rank" -> coin.getMarketCapRank();
            case "market_cap_change_percentage_24hr" -> coin.getMarketCapChangePercentage24h();
            case "total_supply" -> coin.getTotalSupply();
            default -> {
                logger.warn("Unsupported data type: {}", dataType);
                yield 0;
            }
        };
    }

    /**
     * Retrieves the price change percentage for a given time interval.
     *
     * @param coin         the {@link Coin} object
     * @param timeInterval the time interval
     * @return             the price change percentage
     */
    private double getPriceChangeForInterval(Coin coin, String timeInterval) {
        if (timeInterval == null) return 0;
        return switch (timeInterval) {
            case "1h" -> coin.getPriceChangePercentage1h();
            case "24h" -> coin.getPriceChangePercentage24h();
            case "7d" -> coin.getPriceChangePercentage7d();
            case "14d" -> coin.getPriceChangePercentage14d();
            case "30d" -> coin.getPriceChangePercentage30d();
            case "200d" -> coin.getPriceChangePercentage200d();
            case "1y" -> coin.getPriceChangePercentage1y();
            default -> {
                logger.warn("Unsupported time interval: {}", timeInterval);
                yield 0;
            }
        };
    }

    /**
     * Simple Pair class for holding two values.
     *
     * @param <F> the type of the first element
     * @param <S> the type of the second element
     */
    private static class Pair<F, S> {
        private final F first;
        private final S second;

        /**
         * Constructs a new {@code Pair} instance.
         *
         * @param first  the first element
         * @param second the second element
         */
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        /**
         * Retrieves the first element.
         *
         * @return the first element
         */
        public F getFirst() {
            return first;
        }

        /**
         * Retrieves the second element.
         *
         * @return the second element
         */
        public S getSecond() {
            return second;
        }
    }
}

