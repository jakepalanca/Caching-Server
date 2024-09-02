package jakepalanca.cryptocache.javalin;

import java.util.Optional;

/**
 * The {@code NormalizationUtils} class provides utility methods for normalizing various types of cryptocurrency data.
 * These methods are designed to convert raw data into a normalized format, which can be used for tasks like
 * rendering visualizations or comparing relative values.
 *
 * <p>The class includes methods to normalize market capitalization, trading volume, rank, percentage change,
 * and supply. It also includes a method for determining the color representation based on normalized values.</p>
 */
public class NormalizationUtils {

    /**
     * Constructs a new {@code NormalizationUtils} instance.
     * This utility class provides methods for normalizing various cryptocurrency data points.
     */
    public NormalizationUtils() {
        // Default constructor
    }

    /**
     * Normalizes market capitalization values to a scale of 0 to 1.
     *
     * @param value    the market capitalization value to normalize
     * @param minValue the minimum market capitalization value in the dataset
     * @param maxValue the maximum market capitalization value in the dataset
     * @return         the normalized market capitalization value
     */
    public static double normalizeMarketCap(double value, double minValue, double maxValue) {
        return (value - minValue) / (maxValue - minValue);
    }

    /**
     * Normalizes trading volume values using logarithmic scaling to a scale of 0 to 1.
     *
     * @param value    the trading volume value to normalize
     * @param minValue the minimum trading volume value in the dataset
     * @param maxValue the maximum trading volume value in the dataset
     * @return         the normalized trading volume value
     */
    public static double normalizeVolume(double value, double minValue, double maxValue) {
        double logVolume = Math.log(value);
        return (logVolume - Math.log(minValue)) / (Math.log(maxValue) - Math.log(minValue));
    }

    /**
     * Normalizes rank values to a scale of 0 to 1, where the highest rank (1) is normalized to 1,
     * and the lowest rank is normalized to 0.
     *
     * @param rank    the rank value to normalize
     * @param maxRank the maximum rank in the dataset
     * @return        the normalized rank value
     */
    public static double normalizeRank(int rank, int maxRank) {
        return (double) (maxRank - rank) / (maxRank - 1);
    }

    /**
     * Normalizes percentage change values to a scale of 0 to 1.
     *
     * @param value    the percentage change value to normalize
     * @param minValue the minimum percentage change value in the dataset
     * @param maxValue the maximum percentage change value in the dataset
     * @return         the normalized percentage change value
     */
    public static double normalizePercentageChange(double value, double minValue, double maxValue) {
        return (value - minValue) / (maxValue - minValue);
    }

    /**
     * Normalizes supply values to a scale of 0 to 1.
     *
     * @param value    the supply value to normalize
     * @param minValue the minimum supply value in the dataset
     * @param maxValue the maximum supply value in the dataset
     * @return         the normalized supply value
     */
    public static double normalizeSupply(double value, double minValue, double maxValue) {
        return (value - minValue) / (maxValue - minValue);
    }

    /**
     * Determines the color representation for a data point based on its value and data type.
     * This method considers whether the data type involves a percentage change, such as price change or market cap change.
     *
     * @param coin           the {@link Coin} instance containing the data point
     * @param dataType       the type of data being normalized (e.g., "market_cap_change_percentage_24hr", "price_change")
     * @param timeInterval   the optional time interval for percentage-based data types
     * @return               a string representing the color in hexadecimal format
     */
    public static String getColor(Coin coin, String dataType, Optional<String> timeInterval) {
        double value;

        // Determine the value based on the data type
        if ("price_change".equals(dataType)) {
            value = getValueForDataType(coin, dataType, timeInterval); // Get the price change for the specified interval
        } else if ("market_cap_change_percentage_24hr".equals(dataType)) {
            value = coin.getMarketCapChangePercentage24h(); // Use market cap change percentage
        } else {
            value = 0; // Default to 0 for non-percentage data types
        }

        // Now determine the color based on the value
        if ("price_change".equals(dataType) || "market_cap_change_percentage_24hr".equals(dataType)) {
            if (value > 0) {
                return "#00FF00"; // Green for positive change
            } else if (value < 0) {
                return "#FF0000"; // Red for negative change
            } else {
                return "#0000FF"; // Blue if it's neutral
            }
        } else {
            return "#0000FF"; // Default to blue for all other data types
        }
    }

    /**
     * Retrieves the appropriate value for the given data type and time interval from the {@link Coin} object.
     *
     * @param coin         the {@link Coin} object containing the data
     * @param dataType     the type of data to retrieve (e.g., "market_cap", "total_volume", "price_change")
     * @param timeInterval the optional time interval for percentage-based data types
     * @return             the value corresponding to the data type, or 0 if the data type is unrecognized
     */
    private static double getValueForDataType(Coin coin, String dataType, Optional<String> timeInterval) {
        return switch (dataType) {
            case "market_cap" -> coin.getMarketCap();
            case "total_volume" -> coin.getTotalVolume();
            case "price_change" -> getPriceChangeForInterval(coin, timeInterval);
            case "rank" -> coin.getMarketCapRank();
            case "market_cap_change_percentage_24hr" -> coin.getMarketCapChangePercentage24h();
            case "total_supply" -> coin.getTotalSupply();
            default -> 0;
        };
    }

    /**
     * Retrieves the percentage price change for the given time interval from the {@link Coin} object.
     *
     * @param coin         the {@link Coin} object containing the data
     * @param timeInterval the optional time interval for percentage-based data types
     * @return             the percentage price change for the specified time interval, or 0 if the interval is unrecognized
     */
    private static double getPriceChangeForInterval(Coin coin, Optional<String> timeInterval) {
        return timeInterval.map(interval -> {
            return switch (interval) {
                case "1h" -> coin.getPriceChangePercentage1h();
                case "24h" -> coin.getPriceChangePercentage24h();
                case "7d" -> coin.getPriceChangePercentage7d();
                case "14d" -> coin.getPriceChangePercentage14d();
                case "30d" -> coin.getPriceChangePercentage30d();
                case "200d" -> coin.getPriceChangePercentage200d();
                case "1y" -> coin.getPriceChangePercentage1y();
                default -> 0.0;
            };
        }).orElse(0.0);
    }

    /**
     * Normalizes a given value based on the data type.
     *
     * <p>This method dispatches the normalization process to the appropriate normalization method
     * based on the provided data type.</p>
     *
     * @param value    the value to normalize, provided as a {@link Number}
     * @param minValue the minimum value in the dataset for the specified data type
     * @param maxValue the maximum value in the dataset for the specified data type
     * @param dataType the type of data being normalized (e.g., "market_cap", "total_volume", "rank")
     * @return         the normalized value as a double, or 0 if the data type is unrecognized
     */
    public static double normalizeValue(Number value, double minValue, double maxValue, String dataType) {
        double doubleValue = value.doubleValue(); // Convert any Number to double
        return switch (dataType) {
            case "market_cap" -> normalizeMarketCap(doubleValue, minValue, maxValue);
            case "total_volume" -> normalizeVolume(doubleValue, minValue, maxValue);
            case "price_change", "market_cap_change_percentage_24hr" -> normalizePercentageChange(doubleValue, minValue, maxValue);
            case "total_supply" -> normalizeSupply(doubleValue, minValue, maxValue);
            case "rank" -> normalizeRank((int) doubleValue, (int) maxValue); // Assuming rank is an integer value
            default -> 0;
        };
    }
}
