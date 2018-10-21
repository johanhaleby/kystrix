#!/bin/sh
java -version
echo
read -p "Are you using JDK 8? [y/N]" -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    read -p "Enter the previous version: " oldVersion
    read -p "Enter the current build version (without snapshot): " currentVersion
    read -p "Enter the version to release: " releaseVersion
    read -p "Enter the next build version (without snapshot): " nextVersion
    echo "Starting to release Kystrix $releaseVersion" && \
    git pull --rebase && \
    ./kobaltw clean test && \
    echo "Changing Build version to $releaseVersion" && \
    sed -i "" "s/val kystrix = \"${currentVersion}-SNAPSHOT\"/val kystrix = \"${releaseVersion}\"/g" kobalt/src/Build.kt && \
    echo "Updating README.md" && \
    sed -i "" "s/${oldVersion}/${releaseVersion}/g" README.md && \
    echo "Pushing changes to git" && \
    git ci -am "Preparing for release ${releaseVersion}" && \
    git push && \
    echo "Assembling build artifacts" && \
    ./kobaltw clean assemble && \
    echo "Will create and push git tags.." && \
    git tag -a "${releaseVersion}" -m "Released ${releaseVersion}" && \
    git push --tags && \
    echo "Upload to Bintray.." && \
    ./kobaltw uploadBintray && \
    echo "Bintray upload completed, will update Build version.." && \
    sed -i "" "s/val kystrix = \"${releaseVersion}\"/val kystrix = \"${nextVersion}-SNAPSHOT\"/g" kobalt/src/Build.kt && \
    git ci -am "Setting build version to ${nextVersion}-SNAPSHOT" && \
    git push && \
    echo "Release of Kystrix $releaseVersion completed successfully! Login to Bintray and sync both projects to Maven central!"
fi