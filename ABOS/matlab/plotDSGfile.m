% cd ~/ABOS/git/java-ocean-data-delivery/ABOS/

file = 'data/IMOS_ABOS-SOTS_KRSF_20120718_SAZ47_FV01_SAZ47-15-2012-SedimentTrap-DiscreteGeometries_END-20131008_C-20170929.nc';

station = ncread(file, 'stationIndex');
bsi = ncread(file, 'BSi');
bsi_unit = ncreadatt(file, 'BSi', 'units');
bsi_name = ncreadatt(file, 'BSi', 'name');
time = ncread(file, 'TIME') + datenum(1950,1,1);
stationStr = ncread(file, 'station_name');
depth = ncread(file, 'NOMINAL_DEPTH');

figure(1);
clf
hold on
for i = 0:max(station)
    leg = '.';
    if depth(i+1) == 1000
        leg = 'x'
    end
    if depth(i+1) == 2000
        leg = '*'
    end
    if depth(i+1) > 2000
        leg = 'o'
    end
    plot(time(station==i), bsi(station==i), leg)
end

grid on
ylabel([bsi_name ' (' bsi_unit ')'])
datetick('x', 'keeplimits');

