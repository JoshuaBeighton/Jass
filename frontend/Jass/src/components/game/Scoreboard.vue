<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import type GameMode from '@/interfaces/GameMode.ts'

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
    const res = await fetch(`http://${host}:9000/scores`, {
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
const trumps = ref(false)
const slalom = ref(false)
const fivefour = ref(false)
const saintlegier = ref(false)
const rio = ref(false)
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
  return !trumps.value && !slalom.value && !fivefour.value && !saintlegier.value
}

function resetSelectionSubStates() {
  trumps.value = false
  slalom.value = false
  fivefour.value = false
  saintlegier.value = false
  saintlegierAssignments.value = {}
}

// --- SSE Connection ---

function connectGameChoiceStream() {
  const host = window.location.hostname
  if (eventSource) {
    eventSource.close()
  }

  eventSource = new EventSource(
    `http://${host}:9000/gameChoice?name=${props.name}&lastidx=${counter}&gameroom=${props.gameroom}`,
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
  console.log(game)
  if (game.toLowerCase() === 'trumps') {
    trumps.value = true
    return
  } else if (game.toLowerCase() === 'slalom') {
    slalom.value = true
    return
  } else if (game.toLowerCase() === 'fivefour') {
    fivefour.value = true
    return
  } else if (game.toLowerCase() === 'saint legier') {
    saintlegier.value = true
    return
  } else if (game.toLowerCase() === 'rio') {
    rio.value = true
    return
  }

  const host = window.location.hostname

  let body: Record<string, any> = { name: game }
  if (game.startsWith('trumps-')) {
    body['name'] = game.split('-')[0]
    body['suit'] = game.split('-')[1]
  }
  if (game.startsWith('slalom-') || game.startsWith('fivefour-')) {
    body['name'] = game.split('-')[0]
    body['start'] = game.split('-')[1]
  }
  if (game.startsWith('rio')) {
    body['name'] = game.split('-')[0]
    body['color'] = game.split('-')[1]
  }

  const res = await fetch(`http://${host}:9000/gameChoice`, {
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

  let body: Record<string, any> = { name: 'saint legier' }
  for (const suit of suitNames) {
    body[suit.toLowerCase()] = saintlegierAssignments.value[suit]
  }

  const res = await fetch(`http://${host}:9000/gameChoice`, {
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

    <!-- Active Selection Area rendered when it is this player's turn -->
    <div v-if="isMe" class="selectArea">
      <h2>
        {{
          saintlegier
            ? 'Assign a Game to Each Suit'
            : slalom || fivefour
              ? 'Choose a Start Position'
              : trumps
                ? 'Choose a Suit'
                : 'Choose a Game'
        }}
      </h2>

      <div class="buttons">
        <button
          v-if="trumps"
          v-for="suit in ['Clubs', 'Diamonds', 'Hearts', 'Spades']"
          :key="suit"
          :class="['suit-btn', suit.toLowerCase()]"
          @click="() => sendGame('trumps-' + suit.toLowerCase())"
        >
          {{ suit }}
        </button>

        <button
          v-else-if="slalom || fivefour"
          v-for="opt in ['Top', 'Bottom']"
          :key="opt"
          :class="['suit-btn', opt.toLowerCase()]"
          @click="() => sendGame((slalom ? 'slalom-' : 'fivefour-') + opt.toLowerCase())"
        >
          {{ opt }}
        </button>

        <button
          v-if="rio"
          v-for="color in ['Red', 'Black']"
          :key="color"
          :class="['suit-btn', color.toLowerCase()]"
          @click="() => sendGame('rio-' + color.toLowerCase())"
        >
          {{ color }}
        </button>
      </div>

      <div v-if="saintlegier" class="saintlegier-assign">
        <div v-for="suit in suitNames" :key="suit" class="saintlegier-row">
          <span :class="['suit-name', suit.toLowerCase()]">{{ suit }}</span>

          <div v-if="!saintlegierAssignments[suit]" class="buttons saintlegier-options">
            <button
              v-for="opt in gameTypeOptions"
              :key="opt.key"
              :disabled="remainingCount(opt.key) <= 0"
              @click="() => assignSuit(suit, opt.key)"
            >
              {{ opt.text }}
            </button>
          </div>
          <div v-else class="saintlegier-assigned">
            <span>{{ typeLabel(saintlegierAssignments[suit]) }}</span>
            <button class="change-btn" @click="() => unassignSuit(suit)">Change</button>
          </div>
        </div>

        <button v-if="saintlegierComplete" class="confirm-btn" @click="sendSaintLegier">
          Confirm
        </button>
      </div>

      <button v-if="!(saintlegier || slalom || fivefour || trumps)" @click="sendGame('Pass')">
        Pass
      </button>
    </div>
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

.suit-btn {
  background-color: var(--color-background);
  border-color: var(--color-border);
}

.suit-btn.hearts,
.suit-btn.diamonds {
  color: var(--color-red-suit);
}

.suit-btn.hearts:hover,
.suit-btn.diamonds:hover,
.suit-btn.clubs:hover,
.suit-btn.spades:hover {
  background-color: var(--color-background-mute);
}

.suit-btn.clubs,
.suit-btn.spades {
  color: var(--color-text);
}

.saintlegier-assign {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.saintlegier-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-background);
}

.suit-name {
  font-weight: 600;
  color: var(--color-text);
}

.suit-name.hearts,
.suit-name.diamonds {
  color: var(--color-red-suit);
}

.saintlegier-options button {
  flex: 1 1 100px;
  min-width: 80px;
  font-size: 0.85rem;
  padding: 8px 10px;
}

.saintlegier-assigned {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.change-btn {
  flex: 0 0 auto;
  min-width: unset;
  padding: 6px 10px;
  font-size: 0.8rem;
}

.confirm-btn {
  width: 100%;
  background-color: var(--color-primary);
  color: var(--color-background);
  border-color: var(--color-primary);
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
