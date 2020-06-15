
%file='data/IMOS_ABOS-SOFS_RVT_20150324_SOFS_FV01_SOFS-5-2015-Vector-VEC-5045-VEC-8224-30m_END-20160413_C-20171118_PART01.nc';

%file = 'data/IMOS_ABOS-SOFS_RVT_20150324_SOFS_FV01_SOFS-5-2015-Vector-VEC-5045-VEC-8224-30m_END-20160413_C-20171118_PART02.nc';
%file = 'data/IMOS_ABOS-SOFS_RVT_20150324_SOFS_FV01_SOFS-5-2015-Vector-VEC-5045-VEC-8224-30m_END-20160413_C-20171118_PART03.nc';
file = 'data/IMOS_ABOS-SOFS_RVT_20150324_SOFS_FV01_SOFS-5-2015-Vector-VEC-5045-VEC-8224-30m_END-20160413_C-20171118_PART04.nc';

time=ncread(file, 'TIME') + datenum(1950,1,1);

pres=ncread(file, 'PRES');
accel = ncread(file, 'IMU_ACCEL');

figure(11);
pMax = max(pres, [], 1);
pMin = min(pres, [], 1);
pMean = mean(pres, 1);
plot(time, [pMax' pMean' pMin']);
datetick('x', 'keeplimits'); grid on

samplet=linspace(0,10/60/24*(1-1/19200),19200);
ts = time + samplet;
t1 = reshape(ts',size(ts,1)*size(ts,2),1);
a1 = reshape(accel(1,:,:),size(ts,1)*size(ts,2),1);

figure(12)

aMax = permute(max(accel, [], 2), [3 1 2]);
plot(time, aMax)
datetick('x', 'keeplimits'); grid on

n = 216;
n = 394;
figure(13);
plot(1:19200,accel(:,:,n)); grid

figure(14);
plot(1:19200,pres(:,n)); grid
legend(datestr(time(n)))

fileGPSIMU='~/ABOS/gpsIMU/gpsIMU-08.nc';
gpstime = ncread(fileGPSIMU, 'TIME') + datenum(1950,1,1);

gpspos = ncread(fileGPSIMU, 'gpsllh');

%figure(10); plot(gpstime, gpspos); grid on

gpsaccel = ncread(fileGPSIMU, 'accel');
figure(11); plot(1:size(gpsaccel,2), gpsaccel); grid on

sampleFreqHz = 1/(24*60*(gpstime(end)-gpstime(1))*60/size(gpstime,1));

figure(10);
subplot(2,1,1)
plot(t,gpsaccel); grid on
subplot(2,1,2)
plot(t1, a1)
ax(1)=subplot(2,1,1);
ax(2)=subplot(2,1,2);
linkaxes(ax,'x')

figure(10);
xl1 = [736774.961566175 736774.961623775];

samplet=linspace(0,10/60/24*(1-1/19200),19200) + 15.5/24/60/60;
ts = time + samplet;
t1 = reshape(ts',size(ts,1)*size(ts,2),1);
subplot(2,1,2)
plot(t1,a1)
ax(1).XLim = xl1;
datetick('x', 'keeplimits'); grid on

