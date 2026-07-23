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
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        try {
            manager.resetTrick();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String response = JsonManager.scoreToJson(manager.getTeams());
        int code = 200;
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
