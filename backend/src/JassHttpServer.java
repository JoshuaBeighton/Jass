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
        manager = new GameManager();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);

            server.createContext("/player", new PlayerHandler());
            server.createContext("/nextPlayer", new NextPlayerHandler());
            server.createContext("/teams", new TeamHandler());
            server.createContext("/teamWait", new TeamWaitHandler());
            server.createContext("/hand", new HandHandler());
            server.createContext("/gameChoice", new GameChoiceHandler());
            server.createContext("/cardWait", new CardWaitHandler());
            server.createContext("/nextCard", new NextCardHandler());
            server.createContext("/resetTrick", new ResetTrickHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("Server is running on port 9000");
        }
        catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void respondToOPTIONS(HttpExchange exchange) {
        try {
            OutputStream os = exchange.getResponseBody();
            String response = "";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            os.write(response.getBytes());
            os.close();
        }
        catch (Exception e) {

        }

    }

    static class TeamWaitHandler implements HttpHandler {
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
                handleGet(exchange);
            } else
                if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                    respondToOPTIONS(exchange);
                } else {// todo: implement 400 returns
                    exchange.sendResponseHeaders(200, 0);
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
            } else
                if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                    respondToOPTIONS(exchange);
                } else {// todo: implement 400 returns
                    exchange.sendResponseHeaders(200, 0);
                }
        }

        public void handleGet(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath().split("/hand/")[1];

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

    static class NextPlayerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else
                if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                    respondToOPTIONS(exchange);
                } else {// todo: implement 400 returns
                    exchange.sendResponseHeaders(200, 0);
                }
        }

        public void handleGet(HttpExchange exchange) throws IOException {
            String response = String.valueOf(manager.getNextPlayer());
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
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
                } else
                    if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                        respondToOPTIONS(exchange);
                    } else {// todo: implement 400 returns
                        exchange.sendResponseHeaders(200, 0);
                    }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().toString();
            String[] args = uri.split("\\?")[1].split("\\&");
            String name = args[0].split("=")[1];
            int lastIndex = Integer.parseInt(args[1].split("=")[1]);
            Thread t = new Thread(() -> {
                try {
                    if (Main.getPlayers().get(manager.getNextToChoose()).getPlayerName().equals(name)) {
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        String response = JsonManager.gameChoiceToJson(name, manager.getNextToChoose(),
                                Main.getPlayers(), manager.getGame(), manager.isForced());

                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        return;
                    }
                    while (manager.getNextToChoose() == lastIndex) {

                    }
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    String response = JsonManager.gameChoiceToJson(name, manager.getNextToChoose(), Main.getPlayers(),
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

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String requestString = new String(is.readAllBytes());
            IGame request = JsonManager.jsonToIGame(requestString);
            if (request != null) {
                manager.setGame(request);
                System.out.println("Game Chosen: " + request.getName());
            }
            manager.incrementChooser();
            String response = "success";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class CardWaitHandler implements HttpHandler {
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

                    String response = JsonManager.currentTrickToJSON(manager.getCurrentTrick(), Main.getPlayers().get(manager.getNextPlayer()), Main.getPlayers().get(manager.getNextToChoose()));

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

    static class NextCardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("POST")) {
                handlePost(exchange);
            } else
                if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                    respondToOPTIONS(exchange);
                } else {// todo: implement 400 returns
                    exchange.sendResponseHeaders(200, 0);
                }
        }

        private void handlePost(HttpExchange exchange) throws IOException {

            InputStream is = exchange.getRequestBody();
            String requestString = new String(is.readAllBytes()).replace("\"", "");
            boolean success =false;
            try {
                success = manager.playCard(requestString, Main.getPlayers());
            }
            catch (Exception e) {
                e.printStackTrace();
            }


            String response = success ? "success" : "failure";
            int code = success ? 200 : 401;
            if (!success){
                System.out.println("Failed");
            }
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class ResetTrickHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("POST")) {
                handlePost(exchange);
            } else
                if (exchange.getRequestMethod().equals("OPTIONS")) {// todo: implement 400 returns
                    respondToOPTIONS(exchange);
                } else {// todo: implement 400 returns
                    exchange.sendResponseHeaders(200, 0);
                }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            try {
                manager.resetTrick();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            String response = JsonManager.scoreToJson();
            int code = 200;
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
