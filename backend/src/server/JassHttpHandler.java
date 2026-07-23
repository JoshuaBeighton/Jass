package src.server;

import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import src.GameManager;

/**
 * Base class for HTTP handlers in the Jass server.
 *
 * Provides shared access to the game manager map and common HTTP helper methods.
 */
public class JassHttpHandler {
    protected Map<Integer, GameManager> managers;

    /**
     * Constructs a base handler with access to the active game managers.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public JassHttpHandler(Map<Integer, GameManager> managers) {
        this.managers = managers;
    }

    /**
     * Adds CORS headers for browser-based clients.
     *
     * @param exchange the HTTP exchange to modify
     */
    protected static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Gameroom");
    }

    /**
     * Responds to an OPTIONS preflight request with a successful empty body.
     *
     * @param exchange the HTTP exchange to respond to
     */
    protected static void respondToOPTIONS(HttpExchange exchange) {
        try {
            OutputStream os = exchange.getResponseBody();
            String response = "";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            os.write(response.getBytes());
            os.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
