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

import com.youngbryanyu.simplistash.commands.CommandHandler;
import com.youngbryanyu.simplistash.server.ClientHandler;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Dependency injection configuration class.
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
     * Creates a new instance of an off-heap in-memory DB.
     * 
     * @return An off-heap in-memory DB.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DB db() {
        return DBMaker.memoryDirectDB().make();
    }

    /**
     * Name of the read only client handler beans.
     */
    public static final String READ_ONLY_CLIENT_HANDLER = "readOnlyClientHandler";

    /**
     * Creates an instance of a client handler with read-only permissions.
     * 
     * @param commandHandler The command handler.
     * @param stashManager   The stash manager.
     * @param logger         The application logger.
     * @return A new instance of a client handler.
     */
    @Bean(READ_ONLY_CLIENT_HANDLER)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ClientHandler readOnlyClientHandler(CommandHandler commandHandler, StashManager stashManager,
            Logger logger) {
        return new ClientHandler(commandHandler, stashManager, logger, true);
    }

    /**
     * Name of the writeable client handler beans.
     */
    public static final String WRITEABLE_CLIENT_HANDLER = "writeableClientHandler";

    /**
     * Creates an instance of a client handler with both read and write permissions.
     * 
     * @param commandHandler The command handler.
     * @param stashManager   The stash manager.
     * @param logger         The application logger.
     * @return A new instance of a client handler.
     */
    @Bean(WRITEABLE_CLIENT_HANDLER)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ClientHandler writeableclientHandler(CommandHandler commandHandler, StashManager stashManager,
            Logger logger) {
        return new ClientHandler(commandHandler, stashManager, logger, false);
    }
}
