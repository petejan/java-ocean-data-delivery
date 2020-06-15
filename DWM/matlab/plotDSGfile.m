% cd ~/ABOS/git/java-ocean-data-delivery/ABOS/

%file = 'data/IMOS_ABOS-SOTS_KRSF_20120718_SAZ47_FV01_SAZ47-15-2012-SedimentTrap-DiscreteGeometries_END-20131008_C-20170929.nc';
%file='data/IMOS_ABOS-SOTS_F_20090928_SOFS_FV01_SOFS-1-2010-TOTAL_GAS_PRESSURE-DiscreteGeometries_END-20190322_C-20190610.nc';
file='data/IMOS_ABOS-SOTS_F_20090928_SOFS_FV01_SOFS-1-2010-DOX2-DiscreteGeometries_END-20190322_C-20190610.nc';

%param = 'TOTAL_GAS_PRESSURE';
param = 'DOX2';

station = ncread(file, 'stationIndex');
var = ncread(file, param);
var_unit = ncreadatt(file, param, 'units');
%var_aux = ncreadatt(file, param, 'ancillary_variables');
%var_qc = ncread(file, var_aux);
var_name = ncreadatt(file, param, 'name');
time = ncread(file, 'TIME') + datetime(1950,1,1);
stationStr = ncread(file, 'station_name');

depth = ncread(file, 'NOMINAL_DEPTH');

figure(1);
clf

sh(1) = subplot(2,1,1);
hold on

for i = 0:max(station)
%     leg = '.';
%     if depth(i+1) == 1000
%         leg = 'x'
%     end
%     if depth(i+1) == 2000
%         leg = '*'
%     end
%     if depth(i+1) > 2000
%         leg = 'o'
%     end
    plot(time(station==i), var(station==i), 'DisplayName', stationStr(:,i+1))
end

grid on
ylabel([var_name ' (' var_unit ')'])
%datetick('x', 'keeplimits');

file='data/IMOS_ABOS-SOTS_F_20090928_SOFS_FV01_SOFS-1-2010-TOTAL_GAS_PRESSURE-DiscreteGeometries_END-20190322_C-20190610.nc';

param = 'TOTAL_GAS_PRESSURE';

station = ncread(file, 'stationIndex');
var = ncread(file, param);
var_unit = ncreadatt(file, param, 'units');
%var_aux = ncreadatt(file, param, 'ancillary_variables');
%var_qc = ncread(file, var_aux);
var_name = ncreadatt(file, param, 'name');
time = ncread(file, 'TIME') + datetime(1950,1,1);
stationStr = ncread(file, 'station_name');

depth = ncread(file, 'NOMINAL_DEPTH');

sh(2) = subplot(2,1,2);
hold on
for i = 0:max(station)
%     leg = '.';
%     if depth(i+1) == 1000
%         leg = 'x'
%     end
%     if depth(i+1) == 2000
%         leg = '*'
%     end
%     if depth(i+1) > 2000
%         leg = 'o'
%     end
    plot(time(station==i), var(station==i), 'DisplayName', stationStr(:,i+1))
end

grid on
ylabel([var_name ' (' var_unit ')'])
%datetick('x', 'keeplimits');

