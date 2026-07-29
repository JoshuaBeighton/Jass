// GameMode describes a chosen game configuration for a round.
// - `game`: name of the game (e.g., 'Trumps', 'Top Down')
// - `suit`: optional suit string when relevant to the game (e.g., trump suit)
// - `start`: optional start position ('top'|'bottom') for certain games
// - `caller`: player name who called/selected this game
export default interface GameMode {
  game: string
  suit: string | undefined
  start: string | undefined
  caller: string
}
