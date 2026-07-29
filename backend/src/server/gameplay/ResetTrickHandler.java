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
 * HTTP handler for resetting the current trick and returning updated scores.
 */
public class ResetTrickHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new reset trick handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public ResetTrickHandler(Map<Integer, GameManager> managers) {
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
     * Resets the current trick and returns the updated team scores as JSON.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String header = exchange.getRequestHeaders().getFirst("gameroom");
        if (header == null || header.isBlank()) {
            header = exchange.getRequestHeaders().getFirst("Gameroom");
        }
        if (header == null || header.isBlank()) {
            throw new IOException("Missing gameroom header");
        }

        int key = Integer.parseInt(header);
        GameManager manager = managers.get(key);
        try {
            manager.resetTrick();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String response = "";
        int nextPlayer = manager.getNextPlayer();
        int winner = manager.getTrickWinner();
        if (nextPlayer == -1) {
            response = JsonManager.scoreToJson(manager.getTeams(), manager.getPlayers().get(0), manager.getPlayers().get(0));
        } else {
            try {
                response = JsonManager.scoreToJson(manager.getTeams(), manager.getPlayers().get(nextPlayer), manager.getPlayers().get(winner));
            }
            catch (Exception e) {
                System.out.println("Error Caught!");
            }
        }

        int code = 200;
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
