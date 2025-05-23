# Extract the application version from gradle.properties
APP_VERSION=$(cat gradle.properties | grep 'appVersion' | cut -d'=' -f2)

# Run Gradle to clean and build the farm-server distribution zip
./gradlew :farm-cli-client:distZip

# Unzip the generated distribution into the deploy directory
unzip farm-cli-client/build/app/distributions/farm-cli-client-$APP_VERSION.zip -d deploy

# Rename the extracted folder for consistency
mv deploy/farm-cli-client-$APP_VERSION deploy/farm-cli-client

# Add the farm-server binary directory to the system PATH
export PATH=$PATH:$(pwd)/deploy/farm-cli-client/bin
