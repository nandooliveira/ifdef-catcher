def get_greater(project_arr):
    version0 = project_arr[0].split(',')[1].lower()
    version1 = project_arr[1].split(',')[1].lower()

    split_char = None
    if '.' in version0:
        split_char = '.'
    elif '_' in version0:
        split_char = '_'
    
    if split_char == None:
        if version0 < version1:
            return project_arr[0].split(',') , project_arr[1].split(',')
        else:
            return project_arr[1].split(',') , project_arr[0].split(',')
    
    version0_splited = version0.split(split_char)
    version1_splited = version1.split(split_char)

    for i in range(len(version0_splited)):
        number0 = version0_splited[i]
        if (i + 1) > len(version1_splited):
            break
        number1 = version1_splited[i]
        if number0.isdigit() and number1.isdigit():
            if int(number0) < int(number1):
                return project_arr[0].split(',') , project_arr[1].split(',')
            elif int(number0) > int(number1):
                return project_arr[1].split(',') , project_arr[0].split(',')
        else:
            if number0.lower() < number1.lower():
                return project_arr[0].split(',') , project_arr[1].split(',')
            elif number0.lower() > number1.lower():
                return project_arr[1].split(',') , project_arr[0].split(',')
    
    return project_arr[0].split(',') , project_arr[1].split(',')

projects = {}

with open('result.csv', 'r') as f:
    skip_line = True
    for line in f:
        if skip_line:
            skip_line = False
            continue
        line = line.strip()
        projectname = line.split(',')[0]
        if projectname not in projects:
            projects[projectname] = [line]
        else:
            projects[projectname].append(line)

with open('compare.csv', 'w') as f:
    f.write('projectname,diff loc,diff blocks,diff % disciplined,diff disciplined\n')
    for project in projects:
        if len(projects[project]) < 2:
            continue
        #first, second = ['','']
        
        first,second = get_greater(projects[project])
        #if projects[project][0].split(',')[1].lower() < projects[project][1].split(',')[1].lower():
        #    first = projects[project][0].split(',')
        #    second = projects[project][1].split(',')
        #else:
        #    first = projects[project][1].split(',')
        #    second = projects[project][0].split(',')
        print("SECOND")
        print(second[3])
        print("FIRST")
        print(first[3])
        diff_loc = int(second[3]) - int(first[3])
        diff_blocks = int(second[4]) - int(first[4])
        diff_disciplined = float(second[5]) - float(first[5])
        diff_n_disciplined = int(second[-1]) - int(first[-1])

        f.write(project + ',' + str(diff_loc) + ',' + str(diff_blocks) + ',' + \
            str(round(diff_disciplined,2)) + ',' + str(diff_n_disciplined) + '\n')
