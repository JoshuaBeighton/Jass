package src.server.gameplay;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.objs.Player;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * HTTP handler for retrieving a player's hand from a game room.
 */
public class HandHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new hand handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public HandHandler(Map<Integer, GameManager> managers) {
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
     * Handles a GET request to return the named player's hand.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    public void handleGet(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().getPath().split("/hand/")[1];
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        for (Player p : manager.getPlayers()) {
            if (uri.toLowerCase().equals(p.getPlayerName().toLowerCase())) {
                String response = JsonManager.cardsToJson(p.getCards());
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
        }
        String response = "player not found";
        exchange.sendResponseHeaders(400, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
