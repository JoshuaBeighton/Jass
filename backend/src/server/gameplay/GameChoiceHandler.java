package src.server.gameplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.games.IGame;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

public class GameChoiceHandler extends JassHttpHandler implements HttpHandler {
    public GameChoiceHandler(GameManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (exchange.getRequestMethod().equals("GET")) {
            handleGet(exchange);
        } else
            if (exchange.getRequestMethod().equals("POST")) {
                handlePost(exchange);
            } else
                if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                    respondToOPTIONS(exchange);
                } else {// todo: implement 400 returns
                    exchange.sendResponseHeaders(200, 0);
                }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString();
        String[] args = uri.split("\\?")[1].split("\\&");
        String name = args[0].split("=")[1];
        int lastIndex = Integer.parseInt(args[1].split("=")[1]);
        Thread t = new Thread(() -> {
            try {
                if (manager.getPlayers().get(manager.getNextToChoose()).getPlayerName().equals(name)) {
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    String response = JsonManager.gameChoiceToJson(name, manager.getNextToChoose(),
                            manager.getPlayers(), manager.getGame(), manager.isForced());

                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }
                while (manager.getNextToChoose() == lastIndex) {

                }
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                String response = JsonManager.gameChoiceToJson(name, manager.getNextToChoose(), manager.getPlayers(),
                        manager.getGame(), manager.isForced());

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        System.out.println("Choosing game post received!");
        InputStream is = exchange.getRequestBody();
        String requestString = new String(is.readAllBytes());
        IGame request = JsonManager.jsonToIGame(requestString);
        if (request != null) {
            manager.setGame(request);
            System.out.println("Game Chosen: " + request.getName());
        }
        manager.incrementChooser();
        String response = "success";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

