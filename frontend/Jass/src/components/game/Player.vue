<script setup lang="ts">
import { concatCard } from '@/utils/SuitManipulation.ts'
import Card from './Card.vue'
import type CardInterface from '@/interfaces/CardInterface.ts'
const props = defineProps<{
  name: string | undefined
  card: CardInterface
  upNext: boolean
}>()
</script>

<template>
  <div class="player">
    <p v-bind:class="{ upNext: props.upNext }">{{ props.name }}</p>
    <Card v-if="card != undefined" :card="props.card" :can-play="false"></Card>
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
