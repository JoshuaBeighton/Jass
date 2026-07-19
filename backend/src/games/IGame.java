package src.games;

import java.util.List;

import src.objs.Card;

public interface IGame {
    public String getName();

    public int getType();

    public int wins(List<Card> trick);

    public int score(List<Card> cards);

    public int getSuit();
}
