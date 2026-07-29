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
  canPlay: boolean
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
  const host = window.location.hostname

  try {
    const res = await fetch(`http://${host}:9000/hand/${props.name}`, {
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
      :can-play="props.canPlay"
      :style="cardStyle(i)"
      :gameroom="props.gameroom"
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
