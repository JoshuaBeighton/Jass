package src.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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

    protected int getGameroom(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("gameroom");
        if (header != null && !header.isBlank()) {
            return Integer.parseInt(header);
        }

        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] parts = param.split("=", 2);
                if (parts.length == 2 && "gameroom".equals(parts[0])) {
                    return Integer.parseInt(parts[1]);
                }
            }
        }

        throw new IllegalArgumentException("Missing gameroom");
    }

    /**
     * Starts an SSE response stream for a long-polling endpoint.
     */
    protected static void startSseResponse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream; charset=utf-8");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.sendResponseHeaders(200, 0);
    }

    protected static void writeSseEvent(OutputStream os, String event, String data) throws IOException {
        try {
            os.write(("event: " + event + "\n").getBytes(StandardCharsets.UTF_8));
            os.write(("data: " + data + "\n\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        catch (IOException e) {
            try {
                os.close();
            }
            catch (Exception e1) {

            }
        }
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
