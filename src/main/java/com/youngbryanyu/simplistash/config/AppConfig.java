package com.youngbryanyu.simplistash.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Dependency injection configuration class. We should stick with annotations,
 * unless multiple named versions of the same dependency need to be injected in
 * which we can use @Bean(name=<name>) with @Qualifier("<name>"). Things set in
 * this config file will override annotations.
 * 
 * The application context (IoC container) is automatically injectable
 * wherever @Wired needs it.
 */
@Configuration
@ComponentScan(basePackages = "com.youngbryanyu.simplistash")
public class AppConfig {
    /**
     * Creates a bean for the logger.
     * 
     * @return The logger to use.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Logger logger() {
        return LoggerFactory.getLogger("logger");
    }
}
