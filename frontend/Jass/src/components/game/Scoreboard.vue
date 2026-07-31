<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import type GameMode from '@/interfaces/GameMode.ts'
import SecondarySelection from './SecondarySelection.vue'

interface Team {
  name: string
  score: number
}

interface GameScore {
  games: [string]
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

// --- Props & Emits ---

const props = defineProps<{
  name: string
  gameroom: number
}>()

const emits = defineEmits<{
  (e: 'update:selected', value: GameMode): void
  (e: 'update:finished', value: string): void
}>()

// --- Scoreboard State ---

const scores = ref<Scores>({
  teams: [
    { name: 'Loading...', score: 0 },
    { name: 'Loading...', score: 0 },
  ],
  scores: [],
})

async function fetchScores() {
  const host = window.location.hostname
  try {
    const res = await fetch(`http://${host}:9000/multipliers`, {
      headers: {
        gameroom: props.gameroom.toString(),
      },
    })
    if (!res.ok) throw new Error('Network response was not OK')
    const data: Scores = await res.json()
    scores.value = data

    let seenNegative = false
    scores.value.scores.forEach((element) => {
      if (element['0'] === -1 || element['1'] === -1) {
        seenNegative = true
      }
    })
    if (!seenNegative) {
      emits(
        'update:finished',
        (scores.value.teams[0]?.score ?? -1) > (scores.value.teams[1]?.score ?? -1)
          ? (scores.value.teams[0]?.name ?? 'error')
          : (scores.value.teams[1]?.name ?? 'error'),
      )
    }
  } catch (err) {
    console.error('Error fetching scores:', err)
  }
}

// --- Game Selection & SSE State ---

const isMe = ref(false)

const secondaryChoice = ref('')

const nextChooser = ref('')

let gameIdCounter = 0
let counter = -1
let eventSource: EventSource | null = null

const games = ref([
  { id: gameIdCounter++, text: 'Top Down', key: 'Top Down' },
  { id: gameIdCounter++, text: 'Bottom Up', key: 'Bottom Up' },
  { id: gameIdCounter++, text: 'Middle', key: 'Middle' },
  { id: gameIdCounter++, text: 'Trumps', key: 'Trumps' },
  { id: gameIdCounter++, text: 'Slalom', key: 'Slalom' },
  { id: gameIdCounter++, text: 'Five-Four', key: 'Fivefour' },
  { id: gameIdCounter++, text: 'Elephant', key: 'Elephant' },
  { id: gameIdCounter++, text: 'Saint Legier', key: 'Saint Legier' },
  { id: gameIdCounter++, text: 'Pass', key: 'pass' },
])

// --- Saint-Legier Sub-State & Helpers ---

const suitNames = ['Clubs', 'Diamonds', 'Hearts', 'Spades']
const gameTypeOptions: { key: string; text: string; max: number }[] = [
  { key: 'middle', text: 'Middle', max: 2 },
  { key: 'Top Down', text: 'Top Down', max: 1 },
  { key: 'Bottom Up', text: 'Bottom Up', max: 1 },
]
const saintlegierAssignments = ref<Record<string, string>>({})

function remainingCount(typeKey: string) {
  const max = gameTypeOptions.find((o) => o.key === typeKey)?.max ?? 0
  const used = Object.values(saintlegierAssignments.value).filter((v) => v === typeKey).length
  return max - used
}

const saintlegierComplete = computed(
  () => Object.keys(saintlegierAssignments.value).length === suitNames.length,
)

function assignSuit(suit: string, typeKey: string) {
  if (remainingCount(typeKey) <= 0) return
  saintlegierAssignments.value = { ...saintlegierAssignments.value, [suit]: typeKey }
}

function unassignSuit(suit: string) {
  const updated = { ...saintlegierAssignments.value }
  delete updated[suit]
  saintlegierAssignments.value = updated
}

function typeLabel(typeKey: string) {
  return gameTypeOptions.find((o) => o.key === typeKey)?.text ?? typeKey
}

function showMainButtons() {
  return secondaryChoice.value == ''
}

function resetSelectionSubStates() {
  secondaryChoice.value = ''
  saintlegierAssignments.value = {}
}

// --- SSE Connection ---

function connectGameChoiceStream() {
  const host = window.location.hostname
  if (eventSource) {
    eventSource.close()
  }

  eventSource = new EventSource(
    `http://${host}:9000/gamemodeChoice?name=${props.name}&lastidx=${counter}&gameroom=${props.gameroom}`,
    { withCredentials: false },
  )

  eventSource.addEventListener('game-choice', (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.chooser !== undefined) {
        nextChooser.value = data.chooser
        counter++
        if (counter >= 4) {
          counter = 0
        }
        if (nextChooser.value === props.name) {
          console.log('SSE available games received:', data.available)
          isMe.value = true
          games.value = data.available
        } else {
          isMe.value = false
          resetSelectionSubStates()
        }
      } else {
        const gameMode: GameMode = {
          game: data.game,
          suit: data.suit,
          start: data.start,
          caller: data.caller,
          cross: data.cross,
        }
        emits('update:selected', gameMode)
        if (eventSource) {
          eventSource.close()
          eventSource = null
        }
      }
    } catch (err) {
      console.error('Error parsing game choice stream:', err)
    }
  })

  eventSource.onerror = () => {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }
}

// --- Game Actions ---

