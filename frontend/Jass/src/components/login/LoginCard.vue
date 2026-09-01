<script setup lang="ts">
// LoginCard component
// - Collects a player's name and team/index selection, posts to the
//   backend to register the player, and shows a waiting state once selected.
import { onMounted, ref } from 'vue'
import TeamInfo from './TeamInfo.vue'
import WaitingJoin from './WaitingJoin.vue'
import { setStoredValue } from '@/utils/storage.ts'

// Reactive form state
const name = ref('')
const idx = ref<number | undefined>()
const selected = ref(false)
const props = defineProps<{ gameroom: number; name: string }>()
const containsInvalidChar = ref(false)

/**
 * login
 * - Sends a POST to `/player` with the player's name and chosen index.
 * - Sets `selected` to true to show the waiting UI while backend processes join.
 */
async function login() {
  // Prevent login if name or idx is not set
  if (!name.value || idx.value === undefined || containsInvalidChar.value) return
  const apiUrl = import.meta.env.VITE_API_URL
  selected.value = true
  emit('update:name', name.value)
  await fetch(`${apiUrl}/player`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      gameroom: props.gameroom.toString(),
    },
    body: JSON.stringify({
      name: name.value,
      idx: idx.value,
    }),
  })
}

// Emit local events to parent when the player is ready or name updates
function emitReady() {
  emit('update:name', name.value)
  emit('update:ready', true)
}

const emit = defineEmits<{
  (e: 'update:ready', value: boolean): void
  (e: 'update:name', value: string): void
}>()

onMounted(() => {
  name.value = props.name
})

const invalidChars = ['/','\\','$','%','^','&','*','(',')','{','}','[',']','!','?','<','>',',','.']

const checkChars = () =>{
  let valid = true
  invalidChars.forEach(element => {
    if (name.value.contains(element)){
      valid = false
    }  
  });
  containsInvalidChar.value = !valid;
}
</script>

<template>
  <!-- Header showing room number and the login card -->
  <div class="header">
    <h1>Jass</h1>
    <h1>Room {{ props.gameroom }}</h1>
  </div>
  <div class="loginCard">
    <div class="loginCard">
      <h1>Jass</h1>
      <hr />
      <!-- Player name input -->
      <input @input="checkChars" v-model="name" type="text" placeholder="Enter Name" />
      <p v-if="containsInvalidChar" class="errorText">Name Cannot Contain Special Characters</p>
      <!-- TeamInfo lets the player pick a seat/team; emits ready when done -->
      <TeamInfo
        @update:ready="emitReady"
        v-model:selected="idx"
        :gameroom="props.gameroom"
      ></TeamInfo>
      <!-- Show Go button until the player has selected a seat -->
      <button v-if="!selected" v-on:click="login">Go!</button>
      <!-- After joining, show a waiting indicator -->
      <WaitingJoin v-else text="Waiting for other players to join"></WaitingJoin>
    </div>
  </div>
</template>

<style scoped>
.errorText{
  color: var(--color-danger);
}

.header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 8px;
  color: var(--color-heading);
}

.header h1 {
  margin: 6px 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
}

input,
button {
  font-size: large;
  background-color: var(--color-background-mute);
  border-radius: 5px;
  padding: 2px;
  border: 2px;
  border-style: solid;
  border-color: var(--color-border);
  color: var(--color-text);
}

input::placeholder {
  color: var(--color-text);
}

input:hover,
button:hover {
  border-color: var(--color-border-hover);
}

.loginCard {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  margin-left: auto;
  margin-right: auto;
  gap: 1rem;
}

.inputs {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

h1 {
  font-size: 2.5rem;
  color: var(--color-heading);
  margin-left: auto;
  margin-right: auto;
}
</style>
