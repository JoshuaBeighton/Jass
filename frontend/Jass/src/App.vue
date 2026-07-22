<script setup lang="ts">
import Deck from './components/game/Deck.vue'
import GameSelect from './components/game/GameSelect.vue'
import Mat from './components/game/Mat.vue'
import LoginCard from './components/login/LoginCard.vue'
import { ref } from 'vue'
import type GameMode from './interfaces/GameMode.ts'
import RoomChooser from './components/login/RoomChooser.vue'

const gameroomNumber = ref(-1)

function removeLogin() {
  login.value = false
  select.value = true
  deck.value = true
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
}

const currentGame = ref(defaultGame)
const isMe = ref(false)
const deckRef = ref<InstanceType<typeof Deck>>()

const login = ref(true)
const select = ref(false)
const mat = ref(false)
const deck = ref(false)

function gameFinished(finished: boolean) {
  if (!finished) return

  mat.value = false
  select.value = true
  deckRef.value?.fetchHand()
}
</script>

<template>
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
    <GameSelect
      v-if="select"
      :name="name"
      @update:selected="gameChosen"
      :gameroom="gameroomNumber"
    ></GameSelect>
  </div>
</template>

<style></style>
