<script setup lang="ts">
import type { CrossData, Suit } from '@/interfaces/SaintLegier'
import { computed, h, type FunctionalComponent } from 'vue'

const props = defineProps<{
  data: CrossData
}>()

const SUIT_META: Record<Suit, { symbol: string; color: 'red' | 'black' }> = {
  Spades: { symbol: '♠', color: 'black' },
  Diamonds: { symbol: '♦', color: 'red' },
  Hearts: { symbol: '♥', color: 'red' },
  Clubs: { symbol: '♣', color: 'black' },
}

const SUIT_ORDER: Suit[] = ['Spades', 'Diamonds', 'Hearts', 'Clubs']

const SuitBadge: FunctionalComponent<{ suit: Suit }> = ({ suit }) => {
  const meta = SUIT_META[suit]

  return h('div', { class: ['suit-badge', meta.color] }, [
    h('span', { class: 'symbol' }, meta.symbol),
    h('span', { class: 'label' }, suit),
  ])
}

const directionMap = computed(() => {
  const map: {
    topDown: Suit | null
    bottomUp: Suit | null
    middle: Suit[]
  } = {
    topDown: null,
    bottomUp: null,
    middle: [],
  }

  for (const suit of SUIT_ORDER) {
    switch (props.data[suit]) {
      case 'topDown':
        map.topDown = suit
        break
      case 'bottomUp':
        map.bottomUp = suit
        break
      case 'middle':
        map.middle.push(suit)
        break
    }
  }

  return map
})

const top = computed(() => directionMap.value.topDown)
const bottom = computed(() => directionMap.value.bottomUp)
const left = computed(() => directionMap.value.middle[0] ?? null)
const right = computed(() => directionMap.value.middle[1] ?? null)
</script>

<template>
  <div class="cross-wrapper">
    <div class="cross-grid">
      <div class="cell top">
        <div v-if="top" :class="['suit-badge', SUIT_META[top].color]">
          <span class="symbol">{{ SUIT_META[top].symbol }}</span>
          <span class="label">{{ top }}</span>
        </div>
      </div>

      <div class="cell left">
        <div v-if="left" :class="['suit-badge', SUIT_META[left].color]">
          <span class="symbol">{{ SUIT_META[left].symbol }}</span>
          <span class="label">{{ left }}</span>
        </div>
      </div>

      <div class="cell center">
        <div class="crossbar-h"></div>
        <div class="crossbar-v"></div>
      </div>

      <div class="cell right">
        <div v-if="right" :class="['suit-badge', SUIT_META[right].color]">
          <span class="symbol">{{ SUIT_META[right].symbol }}</span>
          <span class="label">{{ right }}</span>
        </div>
      </div>

      <div class="cell bottom">
        <div v-if="bottom" :class="['suit-badge', SUIT_META[bottom].color]">
          <span class="symbol">{{ SUIT_META[bottom].symbol }}</span>
          <span class="label">{{ bottom }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cross-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
  padding: rem;
}

.cross-header {
  display: flex;
  gap: 1.5rem;
  align-items: baseline;
}

.game-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
}

.caller {
  font-size: 0.85rem;
  color: var(--color-text);
}

.cross-grid {
  display: grid;
  grid-template-columns: 80px 80px 80px;
  grid-template-rows: 80px 80px 80px;
  grid-template-areas:
    '.    top   .'
    'left center right'
    '.    bottom .';
}

.cell {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.top {
  grid-area: top;
}
.bottom {
  grid-area: bottom;
}
.left {
  grid-area: left;
}
.right {
  grid-area: right;
}
.center {
  grid-area: center;
}

.crossbar-h {
  position: absolute;
  top: 50%;
  left: -2vh;
  right: -2vh;
  height: 6px;
  background: var(--color-text-muted);
  transform: translateY(-50%);
  border-radius: 3px;
  z-index: 0;
}

.crossbar-v {
  position: absolute;
  left: 50%;
  top: -2vh;
  bottom: -2vh;
  width: 6px;
  background: var(--color-text-muted);
  transform: translateX(-50%);
  border-radius: 3px;
  z-index: 0;
}

.suit-badge {
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: var(--color-background-soft);
  border: 3px solid var(--color-border);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.suit-badge .symbol {
  font-size: 1.8rem;
  line-height: 1;
}

.suit-badge .label {
  font-size: 0.65rem;
  margin-top: 2px;
  color: var(--color-text);
}

.suit-badge.red .symbol {
  color: #dc2626;
}

.suit-badge.black .symbol {
  color: #111827;
}
</style>
