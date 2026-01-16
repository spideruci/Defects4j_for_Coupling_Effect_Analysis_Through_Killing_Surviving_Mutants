#!/bin/bash

# --- Usage check ---
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
#
# Extract fields with jq
test_name=$(jq -r '.test_name' "$json_file")
class_name=$(jq -r '.simple_class_name' "$json_file")
full_file_name="${test_name%%::*}"
original_copy_path="oracles_mutants/before/${class_name}_before_${full_file_name}.java"

if [ ! -f "$original_copy_path" ]; then
    original_copy_path="oracles_mutants/before/${class_name}_before.java"
fi


new_file_name="oracles_mutants/${num}__${class_name}.java"
#
#
original_file_Path="$(printf '%s' "$full_file_name" | tr '.' '/')".java
## Decide the base directory depending on existence

if [ -d "source" ]; then 
    full_original_file_Path="tests/$original_file_Path"
elif [ -d "gson/src/test/java" ]; then
    full_original_file_Path="gson/src/test/java/$original_file_Path"
elif [ -d "src/test/java" ]; then
    full_original_file_Path="src/test/java/$original_file_Path"
elif [ -d "src/test" ]; then
    full_original_file_Path="src/test/$original_file_Path"
elif [ -d "test" ]; then
    full_original_file_Path="test/$original_file_Path"
else
    echo "❌ Error: None of the test directories exist (src/test/java, src/test, tests)"
    exit 1
fi


# if [ -d "src/test/java" ]; then
#     full_original_file_Path="src/test/java/$original_file_Path"
# else
#     full_original_file_Path="src/test/$original_file_Path"
# fi

#
#
cp -f "$new_file_name" "$full_original_file_Path"
#
#
#
#

rm -rf target
rm -rf build-tests
rm -rf build
defects4j_exec=$(which defects4j)
# Go up three levels: defects4j → bin → framework → defects4j-3.0.1
defects4j_root=$(dirname "$(dirname "$(dirname "$defects4j_exec")")")
#
pid=$(grep '^pid=' .defects4j.config | cut -d'=' -f2)
vid=$(grep '^vid=' .defects4j.config | cut -d'=' -f2 | sed 's/b$//')
#
#
#
### from fixed to buggy (no -R)
#patch -p1 -i $PATCH_FILE >/dev/null 2>&1
#

# currently it's fixed program.

## should fail on the buggy program
## Run defects4j compile and capture both stdout and stderr
OUTPUT=$(defects4j compile 2>&1)
#defects4j compile
# Check if it contains the error message
if echo "$OUTPUT" | grep -q "Cannot compile tests!"; then
    echo "ERROR: Compilation failed for tests at $(date)" >> "$LOG_FILE"
    echo "  LOC: prepare_new_test.sh for assertion $num" >> "$LOG_FILE"
    echo "  OUTPUT: $OUTPUT" >> "$LOG_FILE"
    echo "  compilation failure for assertion $num"
fi
#
#
fixed_outcome=false

# Run with a 45s time limit
OUTPUT=$(timeout 45s defects4j test -t "$test_name" 2>&1)
EXIT_STATUS=$?

if [ "$EXIT_STATUS" -eq 124 ]; then
    # Timed out -> keep fixed_outcome=false
    fixed_outcome=false
elif [ "$EXIT_STATUS" -eq 0 ] && echo "$OUTPUT" | grep -q "Failing tests: 0"; then
    # Completed successfully and all tests passed
    fixed_outcome=true
fi


#echo $OUTPUT

rm -rf target
rm -rf build-tests
rm -rf build
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS (BSD sed requires empty backup extension)
  sed -i '' "31s|.*|    public static int __M_NO = ${m_id};|" src/test/major/mutation/Config.java >/dev/null 2>&1
  sed -i '' "31s|.*|    public static int __M_NO = ${m_id};|" src/test/java/major/mutation/Config.java >/dev/null 2>&1
  sed -i '' "31s|.*|    public static int __M_NO = ${m_id};|" tests/major/mutation/Config.java >/dev/null 2>&1
  sed -i '' "31s|.*|    public static int __M_NO = ${m_id};|" gson/src/test/java/major/mutation/Config.java >/dev/null 2>&1
else
  # Linux / GNU sed
  sed -i "31s|.*|    public static int __M_NO = ${m_id};|" src/test/major/mutation/Config.java >/dev/null 2>&1
  sed -i "31s|.*|    public static int __M_NO = ${m_id};|" src/test/java/major/mutation/Config.java >/dev/null 2>&1
  sed -i "31s|.*|    public static int __M_NO = ${m_id};|" tests/major/mutation/Config.java >/dev/null 2>&1
  sed -i "31s|.*|    public static int __M_NO = ${m_id};|" gson/src/test/java/major/mutation/Config.java >/dev/null 2>&1
fi

OUTPUT=$(defects4j compile 2>&1)

find bin -exec touch {} +
if [ -d "source" ]; then
    # Case 1: source/ exists
    rsync -av --include='*/' --include='*.class' --exclude='*' bin/ build/ >/dev/null 2>&1

elif [ -d "build/classes" ]; then
    # Case 2: build/classes/ exists
    rsync -av --include='*/' --include='*.class' --exclude='*' bin/ build/classes/ >/dev/null 2>&1
else
    # Case 4: default fallback
    rsync -av --include='*/' --include='*.class' --exclude='*' bin/ target/classes/ >/dev/null 2>&1
fi


# Default value
buggy_outcome=false

# Run with a 45s time limit
OUTPUT=$(timeout 45s defects4j test -t "$test_name" 2>&1)
EXIT_STATUS=$?

if [ "$EXIT_STATUS" -eq 124 ]; then
    # Command timed out → treat as not buggy (no reliable outcome)
    buggy_outcome=false
elif ! echo "$OUTPUT" | grep -q "Failing tests: 0"; then
    # Tests did not report "Failing tests: 0" → at least one failure
    buggy_outcome=true
    # echo "  LOC: verify_new_test.sh for assertion $num" >> "$LOG_FILE"
fi


#echo $OUTPUT
#
#
#
## fall back to the original file
cp -f "$original_copy_path" "$full_original_file_Path"
#
#
#
#
#
if [[ "$buggy_outcome" == true && "$fixed_outcome" == true ]]; then
    echo "accept assertion $num"
    outcome="accept"
else
    echo "reject assertion $num, buggy_outcome=$buggy_outcome, fixed_outcome=$fixed_outcome"
    outcome="reject"
fi
#
# Ensure file exists and is initialized
if [[ ! -f test_outcome_mutants.json ]]; then
    echo "[]" > test_outcome_mutants.json
fi

# Append new record
jq --arg num "$num" \
   --arg outcome "$outcome" \
   --arg m_id "$m_id" \
   --arg buggy "$buggy_outcome" \
   --arg fixed "$fixed_outcome" \
   '. += [{"assertion": $num, "m_id": $m_id, "outcome": $outcome, "buggy_outcome": $buggy, "fixed_outcome": $fixed}]' \
   test_outcome_mutants.json > tmp.json && mv tmp.json test_outcome_mutants.json

