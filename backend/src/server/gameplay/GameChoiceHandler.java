package src.server.gameplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.games.IGame;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * HTTP handler for game choice polling and submission within a game room.
 */
public class GameChoiceHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new game choice handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public GameChoiceHandler(Map<Integer, GameManager> managers) {
        super(managers);
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

    /**
     * Handles a GET request to poll for the current game chooser and game options.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        String uri = exchange.getRequestURI().toString();
        String[] args = uri.split("\\?")[1].split("\\&");
        String name = args[0].split("=")[1];
        int lastIndex = Integer.parseInt(args[1].split("=")[1]);
        Thread t = new Thread(() -> {
            try {
                if (manager.getPlayers().get(manager.getNextToChoose()).getPlayerName().equals(name)) {
                    String response = JsonManager.gameChoiceToJson(manager.getNextToChoose(),
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
                String response = JsonManager.gameChoiceToJson(manager.getNextToChoose() == -1 ? manager.getNextPlayer() : manager.getNextToChoose(), manager.getPlayers(),
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

    /**
     * Handles a POST request to submit a selected game choice.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if reading or writing the request/response fails
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        System.out.println("Choosing game post received!");
        InputStream is = exchange.getRequestBody();
        String requestString = new String(is.readAllBytes());
        IGame request = JsonManager.jsonToIGame(requestString);
        if (request != null) {
            manager.setGame(request);
            System.out.println("Game Chosen: " + request.getName());
        }
        manager.incrementChooser();
        String response = JsonManager.gameChoiceToJson(manager.getNextToChoose() == -1 ? manager.getNextPlayer() : manager.getNextToChoose(), manager.getPlayers(), manager.getGame(), manager.isForced());
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

