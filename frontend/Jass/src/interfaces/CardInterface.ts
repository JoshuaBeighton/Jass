// CardInterface describes a playing card used by the frontend.
// - `suit` is a string like 'Hearts'|'Spades' (converted elsewhere to symbols)
// - `value` is the numeric rank (e.g., 11 for J, 12 for Q, 14 for A)
export default interface Card {
  suit: string
  value: number
}
