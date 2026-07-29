<script setup lang="ts">
// GameSelect component
// - Streams game-choice events from the backend so players take turns
//   selecting the game mode.
// - When it's this player's turn (`isMe`), shows UI to pick a game,
//   or additional choices (suit/start) for certain game types.

import { ref, onMounted, onBeforeUnmount } from 'vue'
import Scoreboard from './Scoreboard.vue'
import type GameMode from '@/interfaces/GameMode.ts'

// UI state
const isMe = ref(false) // is it this player's turn to choose?
const trumps = ref(false) // showing trumps suit choices
const slalom = ref(false) // showing slalom start choices
const fivefour = ref(false) // showing five-four start choices
const nextChooser = ref('') // name of the player who will choose next

// Helpers for building the list of available games
let id = 0
let counter = -1
let eventSource: EventSource | null = null

const games = ref([
  { id: id++, text: 'Top Down', key: 'topDown' },
  { id: id++, text: 'Bottom Up', key: 'bottomUp' },
  { id: id++, text: 'Middle', key: 'middle' },
  { id: id++, text: 'Trumps', key: 'trumps' },
  { id: id++, text: 'Slalom', key: 'slalom' },
  { id: id++, text: 'Five-Four', key: 'fivefour' },
  { id: id++, text: 'Elephant', key: 'elephant' },
  { id: id++, text: 'Pass', key: 'pass' },
])

// Props: player's display name and gameroom id
const props = defineProps<{
  name: string
  gameroom: number
}>()

// Emits a `update:selected` event with the chosen `GameMode` when selection completes
const emits = defineEmits<{
  (e: 'update:selected', value: GameMode): void
  (e: 'update:finished', value: string): void
}>()

/**
 * connectGameChoiceStream
 * - Opens an SSE connection to `/gameChoice` to receive turn/choice updates.
 * - When `data.chooser` is present, update who's next and available options.
 * - When the final selection is made, emit `update:selected` and close the stream.
 */
function connectGameChoiceStream() {
  const host = window.location.hostname
  if (eventSource) {
    eventSource.close()
  }

  eventSource = new EventSource(
    `http://${host}:9000/gameChoice?name=${props.name}&lastidx=${counter}&gameroom=${props.gameroom}`,
    {
      withCredentials: false,
    },
  )

  eventSource.addEventListener('game-choice', (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.chooser != undefined) {
        // new chooser rotation update
        nextChooser.value = data.chooser
        counter++
        if (counter >= 4) {
          counter = 0
        }
        if (nextChooser.value == props.name) {
          // it's this player's turn: show available game options
          isMe.value = true
          games.value = data.available
        } else {
          isMe.value = false
        }
      } else {
        // selection completed: backend returned the chosen game mode
        const gameMode: GameMode = {
          game: data.game,
          suit: data.suit,
          start: data.start,
          caller: data.caller,
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
    // on any SSE error, close the connection and rely on re-open logic elsewhere
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }
}

// Helper to decide whether to show the main game buttons
function showMainButtons() {
  return !trumps.value && !slalom.value && !fivefour.value
}

/**
 * sendGame
 * - Handles clicks from the UI to choose a game or sub-option (suit/start).
 * - For composite options (e.g., `trumps-hearts`) the code builds the
 *   request body accordingly and posts to `/gameChoice` with `gameroom` header.
 */
async function sendGame(game: string) {
  if (game.toLowerCase() == 'trumps') {
    trumps.value = true
    return
  } else if (game.toLowerCase() == 'slalom') {
    slalom.value = true
    return
  } else if (game.toLowerCase() == 'fivefour') {
    fivefour.value = true
    console.log('yo')
    return
  }

  const host = window.location.hostname

  let body: any = {}
  body['name'] = game
  if (game.startsWith('trumps-')) {
    body['name'] = game.split('-')[0]
    body['suit'] = game.split('-')[1]
  }
  if (game.startsWith('slalom-') || game.startsWith('fivefour-')) {
    body['name'] = game.split('-')[0]
    body['start'] = game.split('-')[1]
  }

  const res = await fetch(`http://${host}:9000/gameChoice`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      gameroom: props.gameroom.toString(),
    },
    body: JSON.stringify(body),
  })
  if (game == 'Pass') {
    // if player passed, re-open the stream to wait for next chooser
    connectGameChoiceStream()
  } else {
    // backend responded with final selection immediately
    const data = await res.json()
    const gameMode: GameMode = {
      game: data.game,
      suit: data.suit,
      start: data.start,
      caller: data.caller,
    }
    emits('update:selected', gameMode)
  }
}

onMounted(() => {
  connectGameChoiceStream()
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<template>
  <div class="parent">
    <hr class="smallHr" />
    <div class="gameSelect">
      <h2 v-if="!isMe" class="waitingText">
        Waiting on <span class="highlightName">{{ nextChooser }}</span>
      </h2>
      <div class="selectArea" v-if="isMe">
        <h2>
          {{
            slalom || fivefour
              ? 'Choose a Start Position'
              : trumps
                ? 'Choose a Suit'
                : 'Choose a Game'
          }}
        </h2>
        <div class="buttons">
          <button
            v-if="showMainButtons()"
            v-for="game in games"
            :key="game.id"
            @click="() => sendGame(game.key)"
          >
            {{ game.text }}
          </button>

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
            v-if="slalom || fivefour"
            v-for="opt in ['Top', 'Bottom']"
            :key="opt"
            :class="['suit-btn', opt.toLowerCase()]"
            @click="() => sendGame((slalom ? 'slalom-' : 'fivefour-') + opt.toLowerCase())"
          >
            {{ opt }}
          </button>
        </div>
      </div>
    </div>
    <Scoreboard :gameroom="props.gameroom" @update:finished="(t) => emits('update:finished', t)" />
  </div>
</template>

<style scoped>
.selectArea {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.parent {
  display: flex;
  flex-direction: column;
  gap: 24px;
  justify-content: center;
  align-items: center;
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
}

.gameSelect {
  width: 100%;
  display: flex;
  justify-content: center;
}

.waitingText {
  color: var(--color-text);
}

.highlightName {
  color: var(--color-primary);
}

.buttons {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  gap: 8px;
  justify-content: center;
}

button {
  flex: 1 1 120px;
  min-width: 100px;
  padding: 12px 16px;
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
</style>
