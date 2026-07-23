package src.utils;

import java.util.List;

import src.GameManager;
import src.games.BottomUp;
import src.games.FiveFour;
import src.games.IGame;
import src.games.Middle;
import src.games.Slalom;
import src.games.TopDown;
import src.games.Trumps;
import src.objs.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class for converting game objects to and from JSON representations.
 */
public class JsonManager {
    /**
     * Converts a list of players to a JSON string.
     *
     * @param players the player list
     * @return JSON array string for the players
     */
    public static String playersToJson(List<Player> players) {
        JSONArray result = new JSONArray();
        players.forEach((p) -> {
            result.put(p.toMap());
        });
        return result.toString();
    }

    /**
     * Converts a list of players to a JSON array.
     *
     * @param players the player list
     * @return JSON array of player objects
     */
    private static JSONArray playersToJsonArray(List<Player> players) {
        JSONArray result = new JSONArray();
        players.forEach((p) -> {
            result.put(p.toMap());
        });
        return result;
    }

    /**
     * Converts a list of teams to a JSON string.
     *
     * @param teams the list of teams
     * @return JSON array string representing teams
     */
    public static String teamsToJson(List<Team> teams) {
        JSONArray result = new JSONArray();
        teams.forEach((t) -> {
            JSONObject teamObj = new JSONObject();
            teamObj.put("index", t.getIndex());
            teamObj.put("score", t.getScore());
            teamObj.put("players", playersToJsonArray(t.players));
            result.put(teamObj);
        });
        return result.toString().replace("\\", "");
    }

    /**
     * Creates a Player instance from JSON input.
     *
     * @param json the request JSON containing name and team idx
     * @param teams list of teams to match against
     * @return a new Player or null if no matching team is found
     */
    public static Player JsonToPlayer(String json, List<Team> teams) {
        JSONObject jo = new JSONObject(json);
        String name = jo.getString("name");
        int idx = jo.getInt("idx");
        System.out.println("Wants team: " + String.valueOf(idx));
        for (Team t : teams) {
            if (t.getIndex() == idx) {
                return new Player(name, t);
            }
        }

        return null;
    }

    /**
     * Converts a list of cards to a JSON string.
     *
     * @param cards the card list
     * @return JSON array string representing the cards
     */
    public static String cardsToJson(List<Card> cards) {
        return cardsToJsonArray(cards).toString();
    }

    /**
     * Converts a list of cards to a JSON array.
     *
     * @param cards the card list
     * @return JSON array of card objects
     */
    public static JSONArray cardsToJsonArray(List<Card> cards) {
        JSONArray result = new JSONArray();
        cards.forEach((c) -> {
            result.put(c.toMap());
        });
        return result;
    }

    /**
     * Converts the current trick state to JSON for client polling.
     *
     * @param cards the cards in the current trick
     * @param p next player to play
     * @param start trick start player
     * @return JSON string describing the current trick state
     */
    public static String currentTrickToJSON(List<Card> cards, Player p, Player start) {
        JSONObject result = new JSONObject();
        result.put("currentTrick", cardsToJsonArray(cards));
        result.put("next", p.getPlayerName());
        result.put("start", start.getPlayerName());
        System.out.println("Next: " + p.getPlayerName() + ", Start: " + start.getPlayerName());
        return result.toString();
    }

    /**
     * Parses game selection JSON into an IGame instance.
     *
     * @param json the game selection JSON
     * @return the chosen IGame or null for pass
     */
    public static IGame jsonToIGame(String json) {
        JSONObject jo = new JSONObject(json);
        String name = jo.getString("name");
        switch (name.toLowerCase()) {
            case "pass":
                return null;
            case "topdown":
                return new TopDown();
            case "bottomup":
                return new BottomUp();
            case "middle":
                return new Middle();
            case "trumps":
                String suit = jo.getString("suit");
                return new Trumps(Suit.fromChar(suit.charAt(0)));
            case "slalom":
                String start = jo.getString("start");
                return new Slalom(start);
            case "fivefour":
                start = jo.getString("start");
                System.out.println(start);
                return new FiveFour(start);
            default:
                break;
        }
        return null;
    }

    /**
     * Formats game choice data for the client.
     *
     * @param index the index of the current chooser
     * @param players the list of players
     * @param g the currently selected game, or null if no game is chosen yet
     * @param forced whether the chooser is forced to select a game
     * @return JSON string representing the game selection payload
     */
    public static String gameChoiceToJson(int index, List<Player> players, IGame g, boolean forced) {
        JSONObject jo = new JSONObject();
        if (g == null) {
            jo.put("chooser", players.get(index).getPlayerName());
            jo.put("available", availableGamesToJson(players.get(index).getTeam(), forced));
        } else {
            jo.put("game", g.getName());
            jo.put("caller", players.get(index).getPlayerName());
            if (g instanceof Trumps) {
                jo.put("suit", Suit.toString(Suit.fromIndex(g.getType())));
            }
            if (g instanceof Slalom || g instanceof FiveFour) {
                jo.put("start", g.getType() == 0 ? "Top" : "Bottom");
            }
        }
        return jo.toString();
    }

    /**
     * Builds a JSON array describing games available to the current team.
     *
     * @param t the team whose available games are listed
     * @param forced whether passing is disallowed
     * @return JSON array of available game options
     */
    private static JSONArray availableGamesToJson(Team t, boolean forced) {
        JSONArray available = new JSONArray();
        int id = 0;
        for (String game : t.getGamesAvailable()) {
            JSONObject obj = new JSONObject();
            obj.put("id", id++);
            obj.put("text", game);
            obj.put("key", game.replace(" ", ""));
            available.put(obj);
        }

        if (!forced) {
            JSONObject obj = new JSONObject();
            obj.put("id", id++);
            obj.put("text", "Pass");
            obj.put("key", "Pass");
            available.put(obj);
        }
        return available;
    }

    /**
     * Converts team score summary data to JSON.
     *
     * @param teams the list of teams
     * @return JSON string containing player names and score totals
     */
    public static String scoreToJson(List<Team> teams) {
        JSONArray scores = new JSONArray();
        for (Team t : teams) {
            JSONObject obj = new JSONObject();
            obj.put("p1", t.players.get(0).getPlayerName());
            obj.put("p2", t.players.get(1).getPlayerName());
            obj.put("score", t.getScore());
            scores.put(obj);
        }
        return scores.toString();
    }

    /**
     * Converts game-by-game team scores to JSON.
     *
     * @param teams the list of teams
     * @return JSON string containing overall game scores and team labels
     */
    public static String scoresToJson(List<Team> teams) {
        JSONObject result = new JSONObject();

        JSONArray scores = new JSONArray();
        for (String game : GameManager.GAMES) {
            JSONObject obj = new JSONObject();
            obj.put("game", game);
            obj.put("0", teams.get(0).getScore(game));
            obj.put("1", teams.get(1).getScore(game));
            scores.put(obj);
        }
        JSONArray teamsJSON = new JSONArray();
        teams.forEach((t) -> {
            teamsJSON.put(t.players.get(0).getPlayerName() + " & " + t.players.get(1).getPlayerName());
        });

        result.put("teams", teamsJSON);
        result.put("scores", scores);

        return result.toString();
    }
}
