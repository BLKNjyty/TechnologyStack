## caffeine

#### 介绍

在系统中，有些数据，访问十分频繁，往往把这些数据放入分布式缓存中，但为了减少网络传输，加快响应速度，缓存分布式缓存读压力，会把这些数据缓存到本地JVM中，大多是先取本地缓存中，再取分布式缓存中的数据,Caffeine是一个高性能Java 缓存库，使用Java8对Guava缓存重写版本，在Spring Boot 2.0中将取代Guava

#### 使用

springboot使用caffeine的两种方式：

1.直接引入maven依赖，然后使用Caffeine方法实现缓存

```xml
         <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
```

2.引入 Caffeine 和 Spring Cache 依赖，使用 SpringCache 注解方法实现缓存。

```xml
		<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>

```







