<script setup lang="ts">
import Scoreboard from './Scoreboard.vue'

const props = defineProps<{
  winnerName: string
  gameroom: number
}>()
const emits = defineEmits<{
  (e: 'update:playAgain', value: boolean): void
}>()

const palette = ['#F2C14E', '#FF6B6B', '#7EE8CE', '#FDF6EC']

function confettiStyle(n: number) {
  const left = Math.random() * 100
  const delay = Math.random() * 2.5
  const duration = 3 + Math.random() * 2.5
  const size = 6 + Math.random() * 6
  const color = palette[n % palette.length]
  const rotate = Math.random() * 360

  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size * 0.4}px`,
    backgroundColor: color,
    transform: `rotate(${rotate}deg)`,
  }
}
</script>

<template>
  <div class="win-screen">
    <div class="confetti-layer" aria-hidden="true">
      <span v-for="n in 40" :key="n" class="confetti-piece" :style="confettiStyle(n)" />
    </div>

    <div class="win-content">
      <p class="eyebrow">Game over</p>

      <div class="ribbon">
        <span class="ribbon-label">Winner</span>
        <h1 class="winner-name">{{ props.winnerName }}</h1>
      </div>

      <button class="play-again" @click="emits('update:playAgain', true)">Play again</button>
      <button class="play-again" @click="emits('update:playAgain', false)">Leave Room</button>
    </div>
    <Scoreboard :gameroom="gameroom" />
  </div>
</template>

<style scoped>
.win-screen {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 2vh;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  padding: 2rem;
  box-sizing: border-box;
}

.confetti-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.confetti-piece {
  position: absolute;
  top: -10%;
  border-radius: 1px;
  opacity: 0.9;
  z-index: 0;
  animation-name: fall;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

@keyframes fall {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.9;
  }
  100% {
    transform: translateY(115vh) rotate(360deg);
    opacity: 0.6;
  }
}

.win-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 2vh;
  text-align: center;
  max-width: 32rem;
  animation: rise 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.eyebrow {
  margin: 0 0 0.75rem;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text);
}

.ribbon {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  padding: 1.75rem 2.75rem;
  background: var(--color-accent);
  border-radius: 0.5rem;
}

.ribbon::before,
.ribbon::after {
  content: '';
  position: absolute;
  bottom: -0.9rem;
  width: 0;
  height: 0;
  border-left: 1.1rem solid transparent;
  border-right: 1.1rem solid transparent;
}

.ribbon-label {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--color-text);
  opacity: 0.7;
}

.winner-name {
  margin: 0;
  font-weight: 700;
  font-size: clamp(2rem, 6vw, 3.25rem);
  line-height: 1.1;
  color: var(--color-heading);
  word-break: break-word;
}

.play-again {
  border: none;
  padding: 0.85rem 2rem;
  border-radius: 999px;
  background: var(--color-accent);
  color: var(--color-text);
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
  box-shadow: 0 10px 24px -8px var(--color-accent-muted);
}

.play-again:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px -8px var(--color-accent-muted);
}

.play-again:focus-visible {
  outline: 3px solid #7ee8ce;
  outline-offset: 3px;
}

@media (prefers-reduced-motion: reduce) {
  .confetti-piece,
  .win-content {
    animation: none;
  }
}
</style>
