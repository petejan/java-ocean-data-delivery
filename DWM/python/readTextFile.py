## Open the file with read only permit
f = open('../SOFS-1-positions/SOFS-1 GPS data_clipped.csv')
## Read the first line
line = f.readline()

## If the file is not empty keep reading line one at a time
## till the file is empty
n = 0
while line:
    #print n, line
    split = line.split(',')
    #print split[1]
    time = split[0].split(':')
    date = split[1].split('-')
    lat = split[2].split(':')
    lon = split[3].split(':')
    #print lat, float(lat[0])+ float(lat[1][0:len(lat[1])-1])/60
    print '20%02d-%02d-%02d %02d:%02d:%02.0f,lat=%f,lon=%f' % \
          (int(date[2]), int(date[0]), int(date[1]), int(time[0]), int(time[1]), float(time[2]), -(float(lat[0])+ float(lat[1][0:len(lat[1])-1])/60), \
           (float(lon[0])+ float(lon[1][0:len(lon[1])-1])/60))
    n = n + 1
    line = f.readline()
f.close()