<script setup lang="ts">
// Scoreboard component
// - Fetches score data for the gameroom and renders a compact table
// - `Scores` contains an array of team metadata and an array of per-game scores
import { ref, onMounted } from 'vue'

interface Team {
  name: string
  score: number
}

interface GameScore {
  // `0` and `1` hold the raw points for the two teams; `calc0`/`calc1` are
  // computed values shown when available. `-1` denotes unavailable/loading.
  game: string
  multiplier: number
  0: number
  1: number
  calc0: number
  calc1: number
}

interface Scores {
  teams: Team[]
  scores: GameScore[]
}

const props = defineProps<{ gameroom: number }>()

// default placeholder data shown until the network request completes
const scores = ref({
  teams: [
    { name: 'Loading...', score: 0 },
    { name: 'Loading...', score: 0 },
  ],
  scores: [
    { game: 'Game 1', multiplier: 1, 0: -1, 1: -1, calc0: -1, calc1: -1 },
    { game: 'Game 2', multiplier: 2, 0: -1, 1: -1, calc0: -1, calc1: -1 },
    { game: 'Game 3', multiplier: 3, 0: -1, 1: -1, calc0: -1, calc1: -1 },
    { game: 'Game 4', multiplier: 4, 0: -1, 1: -1, calc0: -1, calc1: -1 },
  ],
})

/**
 * fetchScores
 * - Loads the current scoreboard for the gameroom from `/scores`.
 * - On success replaces `scores.value` with the server data.
 */
async function fetchScores() {
  const host = window.location.hostname
  try {
    const res = await fetch(`http://${host}:9000/scores`, {
      headers: {
        gameroom: props.gameroom.toString(),
      },
    })
    if (!res.ok) throw new Error('Network response was not OK')
    const data: Scores = await res.json()
    scores.value = data
  } catch (err) {
    console.error('Error fetching scores:', err)
  }
}

onMounted(() => {
  fetchScores()
})
</script>

<template>
  <div class="scoreboard-container">
    <hr class="smallHr" />
    <h2 class="scoreboard-heading">Scoreboard</h2>
    <div class="scoreboard-card">
      <table class="scoreboard-table">
        <thead>
          <tr>
            <th class="col-game">Game</th>
            <th class="col-game">Multiplier</th>
            <th class="col-team">{{ scores.teams[0]?.name ?? 'Loading...' }}</th>
            <th class="col-team">{{ scores.teams[1]?.name ?? 'Loading...' }}</th>
            <th class="col-team">{{ scores.teams[0]?.name ?? 'Loading...' }}</th>
            <th class="col-team">{{ scores.teams[1]?.name ?? 'Loading...' }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="obj in scores['scores']"
            :key="obj.game"
            :class="{ 'is-loading': obj.game === 'loading' }"
          >
            <td class="cell-game">{{ obj.game }}</td>
            <td class="cell-game">{{ obj.multiplier }}</td>
            <td class="cell-score" :class="{ loser: obj['0'] < obj['1'] }">
              {{ obj['0'] == -1 ? '' : obj['0'] }}
            </td>
            <td class="cell-score" :class="{ loser: obj['1'] < obj['0'] }">
              {{ obj['1'] == -1 ? '' : obj['1'] }}
            </td>
            <td class="cell-score">
              {{ obj['0'] > obj['1'] && obj.calc0 != -1 ? obj.calc0 : '' }}
            </td>
            <td class="cell-score">
              {{ obj['1'] > obj['0'] && obj.calc1 != -1 ? obj.calc1 : '' }}
            </td>
          </tr>
          <tr>
            <td class="cell-game overall" colspan="4">Overall Score</td>
            <td class="cell-game">{{ scores.teams[0]?.score }}</td>
            <td class="cell-game">{{ scores.teams[1]?.score }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.scoreboard-heading {
  width: 100%;
  text-align: left;
  margin: 20px 0 4px 0;
  font-size: 1.5rem;
  color: var(--color-heading);
}

.scoreboard-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: var(--color-text);
}

.scoreboard-card {
  width: 100%;
  border-radius: 12px;
  box-shadow:
    0 10px 25px -5px rgba(0, 0, 0, 0.3),
    0 8px 10px -6px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.scoreboard-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

th {
  padding: 14px 20px;

  border-bottom: 2px solid var(--color-border);
}

.col-game {
  max-width: 40%;
}

.col-team {
  max-width: 30%;
  text-align: center;
  color: var(--color-text);
}

tr {
  border-bottom: 1px solid var(--color-border);
  transition: background-color 0.2s ease;
}

tr:last-child {
  border-bottom: none;
}

td {
  padding: 16px 20px;
}

.cell-game {
  color: var(--color-text);
}

.cell-score {
  font-weight: 600;
  text-align: center;
  font-variant-numeric: tabular-nums;
  color: var(--color-accent);
}

.loser {
  color: var(--color-red-suit);
}

.is-loading {
  opacity: 0.5;
  pointer-events: none;
}

.overall {
  font-weight: 600;
}
</style>
