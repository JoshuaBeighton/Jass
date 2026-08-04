<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import type GameMode from '@/interfaces/GameMode.ts'

interface Team {
  name: string
  score: number
}

// --- Props & Emits ---

const props = defineProps<{
  name: string
  gameroom: number
}>()

const emits = defineEmits<{
  (e: 'update:finished', value: GameMode): void
}>()

// --- Scoreboard State ---

// --- Game Selection & SSE State ---
let eventSource: EventSource | null = null

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

// --- Game Actions ---

async function sendGame(game: string) {
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

  const apiUrl = import.meta.env.VITE_API_URL
  const res = await fetch(`${apiUrl}/gamemodeChoice`, {
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
  emits('update:finished', gameMode)
}

async function sendSaintLegier() {
  if (!saintlegierComplete.value) return

  let body: Record<string, any> = { gamemode: 'saint legier' }
  for (const suit of suitNames) {
    body[suit.toLowerCase()] = saintlegierAssignments.value[suit]
  }

  const apiUrl = import.meta.env.VITE_API_URL

  const res = await fetch(`${apiUrl}/gamemodeChoice`, {
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
  emits('update:finished', gameMode)
}
</script>

<template>
  <div class="scoreboard-container">
    <!-- Active Selection Area rendered when it is this player's turn -->
    <div class="buttons">
      <button
        v-if="props.name == 'trumps'"
        v-for="suit in ['Clubs', 'Diamonds', 'Hearts', 'Spades']"
        :key="suit"
        :class="['suit-btn', suit.toLowerCase()]"
        @click="() => sendGame('trumps-' + suit.toLowerCase())"
      >
        {{ suit }}
      </button>

      <button
        v-else-if="props.name === 'slalom' || props.name === 'fivefour'"
        v-for="opt in ['Top', 'Bottom']"
        :key="opt"
        :class="['suit-btn', opt.toLowerCase()]"
        @click="() => sendGame(props.name + '-' + opt.toLowerCase())"
      >
        {{ opt }}
      </button>

      <button
        v-if="props.name === 'rio'"
        v-for="color in ['Red', 'Black']"
        :key="color"
        :class="['suit-btn', color.toLowerCase()]"
        @click="() => sendGame('rio-' + color.toLowerCase())"
      >
        {{ color }}
      </button>
    </div>

    <div v-if="props.name === 'saint legier'" class="saintlegier-assign">
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

    <button v-if="props.name === ''" @click="sendGame('Pass')">Pass</button>
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
  border: 2px solid var(--color-border);
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
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
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
</style>
