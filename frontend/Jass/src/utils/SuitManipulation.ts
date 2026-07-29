// SuitManipulation helpers
// - Convert backend card strings to symbols and vice versa
// - Provide small utilities used by Card/Deck components

export function toSym(num: String) {
  // Convert numeric-prefixed card strings to short symbol form
  // e.g. '14♦' -> 'A♦', '11♠' -> 'J♠'
  const firstTwo = num.substring(0, 2)
  switch (firstTwo) {
    case '14':
      return 'A' + num.charAt(num.length - 1)
    case '13':
      return 'K' + num.charAt(num.length - 1)
    case '12':
      return 'Q' + num.charAt(num.length - 1)
    case '11':
      return 'J' + num.charAt(num.length - 1)
    default:
      return num.toString()
  }
}

export function replaceCardSuits(input: string): string {
  // Replace unicode suit symbols with single-letter codes expected by backend
  const suitMap: { [key: string]: string } = {
    '♠': 'S',
    '♥': 'H',
    '♦': 'D',
    '♣': 'C',
  }

  return input.replace(/[\u2660\u2665\u2666\u2663]/g, (match) => suitMap[match] || match)
}

export function isRed(cardText: String): boolean {
  // True for hearts and diamonds (red suits)
  return cardText.endsWith('♦') || cardText.endsWith('♥')
}

export function concatCard(card: any) {
  // Build a textual representation from a Card-like object and a suit symbol
  return card.number + suitToUnicode(card.suit)
}

export function suitToUnicode(inp: string) {
  // Map suit names (case-insensitive) to unicode symbols
  switch (inp.toUpperCase()) {
    case 'DIAMONDS':
      return '♦'
    case 'HEARTS':
      return '♥'
    case 'SPADES':
      return '♠'
    case 'CLUBS':
      return '♣'
    default:
      return '?'
  }
}
