<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'

// --- Interfaces & Types ---
export interface GameOption {
  id: string
  name: string
}

export type TierAssignments = Record<number | string, GameOption[]>

export interface RoomStatePayload {
  multipliers: number[]
  gamemodes: TierAssignments
}

export interface ComponentProps {
  multipliers?: number[]
  roomId?: string
}

// --- Props & Emits ---
const props = withDefaults(defineProps<ComponentProps>(), {
  multipliers: () => [1, 2, 3, 4, 5],
  roomId: 'default-room',
})

const emit = defineEmits<{
  (e: 'confirm', assignments: TierAssignments): void
  (e: 'update:assignments', assignments: TierAssignments): void
}>()

// --- Reactive State ---
const activeMultipliers = ref<number[]>([...props.multipliers])
const loading = ref<boolean>(true)
const loadError = ref<string>('')
const games = ref<GameOption[]>([])
const assignments = reactive<TierAssignments>({})

// Track active SSE connection
let eventSource: EventSource | null = null

// Initialize tier keys in assignments object
activeMultipliers.value.forEach((m) => {
  assignments[m] = []
})

// Drag and Drop state variables
const dragOverTarget = ref<number | string | null>(null)
let draggedGame: GameOption | null = null
let draggedFrom: number | string | null = null

// --- Helper Methods ---
function normalizeGameOptions(raw: unknown[]): GameOption[] {
  return raw.map((item, index) => {
    if (typeof item === 'string') {
      return { id: item, name: item }
    }
    if (typeof item === 'object' && item !== null) {
      const obj = item as Record<string, unknown>
      const name = String(obj.name ?? obj.label ?? obj.title ?? obj.id ?? index)
      const id = String(obj.id ?? name)
      return { id, name }
    }
    return { id: String(index), name: String(index) }
  })
}

// Sync helper: Formats payload and emits to parent + pushes to backend
async function broadcastStateChange(): Promise<void> {
  const payload: RoomStatePayload = {
    multipliers: [...activeMultipliers.value],
    gamemodes: JSON.parse(JSON.stringify(assignments)),
  }

  // 1. Emit to Vue parent component
  emit('update:assignments', payload.gamemodes)

  // 2. Broadcast state update to SSE room server
  try {
    const host = window.location.hostname
    await fetch(`/api/multiplierConfiguration/?gameroom=${props.roomId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  } catch (err) {
    console.error('Failed to sync state update to server:', err)
  }
}

// Replace active state safely with inbound SSE payload
function applyRemoteState(data: Partial<RoomStatePayload>): void {
  if (data.multipliers && Array.isArray(data.multipliers)) {
    activeMultipliers.value = data.multipliers
  }

  if (data.gamemodes) {
    // Clear out keys no longer present
    Object.keys(assignments).forEach((key) => {
      if (!(key in data.gamemodes!)) {
        delete assignments[key]
      }
    })
    // Merge new tier assignments reactively
    Object.assign(assignments, data.gamemodes)
  }
}

// Establish SSE connection
function connectSSE(): void {
  const host = window.location.hostname
  eventSource = new EventSource(`/api/multiplierConfiguration/?gameroom=${props.roomId}`)

  eventSource.onmessage = (event: MessageEvent) => {
    try {
      if (event.data == 'done') {
        emit('confirm', JSON.parse(JSON.stringify(assignments)))
        return
      }
      const data: Partial<RoomStatePayload> = JSON.parse(event.data)
      applyRemoteState(data)
    } catch (err) {
      console.error('Failed to parse SSE payload:', err)
    }
  }

  eventSource.onerror = (err) => {
    console.error('SSE Connection error:', err)
  }
}

async function loadGameOptions(): Promise<void> {
  loading.value = true
  loadError.value = ''
  try {
    const host = window.location.hostname
    const response = await fetch(`/api/gamemodeList`)
    if (!response.ok) {
      throw new Error(`Request failed with status ${response.status}`)
    }
    const data = await response.json()
    const rawGames = Array.isArray(data) ? data : (data.games ?? [])
    games.value = normalizeGameOptions(rawGames)
  } catch (err: unknown) {
    if (err instanceof Error) {
      loadError.value = err.message
    } else {
      loadError.value = String(err)
    }
  } finally {
    loading.value = false
  }
}

// --- Lifecycle Hooks ---
onMounted(() => {
  loadGameOptions()
  connectSSE()
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})

// --- Computed Properties ---
const assignedIds = computed<Set<string>>(() => {
  const ids = new Set<string>()
  for (const mult of activeMultipliers.value) {
    for (const g of assignments[mult] || []) {
      ids.add(g.id)
    }
  }
  return ids
})

const unassignedGames = computed<GameOption[]>(() =>
  games.value.filter((g) => !assignedIds.value.has(g.id)),
)

const allTiersFilled = computed<boolean>(() =>
  activeMultipliers.value.every((m) => (assignments[m] || []).length > 0),
)

const emptyTierCount = computed<number>(
  () => activeMultipliers.value.filter((m) => (assignments[m] || []).length === 0).length,
)

// --- Drag & Drop Handlers ---
function formatMultiplier(m: number): string {
  return `${m}x`
}

function onDragStart(game: GameOption, from: number | string): void {
  draggedGame = game
  draggedFrom = from
}

function onDragLeave(target: number | string): void {
  if (dragOverTarget.value === target) dragOverTarget.value = null
}

function removeFromSource(): void {
  if (!draggedGame || draggedFrom === null) return
  if (draggedFrom === 'pool') return

  const list = assignments[draggedFrom]
  if (!list) return

  const idx = list.findIndex((g) => g.id === draggedGame!.id)
  if (idx !== -1) list.splice(idx, 1)
}

function onDrop(target: number | string): void {
  dragOverTarget.value = null
  if (!draggedGame || draggedFrom === target) {
    draggedGame = null
    draggedFrom = null
    return
  }

  removeFromSource()

  if (target !== 'pool') {
    if (!assignments[target]) {
      assignments[target] = []
    }
    if (!assignments[target].some((g) => g.id === draggedGame!.id)) {
      assignments[target].push(draggedGame)
    }
  }

  draggedGame = null
  draggedFrom = null

  // Send update to server
  broadcastStateChange()
}

// --- Actions ---
async function confirm() {
  if (!allTiersFilled.value) return
  try {
    const host = window.location.hostname
    await fetch(`/api/multiplierConfiguration/?gameroom=${props.roomId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: 'done',
    })
  } catch (err) {
    console.error('Failed to sync state update to server:', err)
  }
  emit('confirm', JSON.parse(JSON.stringify(assignments)))
}

