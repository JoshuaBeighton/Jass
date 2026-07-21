export function toSym(num: String) {
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
  const suitMap: { [key: string]: string } = {
    '♠': 'S',
    '♥': 'H',
    '♦': 'D',
    '♣': 'C',
  }

  return input.replace(/[\u2660\u2665\u2666\u2663]/g, (match) => suitMap[match] || match)
}

export function isRed(cardText: String): boolean {
  return cardText.endsWith('♦') || cardText.endsWith('♥')
}

export function concatCard(card: any) {
  return card.number + suitToUnicode(card.suit)
}

export function suitToUnicode(inp: string) {
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
