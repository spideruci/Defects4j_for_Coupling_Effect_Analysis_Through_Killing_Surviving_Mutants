import json
import os
import re
import pickle
import hashlib

id_lookup = {}

# Maps of id -> paths
id_paths = {}
id_access = {}  # list-of-keys form

# Build the paths
# Load the JSON file

def format_float(match):
    value = float(match.group(0))           # Convert matched string to float
    if f"{value:.4f}" == "-0.0000":
        return "0.0000"
    else:
        return f"{value:.4f}"# Format with 4 decimal places

def normalize_number(match):
    text = match.group(0)
    value = float(text)  # Step 1: parse the numeric literal

    # Step 2: Decide how to display it after converting to real number
    if value == 0.0:
        return "0.0000"                         # uniform zero
    elif abs(value) < 1e-4 or abs(value) >= 1e6:
        return f"{value:.8e}"                   # scientific notation (8 digits)
    else:
        return f"{value:.4f}"                   # fixed-point 4 decimals



def evaluate_two_strings(a, b):
    """
    handle float/double type variables
    """
    # Step 3: Regex for all numeric literals (int, float, scientific)
    pattern = r'[-+]?(?:\d*\.\d+|\d+\.\d*|\d+)(?:[eE][-+]?\d+)?'
    formatted_a = re.sub(pattern, format_float, str(a))
    formatted_b = re.sub(pattern, format_float, str(b))

    return formatted_a == formatted_b

def get_all_first_level_directory(directory):
    """
    List all first-level directories within the specified directory.

    Args:
    directory (str): The path to the directory from which to list first-level directories.

    Returns:
    list: A list of first-level directories.
    """
    # Check if the specified path is a directory
    if not os.path.isdir(directory):
        # print("The specified path is not a directory." + directory)
        return []

    # List all entries in the directory using os.listdir
    entries = os.listdir(directory)

    # Filter the entries to get only directories
    first_level_dirs = [entry for entry in entries if os.path.isdir(os.path.join(directory, entry)) and entry.split("/")[-1][0] != "."]

    return first_level_dirs


def collect_ids(node, path="root"):
    """Recursively walk JSON and collect $id nodes and their paths."""
    if isinstance(node, dict):
        if "$id" in node:
            id_lookup[node["$id"]] = node
        for key, value in node.items():
            collect_ids(value, f"{path}/{key}")
    elif isinstance(node, list):
        for idx, item in enumerate(node):
            collect_ids(item, f"{path}[{idx}]")

    
def build_lookup(node, lookup):
    """Collect all $id nodes for resolving $ref."""
    if isinstance(node, dict):
        if "$id" in node:
            lookup[node["$id"]] = node
        for v in node.values():
            build_lookup(v, lookup)
    elif isinstance(node, list):
        for item in node:
            build_lookup(item, lookup)

def resolve_refs(node, lookup):
    """Replace $ref with actual node, drop $id."""
    if isinstance(node, dict):
        if "$ref" in node:
            return resolve_refs(lookup[node["$ref"]], lookup)
        return {k: resolve_refs(v, lookup)
                for k, v in node.items() if k not in ("$id", "$ref")}
    elif isinstance(node, list):
        return [resolve_refs(item, lookup) for item in node]
    return node

def resolve_refs_keep_ids(node, lookup):
    """Replace $ref with actual node, but keep $id in place."""
    if isinstance(node, dict):
        if "$ref" in node:
            return resolve_refs_keep_ids(lookup[node["$ref"]], lookup)
        return {k: resolve_refs_keep_ids(v, lookup)
                for k, v in node.items() if k != "$ref"}  # keep $id
    elif isinstance(node, list):
        return [resolve_refs_keep_ids(item, lookup) for item in node]
    return node

