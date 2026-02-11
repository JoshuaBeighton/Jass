<script setup lang="ts">
import Deck from './components/game/Deck.vue'
import GameSelect from './components/game/GameSelect.vue'
import Mat from './components/game/Mat.vue'
import LoginCard from './components/login/LoginCard.vue'
import { ref } from 'vue'

function removeLogin() {
  console.log('boo')
  login.value = false
  select.value = true
  deck.value = true
}

function setName(nameInput: string) {
  name.value = nameInput
}

function gameChosen(gameSelected: string) {
  currentGame.value = gameSelected
  select.value = false
  mat.value = true
}

const name = ref('')
const currentGame = ref('')

const login = ref(true)
const select = ref(false)
const mat = ref(false)
const deck = ref(false)
</script>

<template>
  <LoginCard @update:name="setName" @update:ready="removeLogin" v-if="login"></LoginCard>
  <Mat :game="currentGame" :name="name" v-if="mat"></Mat>
  <Deck v-if="deck" :name="name"></Deck>
  <GameSelect v-if="select" :name="name" @update:selected="gameChosen"></GameSelect>
</template>

<style></style>
