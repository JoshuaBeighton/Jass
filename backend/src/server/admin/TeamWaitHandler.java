package src.server.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * HTTP handler for waiting until enough players have joined a team.
 */
public class TeamWaitHandler extends JassHttpHandler implements HttpHandler {
    /**
     * Creates a new team wait handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public TeamWaitHandler(Map<Integer, GameManager> managers) {
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
     * Handles a GET request that blocks until the team count grows past a threshold.
     *
     * @param exchange the HTTP exchange
     * @throws IOException if writing the response fails
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        int key = getGameroom(exchange);
        GameManager manager = managers.get(key);
        String pathSegment = exchange.getRequestURI().getPath().split("/teamWait/")[1];
        int count = pathSegment.equals("-1") ? 0 : Integer.parseInt(pathSegment);

        startSseResponse(exchange);
        OutputStream os = exchange.getResponseBody();

        AtomicInteger lastSeen = new AtomicInteger(count);
        Thread t = new Thread(() -> {
            try {
                while (lastSeen.get() < 4) {
                    while (!Thread.currentThread().isInterrupted()) {
                        int currentSize = manager.getPlayers().size();
                        if (currentSize > lastSeen.get()) {
                            String payload = JsonManager.teamsToJson(manager.getTeams());
                            writeSseEvent(os, "teams", payload);
                            lastSeen.set(currentSize);
                            break;
                        }
                        Thread.sleep(100);
                    }
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

}

/**
 * [ {"score":0, "players":[ ` {"name":"s","team":0}, {"name":"u","team":0}], "index":0}, {"score":0, "players":
 * [{"name":"a","team":1},{"name":"y","team":1}],"index":1}]
 */
