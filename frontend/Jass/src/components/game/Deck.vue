<script setup lang="ts">
// Deck component
// - Loads the player's hand from the backend and renders the set of `Card`
//   components in a fanned layout. Exposes `fetchHand` so parents can refresh.
import { onMounted, ref } from 'vue'
import Card from './Card.vue'
import { concatCard } from '@/utils/SuitManipulation.ts'
import type CardInterface from '@/interfaces/CardInterface.ts'

const props = defineProps<{
  name: string
  canPlay: boolean | CardInterface[]
  gameroom: number
}>()

// `round` increments when the hand changes — used to force new keys for v-for
const round = ref(0)
const cards = ref<CardInterface[]>([])

/**
 * fetchHand
 * - GET `/hand/{name}` to retrieve the player's cards for the current round
 * - Updates `cards` and bumps `round` to trigger reactivity in the template
 */
async function fetchHand() {
  const apiUrl = import.meta.env.VITE_API_URL

  try {
    const res = await fetch(`${apiUrl}/hand/${props.name}`, {
      headers: {
        Gameroom: props.gameroom.toString(),
      },
    })
    if (!res.ok) throw new Error('Network response was not OK')
    const data = await res.json()
    cards.value = data
    round.value++
    console.log(cards.value)
  } catch (err) {
    console.error('Error fetching hand:', err)
  }
}

onMounted(() => {
  fetchHand()
})

/**
 * cardStyle
 * - Computes a translateX + rotate transform for card i to produce a fanned hand.
 * - `spread` controls horizontal spacing; `angleSpread` controls rotation per card.
 */
function cardStyle(i: number) {
  const total = cards.value.length
  const spread = 50
  const angleSpread = 10

  const center = (total - 1) / 2
  const offset = i - center

  return {
    transform: `translateX(${offset * spread}px) rotate(${offset * angleSpread}deg)`,
    transformOrigin: 'bottom center',
    zIndex: i,
  }
}

function canPlayCard(card: any): boolean {
  if (typeof props.canPlay === 'boolean') {
    return props.canPlay
  }

  for (const element of props.canPlay) {
    if (element.suit === card.suit && element.value === card.number) {
      return element.playable
    }
  }

  return true
}

function overlayCard(card: any): boolean {
  if (typeof props.canPlay === 'boolean') {
    return true
  }

  for (const element of props.canPlay) {
    if (element.suit === card.suit && element.value === card.number) {
      return element.playable
    }
  }

  return true
}

// Allow parent components to call `fetchHand()` via template ref
defineExpose({
  fetchHand,
})
</script>

<template>
  <!-- Render the player's hand as a fanned stack of Card components -->
  <div class="cards">
    <Card
      v-for="(card, i) in cards"
      :key="String(i) + ':' + String(round)"
      :card="card"
      :can-play="canPlayCard(card)"
      :style="cardStyle(i)"
      :gameroom="props.gameroom"
      :overlay="overlayCard(card)"
    />
  </div>
</template>

<style>
.cards {
  position: relative;
  margin: 10px 0px;
  top: 10px;
  height: 200px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.cards > * {
  position: absolute;
}
</style>
