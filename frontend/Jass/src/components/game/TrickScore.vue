<script setup lang="ts">
import type GameMode from '@/interfaces/GameMode'
import { isRed, suitToUnicode } from '@/utils/SuitManipulation'
import { computed } from 'vue'

interface TeamScore {
  p1: string
  p2: string
  score: number | string
}

const props = defineProps<{
  scores: TeamScore[]
  game: GameMode
}>()

const gameDescriptions: Record<string, string> = {
  'Top Down': 'The highest card wins!',
  'Bottom Up': 'The lowest card wins!',
  Middle:
    'The card closest to a 10 wins! If two cards are equally close, the card played first wins.',
  Trumps:
    'The highest card of the trump suit wins! If no trump cards are played, the highest card of the leading suit wins.',
}

const description = computed(() => {
  return (
    gameDescriptions[props.game.game] ||
    'Work together with your teammate to outscore the opposition.'
  )
})
</script>

<template>
  <div class="game-card">
    <header class="game-header">
      <div class="game-title-container">
        <h2 class="game-title">{{ game.game }}</h2>
        <h2 :class="isRed(suitToUnicode(game.suit)) ? 'red' : '' + ' game-title'" v-if="game.suit">
          {{ suitToUnicode(game.suit) }}
        </h2>
      </div>
      <p class="game-description">{{ description }}</p>
    </header>

    <hr class="divider" />

    <div class="scores-container">
      <h3 class="scores-title">Scores</h3>

      <div class="scores-list">
        <div v-for="(team, index) in scores" :key="index" class="score-row">
          <div class="players">
            <p>{{ team.p1 }} & {{ team.p2 }}</p>
          </div>
          <div
            :class="
              'score-badge ' +
              (team.p1 == game.caller || team.p2 == game.caller
                ? 'score-badge-selected'
                : 'score-badge-deselected')
            "
          >
            <p>{{ team.score }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.game-title-container {
  display: flex;
  flex-direction: row;
  flex: 1;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.game-title-container h2 {
  margin: 0;
}

.game-card {
  background-color: var(--color-background);
  max-width: 300px;
  border-radius: 12px;
  padding: 6px 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  border: 1px solid #2e2e38;
}

.game-header {
  margin-bottom: 16px;
}

.game-title {
  margin: 0 0 8px 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-heading);
}

.game-description {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text);
  line-height: 1.4;
}

.divider {
  border: 0;
  height: 1px;
  background: var(--color-border);
  margin: 16px 0;
}

.scores-title {
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text);
  margin: 0 0 12px 0;
}

.scores-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-row {
  display: flex;
  justify-content: space-between;
  align-content: center;
  align-items: center;
  background: var(--color-background);
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
}

.players {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  font-size: 0.95rem;
  width: 100%;
}

.score-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-heading);
  font-weight: 700;
  font-size: 1rem;
  padding: 4px 10px;
  margin: 4px;
  border-radius: 6px;
  min-width: 24px;
  box-sizing: border-box;
  line-height: 1;
}
.score-badge-selected {
  background-color: var(--color-accent);
}

.score-badge-deselected {
  background-color: var(--color-background-mute);
}
</style>
