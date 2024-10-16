// ----- CoinCache.java -----
package jakepalanca.caching.server;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * The {@code CoinCache} class manages a cache of {@link Coin} objects, providing functionalities
 * to update the cache, search for coins, retrieve top coins, and persist the cache to a file.
 *
 * <p>This class ensures thread-safe operations using synchronization and leverages a
 * {@link CopyOnWriteArrayList} to handle concurrent modifications.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * CoinCache coinCache = new CoinCache("./cache/", false); // Production cache
 * List<Coin> newCoins = fetchLatestCoins(); // Assume this method fetches the latest coins
 * coinCache.updateCache(newCoins);
 * List<Coin> topCoins = coinCache.getTop100Coins();
 * List<Coin> searchResults = coinCache.searchCoins("bitcoin");
 * }</pre>
 */
public class CoinCache implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(CoinCache.class);

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The thread-safe list that holds all cached {@link Coin} objects.
     */
    final CopyOnWriteArrayList<Coin> coins = new CopyOnWriteArrayList<>();

    /**
     * The path to the cache file where the coin data is persisted.
     */
    private final String cachePath;

    /**
     * Constructor for testing or production environments with a specified cache directory.
     *
     * @param cacheDir   the directory where the cache file will be stored
     * @param forTesting a boolean flag indicating the environment:
     *                   - {@code true} for testing (uses "coinCache-test.ser")
     *                   - {@code false} for production (uses "coinCache-prod.ser")
     */
    public CoinCache(String cacheDir, boolean forTesting) {
        // Ensure the cache directory ends with a file separator
        if (!cacheDir.endsWith(File.separator)) {
            cacheDir += File.separator;
        }

        String fileName = forTesting ? "coinCache-test.ser" : "coinCache-prod.ser";
        this.cachePath = cacheDir + fileName;

        // Ensure the cache directory exists
        File dir = new File(cacheDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("Cache directory created at: {}", cacheDir);
            } else {
                logger.error("Failed to create cache directory at: {}", cacheDir);
            }
        }

        loadCacheFromFile();
    }

    /**
     * Updates the coin cache with a new list of coins.
     * This method ensures that existing coins are updated and new coins are added without duplicates.
     *
     * @param newCoins the new list of coins to be added to the cache
     */
    public synchronized void updateCache(List<Coin> newCoins) {
        logger.info("Updating coin cache with new data...");

        // Create a map from coinId to Coin for quick lookup and to eliminate duplicates
        Map<String, Coin> newCoinMap = newCoins.stream()
                .collect(Collectors.toMap(Coin::getCoinId, coin -> coin, (coin1, coin2) -> coin1));

        // Iterate over existing coins and update them if they exist in the newCoinMap
        for (int i = 0; i < coins.size(); i++) {
            Coin existingCoin = coins.get(i);
            if (newCoinMap.containsKey(existingCoin.getCoinId())) {
                coins.set(i, newCoinMap.get(existingCoin.getCoinId()));
                newCoinMap.remove(existingCoin.getCoinId());
            }
        }

        // Add any new coins that were not present in the existing cache
        coins.addAll(newCoinMap.values());

        // Persist the updated cache to the file
        saveCacheToFile();

        logger.info("Cache update complete. Total coins in cache: {}", coins.size());
    }

    /**
     * Saves the current coin cache to a file to persist the data across sessions.
     * The file is stored in the specified cache directory.
     */
    private void saveCacheToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cachePath))) {
            oos.writeObject(new ArrayList<>(coins));
            logger.info("Coin cache successfully saved to file: {}", cachePath);
        } catch (IOException e) {
            logger.error("Failed to save coin cache to {}: {}", cachePath, e.getMessage(), e);
        }
    }

    /**
     * Loads the coin cache from a file if it exists. If the file is not found,
     * the cache is initialized as empty.
     * If the cache file is corrupt, it is deleted and the cache is initialized as empty.
     */
    private void loadCacheFromFile() {
        File file = new File(cachePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Coin> loadedCoins = (List<Coin>) ois.readObject();
                coins.clear();
                coins.addAll(loadedCoins);
                logger.info("Coin cache successfully loaded from file: {}", cachePath);
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Failed to load coin cache from {}: {}", cachePath, e.getMessage(), e);
                // Attempt to delete the corrupt cache file
                boolean deleted = file.delete();
                if (deleted) {
                    logger.warn("Corrupt cache file deleted: {}", cachePath);
                } else {
                    logger.warn("Failed to delete corrupt cache file: {}", cachePath);
                }
            }
        } else {
            logger.info("Cache file not found ({}), starting with an empty cache.", cachePath);
        }
    }

    /**
     * Searches for coins in the cache that match the query string based on Levenshtein distance.
     * This method supports fuzzy searching by considering both coin ID and coin name.
     *
     * @param query the query string to search for
     * @return      a list of coins that match the query string within a threshold distance
     */
    public List<Coin> searchCoins(String query) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        String normalizedQuery = query.toLowerCase();

        // Threshold for fuzzy search
        int threshold = 3;

        return coins.stream()
                .filter(coin -> isMatch(coin, normalizedQuery, levenshtein, threshold))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a coin matches the query string within a threshold distance using Levenshtein distance.
     * The method compares both the coin ID and the coin name.
     *
     * @param coin          the coin to check
     * @param query         the query string to compare against
     * @param levenshtein   the Levenshtein distance object used for comparison
     * @param threshold     the maximum allowed distance for a match
     * @return              {@code true} if the coin matches the query within the threshold; {@code false} otherwise
     */
    private boolean isMatch(Coin coin, String query, LevenshteinDistance levenshtein, int threshold) {
        String coinName = coin.getName().toLowerCase();

        int idDistance = levenshtein.apply(coin.getCoinId(), query);
        int nameDistance = levenshtein.apply(coinName, query);

        return idDistance <= threshold || nameDistance <= threshold;
    }

    /**
     * Retrieves the top 100 coins from the cache, sorted by market capitalization rank.
     *
     * @return a list of the top 100 coins by market capitalization rank
     */
    public List<Coin> getTop100Coins() {
        return coins.stream()
                .sorted(Comparator.comparingInt(Coin::getMarketCapRank))
                .limit(100)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of coins from the cache that match the specified list of IDs.
     *
     * @param ids   the list of coin IDs to search for
     * @return      a list of coins that match the specified IDs
     */
    public List<Coin> getCoinsByIds(List<String> ids) {
        return coins.stream()
                .filter(coin -> ids.contains(coin.getCoinId()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all coins currently stored in the cache.
     *
     * @return  a list of all coins in the cache
     */
    public List<Coin> getAllCoins() {
        return new ArrayList<>(coins);
    }

    /**
     * Empties the cache file and clears the in-memory cache.
     * This is useful for resetting the cache during testing or maintenance.
     */
    public void emptyCacheFile() {
        
        File file = new File(cachePath);
        if (file.exists()) {
            try {
                // Open the file in write mode to truncate its contents
                new FileOutputStream(file, false).close();
                logger.info("Cache file emptied successfully: {}", cachePath);
                this.updateCache(new ArrayList<>());
            } catch (IOException e) {
                logger.error("Failed to empty cache file: {}", cachePath, e);
            }
        } else {
            logger.info("No cache file found to empty: {}", cachePath);
        }
    }
}
