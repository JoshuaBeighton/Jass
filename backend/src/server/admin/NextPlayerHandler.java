package src.server.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;

/**
 * HTTP handler for returning the next player index in a game room.
 */
public class NextPlayerHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new handler for next-player requests.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public NextPlayerHandler(Map<Integer, GameManager> managers) {
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
     * Handles a GET request and returns the next player id for the room.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    public void handleGet(HttpExchange exchange) throws IOException {
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        String response = String.valueOf(manager.getNextPlayer());
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        return;
    }
}
