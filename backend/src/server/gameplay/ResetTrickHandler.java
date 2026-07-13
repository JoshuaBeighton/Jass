package src.server.gameplay;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

public class ResetTrickHandler extends JassHttpHandler implements HttpHandler {
    public ResetTrickHandler(GameManager manager) {
        super(manager);
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

    private void handlePost(HttpExchange exchange) throws IOException {
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