# ---------------------------
# Wrapping reconstructed nodes
# ---------------------------
class NodeWrapper:
    def __init__(self, value, node_id=None):
        self.value = value
        self.node_id = node_id

    def __repr__(self):
        return f"NodeWrapper(id={self.node_id}, value={self.value!r})"

    def __eq__(self, other):
        if not isinstance(other, NodeWrapper):
            return False
        return self._deep_equal(self.value, other.value)

    def __hash__(self):
        return hash(self._make_hashable(self.value))

    def _deep_equal(self, a, b):
        """Recursively compare nested NodeWrapper or containers."""
        # Handle nested NodeWrapper
        if isinstance(a, NodeWrapper) and isinstance(b, NodeWrapper):
            return self._deep_equal(a.value, b.value)

        # Dicts
        if isinstance(a, dict) and isinstance(b, dict):
            if a.keys() != b.keys():
                return False
            return all(self._deep_equal(a[k], b[k]) for k in a)

        # Lists or tuples
        if isinstance(a, (list, tuple)) and isinstance(b, (list, tuple)):
            if len(a) != len(b):
                return False
            return all(self._deep_equal(x, y) for x, y in zip(a, b))

        # Sets
        if isinstance(a, set) and isinstance(b, set):
            if len(a) != len(b):
                return False
            # since sets are unordered, compare via pairwise match
            unmatched = list(b)
            for x in a:
                found = False
                for y in unmatched:
                    if self._deep_equal(x, y):
                        unmatched.remove(y)
                        found = True
                        break
                if not found:
                    return False
            return True

        # Base case
        return a == b

    def _make_hashable(self, v):
        """Convert nested structures into hashable equivalents (ignore node_id)."""
        if isinstance(v, NodeWrapper):
            return self._make_hashable(v.value)
        elif isinstance(v, dict):
            return frozenset((k, self._make_hashable(val)) for k, val in v.items())
        elif isinstance(v, (list, tuple)):
            return tuple(self._make_hashable(x) for x in v)
        elif isinstance(v, set):
            return frozenset(self._make_hashable(x) for x in v)
        else:
            return v


def reconstruct(node, keep_ids=False):
    if isinstance(node, dict):
        node_id = node.get("$id") if keep_ids else None
        kind = node.get("kind")

        if kind == "map":
            # for e in node.get("entries", []):
            #     print("reconstruct")
            #     print(type(reconstruct(e["key"], keep_ids)))
            #     print(type(reconstruct(e["value"], keep_ids)))
            #     print()
            #     print("ha")
            # print(len(node.get("entries")))

            m = {reconstruct(e["key"], keep_ids): reconstruct(e["value"], keep_ids)
                 for e in node.get("entries", [])}
            # print(node.get("entries"),[])
            # print("jjjjjj")
            return NodeWrapper(m, node_id) if keep_ids else NodeWrapper(m, node_id)
            # return NodeWrapper(m, node_id) if keep_ids else m

        elif kind in ("Set", "Iterable"):
            elems = [reconstruct(e, keep_ids) for e in node.get("elements", [])]
            s = set(elems) if kind == "Set" else elems
            return NodeWrapper(s, node_id) if keep_ids else NodeWrapper(s, node_id)
            # return NodeWrapper(s, node_id) if keep_ids else s

        elif "value" in node:
            return NodeWrapper(node["value"], node_id) if keep_ids else node["value"]

        else:
            d = {k: reconstruct(v, keep_ids) for k, v in node.items()
                 if k not in ("$id", "$ref")}
            return NodeWrapper(d, node_id) if keep_ids else NodeWrapper(d, node_id)
            return NodeWrapper(d, node_id) if keep_ids else d

    elif isinstance(node, list):
        arr = [reconstruct(x, keep_ids) for x in node]
        return NodeWrapper(arr, None) if keep_ids else arr

    return node

