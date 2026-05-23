<script setup lang="ts">
import Card from './Card.vue'
const props = defineProps<{ name: string | undefined; card: any; upNext: boolean}>()
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

</script>



<template>
    <div class="player">
    <p v-bind:class="{ upNext: props.upNext }">{{ props.name }}</p>
    <Card
          v-if="card != undefined"
          :card-text="concatCard(props.card)"
          :can-play="false"
        ></Card>
        </div>
</template>

<style>
.player{
    position: absolute;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
}
</style>
