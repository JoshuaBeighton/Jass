package src.server.admin;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

public class TeamWaitHandler extends JassHttpHandler implements HttpHandler {
    public TeamWaitHandler(GameManager manager) {
        super(manager);
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

    private void handleGet(HttpExchange exchange) throws IOException {
        int count = Integer.parseInt(exchange.getRequestURI().getPath().split("/teamWait/")[1]);
        Thread t = new Thread(() -> {
            try {
                while (count >= manager.getPlayers().size()) {
                    Thread.sleep(100);
                }

                String response = JsonManager.teamsToJson(manager.getTeams());
                System.out.println(response);
                exchange.sendResponseHeaders(200, response.getBytes().length);
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
