<script setup>
</script>

<template>
    <div class="auth-logo">
        <img src="../../assets/logo.svg" height="200rem" />  <!-- \TODO: Reemplazar icono -->
        <h1>Clinica Piedra Azul</h1>
        <p>Medicina alternativa y bienestar natural</p>
    </div>
</template>

<style scoped>

.auth-card {
  width: 900px;
  max-width: 100%;
  min-height: 550px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
}

.auth-logo {
  background: #6b21a8;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 40px;
}

.auth-logo img {
  filter: invert();
}

.auth-logo img {
  width: 220px;
  height: auto;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .auth-card {
    grid-template-columns: 1fr;
  }

  .auth-logo {
    min-height: 280px;
    padding: 30px;
  }

  .auth-login {
    padding: 35px 25px;
  }
}
</style>