def diff_json_files(file1, file2):
    def load_and_reconstruct(path, keep_ids=False, resolver=resolve_refs_keep_ids):
        with open(path) as f:
            data = json.load(f)
        lookup = {}
        build_lookup(data, lookup)
        resolved = resolver(data, lookup)
        return reconstruct(resolved, keep_ids=keep_ids)

    # obj1 keeps IDs
    obj1 = load_and_reconstruct(file1, keep_ids=True, resolver=resolve_refs_keep_ids)
    # obj2 drops IDs
    obj2 = load_and_reconstruct(file2, keep_ids=True, resolver=resolve_refs_keep_ids)

    if len(obj1.value["metas"].value) != len(obj2.value["metas"].value):
        print("trace change")

    str1 = ""
    str2 = ""
    for text in obj1.value["metas"].value:
        if "line" in text.value and "this" != text.value["line"]:
            str1 += text.value["line"]
    for text in obj2.value["metas"].value:
        if "line" in text.value and "this" != text.value["line"]:
            str2 += text.value["line"]
    if str1 != str2:
        print("trace change")
        return []

    buggy_ids = deep_diff_ids(obj1, obj2)
    
    return list(dict.fromkeys(buggy_ids))  # deduplicate, preserve order



id_paths = {}
id_access = {}



def normalize_segment(key, style="human"):
    """Convert JSON key/index into a readable or access segment."""
    if style == "human":
        if key == "metas":
            return "var"
        if key in ("graph", "fields"):
            return ""
        if key in ("$id", "$ref", "kind", "value", "entries", "elements"):
            return ""
        if isinstance(key, str):
            return "." + key
        return ""
    elif style == "python":
        if isinstance(key, str):
            return key
        if isinstance(key, int):
            return key
        return None
    return ""



def stringify_key(key_node):
    """Turn a map key node into a short string if possible, else complexKey."""
    if isinstance(key_node, dict):
        if "value" in key_node:
            return str(key_node["value"])
        if "fields" in key_node:
            parts = []
            for k, v in key_node["fields"].items():
                if isinstance(v, dict) and "value" in v:
                    parts.append(f"{k}={v['value']}")
            return "{" + ",".join(parts) + "}" if parts else "complexKey"
        return "complexKey"
       
    return str(key_node)


def collect_paths(node, path="var", access=None):
    if access is None:
        access = []

    if isinstance(node, dict):
        if "$id" in node:
            # print(path)
            id_paths[node["$id"]] = path
            id_access[node["$id"]] = list(access)

        kind = node.get("kind")

        if kind == "map":
            # Treat entries as key-value pairs
            for idx, entry in enumerate(node.get("entries", [])):
                key_node = entry.get("key", {})
                key_str = stringify_key(key_node)
                val_node = entry.get("value")

                # Human-readable: [key]
                new_path = f"{path}[{key_str}]"
                # Programmatic access: still go through entries
                new_access = access + ["entries", idx, "value"]

                collect_paths(val_node, new_path, new_access)


        #TODO SET
        elif kind in ("Set", "Iterable"):
            # Human-readable: []
            for idx, elem in enumerate(node.get("elements", [])):
                new_path = f"{path}[]"
                new_access = access + ["elements", idx]
                collect_paths(elem, new_path, new_access)

        else:
            for key, value in node.items():
                # if key in ("$id", "$ref", "kind", "value", "entries", "elements"):
                #     continue

                if key in ("graph", "fields"):
                    # Skip from human-readable, but keep for programmatic
                    collect_paths(value, path, access + [key])
                else:
                    # Normal field
                    new_path = f"{path}.{key}"
                    new_access = access + [key]
                    collect_paths(value, new_path, new_access)

    elif isinstance(node, list):
        for idx, item in enumerate(node):
            new_path = path  # keep human-readable collapsed
            new_access = access + [idx]  # actual numeric index
            collect_paths(item, new_path, new_access)


