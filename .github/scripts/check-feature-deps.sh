#!/usr/bin/env bash
# Belt-and-suspenders check: fails CI if any feature module build.gradle.kts
# references another :feature: module as a project dependency.
# The Gradle withDependencies hook in root build.gradle.kts catches this at sync time.
# This script catches it at the file diff level during CI for belt-and-suspenders protection.

set -e

VIOLATIONS=$(grep -rn 'project(":feature:' feature/*/build.gradle.kts 2>/dev/null || true)

if [ -n "$VIOLATIONS" ]; then
  echo "ERROR: Feature-to-feature dependency detected. Feature modules must only depend on :core:* modules."
  echo ""
  echo "Violations found:"
  echo "$VIOLATIONS"
  echo ""
  echo "Move shared types to :core:domain instead."
  exit 1
fi

echo "OK: No feature-to-feature dependencies detected."
