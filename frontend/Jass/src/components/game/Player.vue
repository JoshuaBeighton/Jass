<script setup lang="ts">
// Player component - shows a player's name and a (single) card.
// This component is used to render other players around the table.
import { concatCard } from '@/utils/SuitManipulation.ts'
import Card from './Card.vue'
import type CardInterface from '@/interfaces/CardInterface.ts'

// Props:
// - name: player's display name (may be undefined for empty seats)
// - card: a single CardInterface describing the player's shown card
// - upNext: whether this player is the next to play (used for styling)
// - gameroom: id forwarded to the Card component for server requests
const props = defineProps<{
  name: string | undefined
  card: CardInterface
  upNext: boolean
  gameroom: number
}>()
</script>

<template>
  <!-- Render player name and their card (read-only here) -->
  <div class="player">
    <p v-bind:class="{ upNext: props.upNext }">{{ props.name }}</p>
    <Card
      v-if="card != undefined"
      :card="props.card"
      :can-play="false"
      :gameroom="props.gameroom"
    ></Card>
  </div>
</template>

<style>
.player {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
</style>
