#!/bin/bash

# Run the MCBans BanTest class
# This will also compile the code if needed

# Change to the project directory (if running from elsewhere)
cd "$(dirname "$0")"

# Build the project to ensure it's compiled
echo "Building project..."
if ! ./gradlew build; then
    echo "Build failed. Exiting."
    exit 1
fi
echo "Build completed successfully."

# Set up the classpath
CLASSPATH="build/classes/java/main"

# Add all JAR files in the libs directory to the classpath
for jar in libs/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

# Run the BanTest class
# Pass any command line arguments to the BanTest class
echo "Running MCBans Ban Test..."
java -cp "$CLASSPATH" com.mcbans.test.BanTest "$@"

echo "MCBans Ban Test completed."