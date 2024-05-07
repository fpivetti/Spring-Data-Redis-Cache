# Spring Data Redis Cache

With this project you'll take a deep dive into the world of caching and explore how to implement Redis cache in a Spring Boot application, 
unlocking its full potential for producing significant performance advantages. 

## Table of contents

- [About the project](#about-the-project)
    * [Built with](#built-with)
- [Getting started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Installation](#installation)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [License](#license)
- [Acknowledgments](#acknowledgments)

## About the project

To create this project, we will use a small set of cooperating microservices composed of three core microservices, 
the product, review, and recommendation services, all of which deal with one type of resource, and a composite microservice, 
called the product composite service, which aggregates information from the three core services. All of this information
is stored in three different databases, one for each core microservice, and we will use the Spring Data project to persist
data to MongoDB and PostgreSQL databases. Specifically, the product and recommendation microservices will use Spring Data
for MongoDB and the review microservice will use Spring Data for the Java Persistence API (JPA) to access a PostgreSQL database.

![](images/microservice-landscape.png)

### Built with

* [![Spring][Spring.io]][Spring-url]
* [![Redis][Redis.io]][Redis-url]
* [![MongoDB][Mongodb.com]][Mongodb-url]
* [![Postgresql][Postgresql.com]][Postgresql-url]
* [![Ubuntu][Ubuntu.com]][Ubuntu-url]
* [![Java][Java.com]][Java-url]
* [![Java][Jetbrains.com]][Jetbrains-url]

## Getting started
### Prerequisites
### Installation

## Usage

## Roadmap

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

## Acknowledgments

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

