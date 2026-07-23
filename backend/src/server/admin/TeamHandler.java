package src.server.admin;

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
 * HTTP handler for returning team state information for a game room.
 */
public class TeamHandler extends JassHttpHandler implements HttpHandler {

    /**
     * Creates a new team handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public TeamHandler(Map<Integer, GameManager> managers) {
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
     * Handles a GET request and returns team data as JSON.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        System.out.println("players:");
        for (Player p : manager.getPlayers()) {
            System.out.println(p.getPlayerName());
        }
        String response = JsonManager.teamsToJson(manager.getTeams());
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
