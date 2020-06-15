from thredds_crawler.crawl import Crawl
from pydap.client import open_url
from pydap.client import open_dods
import pydap.lib
import os
import urllib

import psycopg2

import dateutil.parser
import pprint

def getAttributeOrDefault(name, default):
    try:
        value = dataset.attributes['NC_GLOBAL'][name]
    except (KeyError):
        value = default

    return value

def getDateOrDefault(name, default):
    value_att = getAttributeOrDefault(name, default)
    if value_att is None:
        value = None;
    else:
        value = dateutil.parser.parse(value_att)

    return value

pydap.lib.CACHE = "/tmp/pydap-cache/"

skips = Crawl.SKIPS + [".*FV00", ".*realtime", ".*Real-time", ".*daily", ".*REAL_TIME", ".*regridded", ".*burst", ".*gridded", ".*long-timeseries"]
#skips = Crawl.SKIPS + [".*realtime", ".*Real-time", ".*daily", ".*REAL_TIME", ".*regridded"]
#skips = Crawl.SKIPS + [".*regridded"]

#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/DA/catalog.xml', select=['.*'] , skip=skips)
c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/DA/EAC2000/Temperature/catalog.xml', select=['.*'] , skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/catalog.xml', select=['.*'] , skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/ASFS/catalog.xml', select=['.*'] , skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/SOTS/2016/catalog.xml', select=['.*'] , skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/SOTS/catalog.xml', select=['.*'] , skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/catalog.xml', select=['.*FV0[^0]'], skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ABOS/ASFS/SOFS/Surface_waves/catalog.xml', select=['.*FV0[^0]'], skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ANMN/NRS/NRSKAI/catalog.xml', select=['.*FV0[^0]'], skip=skips)
#c = Crawl('http://thredds.aodn.org.au/thredds/catalog/IMOS/ANMN/NRS/NRSMAI/catalog.xml', select=['.*FV0[^0]'], skip=skips)

#c = Crawl('http://dods.ndbc.noaa.gov/thredds/catalog/oceansites/DATA/IMOS-EAC/catalog.xml', select=['.*'])
#c = Crawl('http://dods.ndbc.noaa.gov/thredds/catalog/oceansites/DATA/IMOS-ITF/catalog.xml', select=['.*'])
#c = Crawl('http://dods.ndbc.noaa.gov/thredds/catalog/oceansites/DATA/SOTS/catalog.xml', select=['.*'])

#pprint.pprint(c.datasets)

urls = [s.get("url") for d in c.datasets for s in d.services if s.get("service").lower() == "httpserver"]

for url in urls:
    print(url)
    
    

