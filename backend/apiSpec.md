# API Specification

## TeamInfo

Endpoint: `/teamInfo`

Methods: GET

#### GET

Returns: The current teams that are playing the game in JSON. Includes the index of the team, the team's score and the players in that team.

## Players

Endpoint: `/players`

Methods: GET, POST

#### GET

Returns a JSON array of the players currently in the game.

#### POST

Fields: Name (String), Team (Int, 0 / 1)

## Hand

Endpoint: `hand/{playerName}`

Methods: GET

#### GET

Returns: The hand of the current player.

## Game Choice

Endpoint: `/gameChoice`

Methods: GET, POST

#### GET

Returns: which game was most recently chosen, or JSON String containing "your choice" if player requesting it must choose.

#### POST

Fields: Type (String), Modifiers (Int)
