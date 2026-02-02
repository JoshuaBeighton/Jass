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
  console.log(gameSelected)
}

const name = ref('')

const login = ref(true)
const select = ref(false)
const mat = ref(false)
const deck = ref(false)
</script>

<template>
  <LoginCard @update:name="setName" @update:ready="removeLogin" v-if="login"></LoginCard>
  <Mat v-if="mat"></Mat>
  <GameSelect v-if="select" :name="name" @update:selected="gameChosen"></GameSelect>
  <Deck v-if="deck"></Deck>
</template>

<style></style>
