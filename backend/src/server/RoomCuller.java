package src.server;

import java.util.Map;

import src.GameManager;

public class RoomCuller implements Runnable {
    private final int INACTIVITY_THRESHOLD = 1800; // 30 minute threshold for inactivity

    private Map<Integer, GameManager> managers;

    public RoomCuller(Map<Integer, GameManager> managers) {
        this.managers = managers;
    }

    public void run() {
        int currentTime = (int) (System.currentTimeMillis() / 1000L);
        for (Map.Entry<Integer, GameManager> entry : managers.entrySet()) {
            Integer roomId = entry.getKey();
            GameManager manager = entry.getValue();

            if (manager.getLastTime() + INACTIVITY_THRESHOLD < currentTime) {
                managers.remove(roomId);
            }
        }
    }
}
