<script setup lang="ts">

import {ref } from 'vue'

const cardClass = ref('card')
const redClass = ref('red')
const blackClass = ref('black')

function isRed(){
    return props.cardText.endsWith('♦') || props.cardText.endsWith('♥')
}

const props = defineProps<{
    cardText: string;
}>();

</script>

<template>
<div :class="cardClass">
    <p v-bind:class="{ red: isRed(), black: !isRed() }">{{ props.cardText }}</p>
</div>

</template>

<style>
.card{
    width: 100px;
    height:150px;
    background-color: azure;
    padding: 5px;
    margin: 5px;
    display: flexbox;
    justify-content: center;
    align-items: center;
}

.red{
    color: red;
}

.black{
    color: black;
}

.red, .black{
    text-align: center;
    font-size: 30pt;
}
</style>