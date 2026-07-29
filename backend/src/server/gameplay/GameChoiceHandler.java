package src.server.gameplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.games.Elephant;
import src.games.IGame;
import src.objs.Suit;
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
        int key = getGameroom(exchange);
        GameManager manager = managers.get(key);
        String uri = exchange.getRequestURI().toString();

        if (manager.getGame() instanceof Elephant) {
            System.out.println("doing thread thing");
            Thread t = new Thread(() -> handleElephantWait(exchange, manager));
            t.start();
            return;
        }
        String[] args = uri.split("\\?")[1].split("\\&");
        int lastIndex = Integer.parseInt(args[1].split("=")[1]);

        startSseResponse(exchange);
        OutputStream os = exchange.getResponseBody();

        try {
            AtomicInteger lastSeen = new AtomicInteger(lastIndex);
            Thread t = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        int chooser = manager.getNextToChoose();
                        if (chooser != lastSeen.get()) {
                            String response = JsonManager.gameChoiceToJson(chooser == -1 ? manager.getNextPlayer() : chooser, manager.getPlayers(),
                                    manager.getGame(), manager.isForced());
                            writeSseEvent(os, "game-choice", response);
                            lastSeen.set(chooser);
                        }

                        if (manager.getGame() != null) {
                            String response = JsonManager.gameChoiceToJson(chooser == -1 ? manager.getNextPlayer() : chooser, manager.getPlayers(),
                                    manager.getGame(), manager.isForced());
                            writeSseEvent(os, "game-choice", response);
                            break;
                        }
                        Thread.sleep(100);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        os.close();
                    }
                    catch (IOException ignored) {
                    }
                }
            });
            t.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        }
    }

    private void handleElephantWait(HttpExchange exchange, GameManager manager) {
        OutputStream os = exchange.getResponseBody();
        try {
            // Wait for the suit to be set.
            while (manager.getGame().getType() == -1) {
                Thread.sleep(100);
                System.out.println(manager.getGame().getType());
            }
            int suit = manager.getGame().getType();
            String suitString = Suit.toString(Suit.fromIndex(suit));
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, suitString.length());
            os.write(suitString.getBytes());
            os.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                os.close();
            }
            catch (IOException e) {
            }
        }
    }

    /**
     * Handles a POST request to submit a selected game choice.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if reading or writing the request/response fails
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        int key = getGameroom(exchange);
        GameManager manager = managers.get(key);
        InputStream is = exchange.getRequestBody();
        String requestString = new String(is.readAllBytes());

        if (manager.getGame() == null) {
            IGame request = JsonManager.jsonToIGame(requestString);
            if (request != null) {
                manager.setGame(request);
            }
            manager.incrementChooser();
            String response = JsonManager.gameChoiceToJson(manager.getNextToChoose() == -1 ? manager.getNextPlayer() : manager.getNextToChoose(), manager.getPlayers(), manager.getGame(), manager.isForced());
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }


        if (manager.getGame() instanceof Elephant) {
            String payload = requestString.trim();
            if (!payload.isEmpty()) {
                ((Elephant) manager.getGame()).setTrump(Suit.fromChar(payload.charAt(0)));
            }
            manager.incrementChooser();
            String response = "success";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }
    }
}

