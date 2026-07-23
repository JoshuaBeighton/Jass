package src.server.gameplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;

/**
 * HTTP handler for submitting a card play to the game room.
 */
public class NextCardHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new next-card handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public NextCardHandler(Map<Integer, GameManager> managers) {
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
     * Reads the played card from the request body and submits it to the game.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if reading or writing the request/response fails
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        InputStream is = exchange.getRequestBody();
        String requestString = new String(is.readAllBytes()).replace("\"", "");
        boolean success = false;
        try {
            success = manager.playCard(requestString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String response = success ? "success" : "failure";
        int code = success ? 200 : 401;
        if (!success) {
            System.out.println("Failed");
        }
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

