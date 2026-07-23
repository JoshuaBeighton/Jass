package src.objs;

/**
 * Represents the four suits used in Jass and standard card games.
 *
 * Each suit is associated with an integer index that can be used for serialization and game logic comparisons.
 */
public enum Suit {
    HEARTS(0), CLUBS(1), DIAMONDS(2), SPADES(3);

    /** Integer index of the suit, used for compact representation. */
    private final int index;

    Suit(int index) {
        this.index = index;
    }

    /**
     * Returns the numeric index assigned to this suit.
     *
     * @return suit index
     */
    public int index() {
        return index;
    }

    /**
     * Converts an index to its corresponding Suit.
     *
     * @param i the suit index
     * @return the matching Suit
     * @throws IndexOutOfBoundsException if the index is not 0-3
     */
    public static Suit fromIndex(int i) throws IndexOutOfBoundsException {
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

    /**
     * Converts a character to its corresponding Suit.
     *
     * Acceptable inputs are H/h, C/c, D/d, and S/s.
     *
     * @param c the suit character
     * @return the matching Suit
     * @throws IndexOutOfBoundsException if the character is not valid
     */
    public static Suit fromChar(char c) throws IndexOutOfBoundsException {
        switch (c) {
            case 'H':
            case 'h':
                return HEARTS;
            case 'C':
            case 'c':
                return CLUBS;
            case 'D':
            case 'd':
                return DIAMONDS;
            case 'S':
            case 's':
                return SPADES;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Returns the human-readable name of the specified suit.
     *
     * @param s the suit to stringify
     * @return the suit name
     */
    public static String toString(Suit s) {
        switch (s) {
            case HEARTS:
                return "Hearts";
            case CLUBS:
                return "Clubs";
            case DIAMONDS:
                return "Diamonds";
            case SPADES:
                return "Spades";
            default:
                return null;
        }
    }
}
