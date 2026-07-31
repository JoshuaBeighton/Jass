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

            server.createContext("/api/player", new PlayerHandler(manager));
            server.createContext("/api/nextPlayer", new NextPlayerHandler(manager));
            server.createContext("/api/teams", new TeamHandler(manager));
            server.createContext("/api/teamWait", new TeamWaitHandler(manager));
            server.createContext("/api/hand", new HandHandler(manager));
            server.createContext("/api/gamemodeChoice", new GamemodeChoiceHandler(manager));
            server.createContext("/api/cardWait", new CardWaitHandler(manager));
            server.createContext("/api/nextCard", new NextCardHandler(manager));
            server.createContext("/api/resetTrick", new ResetTrickHandler(manager));
            server.createContext("/api/multipliers", new MultipliersHandler(manager));
            server.createContext("/api/room", new RoomHandler(manager));
            server.createContext("/api/publicroomlist", new PublicRoomListHandler(manager));
            server.createContext("/api/endMatch", new EndMatchHandler(manager));
            server.createContext("/api/gamemodeList", new GamemodeListHandler(manager));
            server.createContext("/api/multiplierConfiguration", new MultiplierConfigurationHandler(manager));

            server.setExecutor(Executors.newFixedThreadPool(100));
            server.start();

        }
        catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }
}
