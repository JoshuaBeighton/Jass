package src.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import src.GameManager;
import src.games.BottomUp;
import src.games.Elephant;
import src.games.FiveFour;
import src.games.IGame;
import src.games.Jack9;
import src.games.Middle;
import src.games.Misere;
import src.games.Rio;
import src.games.SaintLegier;
import src.games.Slalom;
import src.games.TopDown;
import src.games.Trumps;
import src.objs.Card;
import src.objs.Player;
import src.objs.Suit;
import src.objs.Team;

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
        return result.toString();
    }

    /**
     * Converts room multipliers and assignment mapping to JSON payload for Vue SSE sync.
     */
    public static String roomStateToJSON(List<Integer> multipliers, Map<Integer, List<String>> assignments) {
        JSONObject root = new JSONObject();
        JSONArray multsArray = new JSONArray(multipliers);
        JSONObject assignmentsObj = new JSONObject();

        assignments.forEach((key, list) -> {
            JSONArray items = new JSONArray();
            for (String gameName : list) {
                JSONObject gameObj = new JSONObject();
                gameObj.put("id", gameName);
                gameObj.put("name", gameName);
                items.put(gameObj);
            }
            assignmentsObj.put(String.valueOf(key), items);
        });

        root.put("multipliers", multsArray);
        root.put("assignments", assignmentsObj);
        return root.toString();
    }

    /**
     * Updates GameManager instance state from incoming Vue POST state JSON.
     */
    public static void updateRoomStateFromJSON(GameManager manager, String json) {
        JSONObject root = new JSONObject(json);
        List<Integer> multipliers = new ArrayList<>();
        Map<Integer, List<String>> assignments = new HashMap<>();

        if (root.has("multipliers")) {
            JSONArray multArr = root.getJSONArray("multipliers");
            for (int i = 0; i < multArr.length(); i++) {
                multipliers.add(multArr.getInt(i));
            }
        }

        if (root.has("assignments")) {
            JSONObject assignObj = root.getJSONObject("assignments");
            for (String key : assignObj.keySet()) {
                int mult = Integer.parseInt(key);
                JSONArray gameArr = assignObj.getJSONArray(key);
                List<String> gamesList = new ArrayList<>();

                for (int i = 0; i < gameArr.length(); i++) {
                    JSONObject gameObj = gameArr.getJSONObject(i);
                    gamesList.add(gameObj.getString("id"));
                }
                assignments.put(mult, gamesList);
            }
        }

        manager.updateAssignments(multipliers, assignments);
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
        System.out.println(name);
        switch (name.toLowerCase()) {
            case "pass":
                return null;
            case "top down":
                return new TopDown();
            case "bottom up":
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
                return new FiveFour(start);
            case "elephant":
                return new Elephant();
            case "saint legier":
                Map<Suit, String> mapping = new HashMap<Suit, String>();
                String diamonds = jo.getString("diamonds");
                String spades = jo.getString("spades");
                String hearts = jo.getString("hearts");
                String clubs = jo.getString("clubs");
                mapping.put(Suit.DIAMONDS, diamonds);
                mapping.put(Suit.SPADES, spades);
                mapping.put(Suit.HEARTS, hearts);
                mapping.put(Suit.CLUBS, clubs);
                return new SaintLegier(mapping);
            case "jack9":
                return new Jack9();
            case "misere":
                return new Misere();
            case "rio":
                String suitColor = jo.getString("color");
                return new Rio(suitColor);
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
        } else {
            jo.put("game", g.getName());
            jo.put("caller", players.get(index).getPlayerName());
            if (g instanceof Trumps) {
                jo.put("suit", Suit.toString(Suit.fromIndex(g.getType())));
            }
            if (g instanceof Slalom || g instanceof FiveFour) {
                jo.put("start", g.getType() == 0 ? "Top" : "Bottom");
            }
            if (g instanceof SaintLegier) {
                JSONObject cross = new JSONObject();
                for (Entry<Suit, String> r : ((SaintLegier) g).getMapping().entrySet()) {
                    cross.put(Suit.toString(r.getKey()), r.getValue());
                }
                jo.put("cross", cross);
            }
        }
        return jo.toString();
    }

    // /**
    // * Builds a JSON array describing games available to the current team.
    // *
    // * @param t the team whose available games are listed
    // * @param forced whether passing is disallowed
    // * @return JSON array of available game options
    // */
    // private static JSONArray availableGamesToJson(Team t, boolean forced, Map<Integer, List<String>> assignments) {
    // JSONArray available = new JSONArray();
    // int id = 0;
    // for (int multiplier : t.getGamesAvailable()) {
    // JSONObject obj = new JSONObject();
    // obj.put("id", id++);
    // obj.put("text", new JSONArray(assignments.get(multiplier)));
    // obj.put("key", multiplier);
    // available.put(obj);
    // }

    // if (!forced) {
    // JSONObject obj = new JSONObject();
    // obj.put("id", id++);
    // obj.put("text", "Pass");
    // obj.put("key", "Pass");
    // available.put(obj);
    // }
    // return available;
    // }

    /**
     * Converts team score summary data to JSON.
     *
     * @param teams the list of teams
     * @return JSON string containing player names and score totals
     */
    public static String scoreToJson(List<Team> teams, Player next, Player winner) {
        JSONObject scoresObj = new JSONObject();
        JSONArray scores = new JSONArray();
        for (Team t : teams) {
            JSONObject obj = new JSONObject();
            obj.put("p1", t.players.get(0).getPlayerName());
            obj.put("p2", t.players.get(1).getPlayerName());
            obj.put("score", t.getScore());
            scores.put(obj);
        }
        scoresObj.put("scores", scores);
        scoresObj.put("next", next.getPlayerName());
        scoresObj.put("winner", winner.getPlayerName());
        return scoresObj.toString();
    }

    public static String scoresToJson(GameManager manager) {
        JSONObject result = new JSONObject();
        List<Team> teams = manager.getTeams();

        JSONArray scores = new JSONArray();
        int t1overall = 0;
        int t2overall = 0;

        for (int multiplier : manager.getActiveMultipliers()) {
            JSONObject obj = new JSONObject();
            obj.put("games", new JSONArray(manager.getAssignments().get(multiplier)));

            obj.put("multiplier", multiplier);
            int t1score = teams.get(0).getScore(multiplier);
            int t2score = teams.get(1).getScore(multiplier);
            obj.put("0", t1score);
            obj.put("1", t2score);

            if (t1score + t2score == -2) {
                obj.put("calc0", -1);
                obj.put("calc1", -1);
            } else {
                if (t1score > t2score) {
                    t1overall += (t1score - (t2score == -1 ? 0 : t2score)) * multiplier;
                } else if (t2score > t1score) {
                    t2overall += ((t2score - (t1score == -1 ? 0 : t1score)) * multiplier);
                }
                obj.put("calc0", t1score > t2score ? (t1score - (t2score == -1 ? 0 : t2score)) * multiplier : 0);
                obj.put("calc1", t2score > t1score ? (t2score - (t1score == -1 ? 0 : t1score)) * multiplier : 0);
            }
            scores.put(obj);
        }

        JSONArray teamsJSON = new JSONArray();

        JSONObject obj1 = new JSONObject();

        obj1.put("name", teams.get(0).players.get(0).getPlayerName() + " & " + teams.get(0).players.get(1).getPlayerName());
        obj1.put("score", t1overall);

        JSONObject obj2 = new JSONObject();
        obj2.put("name", teams.get(1).players.get(0).getPlayerName() + " & " + teams.get(1).players.get(1).getPlayerName());
        obj2.put("score", t2overall);

        teamsJSON.put(obj1);
        teamsJSON.put(obj2);

        result.put("teams", teamsJSON);
        result.put("scores", scores);

        return result.toString();
    }

    public static String gameOptionsToJson() {
        JSONArray ja = new JSONArray();
        for (String game : GameManager.GAMES) {
            ja.put(game);
        }
        return ja.toString();
    }
}
