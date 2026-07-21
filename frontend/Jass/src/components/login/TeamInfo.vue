<script setup lang="ts">
import { ref, onMounted } from 'vue'

const team1 = ref<Array<{ name: string; team: number }>>([])
const team2 = ref<Array<{ name: string; team: number }>>([])
const selectedClass = ref('selected')
const selected = ref(-1)
let counter = -1

async function fetchTeams() {
  const host = window.location.hostname

  try {
    // Long-poll request
    const res = await fetch(`http://${host}:9000/teamWait/${counter}`)
    if (!res.ok) throw new Error('Network response was not OK')

    const data = await res.json()
    team1.value = data[0].players
    team2.value = data[1].players
    counter++
  } catch (err) {
    console.error('Error fetching teams:', err)
  } finally {
    if (counter >= 4) {
      emit('update:ready', true)
    }
    // Immediately poll again
    else {
      fetchTeams()
    }
  }
}

onMounted(() => {
  fetchTeams()
})

const emit = defineEmits<{
  (e: 'update:selected', value: number): void
  (e: 'update:ready', value: boolean): void
}>()

function isTeam1() {
  return selected.value == 0
}

function isTeam2() {
  return selected.value == 1
}

function setTeam1() {
  if (team1.value == undefined || team1.value.length < 2) {
    selected.value = 0
    emit('update:selected', selected.value)
  }
}

function setTeam2() {
  if (team2.value == undefined || team2.value.length < 2) {
    selected.value = 1
    emit('update:selected', selected.value)
  }
}
</script>

<template>
  <div class="teams">
    <div
      class="team"
      @click="setTeam1"
      :class="{ selected: isTeam1() }"
      tabindex="0"
      @keydown.enter="setTeam1()"
      @keydown.space.prevent="setTeam1()"
    >
      <h3>
        Team 1
        <span class="count" :class="{ full: team1.length >= 2 }">{{ team1.length }}/2</span>
      </h3>
      <ul>
        <li v-for="player in team1" :key="player.name">
          {{ player.name }}
        </li>
      </ul>
    </div>

    <div
      class="team"
      @click="setTeam2"
      :class="{ selected: isTeam2() }"
      tabindex="0"
      @keydown.enter="setTeam2()"
      @keydown.space.prevent="setTeam2()"
    >
      <h3>
        Team 2
        <span class="count" :class="{ full: team2.length >= 2 }">{{ team2.length }}/2</span>
      </h3>
      <ul>
        <li v-for="player in team2" :key="player.name">
          {{ player.name }}
        </li>
      </ul>
    </div>
  </div>
</template>

<style>
.teams {
  display: flex;
  gap: 1rem;
  width: 100%;
}

.team {
  flex: 1;
  background-color: var(--color-background-mute);
  display: flex;
  flex-direction: column;
  min-width: 8rem;
  min-height: 5rem;
  width: 100%;
  padding: 0.2rem 0.5rem;

  border: 1px solid #666;
  border-radius: 8px;

  cursor: pointer;
  user-select: none;

  transition:
    border-color 150ms,
    background-color 150ms,
    transform 150ms;
}

.count {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  background: color-mix(in srgb, var(--c-accent) 15%, transparent);
  color: var(--c-accent);
  transition:
    background-color 150ms,
    color 150ms;
}

.count.full {
  background: var(--c-accent);
  color: white;
}

.team h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 0.75rem;
  text-align: center;
  white-space: nowrap;
  justify-content: space-between;
  white-space: nowrap;
}

.team ul {
  margin: 0;
  padding-left: 1.25rem;
}

.team:hover {
  border-color: var(--c-accent-muted);
  transform: translateY(-2px);
}

.team:focus-visible {
  outline: none;
  border-color: var(--c-accent);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--c-accent) 30%, transparent);
}

.selected {
  border-color: var(--c-accent);
  background: rgb(from var(--c-accent) r g b / 10%);
}
</style>
