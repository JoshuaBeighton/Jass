package src.server.gameplay;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

public class CardWaitHandler extends JassHttpHandler implements HttpHandler {
    public CardWaitHandler(GameManager manager) {
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
        int count = Integer.parseInt(exchange.getRequestURI().getPath().split("/cardWait/")[1]);

        Thread t = new Thread(() -> {
            try {
                while (count >= manager.getCurrentTrick().size()) {
                    Thread.sleep(100);
                }

                String response = JsonManager.currentTrickToJSON(manager.getCurrentTrick(),
                        manager.getPlayers().get(manager.getNextPlayer()),
                        manager.getPlayers().get(
                                Math.floorMod(
                                        manager.getNextPlayer() - manager.getCurrentTrick().size(),
                                        4)));

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
