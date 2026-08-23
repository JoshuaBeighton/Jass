<script setup lang="ts">
// App root component
// - Orchestrates the main pages: Room chooser, login, game selection, mat (game view), and deck
import Deck from './components/game/Deck.vue'
import Mat from './components/game/Mat.vue'
import LoginCard from './components/login/LoginCard.vue'
import { onMounted, ref, watch } from 'vue'
import type GameMode from './interfaces/GameMode.ts'
import RoomChooser from './components/login/RoomChooser.vue'
import WinScreen from './components/game/WinScreen.vue'
import Scoreboard from './components/game/Scoreboard.vue'
import ConfigureMultipliers from './components/game/ConfigureMultipliers.vue'
import { getStoredValue, setStoredValue } from './utils/storage'

const STORAGE_KEYS = {
  gameroom: 'jass.gameroom',
  name: 'jass.name',
  currentGame: 'jass.currentGame',
  loginVisible: 'jass.loginVisible',
  configureVisible: 'jass.configureVisible',
  selectVisible: 'jass.selectVisible',
  matVisible: 'jass.matVisible',
  deckVisible: 'jass.deckVisible',
  matchOverVisible: 'jass.matchOverVisible',
  winners: 'jass.winners',
  isMe: 'jass.isMe',
} as const

// Current gameroom (default 1001 for local testing)
const gameroomNumber = ref(getStoredValue<number>(STORAGE_KEYS.gameroom, -1))

// Navigation helpers triggered by child components
function removeLogin() {
  login.value = false
  configure.value = true
  setStoredValue(STORAGE_KEYS.loginVisible, false)
  setStoredValue(STORAGE_KEYS.configureVisible, true)
}

function setName(nameInput: string) {
  name.value = nameInput
  setStoredValue(STORAGE_KEYS.name, name.value)
}

async function selectRoom(room: number) {
  gameroomNumber.value = room
  setStoredValue(STORAGE_KEYS.gameroom, room)
  await refreshCurrentGame()
}

function gameChosen(gameSelected: GameMode) {
  currentGame.value = gameSelected
  select.value = false
  mat.value = true
  setStoredValue(STORAGE_KEYS.selectVisible, false)
  setStoredValue(STORAGE_KEYS.matVisible, true)
}

const name = ref(getStoredValue<string>(STORAGE_KEYS.name, ''))

const defaultGame: GameMode = {
  game: '',
  isJoker: false,
  suit: undefined,
  start: undefined,
  caller: '',
  cross: undefined,
}

const currentGame = ref<GameMode>(defaultGame)
const isMe = ref(getStoredValue<boolean>(STORAGE_KEYS.isMe, false))
const deckRef = ref<InstanceType<typeof Deck>>()
const winners = ref(getStoredValue<string>(STORAGE_KEYS.winners, ''))

// Which panes are visible
const login = ref(getStoredValue<boolean>(STORAGE_KEYS.loginVisible, true))
const configure = ref(getStoredValue<boolean>(STORAGE_KEYS.configureVisible, false))
const select = ref(getStoredValue<boolean>(STORAGE_KEYS.selectVisible, false))
const mat = ref(getStoredValue<boolean>(STORAGE_KEYS.matVisible, false))
const deck = ref(getStoredValue<boolean>(STORAGE_KEYS.deckVisible, false))
const matchOver = ref(getStoredValue<boolean>(STORAGE_KEYS.matchOverVisible, false))

// Called when a game finishes to return to selection and refresh hand
function gameFinished(finished: boolean) {
  if (!finished) return

  mat.value = false
  select.value = true
  setStoredValue(STORAGE_KEYS.matVisible, false)
  setStoredValue(STORAGE_KEYS.selectVisible, true)
  deckRef.value?.fetchHand()
}

function matchFinished(finished: string) {
  login.value = false
  select.value = false
  mat.value = false
  deck.value = false
  matchOver.value = true
  winners.value = finished
  setStoredValue(STORAGE_KEYS.matchOverVisible, true)
  setStoredValue(STORAGE_KEYS.winners, finished)
}

