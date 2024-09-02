package jakepalanca.cryptocache.javalin;

import java.util.ArrayList;
import java.util.List;

/**
 * An in-memory implementation of the CoinCache class.
 * This class is used for testing purposes, where the coin data is stored in memory rather than persisted to disk.
 * It overrides serialization methods to ensure no file operations are performed.
 */
public class InMemoryCoinCache extends CoinCache {
    private List<Coin> coins = new ArrayList<>();

    /**
     * Constructor for InMemoryCoinCache.
     * Initializes the cache without using any file-based persistence.
     */
    public InMemoryCoinCache() {
        super(); // No file-based cache used
    }

    /**
     * Updates the in-memory coin cache with a new list of coins.
     *
     * @param newCoins The list of new coins to update the cache with.
     */
    public void updateCache(List<Coin> newCoins) {
        this.coins = newCoins; // Update the in-memory cache
    }

    /**
     * Deserializes the in-memory coin data.
     * This method overrides the deserialize method from CoinCache and simply returns the in-memory list.
     *
     * @return The current list of coins stored in memory.
     */
    protected List<Coin> deserialize() {
        return coins; // Return in-memory data
    }

    /**
     * Serializes the in-memory coin data.
     * This method is overridden to do nothing, as we are not persisting data to disk in this in-memory implementation.
     */
    protected void serialize() {
        // No operation, as we are not persisting to disk
    }
}