function deleteRow(mult: number): void {
  delete assignments[mult]
  activeMultipliers.value = activeMultipliers.value.filter((m) => m !== mult)

  // Send update to server
  broadcastStateChange()
}

function addRow(): void {
  const lastMult = activeMultipliers.value[activeMultipliers.value.length - 1] ?? 0
  const next = lastMult + 1
  activeMultipliers.value.push(next)
  if (!assignments[next]) {
    assignments[next] = []
  }

  // Send update to server
  broadcastStateChange()
}
</script>

<template>
  <div class="tier-picker">
    <div class="header">
      <h1>Choose Games</h1>
      <p class="hint">
        Drag a game mode from the pool into a multiplier row. Every multiplier needs at least one
        game mode before you can confirm.
      </p>
    </div>

    <div v-if="loading" class="status">Loading game options…</div>
    <div v-else-if="loadError" class="status status--error">
      Couldn't load game options: {{ loadError }}
      <button class="retry-btn" @click="loadGameOptions">Retry</button>
    </div>

    <template v-else>
      <!-- Unassigned pool -->
      <div
        class="pool"
        :class="{ 'pool--over': dragOverTarget === 'pool' }"
        @dragover.prevent="dragOverTarget = 'pool'"
        @dragleave="onDragLeave('pool')"
        @drop="onDrop('pool')"
      >
        <span class="pool-label">Unassigned</span>
        <div class="pool-chips">
          <span
            v-for="game in unassignedGames"
            :key="game.id"
            class="chip"
            draggable="true"
            @dragstart="onDragStart(game, 'pool')"
          >
            {{ game.name }}
          </span>
          <span v-if="unassignedGames.length === 0" class="pool-empty">
            All game modes assigned
          </span>
        </div>
      </div>
      <!-- Tier table -->
      <table class="tier-table">
        <tbody>
          <tr
            v-for="mult in activeMultipliers"
            :key="mult"
            class="tier-row"
            :class="{
              'tier-row--empty': (assignments[mult] || []).length === 0,
              'tier-row--over': dragOverTarget === mult,
            }"
          >
            <td class="tier-label">
              <span>{{ formatMultiplier(mult) }}</span>
            </td>
            <td
              class="tier-drop"
              @dragover.prevent="dragOverTarget = mult"
              @dragleave="onDragLeave(mult)"
              @drop="onDrop(mult)"
            >
              <span
                v-for="game in assignments[mult] || []"
                :key="game.id"
                class="chip chip--assigned"
                draggable="true"
                @dragstart="onDragStart(game, mult)"
              >
                {{ game.name }}
              </span>
              <span v-if="(assignments[mult] || []).length === 0" class="tier-placeholder">
                Drop a game mode here
              </span>
            </td>
            <td class="tier-action">
              <button
                type="button"
                class="delete-btn"
                title="Delete tier row"
                @click="deleteRow(mult)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M3 6h18" />
                  <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" />
                  <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" />
                  <line x1="10" y1="11" x2="10" y2="17" />
                  <line x1="14" y1="11" x2="14" y2="17" />
                </svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="footer">
        <button class="confirm-btn" @click="addRow">Add Tier</button>
        <button class="confirm-btn" :disabled="!allTiersFilled" @click="confirm">
          Confirm selections
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.tier-picker {
  max-width: 720px;
  margin: 0 auto;
  padding: 40px 24px 32px;
  color: var(--color-text);
  box-sizing: border-box;
}

