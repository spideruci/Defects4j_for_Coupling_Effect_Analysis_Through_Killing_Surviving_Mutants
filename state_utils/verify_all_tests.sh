#!/bin/bash


#!/bin/bash

# Directory containing the oracle files


# the script scan the "oracle specification" directory, and extract the assertion number from the file name.
# A file_name can be like assertion_0.json, the assertion number is 1.
# Then it runs the verify_new_test.sh script for each assertion number.

#!/bin/bash

#!/bin/bash

# Directory containing the assertion JSON files

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



ORACLES_DIR="oracle specification"

# Remove old test_outcome_first.json if it exists
if [[ -f test_outcome.json ]]; then
    rm test_outcome.json
    echo "Old test_outcome.json deleted."
fi

# Loop through all assertion files
for file in "$ORACLES_DIR"/assertion_*.json; do
    # Skip if no files found
    [[ -e "$file" ]] || continue

    # Extract just the filename
    filename=$(basename "$file")

    # Extract the number between 'assertion_' and '.json'
    assertion_num=$(echo "$filename" | sed -E 's/^assertion_([0-9]+)\.json$/\1/')

    echo "Processing assertion $assertion_num from $filename"

    # Run the verification script with the assertion number
    ./verify_new_test_bug.sh "$assertion_num"
done

end_time=$(date +%s.%N)

record_execution_time "verify_all_tests_from_bugs" "$start_time" "$end_time"
