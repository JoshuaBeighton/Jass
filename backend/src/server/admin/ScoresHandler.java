package src.server.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * HTTP handler for returning the current scores of the teams in a game room.
 */
public class ScoresHandler extends JassHttpHandler implements HttpHandler {

    /**
     * Creates a new scores handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public ScoresHandler(Map<Integer, GameManager> managers) {
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
     * Handles a GET request and returns team scores as JSON.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        String response = JsonManager.scoresToJson(manager.getTeams());
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
