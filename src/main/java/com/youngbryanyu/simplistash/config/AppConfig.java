package com.youngbryanyu.simplistash.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Dependency injection configuration class. We should stick with annotations,
 * unless multiple named versions of the same dependency need to be injected. In
 * that case we can combined @Bean(name=<name>) in this class with
 * the @Qualifier("<name>") annotation to get the specific instance. Things set
 * in this file with @Configuration will override things set by annotations. We
 * should also stick to injecting the constructor with @Wired, instead of
 * individual fields.
 * 
 * Also, DI doesn't seem to be very good for when the cardinality and lifetime
 * of an object is not well-known (like our server which creates an arbitrary
 * amount of transient client handlers).
 * 
 * It's also worth noting that @Wired isn't needed anymore for @Component
 * classes with only 1 constructor.
 */
@Configuration
@ComponentScan(basePackages = "com.youngbryanyu.simplistash")
public class AppConfig {

}
