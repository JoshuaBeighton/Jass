package src.server.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;

/**
 * HTTP handler for creating new game rooms.
 *
 * Handles GET requests to generate a new game room key and create the associated GameManager. Supports CORS and OPTIONS
 * preflight requests.
 */
public class GameroomHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Constructs a new game room handler with the shared manager map.
     *
     * @param managers the map of active game managers keyed by room id
     */
    public GameroomHandler(Map<Integer, GameManager> managers) {
        super(managers);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (exchange.getRequestMethod().equals("GET")) {
            handleGet(exchange);
        } else
            if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                respondToOPTIONS(exchange);
            } else {// todo: implement 400 returns
                exchange.sendResponseHeaders(200, 0);
            }
    }

    /**
     * Handles GET requests for creating a new game room.
     *
     * Generates a unique numeric room key, constructs a GameManager for the requested visibility, and returns the key in
     * the response body.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if an I/O error occurs writing the response
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String visibility = exchange.getRequestURI().getPath().split("/gameroom/")[1];
        int key = 0;
        Random random = new Random();
        while (key == 0 || managers.keySet().contains(key)) {
            key = random.nextInt(1001, 9999);
        }
        GameManager manager = new GameManager(visibility.equals("public"));
        managers.put(key, manager);
        String response = String.valueOf(key);
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
