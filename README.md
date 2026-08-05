# Jass
Jass is a traditional Swiss card game, typically played by 4 players. To call it a *game* in of itself is a little inaccurate, in fact it is a family of games, with a wide variety of rulesets - this project is focused on a type of Jass called Coiffeur (from the french quoi faire?, or what to do?). This way of playing the game is less popular than *Schieber Jass*, but many online platforms already exist for this. Coiffeur Jass, the type I was taught to love, is much less popular; hence this project!

---
## Technologies
- Java
- Vue.js
- Typescript
- Nginx
- Github Actions

## Features
This project contains many features meant to mimic the real playing of a game of coiffeur Jass, outlined below in the rough order a user experiences them.

### Rooms
To allow for multiple games to be hosted on the same site, the game is separated into rooms. A room can be public or private, with private rooms allowing users to play with just family or friends by sharing a code, and public rooms intended for finding *someone* to play with, should the usage of the site be high enough to facilitate this.

### Game Selection
One of the defining features of Coiffeur Jass is its flexibility. Different gamemodes can be played, and combined into a table like the two examples below:
|           |   | p1 & p2 | p3 & p4 | p1 & p2     | p3 & p4     |
|-----------|---|---------|---------|-------------|-------------|
| Top-Down  | 1 | 120     | 98      | (120-98)*1  | 0           |
| Bottom-Up | 2 | 112     | 134     | 0           | (134-112)*2 |
| Joker     | 3 | 124     | 122     | (124-122)*3 | 0           |
|           |   |         | Total   | 28          | 44          |


In the above game, only two gamemodes are played, making it very quick but offering little interest in choice, or flexibility in the game. The rightmost two columns show how the final score is calculated from the raw scores of each round.

|                    |   | p1 & p2 | p3 & p4 | p1 & p2     | p3 & p4     |
|--------------------|---|---------|---------|-------------|-------------|
| Trumps             | 1 | 120     | 98      | (120-98)*1  | 0           |
| Top-Down/Bottom-Up | 2 | 112     | 134     | 0           | (134-112)*2 |
| Rio                | 3 | 124     | 122     | (124-122)*3 | 0           |
| Misere             | 4 | 76      | 90      |             | 56          |
| Joker              | 5 | 142     | 122     |             |             |
|                    |   |         | Total   | 128         | 100         |


This game of Jass will have taken a bit longer, with more options for a team to play.

Coiffeur Jass supports the creation of users' own collection of games, choosing from a current pool of 11:
1. Trumps
2. Top Down
3. Bottom Up
4. Misere
5. Rio
6. Jack9
7. Middle
8. Saint Legier
9. Slalom
10. Five-Four
11. Elephant
<img width="458" height="436" alt="image" src="https://github.com/user-attachments/assets/35fa7dd2-5da4-4743-a9fb-f99b6fbc800c" />


### Playing The Match
The users then play the game, as described further in the *Game Background* section. This includes card highlighting for what can and can't be played, as well as a running score of how both teams are doing, as well as an indication of the current ruleset, specific to the trick level (for some gamemodes, the ordering system changes trick by trick!).

### Ending The Match
At the end of the match, the winner is displayed, and each user has the option to continue in the same room or move to a different one.

---

## Points For Improvement
### AI Players
The biggest feature that would enhance this project is an AI player that can be added to a lobby. This would allow games with less than 4 humans, something that has often been a barrier to entry of even physical games. This would mostly be a backend addition, and would pose an interesting technical challenge. 

### User Accounts
Another feature that could be added to improve the user experience would be to add an account feature to the game. This would allow users to create accounts and add friends, a feature which would facilitate game management amongst friends easier: instead of having to use an external app to communicate a room code, they could simply send an invite on the website.

---

## Running The Game
The game is hosted [here](https://coiffeurjass.com).

For Linux users, the commands
```Bash
git clone https://github.com/JoshuaBeighton/Jass.git
cd Jass
./run.sh
```
Will clone the repo, and start up a test version of the code. This will run the frontend on port 5173 of your local machine, and the backend on port 9000.
Then, simply open a browser and connect to `localhost:5173` to play the game.

## Game Background
For more background on the game, see 
- https://www.pagat.com/jass/coiffeur.html#four
- https://en.wikipedia.org/wiki/Jass
- Most relevant to this implementation, a YouTube series created by my father, explaining the ruleset I was taught, and the ruleset traditional in my family https://www.youtube.com/@jasstutorialsswisscardgame8304.
