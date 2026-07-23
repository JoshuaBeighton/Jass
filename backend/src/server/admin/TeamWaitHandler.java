package src.server.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

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
        int key = Integer.parseInt(exchange.getRequestHeaders().get("gameroom").get(0));
        GameManager manager = managers.get(key);
        int count = Integer.parseInt(exchange.getRequestURI().getPath().split("/teamWait/")[1]);
        Thread t = new Thread(() -> {
            try {
                while (count >= manager.getPlayers().size()) {
                    Thread.sleep(100);
                }

                String response = JsonManager.teamsToJson(manager.getTeams());
                System.out.println(response);
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        t.start();
    }

}
