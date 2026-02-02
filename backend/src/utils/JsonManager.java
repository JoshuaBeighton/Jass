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
        JSONArray result = new JSONArray();
        cards.forEach((c) -> {
            result.put(c.toMap());
        });
        return result.toString();
    }

    public static IGame jsonToIGame(String json) {
        JSONObject jo = new JSONObject(json);
        String name = jo.getString("name");
        int modifier = jo.getInt("modifier");
        switch (name) {
            case "pass":
                return null;
            case "topDown":
                return new TopDown();
            case "bottomUp":
                return new BottomUp();
            case "middle":
                return new Middle();
            case "trumps":
                return new Trumps(Suit.fromIndex(modifier));
            default:
                break;
        }
        return null;
    }

    public static String gameChoiceToJson(String player, int index, List<Player> players, IGame g) {
        if (players.get(index).getPlayerName().equals(player)) {
            return "YOURTURN";
        }
        JSONObject jo = new JSONObject();
        jo.put("game", g.getName());
        jo.put("type", g.getType());
        return jo.toString();
    }
}
