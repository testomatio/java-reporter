echo "Test"
./gradlew clean test --parallel -X

echo "Publish locally"
./gradlew publish

echo "Release"
./gradlew jreleaserReleaseAll --parallel



