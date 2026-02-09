package src.objs;

import java.util.ArrayList;
import java.util.List;

public class Team {
    public List<Player> players;
    private int currentScore;
    private static int nextIdx = 0;
    private int index;
    private List<String> gamesAvailable;

    public List<String> getGamesAvailable() {
        return gamesAvailable;
    }

    public Team() {
        players = new ArrayList<Player>();
        currentScore = 0;
        index = nextIdx++;
        gamesAvailable = new ArrayList<String>();
        gamesAvailable.add("Top Down");
        gamesAvailable.add("Bottom Up");
        gamesAvailable.add("Middle");
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

    public int getIndex() {
        return index;
    }

    public void printScore() {
        System.out.printf("Team %s & %s:\t%d\n", players.get(0).getPlayerName(), players.get(1).getPlayerName(), currentScore);
    }
}
