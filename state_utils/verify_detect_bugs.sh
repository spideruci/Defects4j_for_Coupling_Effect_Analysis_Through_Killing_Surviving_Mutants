#!/bin/bash



record_execution_time() {
    local label="$1"
    local start_time="$2"
    local end_time="$3"
    local file="time.json"

    # Calculate elapsed seconds (with 4 decimal places)
    local elapsed
    elapsed=$(awk -v s="$start_time" -v e="$end_time" 'BEGIN { printf "%.4f", e - s }')

    # Get current timestamp (human readable)
    local ts
    ts=$(date)

    # Create file if not exist
    if [[ ! -f "$file" ]]; then
        echo "[]" > "$file"
    fi

    # Append the new record using jq
    tmp=$(mktemp)
    jq --arg name "$label" \
       --arg time_s "$elapsed" \
       --arg ts "$ts" \
       '. += [{"name": $name, "time_s": $time_s, "ts": $ts}]' \
       "$file" > "$tmp" && mv "$tmp" "$file"
}



start_time=$(date +%s.%N)
set -euo pipefail
rm -rf detect_real_bugs.json



outcome_file="test_outcome_mutants.json"
if [ ! -f detect_real_bugs.json ]; then
    echo "[]" > detect_real_bugs.json
fi



# Loop through entries with outcome == "accept"
jq -c '.[] | select(.outcome == "accept") | {assertion: .assertion, m_id: .m_id}' "$outcome_file" | while read -r entry; do
    assertion=$(echo "$entry" | jq -r '.assertion')
    m_id=$(echo "$entry" | jq -r '.m_id')
    # Process each pair (leave blank for now)
    # echo "Assertion: $assertion | m_id: $m_id"

    json_file="mutant_oracle_specification/${m_id}/assertion_${assertion}.json"
    # Extract fields with jq
    test_name=$(jq -r '.test_name' "$json_file")
    class_name=$(jq -r '.simple_class_name' "$json_file")
    full_file_name="${test_name%%::*}"
    original_copy_path="oracles_mutants/before/${class_name}_before_${full_file_name}.java"

    if [ ! -f "$original_copy_path" ]; then
        original_copy_path="oracles_mutants/before/${class_name}_before.java"
    fi


    new_file_name="oracles_mutants/${assertion}__${class_name}.java"
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



    cp -f "$new_file_name" "$full_original_file_Path"
    rm -rf target
    rm -rf build-tests
    rm -rf build
    rm -rf ./.instrumented_classes
    OUTPUT=$(defects4j compile 2>&1)
    #defects4j compile
    # Check if it contains the error message
    if echo "$OUTPUT" | grep -q "Cannot compile tests!"; then
        echo "ERROR: Compilation failed for tests at $(date)" >> "$LOG_FILE"
        echo "  LOC: prepare_new_test.sh for assertion $assertion" >> "$LOG_FILE"
        echo "  OUTPUT: $OUTPUT" >> "$LOG_FILE"
        echo "  compilation failure for assertion $assertion"
    fi


    buggy_outcome=false

    # Run with a 45s time limit
    OUTPUT=$(timeout 45s defects4j test -t "$test_name" 2>&1)
    EXIT_STATUS=$?

    if [ "$EXIT_STATUS" -eq 124 ]; then
        # Timed out → keep buggy_outcome=false (no reliable outcome)
        buggy_outcome=false
    elif ! echo "$OUTPUT" | grep -q "Failing tests: 0"; then
        # Tests did not report "Failing tests: 0" → at least one failure
        buggy_outcome=true
    fi


    ## fall back to the original file
    cp -f "$original_copy_path" "$full_original_file_Path"

    outcome="passing"
    if [ "$buggy_outcome" == true ]; then
        outcome="killing"
        echo "killing test found!"
    else
        outcome="passing"
    fi


        # Append new record
    jq --arg assertion "$assertion" \
       --arg outcome "$outcome" \
       --arg m_id "$m_id" \
       '. += [{"assertion": $assertion, "m_id": $m_id, "outcome": $outcome}]' \
       detect_real_bugs.json > tmp.json && mv tmp.json detect_real_bugs.json




done


end_time=$(date +%s.%N)

record_execution_time "verify_detect_bugs" "$start_time" "$end_time"

