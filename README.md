# Spring Data Redis Cache

With this project you'll take a deep dive into the world of caching and explore how to implement Redis Cache in a Spring Boot 
application, unlocking its full potential for producing significant performance advantages. 

## Table of contents

- [Introduction to Cache Abstraction](#introduction-to-Cache-Abstraction)
- [Spring Boot Cache Providers](#spring-Boot-Cache-Providers)
- [What is Redis](#what-is-Redis)
    * [Why use Redis as a Cache in Spring](#why-use-Redis-as-a-Cache-in-Spring)
    * [How does Redis Caching works](#how-does-Redis-Caching-works)
- [About the project](#about-the-project)
- [Getting started](#getting-started)
    * [Configuring Redis Cache in Spring Boot](#configuring-Redis-Cache-in-Spring-Boot)
    * [Adding Redis Configuration class](#adding-Redis-configuration-class)
    * [Adding annotations to cacheable methods](#adding-annotations-to-cacheable-methods)
- [Testing Redis Cache with Testcontainers](#testing-redis-cache-with-testcontainers)
- [Trying out Redis Cache](#trying-out-redis-cache)
- [Built with](#built-with)
- [License](#license)
- [Resources](#resources)

## Introduction to Cache Abstraction

At its essence, the cache abstraction applies caching to Java methods, effectively reducing the number of executions based 
on the cached information. That is, each time a designated method is invoked, the abstraction applies a caching behavior 
that checks whether the method has been previously invoked for the given arguments. If it has been invoked, the cached result
is retrieved and returned without having to execute the method again. However, If the method hasn't been previously invoked, 
then it is called, and the result is cached and returned to the user. This way, the next time the method is invoked, the 
cached result is returned. With this approach, expensive methods (whether CPU- or IO-bound) can be invoked only the first time
for a given set of parameters and the result reused without having to actually invoke the method again. 

![](images/cache-mechanism.png)

The caching logic is applied transparently without any interference to the invoker. In fact, the method's invoker does not
need to be aware of or explicitly handle caching mechanisms. Instead, the caching abstraction handles these operations behind
the scenes, improving performance and reducing unnecessary computational load without requiring additional effort from the
developer.

**_IMPORTANT:_**  this approach works only for methods that are guaranteed to return the same output (result) for a given 
input (or arguments) no matter how many times they are invoked. 

The caching abstraction provides other cache-related operations, such as the ability to update the content of the cache 
or to remove one or all entries. These are useful if the cache deals with data that can change during the course of the 
application.

As with other services in the Spring Framework, the caching service is an abstraction (not a cache implementation) and 
requires the use of actual storage to store the cache data - that is, the abstraction frees you from having to write the 
caching logic but does not provide the actual data store. This abstraction is materialized by the **`org.springframework.cache.Cache`**
and **`org.springframework.cache.CacheManager`** interfaces.

To use the cache abstraction, you need to take care of two aspects:
 * Caching declaration: Identify the methods that need to be cached and their policies.
 * Cache configuration: The backing cache where the data is stored and from which it is read.

## Spring Boot Cache Providers

Cache providers allow us to transparently and clearly configure the cache in the application. We can use the following 
steps to configure any given cache provider:
 * Add the `@EnableCaching` annotation to the configuration file.
 * Add the required cache library to the classpath.
 * Add the cache provider configuration file to the root classpath.

In the following sections we will talk more about each of these steps and how to set up a chosen cache provider correctly.
The following are the cache providers supported by the Spring Boot framework:
 * JCache (JSR-107)
 * EhCache
 * Hazelcast
 * Infinispan
 * Couchbase
 * Redis
 * Caffeine
 * Simple

Redis is a NoSQL database, so it doesn't have any tables, rows, or columns, and it doesn't support SQL-like statements such 
as select, insert, update, or delete. Instead, Redis uses various data structures to store data. As a result, it can serve 
frequently requested items with incredibly fast response times and allow easy scaling for larger workloads without increasing 
the cost of a more expensive back-end system. Hence, we recommend implementing caching in Spring Boot application using Redis.

## What is Redis

Remote Dictionary Server, commonly known as Redis, is an open source and in-memory data store, which represents one of the 
many options for implementing caching in Spring Boot applications. It is a versatile key-value store that supports several 
data structures, such as Strings, Sorted Sets, Hashes, Lists, Streams, Bitmaps, etc. Redis's support for multiple data 
structures comes from its NoSQL nature, eliminating the need for a predetermined schema and offering greater flexibility in
data storage and retrieval.

Redis can be used in various ways, including:
1. **In-Memory Database:** In today’s data-driven world, handling a large amount of real-time data is a common challenge for 
businesses. A real-time database is a type of data repository designed to capture, analyze, and increase an incoming 
stream of data in real time, often immediately after the data is produced. Redis may be used to build data infrastructure 
for real-time applications that need high throughput and low latency.
2. **Cache:** Many applications struggle with the need to store and retrieve data quickly, especially in systems with high 
latency. Due to its speed, Redis is the ideal choice for caching API calls, session states, complex computations, and database
queries.
3. **Message Broker (MQ):** It has always been difficult to stream data around the organization and make it accessible for 
various system components. Redis supports messaging functionalities, serving as a message broker for facilitating communication
between different system components.

### Why use Redis as a Cache in Spring

As mentioned above, the main reason to use caching is for performance. Redis Cache works as an in-memory cache, meaning that
any data that is cached is stored on RAM. This offers significantly more data transfers per second. What this means is that 
RAM will allow you to access more data in terms of size, but also allow you to access it faster than with traditional Hard 
Drive Disks (HDD) or newer Solid State Drives (SSD).

For the end user of your application, this means a faster and more responsive user experience. There is a slight trade-off 
for this increased performance: you need to have more resources put aside for the in-memory cache. However, since you will 
have significantly fewer hits on your actual database, you will certainly end up saving resources when dealing with lots of 
frequent requests on the same URI.

In a nutshell, Redis Cache minimizes the number of network calls made to your application and improves latency, which in 
return improves the overall performance of your system architecture.

### How does Redis Caching works

Redis Cache effectively stores the results of database retrieval operations, allowing subsequent requests to retrieve the 
data directly from the cache. This significantly improves application performance by reducing unnecessary database calls.

![](images/spring-boot-redis-cache.jpg)

When a request is made, the service initially looks in the Redis cache for the desired data. When a cache hit occurs, the 
data is swiftly retrieved from the cache and promptly provided back to the service, avoiding the need to interact with the 
database.

However, if the requested data is not found in the cache (cache miss), the service falls back to the database to retrieve 
the required information. Subsequently, the fetched data is stored in the Redis cache, enabling future requests for the same 
data to be served directly from the cache, thereby eliminating further database queries and speeding up overall response times.

In this project, we will use Redis to perform cache management. Let’s move on to how to set up the cache mechanism with Redis
on a Spring Boot application.

## About the project

This project is developed using a small set of cooperating microservices, composed of three core services: product, review, 
and recommendation. Each of them deals with one type of resource and interacts with a specific database. Additionally, 
there is a composite microservice, called the product composite service, which aggregates information from these three core 
services. All of this information is stored in three different databases, one for each core microservice, and the Spring Data 
project is used to persist data to MongoDB and PostgreSQL databases. Specifically, the product and recommendation microservices
use Spring Data for MongoDB and the review microservice uses Spring Data for the Java Persistence API (JPA) to access a 
PostgreSQL database. In addition, the product composite service interacts with a Redis database used as a cache to store 
the results of database retrieval operations, allowing subsequent requests to retrieve the data directly from the cache.

At the end of this project, we will have layers inside our microservices that will look like the following:

![](images/microservice-landscape.png)

The Protocol layer handles protocol-specific logic. It is very thin, only consisting of the RestController annotations 
in the api project and the common GlobalControllerExceptionHandler in the util project. The main functionality of each 
microservice resides in each Service layer. The product composite service contains an Integration layer used to handle 
the communication with the three core microservices. The core microservices will all have a Persistence layer used for 
communicating with their databases. The cache annotations are integrated into the product composite Service layer, and 
it will invoke the Integration layer only after querying the cache and in case the requested data is not found in the cache.

To keep the source code examples easy to understand, they have a minimal amount of business logic. The information model
for the business objects they process is kept minimal for the same reason. In this section, we will go through the information
that's handled by each microservice.

The **product service** manages product information and describes each product with the following attributes:
```
• Product ID
• Name
• Weight
```
The **review service** manages product reviews and stores the following information about each review:
```
• Product ID
• Review ID
• Author
• Subject
• Content
```
The **recommendation service** manages product recommendations and stores the following information about each recommendation:
```
• Product ID
• Recommendation ID
• Author
• Rate
• Content
```
The **product composite service** aggregates information from the three core services and presents information about a product 
as follows:
```
• Product information, as described in the product service
• A list of product reviews for the specified product, as described in the review service 
• A list of product recommendations for the specified product, as described in the recommendation service
```

In this tutorial we will skip the different steps to generate the skeleton code for our project, and we will focus our attention
on how to implement a caching system using Redis as a cache provider. But despite this, the full source code is available 
in the GitHub repository, so you can consult it anytime. 

## Getting started

### Configuring Redis Cache in Spring Boot

To use Redis Cache in Spring Boot, you first need to set up all the configuration files by following these steps:

* Add the **spring-boot-started-cache** and **spring-boot-starter-data-redis** dependencies to the product composite service 
_pom.xml_ file.
```
	<dependencies>
	...
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-cache</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
		</dependency>
	...
	</dependencies>
```

* Add also the **spring-boot-testcontainers** and **testcontainers-redis** dependencies to the previous file, in order to test that 
the cache logic works accordingly.
```
	<dependencies>
	...
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-testcontainers</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>com.redis</groupId>
			<artifactId>testcontainers-redis</artifactId>
			<version>2.2.2</version>
			<scope>test</scope>
		</dependency>
	...
	</dependencies>
```

* Update the _docker-compose.yml_ file by adding a Redis service. This Redis server contains the database used for caching 
and which Spring Boot project will automatically connect to.
```
services:
  ...
  redis:
    image: redis:latest
    mem_limit: 512m
    ports:
      - "6379:6379"
    healthcheck:
        test: [ "CMD", "redis-cli", "ping" ]
        interval: 5s
        timeout: 2s
        retries: 60
```

* Update the product composite service _application.yml_ file to configure the Spring Boot application to automatically 
connect to Redis.
```
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    root: INFO
    com.fpivetti: DEBUG
    org.springframework.cache: TRACE
    
---
spring.config.activate.on-profile: docker
server.port: 8080
spring.data.redis.host: redis
```

Important parts of the preceding code:

* We set the **type** parameter to **redis**, meaning that our application will automatically make the necessary 
configurations to use Redis as the cache provider.
* When running without Docker using the default Spring profile, the Redis database is expected to be reachable on 
**localhost:6379**.
* Setting the log level for **org.springframework.cache** to **TRACE** will allow us to see which cache statements are 
executed in the log.
* When running inside Docker using the Spring profile, docker, the Redis database is expected to be reachable on 
**redis:6379**.

### Adding Redis Configuration class

To enable Spring Redis cache in the product composite microservice, we have to add some configuration. To do that we create 
a class named _RedisCacheConfig_ under the config folder so that the necessary Redis settings can be detected by Spring 
and seen as a Spring component.
```
@EnableCaching
@Configuration
public class RedisCacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .transactionAware()
                .build();
    }
}
```

Now let's examine the above class in detail:

* `@EnableCaching`: This annotation is used to enable caching support in the Spring application. It will trigger a 
post-processor that inspects every Spring Bean for the presence of caching annotations on public methods.
* `@Configuration`: This annotation indicates that the class is a configuration class and its methods are defined as a 
Spring Bean.
* The configuration of Redis Cache created by RedisCacheManager is defined with `RedisCacheConfiguration`. This specifies the 
default configuration for all caches, including default TTL, that is key expiration time, and serialization settings for 
converting to and from the binary storage format. It also disables caching of null values.
* RedisCacheManger behavior can be configured with `RedisCacheManagerBuilder`. It allows you to set the default RedisCacheConfiguration,
which was defined earlier, and specify the transaction behavior.

### Adding annotations to cacheable methods

You can use `@Cacheable` to demarcate methods that are cacheable - that is, methods for which the result is stored in the cache
so that, on subsequent invocations (with the same arguments), the value in the cache is returned without having to actually
invoke the method again.
```
@Cacheable(cacheNames = "products", key = "#productId")
public ProductAggregateDto getProduct(int productId) {...}
```

The `cacheNames` attribute establishes a cache with a specific name, while the `key` attribute permits the use of Spring
Expression Language to compute the key dynamically. Consequently, the method result is stored in the 'products' cache, where
respective 'productId' serves as the unique key. This approach optimizes caching by associating each result with a distinct key.

The cache abstraction allows not just population of a cache store but also eviction. This process is useful for removing
stale or unused data from the cache. `@CacheEvict` annotation demarcates methods that perform cache eviction, that is methods
that act as triggers for removing data from the cache. As for `@Cacheable` annotation, we can use `cacheNames` and `key` 
attributes to remove a specific data from the cache specified.
```
@CacheEvict(cacheNames = "products", key = "#productId")
public void deleteProduct(int productId) {...}
```

## Testing Redis Cache with Testcontainers

**Testcontainers** is a Java library that simplifies running automated integration tests by running resource managers like a
database or a message broker as a Docker container. Testcontainers can be configured to automatically start up Docker containers
when JUnit tests are started and tear down the containers when the tests are completed.

To enable Testcontainers in an existing test class for a Spring Boot application, we can add the `@Testcontainers` annotation
to the test class. A disadvantage of this approach is that each test class will use its own Docker container, each of them 
taking time to bring up the database in the container. Running multiple test classes that use the same type of test container 
will add this latency for each test class. To avoid this extra latency, we can use the **Single Container Pattern**. Following
this pattern, a base class is used to launch a single Docker container for Redis. 

The base class, _RedisCacheTestBase_, used in the Product composite microservice looks like this:
```
public abstract class RedisCacheTestBase {

    @ServiceConnection
    private static final RedisContainer cache = new RedisContainer("redis:latest");

    @BeforeAll
    static void beforeAll() {
        cache.start();
    }

    @AfterAll
    static void afterAll() {
        cache.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", cache::getHost);
        registry.add("spring.data.redis.port", cache::getFirstMappedPort);
    }
}
```

Explanations for the preceding source code:
* The cache container is declared as a RedisContainer and annotated with `@ServiceConnection` to indicate that this field
provides a service that can be connected to.
* A static method annotated with `@BeforeAll` is used to start the cache container before any JUnit code is invoked.
* A static method annotated with `@AfterAll` is used to stop the cache container after any JUnit code is executed.
* The cache container will get some properties defined when started up, such as which port to use. To register these
dynamically created properties in the application context, a static method _setProperties()_ is defined. The method is annotated
with `@DynamicPropertySource` to override the cache configuration in the application context. 

The test class, _ProductCompositeServiceApplicationTests_, use the base class as follows:
```
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests extends RedisCacheTestBase {...}
```

This test class declares a method, _setUp()_, annotated with `@BeforeEach`, which is executed before each test method. The setup
method, among other things, removes any entities from previous tests in the cache. In this way, every time we start a new
test, the cache is completely empty ensuring that each test case starts in a consistent state, reducing the likelihood 
of unexpected interactions between tests due to shared cached data.
```
@Autowired
private CacheManager cacheManager;

@BeforeEach
void setUp() {
    ...
    cacheManager.getCacheNames().forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
}
```

To ensure that the cache works correctly, we define two test cases which test two different cache operations:
* The first test verify that the second time we call the GET operation for the same productId, it will retrieve the result
from the cache without invoking the getProduct method.
```
@Test
void getProductByIdFromCache() {
    getAndVerifyProduct(PRODUCT_ID_OK, OK);
    getAndVerifyProduct(PRODUCT_ID_OK, OK);
    verify(compositeIntegration, times(1)).getProduct(PRODUCT_ID_OK);
}
```

* The second test verify that if we call the GET operation for a product after deleting it from the database, it will invoke
the getProduct method since no entries are found in the cache.
```
@Test
void deleteCompositeProductFromCache() {
    getAndVerifyProduct(PRODUCT_ID_OK, OK);
    deleteAndVerifyProduct(PRODUCT_ID_OK, OK);
    getAndVerifyProduct(PRODUCT_ID_OK, OK);
    verify(compositeIntegration, times(2)).getProduct(PRODUCT_ID_OK);
}
```

## Trying out Redis Cache

Once everything is set up, we can start the system landscape with the following commands:
```
$ ./mvnw clean package
$ docker-compose build && docker-compose up
```

After running the application you can visit http://localhost:8080/openapi/webjars/swagger-ui/index.html to test and use 
the different operations defined in the project.

![](images/swagger-screenshot.png)

## Built with

* [![Spring][Spring.io]][Spring-url]
* [![Redis][Redis.io]][Redis-url]
* [![MongoDB][Mongodb.com]][Mongodb-url]
* [![Postgresql][Postgresql.com]][Postgresql-url]
* [![Ubuntu][Ubuntu.com]][Ubuntu-url]
* [![Java][Java.com]][Java-url]
* [![Java][Jetbrains.com]][Jetbrains-url]

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

## Resources

- [Microservices with Spring Boot 3 and Spring Cloud - Third Edition](https://www.packtpub.com/product/microservices-with-spring-boot-3-and-spring-cloud-third-edition-third-edition/9781805128694)
- [Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Spring Redis Cache](https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html)




[Spring.io]: https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white
[Spring-url]: https://spring.io/
[Redis.io]: https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white
[Redis-url]: https://redis.io/
[Mongodb.com]: https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white
[Mongodb-url]: https://www.mongodb.com/
[Postgresql.com]: https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white
[Postgresql-url]: https://www.postgresql.org/
[Ubuntu.com]: https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white
[Ubuntu-url]: https://ubuntu.com/
[Java.com]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://www.java.com/it/
[Jetbrains.com]: https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white
[Jetbrains-url]: https://www.jetbrains.com/idea/

