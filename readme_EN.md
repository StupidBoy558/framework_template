## Readme

This project aims to analyze, design, and implement common frameworks to improve design and programming capabilities, enhance code quality, learn high-quality code writing, and maintain the habit of reading source code. The project is continuously updated and currently includes: Rate Limiter Framework, Interface Idempotency Framework, and Gray Release Component.

## Rate Limiter Framework

### Background

- For public service platforms, API requests come from many different systems (callers). After the system has been online for a while, the service platform encounters many problems. For example, due to caller code bugs, incorrect service usage, or sudden business traffic, the number of interface requests from a caller surges, excessively competing for service thread resources. As a result, interface requests from other callers cannot respond in time and wait in queues, leading to significantly increased response times or even timeouts.
- Solution: Develop interface rate limiting functionality to restrict the frequency of interface requests from each caller. When the preset access frequency is exceeded, rate limiting is triggered. For example, limiting caller app-1's total interface request frequency to no more than 1000 times/second, and rejecting any interface requests beyond that.

### Framework Implementation Analysis

#### Overall Architecture

The rate limiter framework adopts a layered architecture design, mainly including the following core modules:

1. Rate Limiter Core (RateLimiter)
   - Acts as the framework's facade, providing simple and easy-to-use rate limiting interfaces
   - Responsible for coordinating work between components
   - Manages the lifecycle of rate limiting counters

2. Rate Limiting Algorithm (RateLimitAlg)
   - Abstract interface defining rate limiting algorithm behavior
   - Supports multiple rate limiting algorithm implementations
   - Current implementation: Fixed time window algorithm (FixedTimeWinRateLimitAlg)
   - Extensible: Sliding window, token bucket, leaky bucket algorithms

3. Rule Management (RateLimitRule)
   - Responsible for storing and querying rate limiting rules
   - Supports quick retrieval of rate limiting rules for specific applications and APIs
   - Implementation class (TrieRateLimitRule) uses efficient data structures

4. Configuration Management
   - Rule Configuration (RuleConfig): Defines rate limiting rule data structure
   - Configuration Parser (RuleConfigParser): Supports multiple formats (YAML, JSON)
   - Configuration Source (RuleConfigSource): Supports multiple configuration sources (local files, Nacos, etc.)

#### Core Implementation Details

1. Rate Limiting Algorithm Implementation (FixedTimeWinRateLimitAlg)
   ```java
   public class FixedTimeWinRateLimitAlg implements RateLimitAlg {
       private final AtomicInteger currentCount; // Current counter
       private final AtomicLong lastResetTime;   // Last reset time
       private final int limit;                  // Maximum requests within time window
       private final long windowSizeInMs;        // Time window size
   }
   ```
   - Uses atomic classes to ensure counter thread safety
   - Implements double-check mechanism for window reset
   - Handles clock rollback exceptions

2. Rule Management Implementation (TrieRateLimitRule)
   ```java
   public class TrieRateLimitRule implements RateLimitRule {
       private final Map<String, ApiLimit> limitMap;  // Store rate limiting rules
       
       public ApiLimit getLimit(String appId, String api) {
           return limitMap.get(appId + ":" + api);
       }
   }
   ```
   - Uses ConcurrentHashMap to ensure thread safety
   - Adopts simple and efficient rule storage structure
   - Supports dynamic rule updates

3. Configuration Management Implementation
   ```java
   public interface RuleConfigParser {
       RuleConfig parse(InputStream in);
       RuleConfig parse(String configText);
   }
   ```
   - Unified configuration parsing interface
   - Supports both file stream and text parsing
   - Extensible to support more configuration formats

4. Exception Handling
   ```java
   public class InternalErrorException extends RuntimeException {
       public InternalErrorException(String message) {
           super(message);
       }
   }
   ```
   - Defines framework-specific exception types
   - Provides clear error messages
   - Supports exception chain propagation

#### Key Features

1. Thread Safety
   - Uses atomic classes (AtomicInteger, AtomicLong)
   - Adopts ConcurrentHashMap for rule storage
   - Uses ReentrantLock to protect critical sections
   - Implements double-checked locking pattern

2. Performance Optimization
   - Minimizes lock scope
   - Uses efficient data structures
   - Avoids frequent object creation
   - Implements fast request determination

3. Extensibility
   - Interface-based design
   - Supports multiple rate limiting algorithms
   - Supports multiple configuration formats
   - Supports multiple configuration sources

4. Ease of Use
   - Simple API design
   - Flexible configuration methods
   - Comprehensive exception handling
   - Detailed logging

#### Usage Examples

1. Basic Usage
   ```java
   RateLimiter limiter = new RateLimiter();
   if (limiter.limit("app-1", "/api/v1/user")) {
       // Request rate limited, execute circuit breaking logic
   } else {
       // Request passed, continue processing
   }
   ```

2. Configuration Example
   ```yaml
   limits:
     - appId: app-1
       api: /v1/user
       limit: 100  # Maximum requests
       unit: 60    # Time window (seconds)
   ```

3. Using Nacos Configuration Center
   ```java
   RuleConfigSource configSource = new NacosRuleConfigSource(
       "localhost:8848",    // Nacos server address
       "ratelimiter-rule", // Configuration ID
       "yaml"              // Configuration format
   );
   ```

#### Design Pattern Application

1. Strategy Pattern: Implements different rate limiting algorithms through the `RateLimitAlg` interface
2. Factory Pattern: Creates different configuration parsers and rate limiting algorithm instances
3. Observer Pattern: Implements dynamic configuration update notifications
4. Singleton Pattern: Manages rate limiter instances
5. Template Method Pattern: Defines configuration loading skeleton process
6. Adapter Pattern: Adapts different configuration formats and sources

#### Future Optimization Directions

1. Algorithm Extension
   - Implement sliding window rate limiting algorithm
   - Implement token bucket rate limiting algorithm
   - Implement leaky bucket rate limiting algorithm

2. Distributed Support
   - Integrate Redis for distributed rate limiting
   - Support rate limiting data synchronization between clusters
   - Implement distributed rate limiting rule management

3. Monitoring and Alerting
   - Add rate limiting metrics statistics
   - Implement rate limiting alert mechanism
   - Provide monitoring data visualization

4. Performance Optimization
   - Introduce local caching
   - Optimize rule lookup algorithm
   - Implement batch rate limiting checks 