import os
import hashlib
import json


def compute_file_hash(filepath):
    """Compute SHA-256 hash of a file."""
    hasher = hashlib.sha256()
    with open(filepath, "rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            hasher.update(chunk)
    return hasher.hexdigest()

def find_json_hashes(root_dir):
    """Recursively find all .json files and compute their hashes."""
    results = []
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith(".json"):
                full_path = os.path.join(dirpath, filename)
                file_hash = compute_file_hash(full_path)
                results.append(file_hash)
    return results


def findvariable_hashes(filepath):
    """Compute SHA-256 hash of a file."""
    hasher = hashlib.sha256()
    with open(filepath) as f:
        data = json.load(f)
    temp = data["test_name"] + data["line_number"] + data["source"] + data["owner"] + data["name"]
    # print(temp)
    return temp

def getTest(filepath):
    """Compute SHA-256 hash of a file."""
    hasher = hashlib.sha256()
    with open(filepath) as f:
        data = json.load(f)
    temp = data["test_name"]
    # print(temp)
    return temp

def getPath(filepath):
    """Compute SHA-256 hash of a file."""
    hasher = hashlib.sha256()
    with open(filepath) as f:
        data = json.load(f)
    temp = data["python_access"]
    # print(temp)
    return ".".join([str(i) for i in temp])



def find_json_variables(root_dir):
    """Recursively find all .json files and compute their hashes."""
    results = []
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith(".json"):
                full_path = os.path.join(dirpath, filename)
                file_hash = findvariable_hashes(full_path)
                results.append(file_hash)
    return results

def create_variable_hashes_dict(root_dir):
    results = {}
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith(".json"):
                full_path = os.path.join(dirpath, filename)
                file_hash = findvariable_hashes(full_path)
                if file_hash not in results:
                    results[file_hash] = []
                results[file_hash].append(getPath(full_path))
    return results






# Example usage:
if __name__ == "__main__":
    folder = "mutant_oracle_specification"
    folder_real = "oracle specification"

    m_ids = set()
    filename = "detect_real_bugs.json"

        # Read and parse JSON
    with open(filename, "r") as f:
        data = json.load(f)


    variable_specification_dict = dict()
    if isinstance(data, list):
        for entry in data:
            assertion = entry.get("assertion")
            m_id = entry.get("m_id")
            outcome = entry.get("outcome")
            if outcome == "killing":
                m_ids.add(m_id)
                specification_path = "mutant_oracle_specification/" + str(m_id) + "/" + "assertion_" + str(assertion) + ".json"
                killing_variable_hash = findvariable_hashes(specification_path)
                if killing_variable_hash not in variable_specification_dict:
                    variable_specification_dict[killing_variable_hash] = []
                variable_specification_dict[killing_variable_hash].append(getPath(specification_path))

    variable_real_bugs_dict = create_variable_hashes_dict("oracle specification")

    print("m_ids: " + str(len(m_ids)))
    m_in_b = 0
    b_in_m = 0
    for key in variable_specification_dict.keys():
        if key not in variable_real_bugs_dict:
            print("?? Please Check")
        else:
            bugs = set(variable_real_bugs_dict[key])
            mutants = set(variable_specification_dict[key])
            for m_path in mutants:
                for b_path in bugs:
                    if m_path in b_path or b_path in m_path:
                        if m_path in b_path:
                            m_in_b +=1
                        else:
                            b_in_m += 1
                            print(m_path)
                            print(b_path)
                        # print(m_path)
                        # print(b_path)

                    elif m_path == "metas.21.graph.fields.detailMessage" or b_path == "metas.21.graph.fields.detailMessage":
                        m_in_b += 1
                    else:
                        print(m_path)
                        print(b_path)
                        print("please check")


    print("【redundant】mutated assertion subsums the real bug assertion:" + str(m_in_b))
    print("[redundant] mutated assertion is subsumed by real bug assertion: " + str(b_in_m))


    total_killing = 0
    total_passing = 0
    killing_hashes = set()
    killing_variable_hashes = set()
    killing_tests = set()

    # Path to your JSON file



    # Ensure it’s a list of entries
    if isinstance(data, list):
        for entry in data:
            assertion = entry.get("assertion")
            m_id = entry.get("m_id")
            outcome = entry.get("outcome")
            if outcome == "killing":
                total_killing += 1
            else:
                total_passing += 1

            if outcome == "killing":
                specification_path = "mutant_oracle_specification/" + str(m_id) + "/" + "assertion_" + str(assertion) + ".json"

                # with open(specification_path, "r") as f:
                #     specification_data = json.load(f)
                # specification_path.get("")
                killing_hash = compute_file_hash(specification_path)
                killing_variable_hash = findvariable_hashes(specification_path)
                killing_hashes.add(killing_hash)
                killing_variable_hashes.add(killing_variable_hash)
                killing_tests.add(getTest(specification_path))
            # Print or process the fields
            # print(f"assertion={assertion}, m_id={m_id}, outcome={outcome}")
    else:
        print("Error: Expected a list of JSON objects.")


    print(total_killing)
    print(total_passing)
    print(len(killing_hashes))
    print("?" + str(len(killing_variable_hashes)))
    print(len(killing_tests))

    variable_hashes = find_json_variables(folder)

    print(len(variable_hashes))
    print(len(set(variable_hashes)))
    variables_real = find_json_variables(folder_real)
    print(len(set(variables_real)))
    print(len(set(variables_real) - set(variable_hashes)))


    
    json_hashes = find_json_hashes(folder)

    print(len(json_hashes))
    print(len(set(json_hashes)))
    json_hashes_real = find_json_hashes("oracle specification")
    print(len(set(json_hashes_real)))
    print("how many assertions do not have exact match" + str(len(set(json_hashes_real) - set(json_hashes))))
