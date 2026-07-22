package src.server;

import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import src.GameManager;

public class JassHttpHandler {
    protected Map<Integer, GameManager> managers;

    public JassHttpHandler(Map<Integer, GameManager> managers) {
        this.managers = managers;
    }


    protected static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Gameroom");
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
