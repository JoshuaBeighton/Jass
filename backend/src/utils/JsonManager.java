package src.utils;

import java.util.List;

import src.Main;
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

    public static String teamsToJson(List<Team> teams) {
        JSONArray result = new JSONArray();
        teams.forEach((t) -> {
            JSONObject teamObj = new JSONObject();
            teamObj.put("index", t.getIndex());
            teamObj.put("score", t.getScore());
            teamObj.put("players", playersToJson(t.players));
            result.put(teamObj);
        });
        return result.toString().replace("\\", "");
    }

    public static Player JsonToPlayer(String json) {
        System.out.println(json);
        JSONObject jo = new JSONObject(json);
        String name = jo.getString("name");
        int idx = jo.getInt("team");
        return new Player(name, Main.getTeam(idx));
    }

    public static String cardsToJson(List<Card> cards) {
        JSONArray result = new JSONArray();
        cards.forEach((c) -> {
            result.put(c.toMap());
        });
        return result.toString();
    }
}
