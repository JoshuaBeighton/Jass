<script setup lang="ts">
import type { NumericLiteral } from 'typescript'
import { ref, onMounted } from 'vue'

const props = defineProps<{ game: string; name: string }>()

const players = ref([props.name, 'loading', 'loading', 'loading'])

const nextPlayer = ref(-1)

const meIdx = ref(-1)

async function getPlayers() {
  const host = window.location.hostname

  try {
    const res = await fetch(`http://${host}:9000/player/`)
    if (!res.ok) throw new Error('Network response was not OK')

    const data = await res.json()

    for (let i = 0; i < data.length; i++) {
      if (data[i].name == props.name) {
        meIdx.value = i
      }
    }
    for (let i = 0; i < 4; i++) {
      players.value[i] = data[(i + meIdx.value) % 4].name
    }
  } catch (err) {
    console.error('Error fetching players:', err)
  }
}

async function getNextPlayer() {
  const host = window.location.hostname

  try {
    const res = await fetch(`http://${host}:9000/nextPlayer/`)
    if (!res.ok) throw new Error('Network response was not OK')

    const data = await res.json()
    console.log(data)
    nextPlayer.value = data
  } catch (err) {
    console.error('Error fetching players:', err)
  }
}

function isPlayer(index: number) {
  console.log('Up[ next: ' + nextPlayer.value)
  console.log('Me: ' + meIdx.value)
  console.log('Idx: ' + index)
  let realIdx = (index + meIdx.value) % 4
  return realIdx == nextPlayer.value
}

onMounted(() => {
  getPlayers()
  getNextPlayer()
})
</script>

<template>
  <h1>{{ props.game }}</h1>
  <div class="parent">
    <div class="mat">
      <p v-bind:class="{ upNext: isPlayer(0) }" class="bottom">{{ players[0] }}</p>
      <p v-bind:class="{ upNext: isPlayer(1) }" class="right">{{ players[1] }}</p>
      <p v-bind:class="{ upNext: isPlayer(2) }" class="top">{{ players[2] }}</p>
      <p v-bind:class="{ upNext: isPlayer(3) }" class="left">{{ players[3] }}</p>
    </div>
  </div>
</template>

<style>
.parent {
  display: flex;
  flex-direction: column;
  gap: 5px;
  justify-content: center;
  align-items: center;
}

.mat {
  position: relative;
  background-color: darkgreen;
  border-radius: 15px;
  width: 500px;
  height: 500px;
  border: 2px solid green;
}

.mat p {
  position: absolute;
  margin: 0;
}

/* Bottom center */
.bottom {
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
}

/* Top center */
.top {
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
}

/* Left center */
.left {
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
}

/* Right center */
.right {
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}

.upNext {
  color: red;
}
</style>