async function sendGame(game: string) {
  if (['trumps', 'slalom', 'fivefour', 'saint legier', 'rio'].includes(game.toLowerCase())) {
    secondaryChoice.value = game.toLowerCase()
    console.log(secondaryChoice.value)
    return
  }

  const host = window.location.hostname

  let body: Record<string, any> = { gamemode: game }
  if (game.startsWith('trumps-')) {
    body['gamemode'] = game.split('-')[0]
    body['suit'] = game.split('-')[1]
  }
  if (game.startsWith('slalom-') || game.startsWith('fivefour-')) {
    body['gamemode'] = game.split('-')[0]
    body['start'] = game.split('-')[1]
  }
  if (game.startsWith('rio')) {
    body['gamemode'] = game.split('-')[0]
    body['color'] = game.split('-')[1]
  }

  const res = await fetch(`http://${host}:9000/gamemodeChoice`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      gameroom: props.gameroom.toString(),
    },
    body: JSON.stringify(body),
  })

  if (game === 'Pass') {
    resetSelectionSubStates()
    connectGameChoiceStream()
  } else {
    const data = await res.json()
    const gameMode: GameMode = {
      game: data.game,
      suit: data.suit,
      start: data.start,
      caller: data.caller,
      cross: data.cross,
    }
    emits('update:selected', gameMode)
  }
}

async function sendSaintLegier() {
  if (!saintlegierComplete.value) return

  const host = window.location.hostname

  let body: Record<string, any> = { gamemode: 'saint legier' }
  for (const suit of suitNames) {
    body[suit.toLowerCase()] = saintlegierAssignments.value[suit]
  }

  const res = await fetch(`http://${host}:9000/gamemodeChoice`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      gameroom: props.gameroom.toString(),
    },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  const gameMode: GameMode = {
    game: data.game,
    suit: data.suit,
    start: data.start,
    caller: data.caller,
    cross: data.cross,
  }
  emits('update:selected', gameMode)
}

// Lifecycle
onMounted(() => {
  fetchScores()
  connectGameChoiceStream()
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<template>
  <div class="scoreboard-container">
    <hr class="smallHr" />

    <div class="header-status">
      <h2 class="scoreboard-heading">Scoreboard</h2>
      <h2 v-if="!isMe && nextChooser">
        Waiting on <span class="highlightName">{{ nextChooser }}</span>
      </h2>
      <h2 v-else>Select A Game</h2>
    </div>

    <!-- Scoreboard Card Table -->
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
            :key="obj.games.join(', ')"
            :class="{ 'is-loading': obj.games.join(', ') === 'loading' }"
          >
            <td class="cell-game">{{ obj.games.join(', ') }}</td>
            <td class="cell-game">{{ obj.multiplier }}</td>
            <td class="cell-score" :class="{ loser: obj['0'] < obj['1'] }">
              <p v-if="obj['0'] != -1">{{ obj['0'] }}</p>
              <button
                v-for="game in obj.games"
                @click="sendGame(game)"
                v-else-if="isMe && scores.teams[0]?.name.includes(props.name)"
              >
                Play {{ game }}
              </button>
            </td>
            <td class="cell-score" :class="{ loser: obj['1'] < obj['0'] }">
              <p v-if="obj['1'] != -1">{{ obj['1'] }}</p>
              <button
                v-for="game in obj.games"
                @click="sendGame(game)"
                v-else-if="isMe && scores.teams[1]?.name.includes(props.name)"
              >
                Play {{ game }}
              </button>
            </td>
            <td class="cell-score">
              {{ obj['0'] > obj['1'] && obj.calc0 !== -1 ? obj.calc0 : '' }}
            </td>
            <td class="cell-score">
              {{ obj['1'] > obj['0'] && obj.calc1 !== -1 ? obj.calc1 : '' }}
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
    <SecondarySelection v-if="isMe" :gameroom="gameroom" :name="secondaryChoice" />
  </div>
</template>

<style scoped>
.scoreboard-container {
  background-color: var(--color-background);
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 800px;
  margin: 8px auto;
  padding: 8px;
  color: var(--color-text);
  gap: 8px;
}

.header-status {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.scoreboard-heading {
  margin: 0;
  font-size: 1.5rem;
  color: var(--color-heading);
}

.waitingText {
  margin: 0;
  font-size: 1rem;
  color: var(--color-text);
}

.highlightName {
  color: var(--color-primary);
  font-weight: 600;
}

.selectArea {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  margin: 8px;
  width: 100%;
  padding: 8px;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background-mute, rgba(0, 0, 0, 0.02));
}

.selectArea h2 {
  margin: 0;
  font-size: 1.2rem;
  color: var(--color-heading);
}

.buttons {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  gap: 4px;
  justify-content: center;
}

button {
  width: 100%;
  flex: 1 1 50px;
  min-width: 100px;
  padding: 4px 4px;
  font-size: 0.95rem;
  color: var(--color-text);
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

button:hover {
  background-color: var(--color-background-mute);
  border-color: var(--color-border-hover);
  transform: translateY(-1px);
}

button:active {
  transform: translateY(0);
}

button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.change-btn {
  width: fit-content;
  padding: 6px 10px;
  font-size: 0.8rem;
}

.confirm-btn {
  width: 100%;
  background-color: var(--color-background);
  color: var(--color-text);
  border-color: var(--color-background);
  border: 2px solid var(--color-border);
  font-weight: 600;
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
