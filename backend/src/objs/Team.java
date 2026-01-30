package src.objs;

import java.util.ArrayList;
import java.util.List;

public class Team {
    public List<Player> players;
    private int currentScore;

    public Team() {
        players = new ArrayList<Player>();
        currentScore = 0;
    }

    public void addScore(int val) {
        currentScore += val;
    }

    public void resetScore(int val) {
        currentScore = 0;
    }

    public int getScore() {
        return currentScore;
    }

    public void printScore() {
        System.out.printf("Team %s & %s:\t%d\n", players.get(0).getPlayerName(), players.get(1).getPlayerName(), currentScore);
    }
}
