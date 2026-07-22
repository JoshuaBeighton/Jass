<script setup lang="ts">
import type Card from '@/interfaces/CardInterface'
import { concatCard, isRed, replaceCardSuits, toSym } from '@/utils/SuitManipulation'
import { ref } from 'vue'

const props = defineProps<{
  card?: Card | undefined
  canPlay: boolean
  gameroom: number
}>()

const played = ref(false)

async function sendCard() {
  if (props.canPlay && props.card) {
    const host = window.location.hostname
    let res = await fetch(`http://${host}:9000/nextCard`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Gameroom: props.gameroom.toString(),
      },
      body: JSON.stringify(replaceCardSuits(concatCard(props.card))),
    })
    if (res.status == 200) {
      played.value = true
    } else {
      alert("You Can't Play That Card!")
    }
  }
}
</script>

<template>
  <div v-if="card && !played" class="card" @click="sendCard">
    <img
      :class="{ red: isRed(concatCard(props.card)), black: !isRed(concatCard(props.card)) }"
      :src="`/images/cards/${replaceCardSuits(concatCard(props.card)).toLowerCase()}.png`"
      :alt="toSym(concatCard(props.card))"
    />
  </div>
</template>

<style scoped>
.card {
  width: 100px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.red {
  color: var(--color-red-suit, red);
}

.black {
  color: var(--color-black-suit, black);
}

img {
  padding: 0;
  height: 100%;
}
</style>
