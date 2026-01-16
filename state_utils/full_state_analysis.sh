#!/bin/bash

# Find the full path to the defects4j executable
defects4j_bin=$(which defects4j 2>/dev/null)

if [ -z "$defects4j_bin" ]; then
  echo "❌ defects4j not found in PATH."
  exit 1
fi

echo "updating helper scripts"

# Get the parent directory (usually .../defects4j/framework/bin -> .../defects4j)
defects4j_root=$(dirname "$(dirname "$(dirname "$defects4j_bin")")")
folder_utils="${defects4j_root}/state_utils/folder_utils"

cp "${folder_utils}/analyzeOracle.py" analyzeOracle.py
cp "${folder_utils}/transform.jar" transform.jar
cp "${folder_utils}/verify_all_test_mutant.sh" verify_all_test_mutant.sh
cp "${folder_utils}/verify_all_tests.sh" verify_all_tests.sh
cp "${folder_utils}/verify_detect_bugs.sh" verify_detect_bugs.sh
cp "${folder_utils}/verify_new_test_bug.sh" verify_new_test_bug.sh
cp "${folder_utils}/verify_new_test_mutant.sh" verify_new_test_mutant.sh
cp "${folder_utils}/verify_one_test_detect_bug.sh" verify_one_test_detect_bug.sh

rm -rf time.json
rm -rf all_states
rm -rf target


defects4j patch -b
defects4j states
defects4j generate_specification
java -jar "transform.jar"
defects4j update_oracle_helper -b
./verify_all_tests.sh
./verify_all_tests_repeat.sh


file="test_outcome.json"
# Check if file exists
if [[ ! -f "$file" ]]; then
  echo "❌ No bug detecting tests are generated on bug analysis"
  exit 1
fi

# Check if any object has outcome == "accept"
if jq -e '.[] | select(.outcome == "accept")' "$file" >/dev/null 2>&1; then
  echo "✅ Found at least one bug revealing test."
else
  echo "❌ No bug detecting tests are generated on bug analysis"
  exit 1
fi



defects4j patch -f
defects4j test -c
defects4j mutation_analysis
defects4j compute_surviving_to_run
defects4j states_on_mutants -m -1
defects4j generate_specification -m
java -jar "transform.jar" -m
defects4j update_oracle_helper -m
./verify_all_test_mutant.sh
./verify_all_test_mutant_repeat.sh
defects4j patch -b
./verify_detect_bugs.sh



