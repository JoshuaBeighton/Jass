<script setup lang="ts">
import { ref } from 'vue'

function isRed() {
  return props.cardText.endsWith('♦') || props.cardText.endsWith('♥')
}

const props = defineProps<{
  cardText: string
  canPlay: boolean
}>()

function toSym(num: String) {
  const firstTwo = num.substring(0, 2)
  switch (firstTwo) {
    case '14':
      return 'A' + num.charAt(num.length - 1)
    case '13':
      return 'K' + num.charAt(num.length - 1)
    case '12':
      return 'Q' + num.charAt(num.length - 1)
    case '11':
      return 'J' + num.charAt(num.length - 1)
    default:
      return num.toString()
  }
}

function replaceCardSuits(input: string): string {
  const suitMap: { [key: string]: string } = {
    '♠': 'S',
    '♥': 'H',
    '♦': 'D',
    '♣': 'C',
  }

  return input.replace(/[\u2660\u2665\u2666\u2663]/g, (match) => suitMap[match] || match)
}

async function sendCard() {
  if (props.canPlay) {
    console.log('test')
    const host = window.location.hostname
    await fetch(`http://${host}:9000/nextCard`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(replaceCardSuits(props.cardText)),
    })
  }
}
</script>

<template>
  <div class="card" @click="sendCard">
    <p v-bind:class="{ red: isRed(), black: !isRed() }">{{ toSym(props.cardText) }}</p>
  </div>
</template>

<style>
.card {
  width: 100px;
  height: 150px;
  background-color: azure;
  padding: 5px;
  margin: 5px;
  display: flexbox;
  justify-content: center;
  align-items: center;
}

.red {
  color: red;
}

.black {
  color: black;
}

.red,
.black {
  text-align: center;
  font-size: 30pt;
}
</style>
