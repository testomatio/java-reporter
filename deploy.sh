echo "Test"
./gradlew clean test --parallel

echo "Publish locally"
./gradlew publish

echo "Release"
./gradlew jreleaserReleaseAll --parallel



