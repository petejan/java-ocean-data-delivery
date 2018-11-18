% add a incomming radiation to PAR file

file = 'data/IMOS_ABOS-SOTS_F_20090928_SOFS_FV01_SOFS-1-2010-PAR-DiscreteGeometries_END-20160413_C-20180423.nc';

ncid = netcdf.open(file,'NC_WRITE');
%varid = netcdf.defVar(ncid,'INCOMMING','double',dimids);

[dimname, dimlen] = netcdf.inqDim(ncid,0);

time1 = ncread(file, 'TIME') + datetime(1950,1,1);

lat = ncreadatt(file, '/', 'geospatial_lat_max');
lon = ncreadatt(file, '/', 'geospatial_lon_max');

jd = time1 - datenum(year,1,1);
[DEC,JD,AZM,RAD] = soradna(lat,lon,jd,year);

varid = netcdf.inqVarID(ncid,'cSR');

netcdf.putAtt(ncid,varid,'name','celestial incoming solar radiation ');
netcdf.putAtt(ncid,varid,'long_name','incoming_solar_radiation');
netcdf.putAtt(ncid,varid,'units','W/m2');
netcdf.putAtt(ncid,varid,'comment','using http://mooring.ucsd.edu/software/matlab/doc/toolbox/geo/suncycle.html');

netcdf.putVar(ncid,varid,0,numel(RAD),RAD);

netcdf.close(ncid);