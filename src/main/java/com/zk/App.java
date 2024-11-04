package com.zk;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class App {
    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.zk");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        final HttpServer server = startServer();
        // ZKFingerReader reader = new ZKFingerReader();

        
        System.out.println("Servidor iniciado em: " + BASE_URI);
        
        // Keep the server running
        try {
            Thread.currentThread().join(); // Keeps the main thread alive
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // server.shutdownNow(); // Uncomment this line if you want to shut down the server later
    }
}
