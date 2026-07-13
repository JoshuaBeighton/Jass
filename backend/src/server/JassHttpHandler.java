package src.server;

import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

import src.GameManager;

public class JassHttpHandler {
    protected GameManager manager;

    public JassHttpHandler(GameManager manager) {
        this.manager = manager;
    }


    protected static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    protected static void respondToOPTIONS(HttpExchange exchange) {
        try {
            OutputStream os = exchange.getResponseBody();
            String response = "";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            os.write(response.getBytes());
            os.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
