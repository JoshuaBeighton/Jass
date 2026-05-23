<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Player from './Player.vue';

const props = defineProps<{ game: string; name: string }>()

const players = ref([props.name, 'loading', 'loading', 'loading'])

const nextPlayer = ref('')

const leftCard = ref(undefined)
const topCard = ref(undefined)
const rightCard = ref(undefined)
const bottomCard = ref(undefined)

const firstPlayer = ref('')

const meIdx = ref(-1)

const isMe = ref(false)

const count = ref(-1)
const emits = defineEmits<{
    (e: 'update:isme', value: boolean): void
}>()

async function getPlayers() {
    const host = window.location.hostname

    try {
        const res = await fetch(`http://${host}:9000/player/`)
        if (!res.ok) throw new Error('Network response was not OK')

        const data = await res.json()

        for (let i = 0; i < data.length; i++) {
            if (data[i].name == props.name) {
                meIdx.value = i
            }
        }
        for (let i = 0; i < 4; i++) {
            players.value[i] = data[(i + meIdx.value) % 4].name
        }
    } catch (err) {
        console.error('Error fetching players:', err)
    }
}

async function getNextCard() {
    const host = window.location.hostname
    while (true) {
        try {
            const res = await fetch(`http://${host}:9000/cardWait/${count.value}`)
            if (!res.ok) throw new Error('Network response was not OK')

            const data = await res.json()
            nextPlayer.value = data.next
            firstPlayer.value = data.start
            updateCard(data.currentTrick)
            isMe.value = nextPlayer.value == props.name
            emits('update:isme', isMe.value)
            count.value++
        } catch (err) {
            console.error('Error fetching players:', err)
        }
    }
}

function updateCard(cards: [any]) {
    const startIndex = players.value.indexOf(firstPlayer.value)
    let mostRecent = cards[cards.length - 1];
    let mostRecentIdx = cards.length - 1;
    let n = (startIndex + mostRecentIdx) % 4; 
    console.log(n);
    switch(n){
        case 0:
            bottomCard.value = mostRecent;
            break;
        case 1:
            rightCard.value = mostRecent;
            break;
        case 2:
            topCard.value = mostRecent;
            break;
        case 3:
            leftCard.value = mostRecent;
    }
}

function isPlayer(index: number) {
    return players.value[index] == nextPlayer.value
}

onMounted(() => {
    getPlayers()
    getNextCard()
})

</script>

<template>
    <h1>{{ props.game }}</h1>
    <div class="parent">
        <div class="mat">
            <Player class="bottom" :card="bottomCard" :name="players[0]" :up-next="isPlayer(0)"></Player>
            <Player class="right" :card="rightCard" :name="players[1]" :up-next="isPlayer(1)"></Player>
            <Player class="top" :card="topCard" :name="players[2]" :up-next="isPlayer(2)"></Player>
            <Player class="left" :card="leftCard" :name="players[3]" :up-next="isPlayer(3)"></Player>
        </div>
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
    position: relative;
    background-color: darkgreen;
    border-radius: 15px;
    width: 500px;
    height: 500px;
    border: 2px solid green;
}

.mat p {
    position: absolute;
    margin: 0;
}

/* Bottom center */
.bottom {
    bottom: 10px;
    left: 50%;
    transform: translateX(-50%);
}

/* Top center */
.top {
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
}

/* Left center */
.left {
    left: 10px;
    top: 50%;
    transform: translateY(-50%);
}

/* Right center */
.right {
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
}


.upNext {
    color: red;
}
</style>
