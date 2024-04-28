package com.youngbryanyu.simplistash.config;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Dependency injection configuration class. We should stick with annotations,
 * unless multiple versions of the same exact dependency need to be injected. In
 * this case we use named annotations. Things set in this config file will
 * override annotations. The default bean scope is singleton.
 * 
 * The application context (IoC container) is automatically injectable.
 */
@Configuration
@ComponentScan(basePackages = "com.youngbryanyu.simplistash")
public class AppConfig {
    /**
     * Creates a singleton logger.
     * 
     * @return The logger to use.
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger("logger");
    }

    /**
     * Creates an off-heap in-memory database session.
     * 
     * @return An off-heap in-memory database session.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DB db() {
        return DBMaker.memoryDirectDB().make();
    }
}
