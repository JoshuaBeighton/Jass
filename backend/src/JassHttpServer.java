package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import src.utils.*;
import src.objs.*;

public class JassHttpServer {
    public static void init() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);

            server.createContext("/player", new PlayerHandler());
            server.createContext("/teams", new TeamHandler());
            server.createContext("/hand", new HandHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("Server is running on port 9000");
        }
        catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    static class PlayerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else
                if (exchange.getRequestMethod().equals("POST")) {
                    handlePost(exchange);
                } else {// todo: implement 400 returns
                }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String response = JsonManager.playersToJson(Main.getPlayers());
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String requestString = new String(is.readAllBytes());
            Player request = JsonManager.JsonToPlayer(requestString);
            Main.addPlayer(request);
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        }
    }

    static class TeamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else {// todo: implement 400 returns
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String response = JsonManager.teamsToJson(Main.getTeams());
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class HandHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else {// todo: implement 400 returns
            }
        }

        public void handleGet(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath().split("/hand/")[1];
            System.out.println(uri);

            for (Player p : Main.getPlayers()) {
                if (uri.toLowerCase().equals(p.getPlayerName().toLowerCase())) {
                    String response = JsonManager.cardsToJson(p.getCards());
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }
            }
            String response = "player not found";
            exchange.sendResponseHeaders(400, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }


    }
}

