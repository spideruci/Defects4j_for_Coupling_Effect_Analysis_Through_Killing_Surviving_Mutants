
#!/bin/bash

# Directory containing the oracle files


# the script scan the "oracle specification" directory, and extract the assertion number from the file name.
# A file_name can be like assertion_0.json, the assertion number is 1.
# Then it runs the verify_new_test.sh script for each assertion number.



start_time=$(date +%s.%N)
set -euo pipefail
# rm -f /tmp/seen_hashes.txt

file_name="/tmp/seen_hashes_$(date +%Y%m%d_%H%M%S).txt"

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






# Directory containing the assertion JSON files
ORACLES_DIR="mutant_oracle_specification"

# Remove old test_outcome_first.json if it exists
if [[ -f test_outcome_mutants.json ]]; then
    rm test_outcome_mutants.json
    echo "Old test_outcome_mutants.json deleted."
fi

# Loop through all assertion files
# for file in "$ORACLES_DIR"/*/assertion_*.json; do
#     # Skip if no files found
#     [[ -e "$file" ]] || continue
# #    echo "Found file: $file"
#     # Extract just the filename
#     filename=$(basename "$file")
#     parent=$(basename "$(dirname "$file")")   # e.g., 39


#     # Extract the number between 'assertion_' and '.json'
# #    echo $filename
#     assertion_num=$(echo "$filename" | sed -E 's/^assertion_([0-9]+)\.json$/\1/')

#     # Run the verification script with the assertion number
#     ./verify_new_test_mutant.sh "$parent" "$assertion_num"
# done

#!/usr/bin/env bash
find "$ORACLES_DIR" -type f -name "assertion_*.json" | \
while read -r file; do
    file_hash=$(shasum "$file" | awk '{print $1}')
    if awk -v h="$file_hash" 'h==$1 {found=1} END{exit !found}' $file_name 2>/dev/null; then
        echo "Skipping duplicate $file"
        continue
    fi
        # record hash as seen
    echo "$file_hash" >> "$file_name"
    parent=$(basename "$(dirname "$file")")
    assertion_num=$(basename "$file" | sed -E 's/^assertion_([0-9]+)\.json$/\1/')
    ./verify_new_test_mutant.sh "$parent" "$assertion_num"
done



end_time=$(date +%s.%N)

record_execution_time "verify_assertions_from_mutants" "$start_time" "$end_time"
rm -f $file_name
