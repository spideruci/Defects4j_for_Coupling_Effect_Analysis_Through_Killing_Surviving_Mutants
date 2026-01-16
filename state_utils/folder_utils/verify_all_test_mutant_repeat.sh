
#!/bin/bash

# Directory containing the oracle files


# the script scan the "oracle specification" directory, and extract the assertion number from the file name.
# A file_name can be like assertion_0.json, the assertion number is 1.
# Then it runs the verify_new_test.sh script for each assertion number.



start_time=$(date +%s.%N)
set -euo pipefail
# rm -f /tmp/seen_hashes.txt

# file_name="/tmp/seen_hashes_$(date +%Y%m%d_%H%M%S).txt"

# /tmp/seen_hashes.txt
# TODO: Bug, multiple programs might be visitng the same file, it can increase the execution time




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



if [ ! -f test_outcome_mutants.json ]; then
    echo "❌ Error: test_outcome_mutants.json does not exist"
    exit 1
fi
defects4j patch -f
defects4j update_oracle_helper -m
# rename file test_outcome_mutants.json to test_outcome_mutants_1.json;
mv test_outcome_mutants.json test_outcome_mutants_1.json

jq -r '.[] | select(.outcome=="accept") | "\(.m_id) \(.assertion)"' test_outcome_mutants_1.json |
while read -r m_id assertion; do
    ./verify_new_test_mutant.sh "$m_id" "$assertion"
done


end_time=$(date +%s.%N)

record_execution_time "verify_assertions_from_mutants_repeat" "$start_time" "$end_time"
# rm -f $file_name
