#!/bin/bash


if [ $# -lt 2 ]; then
    echo "Usage: $0 <mutant_id> <assertion_number>"
    exit 1
fi

fixed_outcome=false

compilation_failure=false


LOG_FILE="compile_errors.log"
#
#
# Take the assertion number from the first argument
m_id="$1"
num="$2"

## Path to your JSON file (dynamic based on input number)
json_file="mutant_oracle_specification/${m_id}/assertion_${num}.json"

test_name=$(jq -r '.test_name' "$json_file")
class_name=$(jq -r '.simple_class_name' "$json_file")
original_copy_path="oracles_mutants/before/${class_name}_before.java"
full_file_name="${test_name%%::*}"
new_file_name="oracles_mutants/${num}__${class_name}.java"
#
#
original_file_Path="$(printf '%s' "$full_file_name" | tr '.' '/')".java
## Decide the base directory depending on existence
if [ -d "src/test/java" ]; then
    full_original_file_Path="src/test/java/$original_file_Path"
else
    full_original_file_Path="src/test/$original_file_Path"
fi

#
#
cp -f "$new_file_name" "$full_original_file_Path"

rm -rf target
defects4j_exec=$(which defects4j)
# Go up three levels: defects4j → bin → framework → defects4j-3.0.1
defects4j_root=$(dirname "$(dirname "$(dirname "$defects4j_exec")")")
#
pid=$(grep '^pid=' .defects4j.config | cut -d'=' -f2)
vid=$(grep '^vid=' .defects4j.config | cut -d'=' -f2 | sed 's/b$//')

OUTPUT=$(defects4j compile 2>&1)
#defects4j compile
# Check if it contains the error message
if echo "$OUTPUT" | grep -q "Cannot compile tests!"; then
    echo "ERROR: Compilation failed for tests at $(date)" >> "$LOG_FILE"
    echo "  LOC: prepare_new_test.sh for assertion $num" >> "$LOG_FILE"
    echo "  OUTPUT: $OUTPUT" >> "$LOG_FILE"
    echo "  compilation failure for assertion $num"
fi

OUTPUT=$(defects4j test -t "$test_name" 2>&1)
if  echo "$OUTPUT" | grep -q "Failing tests: 0"; then
    fixed_outcome=true
#    echo "passing : )"
#    echo "  LOC: verify_new_test.sh for assertion $num" >> "$LOG_FILE"
fi
echo $OUTPUT

## fall back to the original file

cp -f "$original_copy_path" "$full_original_file_Path"


