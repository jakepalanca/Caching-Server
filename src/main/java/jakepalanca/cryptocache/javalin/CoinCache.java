package jakepalanca.cryptocache.javalin;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * The {@code CoinCache} class manages the caching of {@link Coin} objects,
 * allowing for efficient retrieval and updating of cryptocurrency data.
 * This class supports serialization to persist the cache to a file and
 * provides methods for updating, searching, and retrieving coins from the cache.
 *
 * <p>The cache is stored in a {@code CopyOnWriteArrayList} to ensure thread safety
 * when performing read and write operations.</p>
 *
 * <p><strong>Note:</strong> The cache is persisted to a file named {@code coinCache.ser}
 * in the root directory. If the file exists, the cache is loaded from it during instantiation.</p>
 */
public class CoinCache implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String CACHE_FILE = "coinCache.ser";
    private final transient CopyOnWriteArrayList<Coin> coins = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new {@code CoinCache} object and loads the cache from a file if it exists.
     * If the file does not exist, the cache is initialized as empty.
     */
    public CoinCache() {
        loadCacheFromFile();
    }

    /**
     * Updates the coin cache with a new list of coins, preserving old coins that are no longer in the top 1000.
     * The method merges the new coins with the remaining old coins, limits the cache to 1000 coins,
     * and saves the updated cache to a file.
     *
     * @param newCoins the list of new coins to be added to the cache
     */
    public synchronized void updateCache(List<Coin> newCoins) {
        System.out.println("Updating coin cache with new data...");

        // Step 1: Create a Set of IDs from the newCoins to keep track of the current top 1000
        Set<String> newCoinIds = newCoins.stream().map(Coin::getId).collect(Collectors.toSet());

        // Step 2: Filter out the coins that were in the previous cache but are not in the new top 1000
        List<Coin> remainingCoins = coins.stream()
                .filter(coin -> !newCoinIds.contains(coin.getId()))
                .collect(Collectors.toList());

        // Step 3: Merge the new top 1000 with the remaining old coins
        List<Coin> mergedCoins = new ArrayList<>(newCoins);
        mergedCoins.addAll(remainingCoins);

        // Step 4: Keep only the first 1000 coins in the merged list (new top 1000 + previous coins outside top 1000)
        mergedCoins = mergedCoins.stream()
                .sorted(Comparator.comparingInt(Coin::getMarketCapRank))
                .limit(1000)
                .collect(Collectors.toList());

        // Step 5: Replace the old cache with the new cache
        coins.clear();
        coins.addAll(mergedCoins);

        // Step 6: Save the updated cache to file
        saveCacheToFile();

        System.out.println("Cache update complete. Total coins in cache: " + coins.size());
    }

    /**
     * Saves the current coin cache to a file to persist the data across sessions.
     * The file is named {@code coinCache.ser} and is stored in the root directory.
     */
    private void saveCacheToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_FILE))) {
            oos.writeObject(new ArrayList<>(coins));
            System.out.println("Coin cache successfully saved to file.");
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
        File file = new File(CACHE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Coin> loadedCoins = (List<Coin>) ois.readObject();
                coins.clear();
                coins.addAll(loadedCoins);
                System.out.println("Coin cache successfully loaded from file.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Failed to load coin cache: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Cache file not found, starting with an empty cache.");
        }
    }

    /**
     * Merges two lists of coins, sorts them by ID, and returns the sorted list.
     *
     * <p>This method performs a manual merge operation similar to merge sort, which ensures
     * that the merged list is sorted lexicographically by coin ID.</p>
     *
     * @param existingCoins the list of coins already in the cache
     * @param newCoins      the list of new coins to be merged with the existing ones
     * @return              the merged and sorted list of coins
     */
    private List<Coin> mergeAndSort(List<Coin> existingCoins, List<Coin> newCoins) {
        System.out.println("Starting merge and sort operation...");
        List<Coin> sortedList = new ArrayList<>();
        int i = 0, j = 0;

        while (i < existingCoins.size() && j < newCoins.size()) {
            Coin coin1 = existingCoins.get(i);
            Coin coin2 = newCoins.get(j);
            if (coin1.getId().compareTo(coin2.getId()) <= 0) {
                sortedList.add(coin1);
                i++;
            } else {
                sortedList.add(coin2);
                j++;
            }
        }

        // Append remaining elements of existingCoins or newCoins
        while (i < existingCoins.size()) {
            sortedList.add(existingCoins.get(i++));
        }
        while (j < newCoins.size()) {
            sortedList.add(newCoins.get(j++));
        }

        System.out.println("Merge and sort operation completed.");
        return sortedList;
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
     * @param coin       the coin to check
     * @param query      the query string to compare against
     * @param levenshtein the Levenshtein distance object used for comparison
     * @param threshold  the maximum allowed distance for a match
     * @return           {@code true} if the coin matches the query within the threshold; {@code false} otherwise
     */
    private boolean isMatch(Coin coin, String query, LevenshteinDistance levenshtein, int threshold) {
        String coinId = coin.getId().toLowerCase();
        String coinName = coin.getName().toLowerCase();

        int idDistance = levenshtein.apply(coinId, query);
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
                .sorted((c1, c2) -> Integer.compare(c1.getMarketCapRank(), c2.getMarketCapRank()))
                .limit(100)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of coins from the cache that match the specified list of IDs.
     *
     * @param ids the list of coin IDs to search for
     * @return    a list of coins that match the specified IDs
     */
    public List<Coin> getCoinsByIds(List<String> ids) {
        return coins.stream()
                .filter(coin -> ids.contains(coin.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all coins currently stored in the cache.
     *
     * @return a list of all coins in the cache
     */
    public List<Coin> getAllCoins() {
        return new ArrayList<>(coins);
    }
}
