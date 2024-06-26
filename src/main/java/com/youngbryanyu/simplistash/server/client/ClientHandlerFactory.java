package com.youngbryanyu.simplistash.server.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.config.AppConfig;

/**
 * The factory used to create client handlers.
 */
@Component
public class ClientHandlerFactory {
    /**
     * The spring IoC container holding all beans.
     */
    private final ApplicationContext context;

    /**
     * Constructor for the client handler factory.
     * 
     * @param context The spring IoC container.
     */
    @Autowired
    public ClientHandlerFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Creates a new instance of a client handler that has write permissions.
     * 
     * @return A client handler.
     */
    public ClientHandler createPrimaryClientHandler() {
        return context.getBean(AppConfig.PRIMARY_CLIENT_HANDLER, ClientHandler.class);
    }

    /**
     * Creates a new instance of a client handler that is read-only.
     * 
     * @return A client handler.
     */
    public ClientHandler createReadOnlyClientHandler() {
        return context.getBean(AppConfig.READ_ONLY_CLIENT_HANDLER, ClientHandler.class);
    }
}
