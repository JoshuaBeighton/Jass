package src.server.gameplay;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * Returns the current game selection for a room without waiting for any updates.
 */
public class GameStateHandler extends JassHttpHandler implements HttpHandler {
    public GameStateHandler(Map<Integer, GameManager> managers) {
        super(managers);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (exchange.getRequestMethod().equals("GET")) {
            handleGet(exchange);
        } else if (exchange.getRequestMethod().equals("OPTIONS")) {
            respondToOPTIONS(exchange);
        } else {
            exchange.sendResponseHeaders(200, 0);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            GameManager manager = getRoom(exchange, managers);
            if (manager == null) {
                String response = "room not found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            String body = JsonManager.gamemodeChoiceToJson(
                    manager.getGameCaller() >= 0 && manager.getGameCaller() < manager.getPlayers().size()
                            ? manager.getGameCaller()
                            : 0,
                    manager.getPlayers(),
                    manager.getGamemode(),
                    manager.isForced());
            exchange.sendResponseHeaders(200, body.length());
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            OutputStream os = exchange.getResponseBody();
            os.write(body.getBytes());
            os.close();
        }
        catch (IllegalArgumentException e) {
            String response = e.getMessage();
            exchange.sendResponseHeaders(400, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
