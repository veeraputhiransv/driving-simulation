#!/usr/bin/env bash
# Launches the Driving Simulation CLI.
# Fresh process every time — no persisted car/field state between runs.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

# Prefer an explicit JDK if the machine has Homebrew OpenJDK installed.
if [[ -z "${JAVA_HOME:-}" ]]; then
  for candidate in \
      /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
      /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
      /usr/libexec/java_home; do
    if [[ -x "$candidate" ]]; then
      if [[ "$candidate" == */java_home ]]; then
        JAVA_HOME="$("$candidate" 2>/dev/null || true)"
      else
        JAVA_HOME="$candidate"
      fi
      [[ -n "${JAVA_HOME:-}" ]] && export JAVA_HOME && break
    fi
  done
fi

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
  export PATH="${JAVA_HOME}/bin:${PATH}"
fi

if ! command -v java >/dev/null 2>&1; then
  echo "Java is required on PATH (JAVA_HOME recommended)." >&2
  exit 1
fi

if [[ ! -x "./gradlew" ]]; then
  echo "Missing Gradle wrapper (./gradlew). Restore it before running." >&2
  exit 1
fi

# Build (downloads deps on first run) then start the interactive CLI.
./gradlew --quiet installDist
exec "./build/install/driving-simulation/bin/driving-simulation"
