#!/usr/bin/env python

import re
import sys

""" Calculate  the checksum for NMEA sentence 
    from a GPS device. An NMEA sentence comprises
    a number of comma separated fields followed by
    a checksum (in hex) after a "*". An example
    of NMEA sentence with a correct checksum (of
    0x76) is:
    
      GPGSV,3,3,10,26,37,134,00,29,25,136,00*76"
"""

import operator
def checksum(sentence):
    sentence = sentence.strip('\n')
    sentence = sentence.strip('$')
    if sentence.find("*") == -1:
        nmeadata = sentence
        cksum = "100";
    else:
        nmeadata,cksum = sentence.split('*', 1)
    calc_cksum = reduce(operator.xor, (ord(s) for s in nmeadata), 0)

    return nmeadata,int(cksum,16),calc_cksum

if __name__=='__main__':

    """ NMEA sentence with checksum error (3rd field 
       should be 10 not 20)
    """
    line = "$GPGSV,3,3,20,26,37,134,00,29,25,136,00*76\n"
    line = "$GPRMC,141858.000,A,5749.8040,N,01200.6997,E,0.05,54.38,131006,,,A*54"

    #print 'Number of arguments:', len(sys.argv), 'arguments.'
    #print 'Argument List:', str(sys.argv)

    with open(sys.argv[1], 'r') as inf:
        for line in inf:

            idx = line.find('GP')

            nmea = '$' + line[idx:]
            """ Get NMEA data and checksums """

            #print "Line %s " % (line)
            data,cksum,calc_cksum = checksum(nmea)
            #print "Check Sum 0x%02x" % (cksum)
            print "%s*%02x" % (nmea.strip('\n'), calc_cksum)

            """ Verify checksum (will report checksum error) """ 
            #if cksum != calc_cksum:
                #print "Error in checksum for: %s" % (data)
                #print "Checksums are %s and %s" % (cksum, calc_cksum)

        