async function playAgain(sameRoom: boolean) {
  const apiUrl = import.meta.env.VITE_API_URL
  await fetch(`${apiUrl}/resetMatch`, {
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain',
      // backend expects gameroom id in a `Gameroom` header
      Gameroom: gameroomNumber.value.toString(),
    },
  })

  if (!sameRoom) {
    gameroomNumber.value = -1
    setStoredValue(STORAGE_KEYS.gameroom, -1)
    login.value = true
    setStoredValue(STORAGE_KEYS.loginVisible, true)
  } else {
    login.value = true
    setStoredValue(STORAGE_KEYS.loginVisible, true)
  }
  matchOver.value = false
  setStoredValue(STORAGE_KEYS.matchOverVisible, false)
}

function onConfigEnd() {
  configure.value = false
  select.value = true
  deck.value = true
  setStoredValue(STORAGE_KEYS.configureVisible, false)
  setStoredValue(STORAGE_KEYS.selectVisible, true)
  setStoredValue(STORAGE_KEYS.deckVisible, true)
}

async function refreshCurrentGame() {
  if (gameroomNumber.value === -1) {
    currentGame.value = defaultGame
    return
  }

  const apiUrl = import.meta.env.VITE_API_URL
  try {
    const res = await fetch(`${apiUrl}/gameState`, {
      headers: {
        gameroom: gameroomNumber.value.toString(),
      },
    })
    if (!res.ok) throw new Error('Network response was not OK')

    const data = await res.json()

    currentGame.value = {
      game: data.game ?? '',
      suit: data.suit ?? undefined,
      start: data.start ?? undefined,
      caller: data.caller ?? '',
      cross: data.cross ?? undefined,
      isJoker: data.isJoker ?? false,
    }
    console.log('Fetched current game state:', currentGame.value)
    if (currentGame.value.game === '') {
      console.log('No game selected, showing selection screen')
      if (mat.value) {
        mat.value = false
        select.value = true
        setStoredValue(STORAGE_KEYS.matVisible, false)
        setStoredValue(STORAGE_KEYS.selectVisible, true)
      }
    } else {
      if (mat.value) {
        select.value = false
        mat.value = true
        setStoredValue(STORAGE_KEYS.selectVisible, false)
        setStoredValue(STORAGE_KEYS.matVisible, true)
      }
    }
  } catch (err) {
    console.error('Error fetching current game state:', err)
    currentGame.value = defaultGame
  }
}

function persistState() {
  setStoredValue(STORAGE_KEYS.gameroom, gameroomNumber.value)
  setStoredValue(STORAGE_KEYS.name, name.value)
  setStoredValue(STORAGE_KEYS.loginVisible, login.value)
  setStoredValue(STORAGE_KEYS.configureVisible, configure.value)
  setStoredValue(STORAGE_KEYS.selectVisible, select.value)
  setStoredValue(STORAGE_KEYS.matVisible, mat.value)
  setStoredValue(STORAGE_KEYS.deckVisible, deck.value)
  setStoredValue(STORAGE_KEYS.matchOverVisible, matchOver.value)
  setStoredValue(STORAGE_KEYS.winners, winners.value)
  setStoredValue(STORAGE_KEYS.isMe, isMe.value)
}

watch(
  [gameroomNumber, name, login, configure, select, mat, deck, matchOver, winners, isMe],
  persistState,
  { deep: true },
)

onMounted(() => {
  // Fetch the current game state when the component is mounted
  refreshCurrentGame()
})
</script>

<template>
  <ConfigureMultipliers
    v-if="configure"
    :room-id="gameroomNumber.toString()"
    @confirm="onConfigEnd"
  />

  <RoomChooser v-if="gameroomNumber == -1" @update:selected="selectRoom" />
  <div v-else>
    <LoginCard
      @update:name="setName"
      @update:ready="removeLogin"
      v-if="login"
      :gameroom="gameroomNumber"
      :name="name"
    ></LoginCard>
    <Mat
      :gameroom="gameroomNumber"
      :game="currentGame"
      :name="name"
      v-if="mat"
      @update:isme="
        (val) => {
          console.log(val)
          isMe = val
        }
      "
      @update:finished="gameFinished"
    ></Mat>
    <Deck
      v-if="deck"
      :name="name"
      :can-play="mat ? isMe : false"
      ref="deckRef"
      :gameroom="gameroomNumber"
    ></Deck>
    <Scoreboard
      v-if="select"
      :name="name"
      @update:selected="gameChosen"
      @update:finished="matchFinished"
      :gameroom="gameroomNumber"
    />
    <WinScreen
      v-if="matchOver"
      :winner-name="winners"
      :gameroom="gameroomNumber"
      @update:play-again="playAgain"
    />
  </div>
</template>

<style></style>
