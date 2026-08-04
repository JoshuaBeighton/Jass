package src.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

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
        manager = new ConcurrentHashMap<Integer, GameManager>();
        GameManager debugManager = new GameManager(true);
        manager.put(1001, debugManager);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);

            server.createContext("/player", new PlayerHandler(manager));
            server.createContext("/nextPlayer", new NextPlayerHandler(manager));
            server.createContext("/teams", new TeamHandler(manager));
            server.createContext("/teamWait", new TeamWaitHandler(manager));
            server.createContext("/hand", new HandHandler(manager));
            server.createContext("/gamemodeChoice", new GamemodeChoiceHandler(manager));
            server.createContext("/gameState", new GameStateHandler(manager));
            server.createContext("/cardWait", new CardWaitHandler(manager));
            server.createContext("/nextCard", new NextCardHandler(manager));
            server.createContext("/resetTrick", new ResetTrickHandler(manager));
            server.createContext("/multipliers", new MultipliersHandler(manager));
            server.createContext("/room", new RoomHandler(manager));
            server.createContext("/publicroomlist", new PublicRoomListHandler(manager));
            server.createContext("/endMatch", new EndMatchHandler(manager));
            server.createContext("/gamemodeList", new GamemodeListHandler(manager));
            server.createContext("/multiplierConfiguration", new MultiplierConfigurationHandler(manager));
            server.createContext("/", new FallbackHandler());
            server.setExecutor(Executors.newFixedThreadPool(100));
            server.start();

        }
        catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }
}
