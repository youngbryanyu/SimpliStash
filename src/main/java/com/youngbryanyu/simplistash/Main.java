package com.youngbryanyu.simplistash;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.cache.InMemoryCache;
// import com.youngbryanyu.simplistash.config.AppConfig;
import com.youngbryanyu.simplistash.server.Server;

/**
 * The entry point to the application.
 */
public class Main {
    /**
     * The port that the server listens on.
     */
    private static final int PORT = 3000;

    /**
     * The main method which starts the server.
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) {        
        /* Bootstrap Spring Context */
        // AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        InMemoryCache cache = new InMemoryCache();

        // TODO: create DB
        DB db = DBMaker.memoryDirectDB()
            .transactionEnable()
            .make();

        // TODO: load backups from disk

        // TODO: load WAL from disk

        // TODO: create executor for periodic snapshot
        
        // TODO: create shudown hood to close db and sut down executor

        /* Add shutdown hook to clean up resources after exit */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            db.close();
            // context.close();
        }));

        try {
            new Server(cache).start();
        } catch (Exception e) {
            System.out.println("The server failed to start:");
            e.printStackTrace();
        } 
    }
}
