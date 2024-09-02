package jakepalanca.cryptocache.javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@code BubbleService} class provides functionality to create and manage
 * {@link Bubble} objects from {@link Coin} objects. This service includes methods
 * for normalizing data, calculating positions, and generating bubbles for a bubble chart.
 *
 * <p>This class supports various data types and time intervals for creating bubbles,
 * ensuring the correct scaling and positioning of bubbles within the screen dimensions.</p>
 *
 * <p><strong>Note:</strong> The class uses {@code VALID_DATA_TYPES} and {@code VALID_TIME_INTERVALS}
 * to validate the types of data and time intervals that can be used for bubble creation.</p>
 */
public class BubbleService {

    static final List<String> VALID_DATA_TYPES = List.of("market_cap", "total_volume", "price_change", "rank", "market_cap_change_percentage_24hr", "total_supply");
    static final List<String> VALID_TIME_INTERVALS = List.of("1h", "24h", "7d", "14d", "30d", "200d", "1y");

    /**
     * Default constructor for {@code BubbleService}.
     * This class provides methods to create and manage bubbles based on cryptocurrency data.
     */
    public BubbleService() {
        // Default constructor
    }

    /**
     * Creates a list of {@code Bubble} objects from the given list of {@code Coin} objects.
     *
     * @param coins         the list of {@code Coin} objects to be converted into bubbles
     * @param dataType      the data type to be used for calculating bubble size (e.g., market_cap, total_volume)
     * @param timeInterval  an optional time interval for data types that require it (e.g., price_change)
     * @param screenHeight  the height of the screen for positioning bubbles
     * @param screenWidth   the width of the screen for positioning bubbles
     * @return              a list of {@code Bubble} objects
     */
    public List<Bubble> createBubbles(List<Coin> coins, String dataType, Optional<String> timeInterval, int screenHeight, int screenWidth) {

        if (!VALID_DATA_TYPES.contains(dataType)) {
            // If the data type is invalid, return an empty list
            return new ArrayList<>();
        }

        double minValue = getMinValue(coins, dataType, timeInterval);
        double maxValue = getMaxValue(coins, dataType, timeInterval);

        List<Bubble> bubbles = new ArrayList<>();
        int totalCoins = coins.size();

        System.out.println("Starting bubble creation. Total coins: " + totalCoins);

        for (int i = 0; i < totalCoins; i++) {
            Coin coin = coins.get(i);
            double value = getValueForDataType(coin, dataType, timeInterval);
            double normalizedValue = NormalizationUtils.normalizeValue(value, minValue, maxValue, dataType);
            double radius = calculateRadius(normalizedValue, screenWidth, screenHeight);
            String color = NormalizationUtils.getColor(coin, dataType, timeInterval);
            Position position = calculatePosition(i, screenWidth, screenHeight, radius, bubbles);

            Bubble bubble = new Bubble(coin.getId(), coin.getSymbol(), coin.getName(), position.getX(), position.getY(), radius, color);
            populateCoinData(bubble, coin);

            bubbles.add(bubble);

            System.out.println("Created bubble for coin: " + coin.getId() + " at position (" + position.getX() + ", " + position.getY() + ") with radius: " + radius);
        }

        System.out.println("Total bubbles created: " + bubbles.size());

        return bubbles;
    }

    /**
     * Retrieves the value for the specified data type and time interval from a {@code Coin} object.
     *
     * @param coin         the {@code Coin} object from which the value is retrieved
     * @param dataType     the data type to retrieve the value for (e.g., market_cap, total_volume)
     * @param timeInterval an optional time interval, required for data types like price_change
     * @return             the value corresponding to the data type and time interval
     */
    private double getValueForDataType(Coin coin, String dataType, Optional<String> timeInterval) {
        return switch (dataType) {
            case "market_cap" -> coin.getMarketCap();
            case "total_volume" -> coin.getTotalVolume();
            case "price_change" -> getPriceChangeForInterval(coin, timeInterval);
            case "sentiment" -> calculateSentimentValue(coin);
            case "rank" -> coin.getMarketCapRank();
            case "market_cap_change_percentage_24hr" -> coin.getMarketCapChangePercentage24h();
            case "total_supply" -> coin.getTotalSupply();
            default -> 0;
        };
    }

