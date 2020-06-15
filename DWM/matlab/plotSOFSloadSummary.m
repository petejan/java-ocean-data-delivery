% file = 'IMOS_ABOS-SOTS_RW_20100318T000000Z_SOFS_FV00_SOFS-1-2010-3DM-GX1-9476_END-20110420T040000Z_C-20161018T232551Z.nc';

%file = 'data\IMOS_ABOS-ASFS_RW_20150324T023434Z_SOFS_FV00_SOFS-5-2015_3DM-GX1-9476_END-20160413T201719Z_C-20160518T101215Z.nc';

file = 'data\IMOS_ABOS-SOTS_RW_20160316T130318Z_FluxPulse_FV00_FluxPulse-1-2016-3DM-GX1-9476_END-20160627T000000Z_C-20160627T101835Z.nc';

load = ncread(file, 'load');
swh = ncread(file, 'SWH');
s=size(load);
x=1:s(2);

load_range = [mean(load, 'omitnan')' min(load)' max(load)'];
swh_20 = swh;
swh_20(swh>20)=NaN();

accel = ncread(file, 'acceleration');
accel_range = permute([mean(accel(3,:,:), 'omitnan') min(accel(3,:,:)) max(accel(3,:,:))], [3 2 1]);

%ttl = ncreadatt(file, '/', 'title');
ttl = 'FluxPulse-1';

subplot(3,1,1);
plot(x, swh_20); grid();
title([ttl ' SWH (m)']);

subplot(3,1,2);
plot(x, load_range); grid();
title('load range, min, mean, max (kg)');

subplot(3,1,3);
plot(x, accel_range); grid(); ylim([-20 0]);
title('z acceleration range, min, mean, max (m/s/s)');
xlabel('sample no');