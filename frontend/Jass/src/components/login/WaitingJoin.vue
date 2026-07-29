<script setup lang="ts">
// WaitingJoin component
// - Small UI helper that shows an animated ellipsis after a message.
import { ref, onMounted, onUnmounted } from 'vue'

const props = withDefaults(
  defineProps<{
    text?: string
    intervalMs?: number
  }>(),
  {
    text: 'Waiting for other players to join',
    intervalMs: 500,
  },
)

// Simple dot animation state
const dotCount = ref(1)
let dotInterval: number | undefined

onMounted(() => {
  dotInterval = window.setInterval(() => {
    dotCount.value = (dotCount.value % 3) + 1
  }, props.intervalMs)
})

onUnmounted(() => {
  if (dotInterval) clearInterval(dotInterval)
})
</script>

<template>
  <!-- Message with animated dots to indicate waiting state -->
  <p class="waiting">
    {{ text }}<span class="dots">{{ '.'.repeat(dotCount) }}</span>
  </p>
</template>

<style scoped>
.waiting {
  font-size: large;
  color: var(--color-text);
}

.dots {
  display: inline-block;
  width: 1.5em;
  text-align: left;
}
</style>
