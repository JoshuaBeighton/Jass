<script setup lang="ts">
// App root component
// - Orchestrates the main pages: Room chooser, login, game selection, mat (game view), and deck
import Deck from './components/game/Deck.vue'
import Mat from './components/game/Mat.vue'
import LoginCard from './components/login/LoginCard.vue'
import { ref } from 'vue'
import type GameMode from './interfaces/GameMode.ts'
import RoomChooser from './components/login/RoomChooser.vue'
import WinScreen from './components/game/WinScreen.vue'
import Scoreboard from './components/game/Scoreboard.vue'
import ConfigureGame from './components/game/ConfigureGame.vue'

// Current gameroom (default 1001 for local testing)
const gameroomNumber = ref(1001)

// Navigation helpers triggered by child components
function removeLogin() {
  login.value = false
  configure.value = true
}

function setName(nameInput: string) {
  name.value = nameInput
}

function gameChosen(gameSelected: GameMode) {
  console.log('Game chosen:', gameSelected)
  currentGame.value = gameSelected
  select.value = false
  mat.value = true
}

const name = ref('')

const defaultGame: GameMode = {
  game: '',
  suit: undefined,
  start: undefined,
  caller: '',
  cross: undefined,
}

const currentGame = ref(defaultGame)
const isMe = ref(false)
const deckRef = ref<InstanceType<typeof Deck>>()
const winners = ref('')

// Which panes are visible
const login = ref(true)
const configure = ref(false)
const select = ref(false)
const mat = ref(false)
const deck = ref(false)
const matchOver = ref(false)

// Called when a game finishes to return to selection and refresh hand
function gameFinished(finished: boolean) {
  if (!finished) return

  mat.value = false
  select.value = true
  deckRef.value?.fetchHand()
}

function matchFinished(finished: string) {
  login.value = false
  select.value = false
  mat.value = false
  deck.value = false
  matchOver.value = true
  winners.value = finished
}

async function playAgain(sameRoom: boolean) {
  const host = window.location.hostname
  let res = await fetch(`http://${host}:9000/resetMatch`, {
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain',
      // backend expects gameroom id in a `Gameroom` header
      Gameroom: gameroomNumber.value.toString(),
    },
  })

  if (!sameRoom) {
    gameroomNumber.value = -1
    login.value = true
  } else {
    login.value = true
  }
  matchOver.value = false
}

function onConfigEnd() {
  configure.value = false
  select.value = true
  deck.value = true
}
</script>

<template>
  <ConfigureGame v-if="configure" :room-id="gameroomNumber.toString()" @confirm="onConfigEnd" />

  <RoomChooser v-if="gameroomNumber == -1" @update:selected="(val) => (gameroomNumber = val)" />
  <div v-else>
    <LoginCard
      @update:name="setName"
      @update:ready="removeLogin"
      v-if="login"
      :gameroom="gameroomNumber"
    ></LoginCard>
    <Mat
      :gameroom="gameroomNumber"
      :game="currentGame"
      :name="name"
      v-if="mat"
      @update:isme="
        (val) => {
          isMe = val
        }
      "
      @update:finished="gameFinished"
    ></Mat>
    <Deck v-if="deck" :name="name" :can-play="isMe" ref="deckRef" :gameroom="gameroomNumber"></Deck>
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
