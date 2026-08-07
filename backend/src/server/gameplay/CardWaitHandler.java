package src.server.gameplay;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * HTTP handler for waiting until a new card is played in the current trick.
 */
public class CardWaitHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new card wait handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public CardWaitHandler(Map<Integer, GameManager> managers) {
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
            }
    }

    /**
     * Handles a GET request that waits until the current trick advances.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        GameManager manager = getRoom(exchange, managers);
        int count = Integer.parseInt(exchange.getRequestURI().getPath().split("/cardWait/")[1]);

        // Get player name.
        String playerName = "";
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] parts = param.split("=", 2);
                if (parts.length == 2 && parts[0].equals("player")) {
                    playerName = parts[1];
                }
            }
        }
        final String player = playerName;
        startSseResponse(exchange);
        OutputStream os = exchange.getResponseBody();

        try {
            AtomicInteger lastSeen = new AtomicInteger(count);
            Thread t = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        int size = manager.getCurrentTrick().size();
                        if (size != lastSeen.get()) {
                            String response = JsonManager.currentTrickToJSON(manager, player);
                            writeSseEvent(os, "card-state", response);
                            lastSeen.set(size);
                        }
                        if (size >= 4) {
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

}
