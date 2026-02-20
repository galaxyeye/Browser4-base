#!/usr/bin/env bash


repoRoot=$(cd "$(dirname "$0")">/dev/null || exit 1; pwd)
while [[ ! -f "$repoRoot/VERSION" && "$repoRoot" != "/" ]]; do
  repoRoot=$(dirname "$repoRoot")
done
[[ -f "$repoRoot/VERSION" ]] && cd "$repoRoot" || exit 1

"$repoRoot"/bin/build/build.sh "$@"

"$repoRoot"/bin/tools/install-depends.sh

"$repoRoot"/bin/browser4.sh "$@"
