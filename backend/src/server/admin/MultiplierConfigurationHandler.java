package src.server.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import src.GameManager;
import src.server.JassHttpHandler;
import src.utils.JsonManager;

/**
 * HTTP handler for room synchronization. GET - Establishes a long-lived SSE connection to stream room state updates.
 * POST - Accepts updated room state and applies it to the GameManager.
 */
public class MultiplierConfigurationHandler extends JassHttpHandler implements HttpHandler {

    /**
     * Creates a new room state handler.
     *
     * @param managers shared map of room ids to GameManager instances
     */
    public MultiplierConfigurationHandler(Map<Integer, GameManager> managers) {
        super(managers);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("GET")) {
            handleGet(exchange);
        } else if (method.equals("POST")) {
            handlePost(exchange);
        } else if (method.equals("OPTIONS")) {
            respondToOPTIONS(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            exchange.close();
        }
    }

    /**
     * Handles GET requests: Streams real-time room updates over SSE.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        GameManager manager = getRoom(exchange, managers);

        if (manager == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        startSseResponse(exchange);
        OutputStream os = exchange.getResponseBody();

        Thread sseThread = new Thread(() -> {
            try {
                String lastSentJson = "";

                while (!Thread.currentThread().isInterrupted()) {
                    // Convert active state into JSON payload
                    String currentJson = JsonManager.multipliersToJSON(
                            manager.getActiveMultipliers(),
                            manager.getGamemodes());

                    // Push event down stream if state changed
                    if (!Objects.equals(currentJson, lastSentJson)) {
                        writeSseEvent(os, "message", currentJson);
                        lastSentJson = currentJson;
                    }
                    if (manager.gamesConfigrured) {
                        writeSseEvent(os, "message", "done");
                        break;
                    }

                    Thread.sleep(150);
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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

        sseThread.start();
    }

    /**
     * Handles POST requests: Receives new table state from client and updates GameManager.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        GameManager manager = getRoom(exchange, managers);

        if (manager == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        try (InputStream is = exchange.getRequestBody()) {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            if (requestBody.equals("done")) {
                manager.confirmMultipliers();
                return;
            }
            // Mutate state in shared GameManager
            JsonManager.updateMultipliersFromJSON(manager, requestBody);

            byte[] response = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
        catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        }
    }
}
