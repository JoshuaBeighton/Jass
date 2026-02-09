<script setup lang="ts">

import {onMounted, ref } from 'vue'
import Card from './Card.vue';

const props = defineProps<{
    name: string
}>();

const cards = ref([])
async function fetchHand() {
  const host = window.location.hostname

  try {
    const res = await fetch(`http://${host}:9000/hand/${props.name}`)
    if (!res.ok) throw new Error('Network response was not OK')
    const data = await res.json()
    cards.value = data
  } catch (err) {
    console.error('Error fetching hand:', err)
  }
}

function concatCard(card: any){
    return card.number + suitToUnicode(card.suit)
}

function suitToUnicode(inp : string){
    switch (inp){
        case "DIAMONDS":
            return "♦"
        case "HEARTS":
            return "♥"
        case "SPADES":
            return "♠"
        case "CLUBS":
            return "♣"
        default:
            return "?"
    }
}

onMounted(() => {
  fetchHand();
})

</script>

<template>

<div class="cards"> 
    <Card v-for="card in cards" :card-text="concatCard(card)"></Card>
</div>

</template>

<style>
.cards{
    display: flex;
}
</style>