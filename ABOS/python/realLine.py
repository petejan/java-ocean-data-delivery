import sys

for filen in sys.argv[1:len(sys.argv)]:
#    print 'file ', filen

    with open(filen) as f:
        hdr = f.readline()
#        print 'header ', hdr

        ln = f.readline()
        splitLine = ln.split(',')
#        print 'line', len(splitLine)

        while len(splitLine) < 10:
#            print 'process line', ln

            ln = f.readline()
            splitLine = ln.split(',')

        print ('%s,%s,%s,%s,%s,%s') % (splitLine[0], splitLine[1], splitLine[2], splitLine[11], splitLine[12], splitLine[15])