def get_by_path(obj, path_list):
    for key in path_list:
        if isinstance(obj, dict):
            obj = obj[key]
        elif isinstance(obj, list):
            # key must be integer
            if not isinstance(key, int):
                raise TypeError(f"Expected int for list index, got {key!r}")
            obj = obj[key]
        else:
            raise TypeError(f"Cannot index into {type(obj)} with {key!r}")
    return obj




def get_test_file_location(test_name):
    """
    Given a test_name (without .java), check for its file under:
      1. src/java/test_name.java
      2. src/test_name.java
    Returns the valid path if found, otherwise raises FileNotFoundError.
    """
    # candidate paths
    candidates = [
        os.path.join("src", "test", f"{test_name}.java"),
        os.path.join("src", f"{test_name}.java"),
        os.path.join("src", "test", "java",f"{test_name}.java"),
        os.path.join("tests",f"{test_name}.java"),
        os.path.join("gson","src","test","java",f"{test_name}.java")
    ]
    
    
    # check each
    for path in candidates:
        if os.path.isfile(path):
            return path
    
    # if none found
    raise FileNotFoundError(
        f"Test file for '{test_name}' not found in either location: {candidates}"
    )

# differences_fixed is defined globally
def deep_diff_ids(a, b, current_id=None, current_index = -1):
    buggy_ids = []

    # if isinstance(a, NodeWrapper) and a.node_id == "n437":
    #     print("437")

    if isinstance(a, NodeWrapper):

        a_val, a_id = a.value, a.node_id or current_id

    else:
        # print("!!!")
        # print(current_id)

        a_val, a_id = a, current_id

    if a_id != None and a_id in differences_fixed:
        # print("non-determinism!")
        return buggy_ids

    if current_index != None and current_index in differences_fixed:
        # print("non-determinism!")
        return buggy_ids


    if isinstance(b, NodeWrapper):
        b_val = b.value
    else:
        b_val = b
    if isinstance(a_val, dict) and isinstance(b_val, dict):
        key_mismatch = False

        for k in set(a_val.keys()) | set(b_val.keys()):
            if k == "$id":
                continue
            if k == "index":
                current_index = a_val["index"]
            if k not in a_val or k not in b_val:
                
                if a_id:
                    buggy_ids.append(a_id)
                    key_mismatch = True
                else:
                    key_mismatch = True
                    buggy_ids.append(current_index)

        # if isinstance(a, NodeWrapper) and a.node_id == "n436":
        # print("please 1 " + str(key_mismatch))
        # print(a_val.keys())
        # print(a_val["size"])
        # print(a_val["entries"])

        if not key_mismatch:
            for k in set(a_val.keys()) | set(b_val.keys()):
                if k == "$id":
                    continue
                buggy_ids.extend(deep_diff_ids(a_val[k], b_val[k], a_id,current_index))

    elif isinstance(a_val, list) and isinstance(b_val, list):
        for i in range(max(len(a_val), len(b_val))):
            if i >= len(a_val) or i >= len(b_val):
                if a_id: buggy_ids.append(a_id)
            else:
                buggy_ids.extend(deep_diff_ids(a_val[i], b_val[i], a_id,current_index))
    elif isinstance(a_val, set) and isinstance(b_val, set):
        if evaluate_two_strings(str(a_val), str(b_val)) and a_id:
            buggy_ids.append(a_id)
    else:

        if not evaluate_two_strings(a_val, b_val) and a_id:
            buggy_ids.append(a_id)
        elif not evaluate_two_strings(a_val, b_val):
            buggy_ids.append(current_index)
            pass
            # print("j")
            # print(b_val)
            # print(a_val)
    return buggy_ids




if not os.path.exists("mutant_states"):
    print("Error: Please Run Tests to Collect States Analysis First!!!")



