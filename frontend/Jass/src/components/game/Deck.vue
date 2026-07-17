<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Card from './Card.vue'

const props = defineProps<{
  name: string
  canPlay: boolean
}>()

const round = ref(0)
const cards = ref([])
async function fetchHand() {
  const host = window.location.hostname

  try {
    const res = await fetch(`http://${host}:9000/hand/${props.name}`)
    if (!res.ok) throw new Error('Network response was not OK')
    const data = await res.json()
    cards.value = data
    round.value++
    console.log(cards.value)
  } catch (err) {
    console.error('Error fetching hand:', err)
  }
}

function concatCard(card: any) {
  return card.number + suitToUnicode(card.suit)
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

onMounted(() => {
  fetchHand()
})
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

defineExpose({
  fetchHand,
})
</script>

<template>
  <div class="cards">
    <Card
      v-for="(card, i) in cards"
      :key="String(i) + ':' + String(round)"
      :card-text="concatCard(card)"
      :can-play="props.canPlay"
      :style="cardStyle(i)"
    />
  </div>
</template>

<style>
.cards {
  position: relative;
  margin: 10px;
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
