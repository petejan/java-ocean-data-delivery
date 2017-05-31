import sys
from datetime import datetime
from datetime import timedelta

i = 0
ts = datetime.strptime('2000-01-01 00:00:00', '%Y-%m-%d %H:%M:%S')
lastTs = ts

col_dict = {0: 'record', 2:'bat', 3:'MRUsamples', 4:'MRUbad', 5: 'accel', 6: 'optodeTemp', 7:'optodeBPhase', 8: 'optodeTemp', 9:'optodeBPhase', 10:'CHL', 11:'NTU', 12:'CHL', 13:'NTU', 14:'PAR', 15:'PAR', 16:'load', 17:'load_sd'}

with open(sys.argv[1]) as f:
    for line in f:
        ln = line.replace('\n', '')
        splitLine = ln.split(',')
        if len(splitLine[1]) >= 19:
            ts = date = datetime.strptime(splitLine[1], '%Y-%m-%d %H:%M:%S')
        else:
            ts = lastTs + timedelta(seconds=60*60)

        values = ''
        for i in (0, 2, 3, 4, 6, 7, 10, 11, 14, 16, 17):
            if len(splitLine[i]) > 1:
                values = values + ',' + col_dict[i] + '='+splitLine[i]

        print('%s%s') % (ts, values)

        values = ''
        for i in (8, 9, 12, 13, 15):
            if len(splitLine[i]) > 0:
                values = values + ',' + col_dict[i] + '='+splitLine[i]
        if len(values) > 0:
            ts1 = ts + timedelta(seconds=60*30)
            print('%s%s') % (ts1, values)

        lastTs = ts
        i = i + 1

print 'end of file, lines ', i