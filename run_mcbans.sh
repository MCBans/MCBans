#!/bin/bash

# Run the MCBans Main class
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
fi

# Run the Main class
# Pass any command line arguments to the Main class
echo "Running MCBans test..."
java -cp "$CLASSPATH" com.mcbans.test.Main "$@"

echo "MCBans test completed."
