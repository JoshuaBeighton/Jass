<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Card from './Card.vue'
import TrickScore from './TrickScore.vue'
import type GameMode from '@/interfaces/GameMode.ts'
import type CardInterface from '@/interfaces/CardInterface.ts'

const props = defineProps<{ game: GameMode; name: string }>()

const players = ref([props.name, 'loading', 'loading', 'loading'])
const nextPlayer = ref('')

const scores = ref([
  { p1: 'loading', p2: 'loading', score: 0 },
  { p1: 'loading', p2: 'loading', score: 0 },
])

const leftCard = ref<undefined | CardInterface>(undefined)
const topCard = ref<undefined | CardInterface>(undefined)
const rightCard = ref<undefined | CardInterface>(undefined)
const bottomCard = ref<undefined | CardInterface>(undefined)

const firstPlayer = ref('')

const meIdx = ref(-1)

const isMe = ref(false)
const tricksPlayed = ref(0)

const count = ref(-1)
const emits = defineEmits<{
  (e: 'update:isme', value: boolean): void
  (e: 'update:finished', value: boolean): void
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
    scores.value = [
      {
        p1: String(players.value[0]),
        p2: String(players.value[2]),
        score: 0,
      },
      {
        p1: String(players.value[1]),
        p2: String(players.value[3]),
        score: 0,
      },
    ]
  } catch (err) {
    console.error('Error fetching players:', err)
  }
}

async function getNextCard() {
  const host = window.location.hostname
  for (let x = 0; x <= 4; x++) {
    try {
      const res = await fetch(`http://${host}:9000/cardWait/${count.value % 4}`)
      if (!res.ok) throw new Error('Network response was not OK')

      const data = await res.json()
      nextPlayer.value = data.next
      firstPlayer.value = data.start
      updateCard(data.currentTrick)
      isMe.value = nextPlayer.value == props.name
      emits('update:isme', isMe.value)
      count.value = data.currentTrick.length
    } catch (err) {
      console.error('Error fetching players:', err)
    }
  }
  tricksPlayed.value++
}

function updateCard(cards: [CardInterface]) {
  const startIndex = players.value.indexOf(firstPlayer.value)
  for (let i = 0; i < cards.length; i++) {
    let currentCard = cards[i]
    let n = (startIndex + i) % 4
    console.log(n)
    switch (n) {
      case 0:
        bottomCard.value = currentCard
        break
      case 1:
        rightCard.value = currentCard
        break
      case 2:
        topCard.value = currentCard
        break
      case 3:
        leftCard.value = currentCard
    }
  }
}

function isPlayer(index: number) {
  return players.value[index] == nextPlayer.value
}

async function clearDeck() {
  console.log('Tricks Played:' + tricksPlayed.value)

  const host = window.location.hostname
  count.value = -1
  leftCard.value = undefined
  topCard.value = undefined
  rightCard.value = undefined
  bottomCard.value = undefined
  const settings = { method: 'POST' }
  const res = await fetch(`http://${host}:9000/resetTrick`, settings)
  if (!res.ok) throw new Error('Network response was not OK')
  const data = await res.json()
  scores.value = data
  if (tricksPlayed.value == 9) {
    emits('update:finished', true)
  } else {
    getNextCard()
  }
}

onMounted(() => {
  getPlayers()
  getNextCard()
})
</script>

<template>
  <div class="header">
    <h1>Jass</h1>
    <h1>{{ props.name }}</h1>
  </div>

  <div class="parent">
    <div class="row">
      <div class="mat">
        <div class="player-info p-bottom" :class="{ upNext: isPlayer(0) }">
          <span class="player-name">{{ players[0] }}</span>
        </div>
        <div class="player-info p-right" :class="{ upNext: isPlayer(1) }">
          <span class="player-name">{{ players[1] }}</span>
        </div>
        <div class="player-info p-top" :class="{ upNext: isPlayer(2) }">
          <span class="player-name">{{ players[2] }}</span>
        </div>
        <div class="player-info p-left" :class="{ upNext: isPlayer(3) }">
          <span class="player-name">{{ players[3] }}</span>
        </div>

        <div class="trick-area">
          <div class="card-slot card-bottom">
            <Card :card="bottomCard" :can-play="false" />
          </div>
          <div class="card-slot card-right">
            <Card :card="rightCard" :can-play="false" />
          </div>
          <div class="card-slot card-top">
            <Card :card="topCard" :can-play="false" />
          </div>
          <div class="card-slot card-left">
            <Card :card="leftCard" :can-play="false" />
          </div>
        </div>
      </div>
      <TrickScore :scores="scores" :game="props.game" />
    </div>
    <button class="continue-button" v-if="count == 4" v-on:click="clearDeck">Continue</button>
  </div>
</template>
<style scoped>
.parent {
  display: flex;
  flex-direction: column;
  gap: 16px;
  justify-content: center;
  align-items: center;
  margin: 10px;
}

.row {
  display: flex;
  flex-direction: row;
  gap: 24px;
  justify-content: center;
  align-items: stretch;
  padding: 20px;
  background-color: var(--color-background-mute);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  width: fit-content;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.mat {
  position: relative;
  background-color: #1b5e20;
  border-radius: 20px;
  width: 550px;
  height: 550px;
  box-shadow:
    inset 0 0 30px rgba(0, 0, 0, 0.5),
    0 8px 16px rgba(0, 0, 0, 0.2);
  box-sizing: border-box;
}

.player-info {
  position: absolute;
  padding: 6px 14px;
  background: var(--color-background-soft);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  z-index: 2;
  transition: all 0.2s ease;
}

.player-info.upNext {
  background: var(--color-accent);
  color: var(--color-heading);
  border-color: var(--color-heading);
  box-shadow: 0 0 12px var(--color-heading);
}

.p-bottom {
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
}
.p-bottom.upNext {
  transform: translateX(-50%) scale(1.08);
}

.p-top {
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
}
.p-top.upNext {
  transform: translateX(-50%) scale(1.08);
}

.p-left {
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
}
.p-left.upNext {
  transform: translateY(-50%) scale(1.08);
}

.p-right {
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
}
.p-right.upNext {
  transform: translateY(-50%) scale(1.08);
}

.trick-area {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 320px;
  height: 320px;
  transform: translate(-50%, -50%);
}

.card-slot {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-bottom {
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

.card-top {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}

.card-left {
  left: 0;
  top: 50%;
  transform: translateY(-50%);
}

.card-right {
  right: 0;
  top: 50%;
  transform: translateY(-50%);
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

.continue-button {
  padding: 12px 24px;
  font-size: 1rem;
  font-weight: 600;
  color: #ffffff;
  background-color: var(--color-accent);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  margin-top: 8px;
  transition:
    opacity 0.2s ease,
    transform 0.1s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.continue-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.continue-button:active {
  transform: translateY(0);
}
</style>
