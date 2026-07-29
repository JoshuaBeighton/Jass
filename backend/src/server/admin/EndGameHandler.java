package src.server.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;

/**
 * HTTP handler for retrieving and adding players to a game room.
 */
public class EndGameHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new player handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public EndGameHandler(Map<Integer, GameManager> managers) {
        super(managers);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if (exchange.getRequestMethod().equals("POST")) {
            handlePost(exchange);

        } else
            if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                respondToOPTIONS(exchange);
            } else {// todo: implement 400 returns
                exchange.sendResponseHeaders(200, 0);
            }
    }

    /**
     * Adds a new player to the requested game room.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if reading or writing the request/response fails
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        InputStream is = exchange.getRequestBody();
        String toRemove = new String(is.readAllBytes());
        manager.resetMatch(toRemove);
        String response = "Success";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
