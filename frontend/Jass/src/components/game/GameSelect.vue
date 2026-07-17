<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Scoreboard from './Scoreboard.vue'

const isMe = ref(false)
const nextChooser = ref('')

let id = 0
let counter = -1

const games = ref([
  { id: id++, text: 'Top Down', key: 'topDown' },
  { id: id++, text: 'Bottom Up', key: 'bottomUp' },
  { id: id++, text: 'Middle', key: 'middle' },
  { id: id++, text: 'Pass', key: 'pass' },
])

const props = defineProps<{
  name: string
}>()

const emits = defineEmits<{
  (e: 'update:selected', value: string): void
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
      emits('update:selected', data.game)
    }
    if (!isMe.value && keepSending) {
      fetchNextPlayer()
    }
  } catch (err) {
    console.error('Error fetching teams:', err)
  }
}

async function sendGame(game: string) {
  const host = window.location.hostname

  await fetch(`http://${host}:9000/gameChoice`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      name: game,
    }),
  })
  if (game == 'Pass') {
    fetchNextPlayer()
  } else {
    emits('update:selected', game)
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
      <p v-if="!isMe">Waiting on {{ nextChooser }}</p>

      <div v-if="isMe" class="buttons">
        <button v-for="game in games" @click="() => sendGame(game.key)">{{ game.text }}</button>
      </div>
    </div>
    <Scoreboard />
  </div>
</template>

<style>
.smallHr {
  width: 80%;
}

.buttons {
  display: flex;
  width: fit-content;
  gap: 2px;
}

.parent {
  display: flex;
  flex-direction: column;
  gap: 5px;
  justify-content: center;
  align-items: center;
}

button {
  width: 100%;
}
</style>
