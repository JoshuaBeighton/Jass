<script setup lang="ts">

import {ref } from 'vue'
import TeamInfo from './TeamInfo.vue';

const name = ref('')
const idx = ref<number | undefined>()

async function login(){
    const host = window.location.hostname

    await fetch(`http://${host}:9000/player`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: name.value,
      idx: idx.value
    })
  })
}

</script>

<template>
<div class="loginCard">
    <div class="loginCard">
        <input v-model="name" type="text" placeholder="Enter Name">
        <TeamInfo v-model:selected="idx"></TeamInfo>
        <button v-on:click="login">Go!</button>
    </div>
</div>
</template>

<style>
    input, button{
        font-size: large;
        background-color: var(--color-background-mute);
        border-radius: 5px;
        padding: 2px;
        border: 2px;
        border-style: solid;
        border-color: var(--color-border);
        color: var(--color-text);
    }


    input::placeholder{
        color: var(--color-text);
    }

    input:hover, button:hover{
        border-color: var(--color-border-hover);
    }

    .loginCard{
        margin-top: 2rem;
        display: flex;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        gap: 1rem;
    }

    .inputs{
        display: flex;
        flex-direction: column;
        gap: 2rem;
    }
</style>