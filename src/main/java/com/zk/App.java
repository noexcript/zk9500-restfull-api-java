package com.zk;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.server.ResourceConfig;
// import org.glassfish.jersey.media.json.JsonProcessingFeature;

import com.zk.exception.FingerPrintException;
import com.zk.service.FingerFingerSocketService;
import com.zk.utils.CORSFilter;
import com.zk.utils.NotFoundFilter;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static final String HTTP_SERVER_URI = "http://localhost:8080/";
    private static final String SOCKET_SERVER_URI = "http://localhost:8081";

    private HttpServer server;
    private static App instance;

    private App() {
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public void startApplication() {
        try {
            initializeServers();
            waitForShutdown();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting application", e);
            shutdown();
        }
    }

    private void initializeServers() {
        try {

            server = createHttpServer();
            FingerFingerSocketService.getInstance();
            logServerStartup();
        } catch (Exception e) {
            throw new FingerPrintException("Failed to initialize servers", e);
        }
    }

    private HttpServer createHttpServer() {
        ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.zk")
                .register(JsonProcessingFeature.class)
                .register(JacksonFeature.class)
                .register(NotFoundFilter.class)
                .register(CORSFilter.class);

        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(HTTP_SERVER_URI),
                resourceConfig,
                false);
    }

    private void logServerStartup() {
        LOGGER.info(() -> String.format("HTTP Server started at: %s", HTTP_SERVER_URI));
        LOGGER.info(() -> String.format("Socket.IO Server started at: %s", SOCKET_SERVER_URI));
    }

    private void waitForShutdown() {

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        try {
            server.start();
            System.out.println("Servidor iniciado. Pressione Ctrl+C para encerrar.");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Server interrupted", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during server operation", e);
        }
    }

    public void shutdown() {
        LOGGER.info("Initiating server shutdown...");
        try {
            if (server != null) {
                server.shutdownNow();
            }
            LOGGER.info("Server shutdown completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during server shutdown", e);
        }
    }

    public static void main(String[] args) {
        App.getInstance().startApplication();

    }
}
