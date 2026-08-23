package src.games;

import java.util.List;

import src.objs.Card;

public class Joker implements IGamemode {
    private IGamemode gamemode;

    public Joker(IGamemode gamemode) {
        this.gamemode = gamemode;
    }

    public int getType() {
        return gamemode.getType();
    }

    public String getName() {
        return gamemode.getName();
    }

    public int wins(List<Card> trick, int trickNo) {
        return gamemode.wins(trick, trickNo);
    }

    public int score(List<Card> cards) {
        return gamemode.score(cards);
    }
}
