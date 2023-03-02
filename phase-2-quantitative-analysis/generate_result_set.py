import csv

headers = []

def get_map():
    map = {}
    global headers
    with open('result.csv', 'r') as f:
        reader = csv.reader(f)
        first_row = True
        for row in reader:
            if first_row:
                headers = row
                first_row = False
            else:
                map_values = {}
                for index in range(0,len(headers)):
                    map_values[headers[index]] = row[index]
                map_versions = {}
                if map_values['projectname'] in map:
                    map_versions = map[map_values['projectname']]
                map_versions[map_values['version']] = map_values
                map[map_values['projectname']] = map_versions
    return map

def get_versions_ordered(project_arr):
    #print(project_arr)
    array_ver = []
    for v in project_arr:
        array_ver.append(v)
    version0 = array_ver[0]
    version1 = array_ver[1]

    split_char = None
    if '.' in version0:
        split_char = '.'
    elif '_' in version0:
        split_char = '_'

    if split_char == None:
        if version0 < version1:
            return project_arr[version0] , project_arr[version1]
        else:
            return project_arr[version1] , project_arr[version0]

    version0_splited = version0.split(split_char)
    version1_splited = version1.split(split_char)

    for i in range(len(version0_splited)):
        number0 = version0_splited[i]
        if (i + 1) > len(version1_splited):
            break
        number1 = version1_splited[i]
        if number0.isdigit() and number1.isdigit():
            if int(number0) < int(number1):
                return project_arr[version0] , project_arr[version1]
            elif int(number0) > int(number1):
                return project_arr[version1] , project_arr[version0]
        else:
            if number0.lower() < number1.lower():
                return project_arr[version0] , project_arr[version1]
            elif number0.lower() > number1.lower():
                return project_arr[version1] , project_arr[version0]

    return project_arr[version0] , project_arr[version1]

def get_first_version(map, projectname):
    a, b = get_versions_ordered(map[projectname])
    return a['version']


def get_second_version(map, projectname):
    a, b = get_versions_ordered(map[projectname])
    return b['version']

def create_table():
    table = ''
    map = get_map()

    total = []

    AA_OLD_D_SUM = 0
    AA_OLD_ND_SUM = 0
    AA_NEW_D_SUM = 0
    AA_NEW_ND_SUM = 0

    for projectname in map:
        first_version = get_first_version(map, projectname)
        second_version = get_second_version(map, projectname)

        color_LOC = 'blue'
        calc_LOC = int(map[projectname][second_version]['#LOC']) - int(map[projectname][first_version]['#LOC'])
        if calc_LOC < 0:
            color_LOC = 'red'

        color_AA = 'blue'
        calc_AA = int(map[projectname][second_version]['#AA']) - int(map[projectname][first_version]['#AA'])
        if calc_AA < 0:
            color_AA = 'red'

        color_disc = 'blue'
        calc_disc = float(map[projectname][second_version]['%disciplined']) -\
                float(map[projectname][first_version]['%disciplined'])
        if calc_disc < 0:
            color_disc = 'red'

        total.append(calc_disc)

        AA_OLD = int(map[projectname][first_version]['#AA'])
        AA_NEW = int(map[projectname][second_version]['#AA'])
        D_OLD = float(map[projectname][first_version]['%disciplined'])/100.0
        D_NEW = float(map[projectname][second_version]['%disciplined'])/100.0
        ND_OLD = 1.0 - D_OLD
        ND_NEW = 1.0 - D_NEW

        AA_OLD_D = AA_OLD * D_OLD
        AA_OLD_ND = AA_OLD * ND_OLD
        AA_NEW_D = AA_NEW * D_NEW
        AA_NEW_ND = AA_NEW * ND_NEW

        AA_OLD_D_SUM += AA_OLD_D
        AA_OLD_ND_SUM += AA_OLD_ND
        AA_NEW_D_SUM += AA_NEW_D
        AA_NEW_ND_SUM += AA_NEW_ND

        table += projectname + ',' + map[projectname][first_version]['#LOC'] + ',' + \
            map[projectname][second_version]['#LOC'] + ',' + str(calc_LOC) + ',' + \
            map[projectname][first_version]['#AA'] + ',' + map[projectname][second_version]['#AA'] + ',' +\
            str(calc_AA)  + \
            ',' + map[projectname][first_version]['%disciplined'] + ',' + \
            map[projectname][second_version]['%disciplined'] + ',' + color_disc + ',' + \
            "{:.2f}".format(calc_disc) + '\n'

    SUM_TOTAL = []
    SUM_TOTAL.append(AA_OLD_D_SUM)
    SUM_TOTAL.append(AA_OLD_ND_SUM)
    SUM_TOTAL.append(AA_NEW_D_SUM)
    SUM_TOTAL.append(AA_NEW_ND_SUM)

    return table, total, SUM_TOTAL

result, total, sum_total = create_table()
print(result)
#print("\n")
#print(total)
#print("\n")
#print(sum_total)
