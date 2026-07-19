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

    public static Suit fromIndex(int i) throws IndexOutOfBoundsException{
        switch (i) {
            case 0:
                return HEARTS;
            case 1:
                return CLUBS;
            case 2:
                return DIAMONDS;
            case 3:
                return SPADES;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    public static Suit fromChar(char c) throws IndexOutOfBoundsException{
        switch (c) {
            case 'H': case 'h':
                return HEARTS;
            case 'C': case 'c':
                return CLUBS;
            case 'D': case 'd':
                return DIAMONDS;
            case 'S': case 's':
                return SPADES;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
