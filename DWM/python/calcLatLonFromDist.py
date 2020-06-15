import geopy
from geopy.distance import VincentyDistance

# given: lat1, lon1, b = bearing in degrees, d = distance in kilometers

lat1=-46.02652
lon1=142.12901
d = 10
b = 0

origin = geopy.Point(lat1, lon1)
destination = VincentyDistance(kilometers=d).destination(origin, b)

print 'centre', origin.latitude, origin.longitude

print b, destination.latitude, destination.longitude

b = 90
destination = VincentyDistance(kilometers=d).destination(origin, b)
print b, destination.latitude, destination.longitude

b = 180
destination = VincentyDistance(kilometers=d).destination(origin, b)
print b, destination.latitude, destination.longitude

b = 270
destination = VincentyDistance(kilometers=d).destination(origin, b)
print b, destination.latitude, destination.longitude
