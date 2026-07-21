<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Scoreboard from './Scoreboard.vue'
import type GameMode from '@/interfaces/GameMode.ts'

const isMe = ref(false)
const trumps = ref(false)
const nextChooser = ref('')

let id = 0
let counter = -1

const games = ref([
  { id: id++, text: 'Top Down', key: 'topDown' },
  { id: id++, text: 'Bottom Up', key: 'bottomUp' },
  { id: id++, text: 'Middle', key: 'middle' },
  { id: id++, text: 'Trumps', key: 'trumps' },
  { id: id++, text: 'Pass', key: 'pass' },
])

const props = defineProps<{
  name: string
}>()

const emits = defineEmits<{
  (e: 'update:selected', value: GameMode): void
}>()

async function fetchNextPlayer() {
  const host = window.location.hostname

  try {
    let keepSending = true
    const res = await fetch(`http://${host}:9000/gameChoice?name=${props.name}&lastidx=${counter}`)
    if (!res.ok) throw new Error('Network response was not OK')
    const data = await res.json()
    if (data.chooser != undefined) {
      nextChooser.value = data.chooser
      counter++
      if (counter >= 4) {
        counter = 0
      }
      if (nextChooser.value == props.name) {
        console.log("It's me!")
        isMe.value = true
        games.value = data.available
      } else {
        isMe.value = false
      }
    } else {
      keepSending = false
      const gameMode: GameMode = {
        game: data.game,
        suit: data.suit,
        start: data.start,
        caller: data.caller,
      }
      emits('update:selected', gameMode)
    }
    if (!isMe.value && keepSending) {
      fetchNextPlayer()
    }
  } catch (err) {
    console.error('Error fetching teams:', err)
  }
}

function showMainButtons() {
  return !trumps.value
}

async function sendGame(game: string) {
  if (game.toLowerCase() == 'trumps') {
    trumps.value = true
    return
  }

  const host = window.location.hostname

  let body: any = {}
  body['name'] = game
  if (game.startsWith('trumps-')) {
    body['name'] = game.split('-')[0]
    body['suit'] = game.split('-')[1]
  }

  const res = await fetch(`http://${host}:9000/gameChoice`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })
  if (game == 'Pass') {
    fetchNextPlayer()
  } else {
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
  fetchNextPlayer()
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
        <h2>{{ trumps ? 'Choose a Suit' : 'Choose a Game' }}</h2>
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
        </div>
      </div>
    </div>
    <Scoreboard />
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
