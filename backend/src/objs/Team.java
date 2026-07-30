package src.objs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a team in the Jass game.
 *
 * Each team holds two players, tracks the current total score, and stores scores for individual game modes.
 */
public class Team {
    /** Players assigned to this team. */
    public List<Player> players;

    /** The team's accumulated score for the current match. */
    private int currentScore;

    /** The team index used to identify teams in game state and serialization. */
    private int index;

    /** Scores for each game mode, or -1 for modes that have not yet been played. */
    private Map<Integer, Integer> gameScores;

    /**
     * Returns the list of game modes still available for this team.
     *
     * @return list of available game mode names
     */
    public List<Integer> getGamesAvailable() {
        List<Integer> available = new ArrayList<Integer>();
        gameScores.forEach((game, score) -> {
            if (score == -1) {
                available.add(game);
            }
        });
        return available;
    }

    /**
     * Constructs a new team with the given index and initializes scores.
     *
     * @param index the team index
     */
    public Team(int index) {
        players = new ArrayList<Player>();
        currentScore = 0;
        this.index = index;
        gameScores = new HashMap<Integer, Integer>();
    }

    public void configureGames(List<Integer> multipliers) {
        if (multipliers != null) {
            for (int n : multipliers) {
                gameScores.put(n, -1);
            }
        }
    }


    /**
     * Adds points to the team's current total score.
     *
     * @param val points to add
     */
    public void addScore(int val) {
        currentScore += val;
    }

    /**
     * Resets the team's current score to zero.
     */
    public void resetScore() {
        currentScore = 0;
    }

    /**
     * Returns the team's current score.
     *
     * @return current score
     */
    public int getScore() {
        return currentScore;
    }

    /**
     * Returns the index used to identify this team.
     *
     * @return team index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Prints the team score and player names to standard output.
     */
    public void printScore() {
        System.out.printf("Team %s & %s:\t%d\n", players.get(0).getPlayerName(), players.get(1).getPlayerName(), currentScore);
    }

    /**
     * Stores the score for a specific game mode.
     *
     * @param multiplier the game mode name
     * @param score the score achieved in that mode
     */
    public void setScore(int multiplier, int score) {
        gameScores.put(multiplier, score);
    }

    /**
     * Returns the score recorded for a specific game mode.
     *
     * @param multiplier the game mode name
     * @return the score for that mode
     */
    public int getScore(int multiplier) {
        return gameScores.get(multiplier);
    }

    public void resetMatch(List<Integer> multipliers) {
        gameScores = new HashMap<Integer, Integer>();
        for (int n : multipliers) {
            gameScores.put(n, -1);
        }
        currentScore = 0;
        players.clear();
    }
}
