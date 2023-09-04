import csv

data_file = "phase2-analysis.csv"

projects = {}

with open(data_file, "r") as file:
    csvreader = csv.reader(file)

    for row in csvreader:
        if not row[0] in projects:
            projects[row[0]] = {
                "commits": set(),
                "authors": set(),
                "transformations": {
                    "d_to_nd": {
                        "count": 0,
                        "root": 0,
                        "floss": 0,
                        "devs": set()
                    },
                    "nd_to_d": {
                        "count": 0,
                        "root": 0,
                        "floss": 0,
                        "devs": set()
                    }
                }
            }

        project = projects[row[0]]
        project["commits"].add(row[17])
        project["authors"].add(row[18])

        if row[3] == "ND -> D":
            project["transformations"]["nd_to_d"]["count"] += 1

            if row[4] == "Root Canal":
                project["transformations"]["nd_to_d"]["root"] += 1
            elif row[4] == "Floss":
                project["transformations"]["nd_to_d"]["floss"] += 1

            project["transformations"]["nd_to_d"]["devs"].add(row[18])
        elif row[3] == "D -> ND":
            project["transformations"]["d_to_nd"]["count"] += 1

            if row[4] == "Root Canal":
                project["transformations"]["d_to_nd"]["root"] += 1
            elif row[4] == "Floss":
                project["transformations"]["d_to_nd"]["floss"] += 1

            project["transformations"]["d_to_nd"]["devs"].add(row[18])

for project_name in projects:
    project = projects[project_name]

    print("\multicolumn{1}{|l}{%s} & %s & %s & %s & %s & %s & %s & %s & %s & %s & %s \\\\" %
        (
            project_name.capitalize(),
            str(len(project["commits"])),
            str(len(project["authors"])),
            str(project["transformations"]["nd_to_d"]["count"]),
            str(len(project["transformations"]["nd_to_d"]["devs"])),
            str(project["transformations"]["nd_to_d"]["floss"]),
            str(project["transformations"]["nd_to_d"]["root"]),
            str(project["transformations"]["d_to_nd"]["count"]),
            str(len(project["transformations"]["d_to_nd"]["devs"])),
            str(project["transformations"]["d_to_nd"]["floss"]),
            str(project["transformations"]["d_to_nd"]["root"])
        )
    )
