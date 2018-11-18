
fileIMUgps='gpsIMU-08.nc';

imutime = ncread(fileIMUgps, 'TIME') + datenum(1950,1,1);

imut = linspace(imutime(1),imutime(end), size(imutime,1));
imuaccel = ncread(fileIMUgps, 'accel');
imuquant = ncread(fileIMUgps, 'orientation');

figure(1);
plot(imut, imuaccel(3,:))

datetick('x', 'keeplimits'); grid on

figure(2);
spectrogram(imuaccel(3,:),2048*4,1024*2,1024,10,'reassign','MinThreshold',-40,'yaxis')

imuq = quaternion(imuquant);
vr = RotateVector(imuq,imuaccel);

swh = ncread('data/IMOS_ABOS-ASFS_W_20170319T220000Z_SOFS_FV00_SOFS-6-2017-MRU-Surface-wave-height-realtime.nc', 'VAVH');
time_swh = ncread('data//IMOS_ABOS-ASFS_W_20170319T220000Z_SOFS_FV00_SOFS-6-2017-MRU-Surface-wave-height-realtime.nc', 'TIME') + datenum(1950,1,1);
figure(3); plot(time_swh, swh); grid; datetick('x', 'keeplimits');
title('SOFS-6-MRU swh'); xlabel('date'); ylabel('SWH (m)');

