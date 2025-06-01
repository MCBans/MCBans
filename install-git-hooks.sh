#!/bin/bash

# Create the git hooks directory if it doesn't exist
mkdir -p .git/hooks

# Copy the pre-commit hook
cp git-hooks/pre-commit .git/hooks/

# Make the hook executable
chmod +x .git/hooks/pre-commit

echo "Git hooks installed successfully!"