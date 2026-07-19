<script setup lang="ts">
import { ref, onMounted } from 'vue'

const scores = ref({
  teams: ['Loading...', 'Loading...'],
  scores: [
    { game: 'Game 1', 0: -1, 1: -1 },
    { game: 'Game 2', 0: -1, 1: -1 },
    { game: 'Game 3', 0: -1, 1: -1 },
    { game: 'Game 4', 0: -1, 1: -1 },
  ],
})

async function fetchScores() {
  const host = window.location.hostname
  try {
    const res = await fetch(`http://${host}:9000/scores`)
    if (!res.ok) throw new Error('Network response was not OK')
    const data = await res.json()
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
            <th class="col-team">{{ scores['teams'][0] }}</th>
            <th class="col-team">{{ scores['teams'][1] }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="obj in scores['scores']"
            :key="obj.game"
            :class="{ 'is-loading': obj.game === 'loading' }"
          >
            <td class="cell-game">{{ obj.game }}</td>
            <td class="cell-score" :class="{ 'no-score': obj[0] == -1 }">
              {{ obj[0] == -1 ? '' : obj[0] }}
            </td>
            <td class="cell-score" :class="{ 'no-score': obj[1] == -1 }">
              {{ obj[1] == -1 ? '' : obj[1] }}
            </td>
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
  width: 40%;
}

.col-team {
  width: 30%;
  text-align: center;
  color: var(--color-accent);
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
  text-align: center;
  font-variant-numeric: tabular-nums;
  color: var(--color-accent);
}

.no-score {
  color: var(--color-text-secondary);
}

.is-loading {
  opacity: 0.5;
  pointer-events: none;
}
</style>
