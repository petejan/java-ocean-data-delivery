% load data from Ocean Current fils for a location

file = 'IMOS_OceanCurrent_HV_2017_C-20170530T232703Z.nc';
lat = ncread(file, 'LATITUDE');
lon = ncread(file, 'LONGITUDE');

ucur = ncread(file, 'UCUR', [426 71 1], [1 1 inf]);
vcur = ncread(file, 'VCUR', [426 71 1], [1 1 inf]);

spd = vcur.*vcur+ucur.*ucur;
dir=atan2(vcur, ucur)*180/pi();

plot(permute(dir,[3 2 1]));

time=ncread(file, 'TIME') + datenum(1985,1,1);
