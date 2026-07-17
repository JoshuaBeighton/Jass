<template>
  <div class="scoreboard">
    <table>
      <thead>
        <tr>
          <th>Game</th>
          <th>{{ scores['teams'][0] }}</th>
          <th>{{ scores['teams'][1] }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="obj in scores['scores']" :key="obj.game">
          <td>{{ obj.game }}</td>
          <td>{{ obj[0] == -1 ? '' : obj[0] }}</td>
          <td>{{ obj[1] == -1 ? '' : obj[1] }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
const scores = ref({
  teams: ['loading', 'loading'],
  scores: [
    {
      game: 'loading',
      0: 0,
      1: 0,
    },
    {
      game: 'loading',
      0: 0,
      1: 0,
    },
    {
      game: 'loading',
      0: 0,
      1: 0,
    },
    {
      game: 'loading',
      0: 0,
      1: 0,
    },
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

<style scoped>
.scoreboard {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
td {
  padding: 5px;
  text-align: center;
}
tr {
  border-bottom: 1px solid #ddd;
}
table,
th,
td {
  border: 1px solid black;
  border-collapse: collapse;
}
</style>
