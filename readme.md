## Readme

- 本项目旨在分析、设计、开发实现一些通用框架，以提高自己的设计和编程能力，并不断增强代码质量，学习高质量代码编写，保持阅读源码的习惯。本项目持续更新，目前包含：限流框架、接口幂等框架、灰度发布组件等模块



## 限流框架



### 需求背景

- 对于公共服务平台来说，接口请求来自很多不同的系统（调用方），在系统上线一段时间里，服务平台遇到很多问题。比如，因为调用方代码Bug、不正确地使用服务、业务上面的突发流量，导致来自某个调用方的接口请求数激增，过度争用服务的线程资源，而来自其他调用方的接口请求，因此来不及响应而排队等待，导致接口请求的响应时间大幅增加，甚至出现超时
- 解决方案：开发接口限流功能，限制每个调用方对接口请求的频率。当超过预先设定的访问频率后，我们就触发限流熔断，比如，限制调用方 app-1 对公共服务平台总的接口请求频率不超过 1000 次 / 秒，超过之后的接口请求都会被拒绝。



### 框架实现分析

#### 整体架构

限流框架采用分层架构设计，主要包含以下几个核心模块：

1. 限流器核心（RateLimiter）
   - 作为框架的门面，提供简单易用的限流接口
   - 负责协调各个组件的工作
   - 管理限流计数器的生命周期

2. 限流算法（RateLimitAlg）
   - 抽象接口定义限流算法的行为
   - 支持多种限流算法的实现
   - 当前实现：固定时间窗口算法（FixedTimeWinRateLimitAlg）
   - 可扩展：滑动窗口、令牌桶、漏桶等算法

3. 规则管理（RateLimitRule）
   - 负责限流规则的存储和查询
   - 支持快速检索特定应用和API的限流规则
   - 实现类（TrieRateLimitRule）使用高效的数据结构

4. 配置管理
   - 规则配置（RuleConfig）：定义限流规则的数据结构
   - 配置解析（RuleConfigParser）：支持多种格式（YAML、JSON）
   - 配置源（RuleConfigSource）：支持多种配置来源（本地文件、Nacos等）

#### 核心实现细节

1. 限流算法实现（FixedTimeWinRateLimitAlg）
   ```java
   public class FixedTimeWinRateLimitAlg implements RateLimitAlg {
       private final AtomicInteger currentCount; // 当前计数器
       private final AtomicLong lastResetTime;   // 上次重置时间
       private final int limit;                  // 时间窗口内最大请求数
       private final long windowSizeInMs;        // 时间窗口大小
   }
   ```
   - 使用原子类保证计数器的线程安全
   - 采用双重检查机制处理窗口重置
   - 实现时钟回拨的异常处理

2. 规则管理实现（TrieRateLimitRule）
   ```java
   public class TrieRateLimitRule implements RateLimitRule {
       private final Map<String, ApiLimit> limitMap;  // 存储限流规则
       
       public ApiLimit getLimit(String appId, String api) {
           return limitMap.get(appId + ":" + api);
       }
   }
   ```
   - 使用ConcurrentHashMap保证线程安全
   - 采用简单高效的规则存储结构
   - 支持规则的动态更新

3. 配置管理实现
   ```java
   public interface RuleConfigParser {
       RuleConfig parse(InputStream in);
       RuleConfig parse(String configText);
   }
   ```
   - 统一的配置解析接口
   - 支持文件流和文本两种解析方式
   - 可扩展支持更多配置格式

4. 异常处理
   ```java
   public class InternalErrorException extends RuntimeException {
       public InternalErrorException(String message) {
           super(message);
       }
   }
   ```
   - 定义框架专用的异常类型
   - 提供清晰的错误信息
   - 支持异常链传递

#### 关键特性

1. 线程安全
   - 使用原子类（AtomicInteger、AtomicLong）
   - 采用ConcurrentHashMap存储规则
   - 使用ReentrantLock保护临界区
   - 实现双重检查锁定模式

2. 性能优化
   - 最小化锁的范围
   - 使用高效的数据结构
   - 避免频繁的对象创建
   - 实现请求的快速判定

3. 可扩展性
   - 基于接口的设计
   - 支持多种限流算法
   - 支持多种配置格式
   - 支持多种配置来源

4. 易用性
   - 简单的API设计
   - 灵活的配置方式
   - 完善的异常处理
   - 详细的日志记录

#### 使用示例

1. 基本使用
   ```java
   RateLimiter limiter = new RateLimiter();
   if (limiter.limit("app-1", "/api/v1/user")) {
       // 请求被限流，执行熔断逻辑
   } else {
       // 请求通过，继续处理
   }
   ```

2. 配置示例
   ```yaml
   limits:
     - appId: app-1
       api: /api/v1/user
       limit: 100  # 最大请求数
       unit: 60    # 时间窗口（秒）
   ```

3. 使用Nacos配置中心
   ```java
   RuleConfigSource configSource = new NacosRuleConfigSource(
       "localhost:8848",    // Nacos服务器地址
       "ratelimiter-rule", // 配置ID
       "yaml"              // 配置格式
   );
   ```

#### 设计模式应用

