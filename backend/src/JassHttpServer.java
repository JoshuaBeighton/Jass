package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import src.utils.*;
import src.games.IGame;
import src.objs.*;

public class JassHttpServer {
    private static GameManager manager;

    public static void init() {
        manager = new GameManager(Main.getPlayers());
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);

            server.createContext("/player", new PlayerHandler());
            server.createContext("/teams", new TeamHandler());
            server.createContext("/teamWait", new TeamWaitHandler());
            server.createContext("/hand", new HandHandler());
            server.createContext("/gameChoice", new GameChoiceHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("Server is running on port 9000");
        }
        catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        System.out.println("Doing Something!");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static class TeamWaitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else {// todo: implement 400 returns
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            int count = Integer.parseInt(exchange.getRequestURI().getPath().split("/teamWait/")[1]);
            System.out.println("Waiting for new player after count: " + count);

            Thread t = new Thread(() -> {
                try {
                    while (count >= Main.getPlayers().size()) {
                        Thread.sleep(100);
                    }

                    String response = JsonManager.teamsToJson(Main.getTeams());

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

    static class PlayerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(exchange.getRequestMethod());
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else
                if (exchange.getRequestMethod().equals("POST")) {
                    handlePost(exchange);
                } else {// todo: implement 400 returns
                    exchange.sendResponseHeaders(200, 0);
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
            String response = "Success";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class TeamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("GET")) {
                System.out.println("Received GET request for teams!");
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
            addCorsHeaders(exchange);
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

    static class GameChoiceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else
                if (exchange.getRequestMethod().equals("POST")) {
                    handlePost(exchange);
                } else {// todo: implement 400 returns
                }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().toString();
            System.out.println(uri);
            String[] args = uri.split("?")[1].split("&");
            String name = args[0].split("=")[1];
            System.out.println(name);
            int lastIndex = Integer.parseInt(args[1].split("=")[1]);
            Thread t = new Thread(() -> {
                try {
                    while (manager.getNextToChoose() == lastIndex) {

                    }

                    String response = JsonManager.gameChoiceToJson(name, manager.getNextToChoose(), Main.getPlayers(), manager.getGame());

                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t.start();

        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String requestString = new String(is.readAllBytes());
            IGame request = JsonManager.jsonToIGame(requestString);
            if (request != null) {
                manager.setGame(request);
            }
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        }
    }
}

