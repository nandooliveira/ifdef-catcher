import json
import os

dir_path = '/Users/fernandooliveira/workspaces/doutorado/ifdef-catcher/phase-3-ifdef-transformation-detector/check_commits/output'
res = {}

for path in os.listdir(dir_path):
    if path.endswith('.csv'):
        with open(path, 'r') as f:
            res[f.name] = len(f.readlines())

print(json.dumps(res, sort_keys=True, indent=4))
