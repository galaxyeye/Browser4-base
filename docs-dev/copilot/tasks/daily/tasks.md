# Tasks

## Prerequisites

## Bugs
Run if [ "failed" != "success" ]; then
if [ "failed" != "success" ]; then
echo "❌ Tests failed with status: failed"
echo "📊 Test Results:"
echo "  - Total Tests: 1589"
echo "  - Failed Tests: 0"
echo "  - Passed Tests: 1568"
echo "  - Skipped Tests: 21"
exit 1
else
echo "✅ All tests passed successfully"
echo "📊 Test Results: 1568 tests passed"
fi
shell: /usr/bin/bash -e {0}
env:
IMAGE_NAME: browser4
NETWORK_NAME: browser4_backend
CONTAINER_NAME: browser4-test
DOCKER_COMPOSE_FILE: ./docker-compose.yml
DEPENDENCY_SERVICES: mongodb
MONGODB_CONTAINER: mongodb
MONGODB_PORT: 27017
JAVA_VERSION: 17
MAVEN_OPTS: -Xmx3g -XX:+UseG1GC
JAVA_HOME: /opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/17.0.18-8/x64
JAVA_HOME_17_X64: /opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/17.0.18-8/x64
VERSION: 4.5.0-rc.1
SERVICES_START_TIME: 1770787528
MAVEN_CMD: ./mvnw
❌ Tests failed with status: failed
📊 Test Results:
- Total Tests: 1589
- Failed Tests: 0
- Passed Tests: 1568
- Skipped Tests: 21
  Error: Process completed with exit code 1.
### Skill

## Features

### Improve



## Docs

## Notes

