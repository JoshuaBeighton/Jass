package src.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import src.GameManager;
import src.server.admin.*;
import src.server.gameplay.*;

/**
 * Application entry point for the Jass HTTP server.
 *
 * Registers REST endpoints and starts the embedded HTTP server.
 */
public class JassHttpServer {
    private static Map<Integer, GameManager> manager;

    /**
     * Initializes the server, registers request handlers, and starts listening.
     */
    public static void init() {
        manager = new HashMap<Integer, GameManager>();
        GameManager debugManager = new GameManager(true);
        manager.put(1001, debugManager);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);

            server.createContext("/player", new PlayerHandler(manager));
            server.createContext("/nextPlayer", new NextPlayerHandler(manager));
            server.createContext("/teams", new TeamHandler(manager));
            server.createContext("/teamWait", new TeamWaitHandler(manager));
            server.createContext("/hand", new HandHandler(manager));
            server.createContext("/gameChoice", new GameChoiceHandler(manager));
            server.createContext("/cardWait", new CardWaitHandler(manager));
            server.createContext("/nextCard", new NextCardHandler(manager));
            server.createContext("/resetTrick", new ResetTrickHandler(manager));
            server.createContext("/scores", new ScoresHandler(manager));
            server.createContext("/gameroom", new GameroomHandler(manager));
            server.createContext("/publicgameroom", new PublicGameRoomHandler(manager));

            server.setExecutor(null);
            server.start();

            System.out.println("Server is running on port 9000");
        }
        catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }
}