1. 策略模式：通过 `RateLimitAlg` 接口实现不同的限流算法
2. 工厂模式：创建不同的配置解析器和限流算法实例
3. 观察者模式：实现配置的动态更新通知
4. 单例模式：管理限流器实例
5. 模板方法模式：定义配置加载的骨架流程
6. 适配器模式：适配不同的配置格式和来源

#### 未来优化方向

1. 算法扩展
   - 实现滑动窗口限流算法
   - 实现令牌桶限流算法
   - 实现漏桶限流算法

2. 分布式支持
   - 集成Redis实现分布式限流
   - 支持集群间限流数据同步
   - 实现分布式限流规则管理

3. 监控告警
   - 添加限流指标统计
   - 实现限流告警机制
   - 提供监控数据展示

4. 性能优化
   - 引入本地缓存
   - 优化规则查找算法
   - 实现批量限流检查



### 需求分析

- 功能性需求分析：

  - 首先设置限流规则，为了做到不修改代码的前提下修改规则，将规则放置到配置文件中（比如XML、YAML配置文件）。在集成了限流框架的应用启动的时候，限流框架会将限流规则，按照事先定义的语法，解析并加载到内存中。

  - ```yaml
    configs:
    - appId: application-1
      limits:
      - api: /v1/admin
        limit: 100
      - api: /v1/user
        limit: 50
    - appId: application-2
      limits:
      - api: /v1/user
        limit: 50
      - api: /v1/order
        limit: 50
    ```

  - 在接收到接口请求之后，应用会将请求发送给限流框架，限流框架会告诉应用，这个接口请求是允许继续处理，还是触发限流熔断。

- 非功能性需求分析：

  - 易用性，希望限流规则的配置、编程接口的使用都很简单
  - 扩展性、灵活性方面，希望能够灵活地扩展各种限流算法
  - 容错性，接入限流框架是为了提高系统的可用性、稳定性，不能因为限流框架的异常，反过来影响到服务本身的可用性



### 需求设计

- 限流算法：
  - 常见的限流算法有：固定时间窗口限流算法、滑动时间窗口限流算法、令牌桶限流算法、漏桶限流算法
  - 首先使用固定时间窗口限流算法，并预先做好设计，预留好扩展点，方便今后扩展其他限流算法
- 限流模式：
  - 单机限流和分布式限流
- 集成使用：希望框架尽可能低侵入，与业务代码松耦合，替换、删除起来也更容易一些



### 设计模式分析

在限流框架的实现中，使用了多种设计模式来提高代码的可维护性、可扩展性和可测试性：

1. 策略模式（Strategy Pattern）
   - 体现：通过 `RateLimitAlg` 接口定义限流算法的抽象策略
   - 实现：`FixedTimeWinRateLimitAlg` 提供了固定时间窗口的具体限流策略
   - 优势：可以轻松添加新的限流算法实现，如滑动窗口、漏桶、令牌桶等

2. 工厂模式（Factory Pattern）
   - 体现：在 `RuleConfigParser` 的实现中，通过 `createParser` 方法根据文件扩展名创建对应的解析器
   - 实现：支持 YAML、JSON 等不同格式的配置文件解析
   - 优势：统一的解析器创建逻辑，方便扩展新的配置格式

3. 观察者模式（Observer Pattern）
   - 体现：在 `NacosRuleConfigSource` 中实现配置变更的监听机制
   - 实现：通过 Nacos 的 `Listener` 接口实现配置变更的实时通知
   - 优势：实现了配置的动态更新，无需重启应用

4. 单例模式（Singleton Pattern）
   - 体现：限流器实例的创建和管理
   - 实现：通过 `RateLimiter` 类管理限流器实例
   - 优势：确保限流器的全局唯一性，统一管理限流资源

5. 组合模式（Composite Pattern）
   - 体现：在规则管理中，通过 `RuleConfig` 和 `ApiLimit` 的组合
   - 实现：一个 `RuleConfig` 包含多个 `ApiLimit`
   - 优势：灵活组织和管理多级限流规则

6. 模板方法模式（Template Method Pattern）
   - 体现：在配置加载过程中的通用流程定义
   - 实现：通过 `RuleConfigSource` 接口定义配置加载的骨架
   - 优势：统一配置加载流程，同时支持不同的配置来源

7. 适配器模式（Adapter Pattern）
   - 体现：在不同配置格式的解析转换中
   - 实现：通过 `RuleConfigParser` 的不同实现适配不同格式的配置
   - 优势：统一配置解析接口，屏蔽不同格式的差异

8. 建造者模式（Builder Pattern）
   - 体现：在创建复杂的限流规则配置时
   - 实现：通过配置类的链式调用构建限流规则
   - 优势：简化配置对象的创建过程

这些设计模式的使用使得限流框架具有以下特点：
- 高度可扩展：易于添加新的限流算法、配置格式和数据源
- 松耦合：各个组件之间通过接口通信，降低耦合度
- 可测试性：接口抽象使得可以方便地进行单元测试
- 灵活配置：支持多种配置方式和动态更新机制
- 良好的可维护性：清晰的职责划分和标准的设计模式使用



## 接口幂等框架







## 灰度发布组件