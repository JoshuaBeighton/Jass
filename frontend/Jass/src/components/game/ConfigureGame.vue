<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Game {
  id?: number | string
  name: string
}

const included = ref<any[]>([
  { id: 1, name: 'Trumps' },
  { id: 2, name: 'Top Down' },
  { id: 3, name: 'Bottom Up' },
])

const excluded = ref<string[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)

const getNextIndex = () => {
  var next = 0
  included.value.forEach((t) => {
    if (t.id >= next) {
      next = t.id + 1
    }
  })
}

// 1. Move items up or down in the array
const moveUp = (index: number) => {
  if (index > 0) {
    const item = included.value.splice(index, 1)[0]
    included.value.splice(index - 1, 0, item)
  }
}

const moveDown = (index: number) => {
  if (index < included.value.length - 1) {
    const item = included.value.splice(index, 1)[0]
    included.value.splice(index + 1, 0, item)
  }
}

// 2. Fetch excluded games from an API endpoint
const fetchExcludedGames = async () => {
  isLoading.value = true
  error.value = null
  try {
    const host = window.location.hostname
    const response = await fetch(`http://${host}:9000/gameOptions`)
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    excluded.value = await response.json()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Failed to load games'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchExcludedGames()
})
</script>

<template>
  <div>
    <h2>Select Games</h2>

    <!-- Included Games Table -->
    <table>
      <thead>
        <tr>
          <th class="col-game">Multiplier</th>
          <th class="col-game">Game</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(game, index) in included.filter((t) => t != undefined)" :key="game.id || index">
          <!-- Multiplier dynamically bound to position (1-indexed) -->
          <td>{{ index + 1 }}x</td>
          <td>{{ game.name }}</td>
          <td>
            <button @click="moveUp(index)" :disabled="index === 0" aria-label="Move Up">▲</button>
            <button
              @click="moveDown(index)"
              :disabled="index === included.length - 1"
              aria-label="Move Down"
            >
              ▼
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Excluded Games Section -->
    <div class="excluded-section">
      <h3>Excluded Games</h3>
      <p v-if="isLoading">Loading excluded games...</p>
      <p v-else-if="error" class="error">{{ error }}</p>
      <ul v-else-if="excluded.length">
        <li
          v-for="game in excluded"
          :key="game"
          @click="
            () => {
              included.push({ id: getNextIndex(), name: game })
            }
          "
        >
          {{ game }}
        </li>
      </ul>
      <p v-else>No excluded games found.</p>
    </div>
  </div>
</template>

<style scoped>
table {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 1.5rem;
}

th,
td {
  border: 1px solid #ccc;
  padding: 8px 12px;
  text-align: left;
}

button {
  margin-right: 4px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.error {
  color: red;
}
li {
  color: var(--colot-text);
}
</style>
