import{runPage}from'./pages.js';
window.addEventListener('DOMContentLoaded',()=>runPage().catch(e=>{console.error(e);document.body.innerHTML=`<main class="container section"><div class="card"><h1>Frontend error</h1><p>${e.message}</p></div></main>`}));
