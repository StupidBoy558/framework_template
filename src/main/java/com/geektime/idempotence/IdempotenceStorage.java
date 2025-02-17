package com.geektime.idempotence;

/**
 * Interface for idempotence storage operations
 */
public interface IdempotenceStorage {
    /**
     * Save idempotenceId into storage if it does not exist
     * @param idempotenceId the idempotence ID
     * @return true if the idempotenceId is saved, otherwise return false
     */
    boolean saveIfAbsent(String idempotenceId);

    /**
     * Delete idempotenceId from storage
     * @param idempotenceId the idempotence ID to delete
     */
    void delete(String idempotenceId);
} 