.header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.hint {
  margin: 0 0 24px;
  font-size: 0.875rem;
  color: var(--color-text);
}

/* --- Status & Alerts --- */

.status {
  padding: 24px;
  text-align: center;
  color: var(--color-text);
}

.status--error {
  color: var(--color-danger);
}

.retry-btn {
  display: block;
  margin: 12px auto 0;
  padding: 6px 14px;
  font: inherit;
  font-size: 0.875rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-background);
  color: var(--color-text);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.retry-btn:hover {
  background: var(--color-accent-muted);
}

/* --- Pool Zone --- */

.pool {
  border: 1px dashed var(--color-border);
  border-radius: 10px;
  padding: 12px 14px;
  margin-bottom: 24px;
  background: var(--color-background);
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease;
}

.pool--over {
  background: var(--color-accent-muted);
  border-color: var(--color-accent);
}

.pool-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--color-text);
  margin-bottom: 8px;
}

.pool-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  align-items: center;
}

.pool-empty {
  font-size: 0.8125rem;
  color: var(--color-text);
  padding: 4px 0;
}

/* Container for the delete action at the end of the row */
.tier-action {
  display: flex !important;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
  background: var(--color-background);
  border-left: 1px solid var(--color-border);
}

/* Delete Trash Button */
.delete-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--color-text);
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;
}

.delete-btn:hover {
  background: var(--color-danger-muted);
  color: var(--color-danger);
}

.delete-btn:focus-visible {
  outline: 2px solid var(--color-danger);
  outline-offset: 1px;
}

/* --- Drag-and-Drop Chips --- */

.chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  font-size: 0.8125rem;
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
  touch-action: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  transition:
    transform 0.1s ease,
    box-shadow 0.1s ease;
}

.chip:active {
  cursor: grabbing;
  transform: scale(0.98);
}

.chip--assigned {
  background: var(--color-accent-muted);
  border-color: var(--color-accent);
}

/* --- Tier Rows Container --- */
.tier-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 12px; /* Spacing between tier rows */
}

.tier-table tbody {
  display: block;
  width: 100%;
}

/* Force each table row to act as a single horizontal flex frame */
.tier-row {
  display: flex !important;
  align-items: stretch;
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  overflow: hidden;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease;
}

/* Tier label on the left */
.tier-label {
  display: flex !important;
  align-items: center;
  justify-content: center;
  width: 88px;
  flex-shrink: 0;
  font-weight: 700;
  font-size: 0.95rem;
  padding: 10px 8px;
  background: var(--color-accent-muted);
  color: var(--color-text);
  border-right: 1px solid var(--color-border);
  text-align: center;
}

/* Drop area extending across the remaining horizontal space */
.tier-drop {
  display: flex !important;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 10px 12px;
  min-height: 48px;
  background: var(--color-background);
}

/* Empty & Dragover States */
.tier-row--empty {
  border-color: var(--color-background-soft);
}

.tier-row--empty .tier-drop {
  background: var(--color-background);
}

.tier-row--over {
  border-color: var(--color-accent);
}

.tier-row--over .tier-drop {
  background: var(--color-accent-muted);
}

.tier-placeholder {
  font-size: 0.8125rem;
  color: var(--color-text);
  font-style: italic;
}

/* --- Footer Actions --- */

.footer {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  /* gap: 12px; */
}

.footer-warning {
  font-size: 0.8125rem;
  color: var(--color-danger);
}

.confirm-btn {
  padding: 8px 18px;
  border: none;
  border-radius: 8px;
  background: var(--color-accent);
  color: var(--color-accent-foreground);
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: opacity 0.15s ease;
}

.confirm-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.confirm-btn:disabled {
  background: var(--color-border);
  color: var(--color-text);
  cursor: not-allowed;
}

/* Focus States for Accessibility */

button:focus-visible,
.chip:focus-visible {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
}
</style>
