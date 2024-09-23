package jakepalanca.caching.server;

import org.apache.commons.text.similarity.LevenshteinDistance;

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
 * CoinCache coinCache = new CoinCache();
 * List<Coin> newCoins = fetchLatestCoins(); // Assume this method fetches the latest coins
 * coinCache.updateCache(newCoins);
 * List<Coin> topCoins = coinCache.getTop100Coins();
 * List<Coin> searchResults = coinCache.searchCoins("bitcoin");
 * }</pre>
 */
public class CoinCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The thread-safe list that holds all cached {@link Coin} objects.
     */
    final transient CopyOnWriteArrayList<Coin> coins = new CopyOnWriteArrayList<>();

    /**
     * The path to the cache file where the coin data is persisted.
     * It differentiates between production and testing environments.
     */
    private String cachePath;

    /**
     * Constructor for testing purposes. Initializes the CoinCache and loads existing cache data if available.
     * The cache file is set to "coinCache-test.ser".
     *
     * @param forTesting a boolean flag indicating that this instance is for testing
     */
    public CoinCache(boolean forTesting) {
        loadCacheFromFile();
        if (forTesting) {
            this.cachePath = "coinCache-test.ser";
        } else {
            this.cachePath = "coinCache-prod.ser";
        }
    }

    /**
     * Updates the coin cache with a new list of coins.
     * This method ensures that existing coins are updated and new coins are added without duplicates.
     *
     * @param newCoins the new list of coins to be added to the cache
     */
    public synchronized void updateCache(List<Coin> newCoins) {
        System.out.println("Updating coin cache with new data...");

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

        System.out.println("Cache update complete. Total coins in cache: " + coins.size());
    }

    /**
     * Saves the current coin cache to a file to persist the data across sessions.
     * The file is named based on the environment (e.g., "coinCache-prod.ser" or "coinCache-test.ser")
     * and is stored in the root directory.
     */
    private void saveCacheToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cachePath))) {
            oos.writeObject(new ArrayList<>(coins));
            System.out.println("Coin cache successfully saved to file: " + cachePath);
        } catch (IOException e) {
            System.err.println("Failed to save coin cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the coin cache from a file if it exists. If the file is not found,
     * the cache is initialized as empty.
     */
    private void loadCacheFromFile() {
        File file = new File(cachePath + "-prod.ser");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Coin> loadedCoins = (List<Coin>) ois.readObject();
                coins.clear();
                coins.addAll(loadedCoins);
                System.out.println("Coin cache successfully loaded from file: " + cachePath + "-prod.ser");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Failed to load coin cache: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Cache file not found (" + cachePath + "), starting with an empty cache.");
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
                new FileWriter(file, false).close();
                System.out.println("Cache file emptied successfully: " + cachePath);
                this.updateCache(new ArrayList<>());
            } catch (IOException e) {
                System.err.println("Failed to empty cache file: " + cachePath);
                e.printStackTrace();
            }
        } else {
            System.out.println("No cache file found to empty: " + cachePath);
        }
    }

}
