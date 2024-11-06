#!/bin/bash

export BACKUP_LOC=/tmp/sanford/$(date +"%Y.%m.%d.%N" | cut -b1-14)
mkdir -p $BACKUP_LOC

# Candidates to be pruned
candidates=(".history" ".github" ".gradle" ".trunk" "config" "deploy-on-tp4cf.sh" "docs" "bin" "build" "README.md")

for item in "${candidates[@]}"; do
    if [ -e "$item" ] || [ -d "$item" ] || compgen -G "$item" > /dev/null; then
        cp -Rf "$item" $BACKUP_LOC
        rm -Rf "$item"
    fi
done
