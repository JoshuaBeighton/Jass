package src.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import src.GameManager;
import src.server.admin.*;
import src.server.gameplay.*;

public class JassHttpServer {
    private static GameManager manager;

    public static void init() {
        manager = new GameManager();
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

            server.setExecutor(null);
            server.start();

            System.out.println("Server is running on port 9000");
        }
        catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }
}
