#!/bin/bash

BUILD_DIR=$("pwd")
SERVER_DIR="$BUILD_DIR/prod-server"
BITS_JAR="$BUILD_DIR/build/libs/bits-vanilla-fabric-$1.jar"
JAVA_PATH="/usr/lib/jvm/java-16-oracle/bin/java"

# setup a directory for the server
rm -rf "$SERVER_DIR"
mkdir "$SERVER_DIR"
cd "$SERVER_DIR" || exit

# download and install fabric
wget -O "$SERVER_DIR/fabric-installer.jar" "https://maven.fabricmc.net/net/fabricmc/fabric-installer/0.7.4/fabric-installer-0.7.4.jar"
$JAVA_PATH -jar "$SERVER_DIR/fabric-installer.jar" server -downloadMinecraft

# download the required mods
mkdir "mods"
cd "mods" || exit
cp "$BITS_JAR" "."
python3 "$BUILD_DIR/test/mods_update.py"
cd "$SERVER_DIR" || exit

# prepare config files
echo "eula=true" >> "eula.txt"
echo "server-name=local" >> "server.properties"

# use a random screen name to prevent conflicts if
# there are builds running at the same time
SCREEN_NAME=$(echo $RANDOM | md5sum | head -c 20; echo;)

# start the server
echo "Starting server ($SCREEN_NAME)..."
screen -dmS "$SCREEN_NAME" $JAVA_PATH -jar "fabric-server-launch.jar" --nogui

# give the server a moment to get started
sleep 5

echo "Waiting for server result..."
success=false
# keep looping until we find a reason to exit
while true
do
  # if the screen dies, we fail
  if ! screen -list | grep -q "\.$SCREEN_NAME"; then
    success=false
    break
  fi
  # if the word "Exception" is found in the log, we fail
  if grep -q "Exception" "$SERVER_DIR/logs/latest.log"; then
    screen -X -S "$SCREEN_NAME" quit
    success=false
    break
  fi
  # if we see the "Done" message in the log, we succeed
  if grep -q " Done (" "$SERVER_DIR/logs/latest.log"; then
    screen -X -S "$SCREEN_NAME" quit
    success=true
    break
  fi
  sleep 2
done

cd "" || exit

if [ "$success" = true ]; then
  echo "Done!"
  rm -rf "$SERVER_DIR"
  exit 0
else
  # if we fail, print the full log to the console output
  cat "$SERVER_DIR/logs/latest.log"
  echo "## PRODUCTION SERVER FAIL ##"
  rm -rf "$SERVER_DIR"
  exit 1 # exit with a non-zero exit code so Jenkins fails this build
fi