    /**
     * Retrieves the minimum value for the specified data type and time interval from a list of {@code Coin} objects.
     *
     * @param coins        the list of {@code Coin} objects to evaluate
     * @param dataType     the data type to evaluate (e.g., market_cap, total_volume)
     * @param timeInterval an optional time interval, required for data types like price_change
     * @return             the minimum value found for the specified data type and time interval
     */
    private double getMinValue(List<Coin> coins, String dataType, Optional<String> timeInterval) {
        return coins.stream()
                .mapToDouble(coin -> getValueForDataType(coin, dataType, timeInterval))
                .min()
                .orElse(0);
    }

    /**
     * Retrieves the maximum value for the specified data type and time interval from a list of {@code Coin} objects.
     *
     * @param coins        the list of {@code Coin} objects to evaluate
     * @param dataType     the data type to evaluate (e.g., market_cap, total_volume)
     * @param timeInterval an optional time interval, required for data types like price_change
     * @return             the maximum value found for the specified data type and time interval
     */
    private double getMaxValue(List<Coin> coins, String dataType, Optional<String> timeInterval) {
        return coins.stream()
                .mapToDouble(coin -> getValueForDataType(coin, dataType, timeInterval))
                .max()
                .orElse(0);
    }

    /**
     * Calculates the radius of a bubble based on the normalized value and screen dimensions.
     *
     * @param normalizedValue the normalized value used to determine the bubble size
     * @param screenWidth     the width of the screen for scaling
     * @param screenHeight    the height of the screen for scaling
     * @return                the calculated radius for the bubble
     */
    private double calculateRadius(double normalizedValue, int screenWidth, int screenHeight) {
        double minScreenDimension = Math.min(screenWidth, screenHeight);
        double maxRadius = minScreenDimension * 0.25; // 25% of the smaller dimension
        double minRadius = minScreenDimension * 0.05; // 5% of the smaller dimension

        if (Double.isNaN(normalizedValue) || Double.isInfinite(normalizedValue)) {
            return minRadius; // Fallback to minimum radius in case of invalid normalization
        }

        return normalizedValue * (maxRadius - minRadius) + minRadius;
    }

