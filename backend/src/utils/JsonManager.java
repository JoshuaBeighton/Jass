package src.utils;

import java.util.List;

import src.Main;
import src.games.BottomUp;
import src.games.IGame;
import src.games.Middle;
import src.games.TopDown;
import src.games.Trumps;
import src.objs.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonManager {
    public static String playersToJson(List<Player> players) {
        JSONArray result = new JSONArray();
        players.forEach((p) -> {
            result.put(p.toMap());
        });
        return result.toString();
    }

    private static JSONArray playersToJsonArray(List<Player> players) {
        JSONArray result = new JSONArray();
        players.forEach((p) -> {
            result.put(p.toMap());
        });
        return result;
    }

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

    public static Player JsonToPlayer(String json) {
        JSONObject jo = new JSONObject(json);
        String name = jo.getString("name");
        int idx = jo.getInt("idx");
        return new Player(name, Main.getTeam(idx));
    }

    public static String cardsToJson(List<Card> cards) {
        return cardsToJsonArray(cards).toString();
    }

    public static JSONArray cardsToJsonArray(List<Card> cards) {
        JSONArray result = new JSONArray();
        cards.forEach((c) -> {
            result.put(c.toMap());
        });
        return result;
    }

    public static String currentTrickToJSON(List<Card> cards, Player p, Player start) {
        JSONObject result = new JSONObject();
        result.put("currentTrick", cardsToJson(cards));
        result.put("next", p.getPlayerName());
        result.put("start", p.getPlayerName());
        return result.toString();
    }

    public static IGame jsonToIGame(String json) {
        JSONObject jo = new JSONObject(json);
        String name = jo.getString("name");
        switch (name) {
            case "Pass":
                return null;
            case "TopDown":
                return new TopDown();
            case "BottomUp":
                return new BottomUp();
            case "Middle":
                return new Middle();
            case "Trumps":
                int modifier = jo.getInt("modifier");
                return new Trumps(Suit.fromIndex(modifier));
            default:
                break;
        }
        return null;
    }

    public static String gameChoiceToJson(String player, int index, List<Player> players, IGame g, boolean forced) {
        JSONObject jo = new JSONObject();
        if (g == null) {
            jo.put("chooser", players.get(index).getPlayerName());
            jo.put("available", availableGamesToJson(players.get(index).getTeam(), forced));

        } else {
            jo.put("game", g.getName());
            jo.put("type", g.getType());
        }

        return jo.toString();
    }

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
}
