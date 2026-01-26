package src.games;
import java.util.List;

import src.objs.Card;
import src.objs.Suit;

public class Scoring {
    public static int TDScore(List<Card> cards){
        int score = 0;
        for (Card c : cards){
            switch (c.getVal()) {
                case 14:
                    score += 11;
                    break;
                case 13:
                    score += 4;
                    break;
                case 12:
                    score += 3;
                    break;
                case 11:
                    score += 2;
                    break;
                case 10:
                    score += 10;
                    break;
                case 8:
                    score +=8;
                    break;
                default:
                    break;
            }
        }
        return score;
    }

    public static int BUScore(List<Card> cards){
        int score = 0;
        for (Card c : cards){
            switch (c.getVal()) {
                case 6:
                    score += 11;
                    break;
                case 13:
                    score += 4;
                    break;
                case 12:
                    score += 3;
                    break;
                case 11:
                    score += 2;
                    break;
                case 10:
                    score += 10;
                    break;
                case 8:
                    score +=8;
                    break;
                default:
                    break;
            }
        }
        return score;
    }

    public static int TrumpScore(List<Card> cards, Suit trumpSuit){
        int score = 0;
        for (Card c : cards){
            switch (c.getVal()) {
                case 14:
                    score += 11;
                    break;
                case 13:
                    score += 4;
                    break;
                case 12:
                    score += 3;
                    break;
                case 11:
                    if (c.getSuit() == trumpSuit){
                        score += 20;
                    }
                    break;
                case 10:
                    score += 10;
                    break;
                case 9:
                    if (c.getSuit() == trumpSuit){
                        score += 14;
                    }
                    break;
                default:
                    break;
            }
        }
        return score;
    }
}
