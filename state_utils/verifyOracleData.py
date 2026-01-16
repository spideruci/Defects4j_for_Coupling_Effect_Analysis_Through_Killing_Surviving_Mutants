import json
import os
import sys

id_lookup = {}

# Maps of id -> paths
id_paths = {}
id_access = {}  # list-of-keys form

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


# ---------------------------
# Utilities for lookup & refs
# ---------------------------
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

def reconstruct(node, keep_ids=False):
    if isinstance(node, dict):
        node_id = node.get("$id") if keep_ids else None
        kind = node.get("kind")

        if kind == "Map":
            m = {reconstruct(e["key"], keep_ids): reconstruct(e["value"], keep_ids)
                 for e in node.get("entries", [])}
            return NodeWrapper(m, node_id) if keep_ids else m

        elif kind in ("Set", "Iterable"):
            elems = [reconstruct(e, keep_ids) for e in node.get("elements", [])]
            s = set(elems) if kind == "Set" else elems
            return NodeWrapper(s, node_id) if keep_ids else s

        elif "value" in node:
            return NodeWrapper(node["value"], node_id) if keep_ids else node["value"]

        else:
            d = {k: reconstruct(v, keep_ids) for k, v in node.items()
                 if k not in ("$id", "$ref")}
            return NodeWrapper(d, node_id) if keep_ids else d

    elif isinstance(node, list):
        arr = [reconstruct(x, keep_ids) for x in node]
        return NodeWrapper(arr, None) if keep_ids else arr

    return node

# ---------------------------
# Diff function
# ---------------------------

# ---------------------------
def deep_diff_ids(a, b, current_id=None):

    buggy_ids = []


    if isinstance(a, NodeWrapper):
        a_val, a_id = a.value, a.node_id or current_id

    else:
        # print(current_id)
        a_val, a_id = a, current_id
        # with open("output.txt", "a") as f:  # "w" = write (overwrites existing content)
        #     f.write(str(a_val) + "\n")
        #     f.write(str(a_id)+ "\n")



    if isinstance(b, NodeWrapper):
        b_val = b.value
    else:
        b_val = b



    if isinstance(a_val, dict) and isinstance(b_val, dict):
        key_mismatch = False
        # print(a_val.keys(), b_val.keys())
        for k in set(a_val.keys()) | set(b_val.keys()):
            if k == "$id" or k == "visibility":
                continue
            if k not in a_val or k not in b_val:
                if a_id:
                    buggy_ids.append(a_id)
                    key_mismatch = True
        if not key_mismatch:
            # print(a_val.keys(), b_val.keys())
            for k in set(a_val.keys()) | set(b_val.keys()):
                if k == "$id" or k == "visibility":
                    continue
                buggy_ids.extend(deep_diff_ids(a_val[k], b_val[k], a_id))

    elif isinstance(a_val, list) and isinstance(b_val, list):
        for i in range(max(len(a_val), len(b_val))):
            if i >= len(a_val) or i >= len(b_val):
                if a_id: buggy_ids.append(a_id)
            else:
                buggy_ids.extend(deep_diff_ids(a_val[i], b_val[i], a_id))
    elif isinstance(a_val, set) and isinstance(b_val, set):
        if a_val != b_val and a_id:
            buggy_ids.append(a_id)
    else:
        if a_val != b_val and a_id:
            buggy_ids.append(a_id)

    return buggy_ids


# ---------------------------
# Entry point
# ---------------------------
def diff_json_files(file1, file2):
    def load_and_reconstruct(path, keep_ids=False, resolver=resolve_refs):
        with open(path) as f:
            data = json.load(f)
        lookup = {}
        build_lookup(data, lookup)
        resolved = resolver(data, lookup)
        return reconstruct(resolved, keep_ids=keep_ids)

    # obj1 keeps IDs
    obj1 = load_and_reconstruct(file1, keep_ids=True, resolver=resolve_refs_keep_ids)
    # obj2 drops IDs
    obj2 = load_and_reconstruct(file2, keep_ids=False, resolver=resolve_refs)


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



def collect_paths(node, path="", access=None):
    if access is None:
        access = []

    if isinstance(node, dict):
        if "$id" in node:
            clean_path = path.lstrip(".") or "var"

            id_paths[node["$id"]] = clean_path
            id_access[node["$id"]] = list(access)  # copy the list

        for key, value in node.items():
            if key in ("$id", "$ref"):
                continue
            human_seg = normalize_segment(key, "human")
            py_seg = normalize_segment(key, "python")

            new_path = path + human_seg if human_seg else path
            new_access = access + [py_seg] if py_seg is not None else list(access)

            collect_paths(value, new_path, new_access)

    elif isinstance(node, list):
        for idx, item in enumerate(node):
            new_path = path  # collapse indices for human-readable
            new_access = access + [idx]  # keep index for python path
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


# Build the paths
# Load the JSON file

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

def main():

    if len(sys.argv) < 2:
        print("Usage: python verifyOracleData.py oracle_index")
        sys.exit(1)

    # Get the first argument after the script name
    argument = sys.argv[1]

    with open("oracle specification" + os.sep + "assertion_" + str(argument) ,"r" ) as f:
        data = json.load(f)


    access = data["python_access"]
    test_name = data["test_name"]



    with open("stateData/temp/statefile.json") as f:
        vardata = json.load(f)

    # Open the file in append mode
    # with open("output.txt", "a") as f:
    #     f.write(str(vardata) + "\n")


    with open("all_states" + os.sep + test_name + os.sep + "fixed" + os.sep + "1.json") as f:
        testdata = json.load(f)

    for ac in access:
        temp = testdata[ac]
        testdata = temp
        # print(ac)
    # Open the file in append mode
    with open("output.txt", "a") as f:
        f.write("correct" + str(testdata) + "\n")

    temp = vardata["metas"][0]["graph"]

    for ac in access[3:]:
        temp = temp[ac]
    with open("output.txt", "a") as f:  # "w" = write (overwrites existing content)
        f.write("buggy" + str(testdata) + "\n" )

    if "$id" in testdata:
        node_id = testdata.get("$id")
    else:
        node_id = None
    # with open("output.txt", "a") as f:  # "w" = write (overwrites existing content)
    #     f.write(str(node_id))
    if len((deep_diff_ids(testdata,temp,node_id))) == 0:
        # with open("output.txt", "a") as f:  # "w" = write (overwrites existing content)
        #     f.write("same")
        return True
    else:
        # with open("output.txt", "a") as f:  # "w" = write (overwrites existing content)
        #     f.write("diff")
        return False

if __name__ == "__main__":
    # Open the file in append mode
    # with open("output.txt", "a") as f:
    #     f.write("hahah" + "\n")
    print(main())