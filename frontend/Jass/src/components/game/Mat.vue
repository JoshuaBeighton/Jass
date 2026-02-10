<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{ game: string }>()
</script>

<template>
  <h1>{{ props.game }}</h1>
  <div class="parent">
    <div class="mat"></div>
  </div>
</template>

<style>
.parent {
  display: flex;
  flex-direction: column;
  gap: 5px;
  justify-content: center;
  align-items: center;
}

.mat {
  background-color: darkgreen;
  border-radius: 15px;
  width: 500px;
  height: 500px;
  border-color: green;
  border-style: solid;
}
</style>
