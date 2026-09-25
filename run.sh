#!/bin/bash

./mvnw spring-boot:run & >/dev/null

echo "Esperando Spring Boot..."


echo "✓ Spring Boot listo"

npm run --prefix frontend/ dev &

echo "Esperando Vue..."

until curl -sf http://localhost:5173/ > /dev/null 2>&1; do
  sleep 1
done

echo "✓ Vue listo"

xdg-open http://localhost:5173/

wait

