<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Card from './Card.vue'

const props = defineProps<{ game: string; name: string }>()

const players = ref([props.name, 'loading', 'loading', 'loading'])

const nextPlayer = ref('')

const played = ref([null, null, null, null])

const firstPlayer = ref('')

const meIdx = ref(-1)

const isMe = ref(false)

const count = ref(-1)

function concatCard(card: any) {
  if (card == null || card.number == undefined) {
    return 'undefined'
  } else {
    return card.number + suitToUnicode(card.suit)
  }
}

function suitToUnicode(inp: string) {
  switch (inp) {
    case 'DIAMONDS':
      return '♦'
    case 'HEARTS':
      return '♥'
    case 'SPADES':
      return '♠'
    case 'CLUBS':
      return '♣'
    default:
      return '?'
  }
}

const emits = defineEmits<{
  (e: 'update:isme', value: boolean): void
}>()

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

async function getNextCard() {
  const host = window.location.hostname

  try {
    const res = await fetch(`http://${host}:9000/cardWait/${count.value}`)
    if (!res.ok) throw new Error('Network response was not OK')

    const data = await res.json()
    nextPlayer.value = data.next
    firstPlayer.value = data.start
    played.value = data.currentTrick
    isMe.value = nextPlayer.value == props.name
    emits('update:isme', isMe.value)
    count.value++
    getNextCard()
  } catch (err) {
    console.error('Error fetching players:', err)
  }
}

function isPlayer(index: number) {
  return players.value[index] == nextPlayer.value
}

onMounted(() => {
  getPlayers()
  getNextCard()
})

function matToCardIndex(inp: number) {
  const f = players.value.indexOf(firstPlayer.value)
  const l = played.value.length
  const m = meIdx.value
  console.log(f - m)
  const indexInTrick = (f - m + l) % 4
  return indexInTrick
}
</script>

<template>
  <h1>{{ props.game }}</h1>
  <div class="parent">
    <div class="mat">
      <div class="bottom">
        <p v-bind:class="{ upNext: isPlayer(0) }">{{ players[0] }}</p>
        <Card
          v-if="concatCard(played[matToCardIndex(0)]) != 'undefined'"
          :card-text="concatCard(played[matToCardIndex(0)])"
          :can-play="false"
        ></Card>
      </div>
      <div class="right">
        <p v-bind:class="{ upNext: isPlayer(1) }">{{ players[1] }}</p>
        <Card
          v-if="concatCard(played[matToCardIndex(1)]) != 'undefined'"
          :card-text="concatCard(played[matToCardIndex(1)])"
          :can-play="false"
        ></Card>
      </div>
      <div class="top">
        <p v-bind:class="{ upNext: isPlayer(2) }">{{ players[2] }}</p>
        <Card
          v-if="concatCard(played[matToCardIndex(2)]) != 'undefined'"
          :card-text="concatCard(played[matToCardIndex(2)])"
          :can-play="false"
        ></Card>
      </div>
      <div class="left">
        <p v-bind:class="{ upNext: isPlayer(3) }">{{ players[3] }}</p>
        <Card
          v-if="concatCard(played[matToCardIndex(3)]) != 'undefined'"
          :card-text="concatCard(played[matToCardIndex(3)])"
          :can-play="false"
        ></Card>
      </div>
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

.left,
.bottom,
.right,
.top {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upNext {
  color: red;
}
</style>
