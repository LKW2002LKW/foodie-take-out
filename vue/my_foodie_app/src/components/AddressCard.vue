<template>
  <div class="address-card">
    <p>{{ address.name }} {{ address.tel }}</p>
    <p>{{ address.province }}{{ address.city }}{{ address.county }}{{ address.addressDetail }}</p>
  </div>
</template>

<script setup>
defineProps(['address'])
</script>

<style scoped>
.address-card {
  padding: 10px;
  background: white;
  border-radius: 8px;
  margin-bottom: 10px;
}
</style>
