<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  gameroom: number
}>()

const suits = ['Clubs', 'Diamonds', 'Hearts', 'Spades']
const selectedSuit = ref<string | null>(null)

const emit = defineEmits<{
  (e: 'finished', suit: string): void
}>()

function chooseSuit(suit: string) {
  selectedSuit.value = suit
}

async function finishSelection() {
  if (!selectedSuit.value) {
    return
  }

  const host = window.location.hostname

  try {
    const response = await fetch(`http://${host}:9000/gamemodeChoice`, {
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain',
        gameroom: props.gameroom.toString(),
      },
      body: selectedSuit.value.charAt(0).toUpperCase(),
    })

    if (!response.ok) {
      console.error('Failed to submit suit selection', response.status)
      return
    }
  } catch (error) {
    console.error('Failed to submit suit selection', error)
    return
  }

  emit('finished', selectedSuit.value)
}
</script>

<template>
  <div class="selector">
    <h3>Choose a suit</h3>

    <div class="buttons">
      <button
        v-for="suit in suits"
        :key="suit"
        type="button"
        class="suit-button"
        :class="{ active: selectedSuit === suit }"
        @click="chooseSuit(suit)"
      >
        {{ suit }}
      </button>
    </div>

    <button type="button" class="finish-button" :disabled="!selectedSuit" @click="finishSelection">
      Finished
    </button>
  </div>
</template>

<style scoped>
.selector {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.suit-button,
.finish-button {
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
}

.suit-button.active {
  background: var(--color-background);
  color: white;
  border-color: var(--color-border);
}

.finish-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
