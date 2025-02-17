package com.geektime.idempotence;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis cluster based implementation of IdempotenceStorage
 */
public class RedisClusterIdempotenceStorage implements IdempotenceStorage {
    private static final long DEFAULT_EXPIRATION_SECONDS = TimeUnit.HOURS.toSeconds(24); // 24 hours default
    private final JedisCluster jedisCluster;
    private final long expirationSeconds;

    /**
     * Constructor
     * @param redisClusterAddress the format is 128.91.12.1:3455;128.91.12.2:3452;289.13.2.12:8978
     * @param config should not be null
     */
    public RedisClusterIdempotenceStorage(String redisClusterAddress, GenericObjectPoolConfig<Object> config) {
        this(redisClusterAddress, config, DEFAULT_EXPIRATION_SECONDS);
    }

    /**
     * Constructor
     * @param redisClusterAddress the format is 128.91.12.1:3455;128.91.12.2:3452;289.13.2.12:8978
     * @param config should not be null
     * @param expirationSeconds expiration time in seconds for idempotence IDs
     */
    public RedisClusterIdempotenceStorage(String redisClusterAddress, GenericObjectPoolConfig<Object> config, long expirationSeconds) {
        Set<HostAndPort> redisNodes = parseHostAndPorts(redisClusterAddress);
        this.jedisCluster = new JedisCluster(redisNodes);
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Constructor with existing JedisCluster
     * @param jedisCluster the JedisCluster instance
     */
    public RedisClusterIdempotenceStorage(JedisCluster jedisCluster) {
        this(jedisCluster, DEFAULT_EXPIRATION_SECONDS);
    }

    /**
     * Constructor with existing JedisCluster and expiration time
     * @param jedisCluster the JedisCluster instance
     * @param expirationSeconds expiration time in seconds for idempotence IDs
     */
    public RedisClusterIdempotenceStorage(JedisCluster jedisCluster, long expirationSeconds) {
        this.jedisCluster = jedisCluster;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public boolean saveIfAbsent(String idempotenceId) {
        String result = jedisCluster.set(idempotenceId, "1", SetParams.setParams().nx().ex(expirationSeconds));
        return result != null;
    }

    @Override
    public void delete(String idempotenceId) {
        jedisCluster.del(idempotenceId);
    }

    @VisibleForTesting
    protected Set<HostAndPort> parseHostAndPorts(String redisClusterAddress) {
        String[] addressArray = redisClusterAddress.split(";");
        Set<HostAndPort> redisNodes = new HashSet<>();
        for (String address : addressArray) {
            String[] hostAndPort = address.split(":");
            redisNodes.add(new HostAndPort(hostAndPort[0], Integer.valueOf(hostAndPort[1])));
        }
        return redisNodes;
    }
} 