# Extract the application version from gradle.properties
APP_VERSION=$(cat gradle.properties | grep 'appVersion' | cut -d'=' -f2)

# Run Gradle to clean and build the farm-server distribution zip
./gradlew :farm-server:distZip

# Unzip the generated distribution into the deploy directory
unzip farm-server/build/app/distributions/farm-server-$APP_VERSION.zip -d deploy

# Rename the extracted folder for consistency
mv deploy/farm-server-$APP_VERSION deploy/farm-server

# Add the farm-server binary directory to the system PATH
export PATH=$PATH:$(pwd)/deploy/farm-server/bin

# Start the farm-server with specific configuration
# Start the farm-server with specific configuration
farm-server --max_amount 7 \
  --start_port 10000 \
  --end_port 11000 \
  --keep_alive_devices 32=7 \
  --device_busy_timeout 3600 \
  --img 32=yelisseyoshlokov/android-emulator:5.0 \


