package src.server.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;

public class PublicGameRoomHandler extends JassHttpHandler implements HttpHandler {
    public PublicGameRoomHandler(Map<Integer, GameManager> managers) {
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

    private void handleGet(HttpExchange exchange) throws IOException {
        JSONArray array = new JSONArray();
        for (int key : managers.keySet()) {
            if (managers.get(key).visible && managers.get(key).getPlayers().size() < 4) {
                JSONObject obj = new JSONObject();
                obj.put("id", key);
                obj.put("playerCount", managers.get(key).getPlayers().size());
                array.put(obj);
            }
        }
        String response = array.toString();
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
