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
    private int gameScore;

    /** The team index used to identify teams in game state and serialization. */
    private int index;

    /** Scores for each game mode, or -1 for modes that have not yet been played. */
    private Map<Integer, Integer> multiplierScores;

    /**
     * Constructs a new team with the given index and initializes scores.
     *
     * @param index the team index
     */
    public Team(int index) {
        players = new ArrayList<Player>();
        gameScore = 0;
        this.index = index;
        multiplierScores = new HashMap<Integer, Integer>();
    }

    public void configureMultipliers(List<Integer> multipliers) {
        if (multipliers != null) {
            for (int n : multipliers) {
                multiplierScores.put(n, -1);
            }
        }
    }


    /**
     * Adds points to the team's current total score.
     *
     * @param val points to add
     */
    public void addGameScore(int val) {
        gameScore += val;
    }

    /**
     * Resets the team's current score to zero.
     */
    public void resetGameScore() {
        gameScore = 0;
    }

    /**
     * Returns the team's current score.
     *
     * @return current score
     */
    public int getGameScore() {
        return gameScore;
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
     * Stores the score for a specific game mode.
     *
     * @param multiplier the game mode name
     * @param score the score achieved in that mode
     */
    public void setMultiplierScore(int multiplier, int score) {
        multiplierScores.put(multiplier, score);
    }

    /**
     * Returns the score recorded for a specific game mode.
     *
     * @param multiplier the game mode name
     * @return the score for that mode
     */
    public int getMultiplierScore(int multiplier) {
        return multiplierScores.get(multiplier);
    }

    public void resetMatch(List<Integer> multipliers) {
        multiplierScores = new HashMap<Integer, Integer>();
        for (int n : multipliers) {
            multiplierScores.put(n, -1);
        }
        gameScore = 0;
        players.clear();
    }
}
