#!/bin/bash

# --- Usage check ---
if [ $# -lt 1 ]; then
    echo "Usage: $0 <assertion_number>"
    exit 1
fi

fixed_outcome=false
buggy_outcome=false
compilation_failure=false


LOG_FILE="compile_errors.log"


# Take the assertion number from the first argument
num="$1"

# Path to your JSON file (dynamic based on input number)
json_file="oracle specification/assertion_${num}.json"

# Extract fields with jq
test_name=$(jq -r '.test_name' "$json_file")
class_name=$(jq -r '.simple_class_name' "$json_file")
original_copy_path="oracles/before/${class_name}_before.java"
full_file_name="${test_name%%::*}"
new_file_name="oracles/${num}__${class_name}.java"


original_file_Path="$(printf '%s' "$full_file_name" | tr '.' '/')".java
full_original_file_Path=
# Decide the base directory depending on existence
if [ -d "src/test/java" ]; then
    full_original_file_Path="src/test/java/$original_file_Path"
else
    full_original_file_Path="src/test/$original_file_Path"
fi


cp -f "$new_file_name" "$full_original_file_Path"

PATCH_FILE="2.src.patch"



defects4j_exec=$(which defects4j)
# Go up three levels: defects4j → bin → framework → defects4j-3.0.1
defects4j_root=$(dirname "$(dirname "$(dirname "$defects4j_exec")")")

pid=$(grep '^pid=' .defects4j.config | cut -d'=' -f2)
vid=$(grep '^vid=' .defects4j.config | cut -d'=' -f2 | sed 's/b$//')
PATCH_FILE="${defects4j_root}/framework/projects/${pid}/patches/${vid}.src.patch"



# from buggy to fixed
patch -p1 -R -i $PATCH_FILE >/dev/null 2>&1

# should fail on the buggy program
# Run defects4j compile and capture both stdout and stderr
OUTPUT=$(defects4j compile 2>&1)
#defects4j compile
# Check if it contains the error message
if echo "$OUTPUT" | grep -q "Cannot compile tests!"; then
    echo "ERROR: Compilation failed for tests at $(date)" >> "$LOG_FILE"
    echo "  LOC: prepare_new_test.sh for assertion $num" >> "$LOG_FILE"
    echo "  OUTPUT: $OUTPUT" >> "$LOG_FILE"
    echo "  compilation failure for assertion $num"
fi


OUTPUT=$(Defects4j test -t "$test_name" 2>&1)
if  echo "$OUTPUT" | grep -q "Failing tests: 0"; then
    fixed_outcome=true
#    echo "  LOC: verify_new_test.sh for assertion $num" >> "$LOG_FILE"
fi

## from fixed to buggy
patch -p1 -i $PATCH_FILE >/dev/null 2>&1

# should fail on the buggy program
OUTPUT=$(defects4j compile 2>&1)
# Check if it contains the error message
if echo "$OUTPUT" | grep -q "Cannot compile tests!"; then
    echo "ERROR: Compilation failed for tests at $(date)" >> "$LOG_FILE"
    echo "  LOC: prepare_new_test.sh for assertion $num" >> "$LOG_FILE"
    echo "  OUTPUT: $OUTPUT" >> "$LOG_FILE"
    echo "  compilation failure for assertion $num"
fi



OUTPUT=$(Defects4j test -t "$test_name" 2>&1)
# Check if it passes
if ! echo "$OUTPUT" | grep -q "Failing tests: 0"; then
    buggy_outcome=true
#    echo "  LOC: verify_new_test.sh for assertion $num" >> "$LOG_FILE"
fi



# fall back to the original file
cp -f "$original_copy_path" "$full_original_file_Path"





if [[ "$buggy_outcome" == true && "$fixed_outcome" == true ]]; then
    echo "accept assertion $num"
    outcome="accept"
else
    echo "reject assertion $num, buggy_outcome=$buggy_outcome, fixed_outcome=$fixed_outcome"
    outcome="reject"
fi

# Ensure file exists and is initialized
if [[ ! -f test_outcome.json ]]; then
    echo "[]" > test_outcome.json
fi

# Append new record
jq --arg num "$num" \
   --arg outcome "$outcome" \
   --arg buggy "$buggy_outcome" \
   --arg fixed "$fixed_outcome" \
   '. += [{"assertion": $num, "outcome": $outcome, "buggy_outcome": $buggy, "fixed_outcome": $fixed}]' \
   test_outcome.json > tmp.json && mv tmp.json test_outcome.json

