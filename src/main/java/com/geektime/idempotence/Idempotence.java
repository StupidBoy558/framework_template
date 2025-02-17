package com.geektime.idempotence;

/**
 * Main class for handling idempotence operations
 */
public class Idempotence {
    private final IdempotenceStorage storage;

    /**
     * Constructor
     * @param storage the storage implementation to use
     */
    public Idempotence(IdempotenceStorage storage) {
        this.storage = storage;
    }

    /**
     * Save idempotenceId if it does not exist
     * @param idempotenceId the idempotence ID
     * @return true if the idempotenceId is saved, otherwise return false
     */
    public boolean saveIfAbsent(String idempotenceId) {
        return storage.saveIfAbsent(idempotenceId);
    }

    /**
     * Delete idempotenceId from storage
     * @param idempotenceId the idempotence ID to delete
     */
    public void delete(String idempotenceId) {
        storage.delete(idempotenceId);
    }
} 