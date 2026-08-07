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
import src.games.IGamemode;
import src.objs.Suit;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * HTTP handler for game choice polling and submission within a game room.
 */
public class GamemodeChoiceHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new game mode choice handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public GamemodeChoiceHandler(Map<Integer, GameManager> managers) {
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
        GameManager manager = getRoom(exchange, managers);
        String uri = exchange.getRequestURI().toString();

        if (manager.getGamemode() instanceof Elephant) {
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
                            String response = JsonManager.gamemodeChoiceToJson(chooser == -1 ? manager.getNextPlayer() : chooser, manager.getPlayers(),
                                    manager.getGamemode(), manager.isForced());
                            writeSseEvent(os, "game-choice", response);
                            lastSeen.set(chooser);
                        }

                        if (manager.getGamemode() != null) {
                            String response = JsonManager.gamemodeChoiceToJson(chooser == -1 ? manager.getNextPlayer() : chooser, manager.getPlayers(),
                                    manager.getGamemode(), manager.isForced());
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
            while (manager.getGamemode().getType() == -1) {
                Thread.sleep(100);
            }
            int suit = manager.getGamemode().getType();
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
     * Handles a POST request to submit a selected gamemode choice.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if reading or writing the request/response fails
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        GameManager manager = getRoom(exchange, managers);
        InputStream is = exchange.getRequestBody();
        String requestString = new String(is.readAllBytes());

        if (manager.getGamemode() == null) {
            try {
                IGamemode request = JsonManager.jsonToIGame(requestString);
                if (request != null) {
                    manager.setGamemode(request);
                }
                manager.incrementChooser();
                String response = JsonManager.gamemodeChoiceToJson(manager.getNextToChoose() == -1 ? manager.getNextPlayer() : manager.getNextToChoose(), manager.getPlayers(), manager.getGamemode(), manager.isForced());
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            catch (Exception e) {
                String response = e.getMessage();
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        if (manager.getGamemode() instanceof Elephant) {
            String payload = requestString.trim();
            if (!payload.isEmpty()) {
                ((Elephant) manager.getGamemode()).setTrump(Suit.fromChar(payload.charAt(0)));
            }
            manager.incrementChooser();
            String response = "success";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }

        System.out.println("Received gamemode choice: " + requestString);
    }
}

