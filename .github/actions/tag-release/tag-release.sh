#!/usr/bin/env bash

GITHUB_USER_TOKEN=$1

if [[ -z GITHUB_USER_TOKEN ]]; then
  echo "Missing GITHUB_USER_TOKEN"
  exit 1
fi

if [[ -z $GITHUB_RUN_NUMBER ]]; then
  echo "Missing GITHUB_RUN_NUMBER"
  exit 1
fi

git config user.name "${GITHUB_ACTOR}"
git config user.email "${GITHUB_ACTOR}@users.noreply.github.com"

if [[ -n $RELEASE_VERSION ]]; then
  releaseVersion=$RELEASE_VERSION
  echo "$RELEASE_VERSION" > app.version
else
  buildNumber=$GITHUB_RUN_NUMBER
  majorVersion=`cat version.sbt | cut -d\" -f2 | sed 's/.0-SNAPSHOT//'`
  releaseVersion="${majorVersion}.${buildNumber}"
  echo "version in ThisBuild := \"$releaseVersion\"" > version.sbt
fi

git remote set-url origin https://x-access-token:GITHUB_USER_TOKEN@github.com/$GITHUB_REPOSITORY

echo "Tagging Release Version: $releaseVersion"

git checkout -b "release-$releaseVersion"

git add .
git commit -m "[CI/CD] Release v$releaseVersion"

git tag "v$releaseVersion"
git push origin "v$releaseVersion"