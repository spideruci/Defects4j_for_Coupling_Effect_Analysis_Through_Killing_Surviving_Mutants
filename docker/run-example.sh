#!/usr/bin/env bash
#
# Run the assertion-generation pipeline for a single Defects4J bug and validate
# the generated outputs. Mirrors the "Assertion generation" CI step.
#
# Usage: run-example.sh [PROJECT] [VERSION]
#   PROJECT  Defects4J project id (default: Cli)
#   VERSION  buggy version id     (default: 2b)   e.g. Lang 11b, Math 5b
#
# Exit code is non-zero if the pipeline fails or any expected output is missing.

set -uo pipefail

PROJECT="${1:-Cli}"
VERSION="${2:-2b}"
WORKDIR="${WORKDIR:-/work}"
DIR="${PROJECT}_${VERSION}"

if ! command -v defects4j >/dev/null 2>&1; then
  echo "❌ defects4j is not on PATH"
  exit 1
fi

mkdir -p "$WORKDIR"
cd "$WORKDIR"

echo "=========================================================="
echo "▶ Preparing ${PROJECT}-${VERSION}  (defects4j get_project)"
echo "=========================================================="
# get_project checks out ${DIR}, stages coverages/${DIR} as
# passing_covering_tests (required by `defects4j states`), and copies all
# helper scripts + full_state_analysis.sh into ${DIR}. It requires a
# coverages/${DIR} file to exist (only curated bugs ship one).
if ! defects4j get_project "$PROJECT" "$VERSION"; then
  echo "❌ defects4j get_project ${PROJECT} ${VERSION} failed."
  echo "   (Does coverages/${DIR} exist? Only curated bugs are supported.)"
  exit 1
fi
cd "$DIR" || { echo "❌ working directory ${DIR} was not created"; exit 1; }

echo "=========================================================="
echo "▶ Running assertion-generation pipeline (real bug + mutants)"
echo "=========================================================="
fail=0
if ! bash full_state_analysis.sh; then
  echo "⚠️  full_state_analysis.sh exited non-zero — validating partial outputs below."
  fail=1
fi

echo "=========================================================="
echo "▶ Validating generated assertion outputs"
echo "=========================================================="
check_outcome () {
  local file="$1" label="$2" desc="$3"
  if [[ ! -f "$file" ]]; then
    echo "❌ $desc: $file was not produced"; fail=1; return
  fi
  local n
  n=$(jq --arg o "$label" '[.[] | select(.outcome == $o)] | length' "$file" 2>/dev/null || echo 0)
  echo "• $desc → ${n} entr(y/ies) with outcome=\"$label\""
  if [[ "${n:-0}" -lt 1 ]]; then
    echo "❌ $desc: expected at least one \"$label\" outcome in $file"; fail=1
  else
    echo "✅ $desc"
  fi
}

check_outcome test_outcome.json         accept  "Real-bug-derived assertions (bug-revealing)"
check_outcome test_outcome_mutants.json accept  "Mutant-derived assertions (validated)"
check_outcome detect_real_bugs.json     killing "Mutant-derived assertions detecting the real bug"

if [[ "$fail" -ne 0 ]]; then
  echo "❌ ${PROJECT}-${VERSION}: not all outputs were produced (see the per-check summary above)."
  echo "   Note: some bugs generate real-bug assertions but no mutant-derived detection"
  echo "   (0 killing) — killing surviving mutants does not always detect the real bug."
else
  echo "🎉 ${PROJECT}-${VERSION}: assertion generation completed and validated."
  echo "   Outputs in: ${WORKDIR}/${DIR}"
fi

# ---------------------------------------------------------------------------
# Point the reviewer at how to run more bugs with the SAME image, once the
# first example has finished.
# ---------------------------------------------------------------------------
echo ""
echo "=========================================================="
echo "▶ Run another bug"
echo "=========================================================="
echo "This image runs the same pipeline for any curated Defects4J version"
echo "(one that ships a coverages/<Project>_<Version> file; see the README table):"
echo ""
echo "    docker run --rm coupling-effect:cli-2 run-example.sh <Project> <Version>"
echo ""
echo "  examples:"
echo "    docker run --rm coupling-effect:cli-2 run-example.sh Lang 6b"
echo "    docker run --rm coupling-effect:cli-2 run-example.sh Cli 4b"
echo "=========================================================="

exit "$fail"
