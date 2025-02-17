package com.geektime.idempotence;

import java.util.UUID;

/**
 * Generator for idempotence IDs
 */
public class IdempotenceIdGenerator {
    /**
     * Generate a unique idempotence ID using UUID
     * @return a unique idempotence ID
     */
    public String generateId() {
        return UUID.randomUUID().toString();
    }
} 