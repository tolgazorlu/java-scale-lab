#!/usr/bin/env bash
# Maintainer script: rebuild the start/NN branches from main + the reference branch.
#
# start/NN = main with the reference solutions of every mission before NN applied, so a learner can
# begin at mission NN without doing the earlier ones (./lab jump NN). Run it after changing main or
# reference, then: git push -f origin 'refs/heads/start/*'
#
# The reference branch must hold exactly one commit per mission, in mission order, starting at the
# first mission in FIRST_SOLVED below.
set -euo pipefail
cd "$(dirname "$0")/.."
[ -z "$(git status --porcelain --untracked-files=no)" ] || { echo "working tree not clean"; exit 1; }
FIRST_SOLVED=3          # missions 01 and 02 change no code
here=$(git rev-parse --abbrev-ref HEAD)
fixes=()
while read -r c; do fixes+=("$c"); done < <(git rev-list --reverse "$(git merge-base main reference)..reference")
written=$(ls labs | grep -cE '^[0-9]{2}-')
echo "${#fixes[@]} reference commits, $written missions written"

git branch -f start/01 main
for ((nn = 2; nn <= written; nn++)); do
  name=$(printf 'start/%02d' "$nn")
  solved=$((nn - FIRST_SOLVED))          # how many reference commits come before mission nn
  git switch -q -C "$name" main
  for ((i = 0; i < solved; i++)); do
    [ -n "${fixes[$i]:-}" ] || { echo "missing reference commit for mission $((i + FIRST_SOLVED))"; exit 1; }
    git cherry-pick -q --allow-empty "${fixes[$i]}" >/dev/null
  done
  echo "$name  $(git log -1 --format=%s)"
done
git switch -q "$here"
