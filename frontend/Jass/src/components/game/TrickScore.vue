<script setup lang="ts">

const props = defineProps<{
  scores: any
}>()

</script>

<template>
  <div>
    <p>Scores:</p>
    <p v-for="team in scores">{{ team.p1 }}+{{ team.p2 }}:{{ team.score }}</p>
  </div>
</template>

<style>

</style>
