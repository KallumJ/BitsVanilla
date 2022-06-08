#!/bin/bash

ROOT_DIR="$(pwd)/bits"

mkdir -p "$ROOT_DIR"

git clone "git@hogwarts.bits.team:Bits/BitsVanilla.git" "$ROOT_DIR/BitsVanilla" || exit
git clone "git@hogwarts.bits.team:Bits/Nibbles.git" "$ROOT_DIR/Nibbles" || exit
git clone "git@hogwarts.bits.team:Bits/PlayerAPI.git" "$ROOT_DIR/PlayerAPI" || exit
git clone "git@hogwarts.bits.team:Bits/WarpAPI.git" "$ROOT_DIR/WarpAPI" || exit
git clone "git@hogwarts.bits.team:Bits/ChatAPI.git" "$ROOT_DIR/ChatAPI" || exit
git clone "git@hogwarts.bits.team:Bits/ServerAPI.git" "$ROOT_DIR/ServerAPI" || exit

cd "$ROOT_DIR/PlayerAPI" || exit
mvn clean package -P production || exit
docker build -t "player-api:dev" . || exit

cd "$ROOT_DIR/WarpAPI" || exit
mvn clean package -P production || exit
docker build -t "warp-api:dev" . || exit

cd "$ROOT_DIR/ChatAPI" || exit
mvn clean package -P production || exit
docker build -t "chat-api:dev" . || exit

cd "$ROOT_DIR/ServerAPI" || exit
mvn clean package -P production || exit
docker build -t "server-api:dev" . || exit

cd "$ROOT_DIR/BitsVanilla" || exit
git checkout 1.19 || exit
cd "$ROOT_DIR/BitsVanilla/docker" || exit
docker-compose up -d || exit
