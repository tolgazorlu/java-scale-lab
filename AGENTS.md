# Scale Lab: agent rules

This repo is a LEARNING lab. The code is naive on purpose.

## If you are helping a learner (the usual case)

Read and follow `.agents/skills/scale-lab-mentor/SKILL.md` before you answer anything about a
mission. In short: **do not give the answer.** Ask, measure, hint one step at a time. The learner
writes the fix. Solutions live on the `reference` branch for when they insist.

## Maintainer rules

- Never fix anything marked `LAB:` unless the user is doing that mission and asks for it.
  Missing indexes, N+1, the stock race, the payment call inside the transaction, the client
  without timeouts are the curriculum.
- New missions are written by the maintainer (Hermes skill `java-scale-lab`, if you have it). Each mission is two English files: `lessons/NN-slug.md` (the concept, 10-15 min
  read) and `labs/NN-slug.md` (the hands-on part), both based on numbers actually measured on this
  machine. Runnable commands sit alone on a line in a bash block starting with `./lab`, because
  dacospace turns those lines into Run buttons.
- dacospace (`~/projects/dacospace`) is the UI for this repo. It parses the `# Mission NN · Title`
  and `# Lesson NN · Title` headings and the PROGRESS.md table: keep those formats stable.
- Ports: app 4180 (`LAB_APP_PORT`), Grafana 4182, Prometheus 4183, Postgres 4184, OTLP 4188,
  payment mock 4189, extra instances 4190-4191.
- The `reference` branch holds one measured solution commit per mission. Never merge it into main.
- Build with Java 25: `./lab` sets JAVA_HOME from SDKMAN. `./lab test` runs the suite.
- Commits: English, one sentence, `type: subject`, no AI attribution.
