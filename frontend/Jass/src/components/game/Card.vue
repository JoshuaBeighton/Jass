<script setup lang="ts">
// Card component - displays a single playing card and allows the
// player to click it to play the card when it's their turn.

import type Card from '@/interfaces/CardInterface'
// Helper utilities for manipulating card suit strings and formatting
import { concatCard, isRed, replaceCardSuits, toSym } from '@/utils/SuitManipulation'
import { ref } from 'vue'

// Props accepted by this component:
// - card: optional Card object describing rank and suit
// - canPlay: whether the local player is allowed to play a card now
// - gameroom: numeric id used for server requests to identify the room
const props = defineProps<{
  card?: Card | undefined
  canPlay: boolean
  gameroom: number
}>()

// Local state: whether this card has been successfully played
const played = ref(false)

/**
 * sendCard
 * - Called when the card is clicked in the UI.
 * - Checks `canPlay` and that a `card` exists, then POSTs the
 *   card to the backend endpoint `/nextCard` with the `Gameroom` header.
 * - Uses `concatCard` and `replaceCardSuits` to produce the string
 *   representation expected by the backend.
 * - On success sets `played` so the card is hidden; on error shows an alert.
 */
async function sendCard() {
  if (props.canPlay && props.card) {
    const host = window.location.hostname
    let res = await fetch(`http://${host}:9000/nextCard`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        // backend expects gameroom id in a `Gameroom` header
        Gameroom: props.gameroom.toString(),
      },
      // send the card string (suits normalized) as the request body
      body: JSON.stringify(replaceCardSuits(concatCard(props.card))),
    })
    if (res.status == 200) {
      // successfully played — hide this card in the UI
      played.value = true
    } else {
      // server rejected the play (invalid card, not your turn, etc.)
      alert("You Can't Play That Card!")
    }
  }
}
</script>

<template>
  <!-- Render card image when a `card` prop is present and it hasn't
       already been played. Clicking the outer div will attempt to play
       the card via the `sendCard` handler. -->
  <div v-if="card && !played" class="card" @click="sendCard">
    <!--
      - `:class` binds red/black classes based on suit color
      - `:src` references the image file matching the normalized card string
      - `:alt` shows a short symbol for accessibility (rank + suit)
    -->
    <img
      :class="{ red: isRed(concatCard(props.card)), black: !isRed(concatCard(props.card)) }"
      :src="`/images/cards/${replaceCardSuits(concatCard(props.card)).toLowerCase()}.png`"
      :alt="toSym(concatCard(props.card))"
    />
  </div>
</template>

<style scoped>
/* Styles for the card container and suit color helpers */
.card {
  width: 100px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.red {
  /* Applied to red suits (hearts, diamonds) */
  color: var(--color-red-suit, red);
}

.black {
  /* Applied to black suits (spades, clubs) */
  color: var(--color-black-suit, black);
}

img {
  /* Ensure the card image fills the container height */
  padding: 0;
  height: 100%;
}
</style>
