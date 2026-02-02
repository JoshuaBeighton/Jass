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
    <div class="team" @click="setTeam1" :class="{ selected: isTeam1() }">
      <h3>Team 1</h3>
      <ul>
        <li v-for="player in team1" :key="player.name">
          {{ player.name }}
        </li>
      </ul>
    </div>

    <div class="team" @click="setTeam2" :class="{ selected: isTeam2() }">
      <h3>Team 2</h3>
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
  flex-direction: row;
  width: 100%;
  gap: 2rem;
}

.team {
  padding: 1rem;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.team h3 {
  margin-top: 0;
}

.team:hover {
  border-color: var(--c-accent-muted);
}

.selected {
  border-color: var(--c-accent);
}
</style>