    /**
     * Calculates the position for a bubble on the screen, ensuring it is within the screen boundaries and does not overlap with other bubbles.
     *
     * @param index      the index of the current bubble
     * @param screenWidth the width of the screen
     * @param screenHeight the height of the screen
     * @param radius     the radius of the current bubble
     * @param bubbles    the list of bubbles created so far, used to check for overlaps
     * @return           the calculated position for the bubble as a {@link Position} object
     */
    private Position calculatePosition(int index, double screenWidth, double screenHeight, double radius, List<Bubble> bubbles) {
        double halfScreenWidth = screenWidth / 2;
        double halfScreenHeight = screenHeight / 2;

        // Calculate maximum and minimum x and y based on the radius and the screen limits
        double maxX = halfScreenWidth - radius;
        double minX = -halfScreenWidth + radius;
        double maxY = halfScreenHeight - radius;
        double minY = -halfScreenHeight + radius;

        // Distribute bubbles around the center with equal angular spacing
        double angleIncrement = 360.0 / Math.max(1, bubbles.size());
        double angle = index * angleIncrement;

        // Calculate the x and y position relative to the center (0, 0)
        double x = Math.cos(Math.toRadians(angle)) * (maxX - radius);
        double y = Math.sin(Math.toRadians(angle)) * (maxY - radius);

        // Ensure within bounds
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));

        // Adjust to avoid overlap with other bubbles
        for (Bubble bubble : bubbles) {
            double distance = Math.hypot(bubble.getX() - x, bubble.getY() - y);
            if (distance < bubble.getRadius() + radius) {
                // Adjust slightly to avoid overlap
                x += radius * 0.1;
                y += radius * 0.1;

                // Recheck bounds
                x = Math.max(minX, Math.min(x, maxX));
                y = Math.max(minY, Math.min(y, maxY));
            }
        }

        // Convert to positive screen coordinates by shifting the origin back
        int finalX = (int) (x + halfScreenWidth);
        int finalY = (int) (y + halfScreenHeight);

        return new Position(finalX, finalY);
    }

    /**
     * Populates the data from a {@code Coin} object into a {@code Bubble} object.
     * This method ensures that all relevant coin data is transferred to the bubble.
     *
     * @param bubble the {@code Bubble} object to populate
     * @param coin   the {@code Coin} object containing the data
     */
    private void populateCoinData(Bubble bubble, Coin coin) {
        bubble.setId(coin.getId());
        bubble.setSymbol(coin.getSymbol());
        bubble.setName(coin.getName());
        bubble.setImage(coin.getImage());
        bubble.setCurrentPrice(coin.getCurrentPrice());
        bubble.setMarketCap(coin.getMarketCap());
        bubble.setMarketCapRank(coin.getMarketCapRank());
        bubble.setFullyDilutedValuation(coin.getFullyDilutedValuation());
        bubble.setTotalVolume(coin.getTotalVolume());
        bubble.setHigh24h(coin.getHigh24h());
        bubble.setLow24h(coin.getLow24h());
        bubble.setPriceChange24h(coin.getPriceChange24h());
        bubble.setPriceChangePercentage24h(coin.getPriceChangePercentage24h());
        bubble.setPriceChangePercentage1h(coin.getPriceChangePercentage1h());
        bubble.setPriceChangePercentage7d(coin.getPriceChangePercentage7d());
        bubble.setPriceChangePercentage14d(coin.getPriceChangePercentage14d());
        bubble.setPriceChangePercentage30d(coin.getPriceChangePercentage30d());
        bubble.setPriceChangePercentage200d(coin.getPriceChangePercentage200d());
        bubble.setPriceChangePercentage1y(coin.getPriceChangePercentage1y());
        bubble.setMarketCapChange24h(coin.getMarketCapChange24h());
        bubble.setMarketCapChangePercentage24h(coin.getMarketCapChangePercentage24h());
        bubble.setCirculatingSupply(coin.getCirculatingSupply());
        bubble.setTotalSupply(coin.getTotalSupply());
        bubble.setMaxSupply(coin.getMaxSupply());
        bubble.setAth(coin.getAth());
        bubble.setAthChangePercentage(coin.getAthChangePercentage());
        bubble.setAthDate(coin.getAthDate());
        bubble.setAtl(coin.getAtl());
        bubble.setAtlChangePercentage(coin.getAtlChangePercentage());
        bubble.setAtlDate(coin.getAtlDate());
        bubble.setRoi(coin.getRoi());
        bubble.setLastUpdated(coin.getLastUpdated());
    }

    /**
     * Placeholder method for calculating price change based on the specified time interval.
     *
     * @param coin         the {@code Coin} object containing the data
     * @param timeInterval the time interval to calculate the price change for
     * @return             the price change value for the specified interval
     */
    private double getPriceChangeForInterval(Coin coin, Optional<String> timeInterval) {
        return timeInterval.map(interval -> {
            return switch (interval) {
                case "1h" -> coin.getPriceChangePercentage1h();
                case "24h" -> coin.getPriceChangePercentage24h();
                case "7d" -> coin.getPriceChangePercentage7d();
                case "14d" -> coin.getPriceChangePercentage14d();
                case "30d" -> coin.getPriceChangePercentage30d();
                case "200d" -> coin.getPriceChangePercentage200d();
                case "1y" -> coin.getPriceChangePercentage1y();
                default -> 0.0; // Ensuring the default case returns a double value
            };
        }).orElse(0.0); // Ensuring that orElse returns a double value
    }

    /**
     * Placeholder method for calculating the sentiment value of a {@code Coin}.
     *
     * <p>This method should be implemented to calculate and return a sentiment score
     * based on external data or analysis. Currently, it returns 0 as a placeholder.</p>
     *
     * @param coin the {@code Coin} object containing the data
     * @return     the sentiment value (currently 0 as a placeholder)
     */
    private double calculateSentimentValue(Coin coin) {
        // Implement your sentiment value calculation logic here
        return 0;
    }

    /**
     * Inner class representing a position on the screen with x and y coordinates.
     */
    private static class Position {
        private final int x;
        private final int y;

        /**
         * Constructs a {@code Position} object with specified x and y coordinates.
         *
         * @param x the x-coordinate of the position
         * @param y the y-coordinate of the position
         */
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Gets the x-coordinate of the position.
         *
         * @return the x-coordinate of the position
         */
        public int getX() {
            return x;
        }

        /**
         * Gets the y-coordinate of the position.
         *
         * @return the y-coordinate of the position
         */
        public int getY() {
            return y;
        }
    }
}
