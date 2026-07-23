<script lang="ts" setup>
import { ref, onMounted } from 'vue'

interface Game {
  id: number
  playerCount: number
}

// Reactive state
const joinCode = ref('')
const games = ref<Game[]>([])
const isLoading = ref(true)
const error = ref<string | null>(null)

const fetchGames = async () => {
  isLoading.value = true
  error.value = null
  try {
    const host = window.location.hostname
    const response = await fetch(`http://${host}:9000/publicgameroom`)
    if (!response.ok) {
      throw new Error(`Failed to load games (${response.status})`)
    }
    const data = await response.json()
    console.log(data)
    games.value = data
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    isLoading.value = false
  }
}

const emits = defineEmits<{
  (e: 'update:selected', value: number): void
}>()

const joinGame = async (roomNo: number | undefined) => {
  console.log(roomNo)

  if (roomNo != undefined) {
    emits('update:selected', roomNo)
    return
  }
  const inp = document.getElementById('codeInput')

  if (!(inp instanceof HTMLInputElement)) {
    error.value = 'Invalid Input'
    return
  }
  roomNo = Number(inp.value)
  if (!Number.isNaN(roomNo)) {
    emits('update:selected', roomNo)
  } else {
    error.value = 'Please enter a number'
  }
}

const makeGame = async (visible: boolean) => {
  try {
    const host = window.location.hostname
    const url = `http://${host}:9000/gameroom/` + (visible ? 'public' : 'private')
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`Failed to join game (${response.status})`)
    }
    const data = await response.json()

    emits('update:selected', data)
  } catch (err) {
    error.value = (err as Error).message
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchGames()
})
</script>

<template>
  <div class="lobby-container">
    <div class="card">
      <h3 class="card-title">Join Game</h3>
      <div class="input-group">
        <input
          v-model="joinCode"
          type="text"
          placeholder="Enter room code..."
          class="input-field"
          id="codeInput"
        />
        <button
          @click="
            () => {
              joinGame(undefined)
            }
          "
          class="btn btn-primary"
        >
          Go
        </button>
      </div>
    </div>

    <div class="card">
      <h3 class="card-title">Create Game</h3>
      <div class="button-group">
        <button @click="makeGame(true)" class="btn btn-secondary">Public 🌏</button>
        <button @click="makeGame(false)" class="btn btn-secondary">Private 🔒</button>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">Available Games</h3>
        <button class="btn-refresh" :disabled="isLoading" @click="fetchGames">↻ Refresh</button>
      </div>

      <div v-if="isLoading" class="state-message">Loading games...</div>

      <div v-else-if="error" class="state-message error">
        {{ error }}
      </div>

      <div v-else-if="games.length === 0" class="state-message">
        No public games available right now.
      </div>

      <ul v-else class="game-list">
        <li v-for="game in games" :key="game.id" class="game-item">
          <div class="game-info">
            <span class="game-name">{{ game.id }}</span>
            <span class="game-players">{{ game.playerCount }}/4 players</span>
          </div>
          <button @click="joinGame(game.id)" class="btn btn-primary btn-sm">Join</button>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.lobby-container {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  max-width: 400px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.5rem;
  background-color: var(--color-background-soft);
  border-radius: 12px;
  border: 1px solid var(--color-border);
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.05),
    0 2px 4px -2px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

/* Input & Button Groups */
.input-group {
  display: flex;
  gap: 0.5rem;
}

.button-group {
  display: flex;
  gap: 0.75rem;
}

/* Input Field */
.input-field {
  flex: 1;
  padding: 0.625rem 0.875rem;
  font-size: 0.95rem;
  background-color: var(--color-background);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  outline: none;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.input-field:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

/* Buttons */
.btn {
  padding: 0.625rem 1.25rem;
  font-size: 0.95rem;
  font-weight: 500;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    transform 0.05s ease;
}

.btn:active {
  transform: scale(0.98);
}

.btn-primary {
  background-color: #6366f1;
  color: #ffffff;
}

.btn-primary:hover {
  background-color: #4f46e5;
}

.btn-secondary {
  background-color: var(--color-background);
  color: var(--color-text);
  flex: 1;
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  background-color: var(--color-background-mute);
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.85rem;
}

.btn-refresh {
  background: none;
  border: none;
  color: #6366f1;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
}

.btn-refresh:hover:not(:disabled) {
  text-decoration: underline;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Game List Styling */
.game-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.game-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.game-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.game-name {
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--color-text);
}

.game-players {
  font-size: 0.8rem;
  opacity: 0.7;
  color: var(--color-text);
}

.state-message {
  font-size: 0.9rem;
  text-align: center;
  padding: 1rem 0;
  opacity: 0.7;
  color: var(--color-text);
}

.state-message.error {
  color: #ef4444;
  opacity: 1;
}
</style>
