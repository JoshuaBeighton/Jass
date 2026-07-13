package src.server.gameplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;

public class NextCardHandler extends JassHttpHandler implements HttpHandler {
    public NextCardHandler(GameManager manager) {
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

