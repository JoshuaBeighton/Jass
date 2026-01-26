package src.objs;
public enum Suit {
    HEARTS (0),
    CLUBS (1),
    DIAMONDS (2),
    SPADES (3);

    private final int index;   

    Suit(int index) {
        this.index = index;
    }

    public int index() { 
        return index; 
    }
}