index = 0
differences_fixed = set()
for m_id in get_all_first_level_directory("mutant_states"):
    # if str(m_id) != "118":
    #     continue
    for test_name in get_all_first_level_directory("mutant_states/" + m_id):
        path_buggy = os.path.join("mutant_states", m_id,test_name,"state.json")
        path_fixed = os.path.join("all_states",test_name, "fixed", "1.json")



        if not os.path.exists(path_buggy) or not os.path.exists(path_fixed) :
            continue

        size_bytes = os.path.getsize(path_fixed)
        size_mb = size_bytes / (1024 * 1024)
        if size_mb > 10:
            continue
        

        path_fixed2 = os.path.join("all_states",test_name, "fixed", "2.json")
        differences_fixed = diff_json_files(path_fixed, path_fixed2)
        if len(differences_fixed)!= 0:
            # print("jj")
            with open(os.path.join("all_states",test_name, "fixed", "bl.pkl"), "wb") as f: 
                pickle.dump(differences_fixed, f)

        differences = diff_json_files(path_fixed, path_buggy)

        with open(path_fixed, "r") as f:
            data = json.load(f)
        collect_paths(data)
        collect_ids(data)

        identifier_map = {}
        identifier_map_counter = {}
        all_identifiers = [  "_".join(
            str(target_variable[k])
            for k in ["line", "kind", "source", "owner", "name", "returnType", "ordinal"] 
            if k in target_variable)
                for target_variable in data["metas"]]
        for _ in all_identifiers:
            if _ not in identifier_map:
                identifier_map[_] = 0
            identifier_map[_] += 1
        loop_keys = [k for k,v in identifier_map.items() if v >= 2]



        for buggy_id in differences:
            # print("*" * 30)
            # print(buggy_id)
            # print("mutation_id" + str(m_id))
            # print(test_name)
            # print("\n")
            folder = "mutant_oracle_specification/" + str(m_id)
            os.makedirs(folder, exist_ok=True)
            target = id_lookup.get(buggy_id)

            if isinstance(buggy_id, int):
                python_access = ["metas", buggy_id]

                readable_access = "primitive"
            else:
                python_access = id_access[buggy_id]
                readable_access = id_paths[buggy_id]

                ## Configuration: if it's too long, continue
                if len(python_access) >= 2 and python_access[1] > 1000:
                    continue
                ## if it's too deep, continue
                if len(python_access) >= 10:
                    continue
                            ## heuristics, to avoid an array which is simply too long
                should_skip = False
                for i in range(0, len(python_access) -1):
                    if python_access[i] == "elements":
                        if isinstance(python_access[i+1], int):
                            if python_access[i+1] > 10:
                                should_skip = True
                                break
                if should_skip:
                    continue

            try:
                test_file = get_test_file_location(test_name.replace(".", os.sep).split("::")[0])
                # print("Found:", test_file)
            except FileNotFoundError as e:
                print("Error:", e)
                raise AssertionError("FileNotFoundError for test code file")

            target_variable = data["metas"][python_access[1]]
            if target_variable["kind"] == "static":
                continue


            if test_name.split("::")[0] != target_variable["line"].split("-")[0].replace(os.sep, "."):
                # this happens, when a test case method invokes a helper method from a another file or extends a class. 
                continue
            # if target_variable["line"].split("-")[0].replace("os.sep", ".").equals(test_name.split("::"))

            identifier = "_".join(
                str(target_variable[k])
                for k in ["line", "kind", "source", "owner", "name", "returnType", "ordinal"] 
                if k in target_variable)
            if identifier not in identifier_map_counter:
                identifier_map_counter[identifier] = 0
            identifier_map_counter[identifier] += 1
            loop = -1
            if identifier in loop_keys:
                loop = identifier_map_counter[identifier]
            if loop > 10:
                continue

            if target_variable["source"] == "return":
                source = "return"
                owner = target_variable["owner"].replace(os.sep, ".")
                name = target_variable["name"]
                returnType = target_variable["returnType"]
                if name == "<init>":
                    name = owner.split(".")[-1]
                    returntype = owner.replace(os.sep,".")
                ordinal = target_variable["ordinal"]
                line_no = target_variable["line"].split("-")[1]
                
                # Data to write
                json_data = {
                    "source": source,
                    "owner": owner,
                    "name": name,
                    "returnType": returnType,
                    "ordinal": ordinal,
                    "readable_access":readable_access.replace("var.metas.","var."),
                    "python_access":python_access,
                    "test_name":test_name,
                    "line_number": line_no,
                    "simple_class_name": test_name.split("::")[0].split(".")[-1],
                    "loop": loop
                    
                }
                
                # File path
                file_path = os.path.join(folder, "assertion_" + str(index) + ".json")

                
                # Write JSON file
                with open(file_path, "w", encoding="utf-8") as f:
                    json.dump(json_data, f, indent=2)
                index+=1
            elif target_variable["source"] == "getField":
                source = "getField"
                owner = target_variable["owner"].replace(os.sep, ".")
                name = target_variable["name"]
                returnType = target_variable["returnType"]
                if name == "<init>":
                    name = owner.split(".")[-1]
                    returntype = owner.replace(os.sep,".")
                ordinal = target_variable["ordinal"]
                line_no = target_variable["line"].split("-")[1]

                # Data to write
                json_data = {
                    "source": source,
                    "owner": owner,
                    "name": name,
                    "returnType": returnType,
                    "ordinal": ordinal,
                    "readable_access":readable_access.replace("var.metas.","var."),
                    "python_access":python_access,
                    "test_name":test_name,
                    "line_number": line_no,
                    "simple_class_name": test_name.split("::")[0].split(".")[-1],
                    "loop": loop
                    
                }
                
                # File path
                file_path = os.path.join(folder, "assertion_" + str(index) + ".json")

                
                # Write JSON file
                with open(file_path, "w", encoding="utf-8") as f:
                    json.dump(json_data, f, indent=2)
                index+=1
            elif target_variable["source"] == "local":

                source = "local"
                # print(index)
                # owner = target_variable["owner"].replace(os.sep, ".")
                name = target_variable["name"]
                owner = target_variable["owner"]
                # returnType = target_variable["returnType"]
                # if name == "<init>":
                #     name = owner.split(".")[-1]
                #     returntype = owner.replace(os.sep,".")
                ordinal = target_variable["ordinal"]
                line_no = target_variable["line"].split("-")[1]

                # Data to write
                json_data = {
                    "source": source,
                    "owner": owner,
                    "name": name,
                    # "returnType": returnType,
                    "ordinal": ordinal,
                    "readable_access":readable_access.replace("var.metas.","var."),
                    "python_access":python_access,
                    "test_name":test_name,
                    "line_number": line_no,
                    "simple_class_name": test_name.split("::")[0].split(".")[-1],
                    "loop":loop
                    
                }
                
                # File path
                file_path = os.path.join(folder, "assertion_" + str(index) + ".json")
                # print("happy")
                
                # Write JSON file
                with open(file_path, "w", encoding="utf-8") as f:
                    json.dump(json_data, f, indent=2)
                index+=1
            else:
                pass




            # print("yes!!!")
# root_dir = "mutant_oracle_specification"

# # To store unique file hashes (content signatures)
# hashes = set()
# total_files = 0
# duplicate_files = 0

# for dirpath, _, filenames in os.walk(root_dir):
#     for filename in filenames:
#         if filename.endswith(".json"):
#             total_files += 1
#             file_path = os.path.join(dirpath, filename)

#             # Compute a hash of the file content to detect duplicates
#             with open(file_path, "rb") as f:
#                 file_hash = hashlib.md5(f.read()).hexdigest()

#             if file_hash in hashes:
#                 duplicate_files += 1
#             else:
#                 hashes.add(file_hash)

# print(f"Total JSON files: {total_files}")
# print(f"Unique JSON contents: {len(hashes)}")
# print(f"Duplicate files (same content): {duplicate_files}")






    
