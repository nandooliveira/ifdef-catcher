import requests
import json
import pdb

import os

from git import Repo

repo_data = {
    "url": "https://github.com/GNOME/dia.git",
    "target": "/Users/fernandooliveira/Downloads/dia"
}

if os.path.exists(repo_data["target"]):
    repo = Repo(repo_data["target"])
else:
    repo = Repo.clone_from(repo_data["url"], repo_data["target"], branch="master")

tree = repo.tree()
commits = repo.iter_commits()

for blob in tree:
    commit = next(repo.iter_commits(paths=blob.path, max_count=1))
    pdb.set_trace()
    print(blob.path, commit.committed_date)

# headers={
#         "Accept": "application/vnd.github+json",
#         "Authorization": "Bearer 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"
#         }

# payload = {
#     "since": "2013-01-01T00:00:00Z",
#     "until": "2022-12-31T23:59:59Z"
# }
# response = requests.get(
#     "https://api.github.com/repos/GNOME/dia/commits",
#     headers=headers, params=payload
# )

# pdb.set_trace();
# print(json.dumps(response.json()))
