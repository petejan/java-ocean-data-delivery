% convert IMU/GPS netcdf files into wave stats

file='/Volumes/DWM-2018/SOFS-6/data/SOFS-6-GPSlogger/gpsIMU-17.nc';
imua = ncread(file, 'accel');
imuq = ncread(file, 'orientation');

time = ncread(file, 'TIME') + datenum(1950,1,1);
gpstime = ncread(file, 'gpsTIME') + datenum(1950,1,1);
gpsidx = ncread(file, 'gpsIdx');
gpsPOS = ncread(file, 'gpsPOS');

d = designfilt('bandpassfir', ...       % Response type
       'FilterOrder',86, ...            % Filter order
       'StopbandFrequency1',0.02, ...    % Frequency constraints
       'PassbandFrequency1',0.0325, ...
       'PassbandFrequency2',0.485, ...
       'StopbandFrequency2',0.6, ...
       'DesignMethod','ls', ...         % Design method
       'StopbandWeight1',1, ...         % Design method options
       'PassbandWeight', 2, ...
       'StopbandWeight2',3, ...
       'SampleRate',2)               % Sample rate

igps = find(gpstime>datenum(2017,6,23,5,0,0),1);
it = find(gpsidx>igps,1);

i = 1;
for it=10:3072:size(time,1)-12288
    imuaccel = imua(:,it:it+12288-1);
    imuquant = imuq(:,it:it+12288-1);

    q1 = quaternion(imuquant(:,:));
    vr = RotateVector(q1,imuaccel(:,:)) * 9.81;
    fs = 10;
    [pxx,f] = pwelch(vr(3,:), 1024, 512, 1024, fs, 'power');
    use = find(f >= 0.03 & f <= 1);

    swh1(i) = 4*sqrt(sum(pxx(use)./((2*pi()*f(use)).^4)));
    disp(swh1(i))
    t(i) = gpstime(gpsidx(it));
    
    i = i + 1;
end

fileMRU = 'IMOS_ABOS-SOTS_RW_20170319_SOFS_FV01_SOFS-6-2017-3DM-GX1-9476-1m_END-20171101_C-20171127.nc';
timeMRU = ncread(fileMRU, 'TIME') + datenum(1950,1,1);
swhMRU = ncread(fileMRU, 'SWH');
plot(t,swh1,'.-'); hold on
grid on
plot(timeMRU, swhMRU)

% 
% fileMRU = 'IMOS_ABOS-SOTS_RW_20170319_SOFS_FV01_SOFS-6-2017-3DM-GX1-9476-1m_END-20171101_C-20171127.nc';
% 
% imuaccel = ncread(fileMRU, 'acceleration');
% imuquant = ncread(fileMRU, 'quaternion');
% sample_time = ncread(fileMRU, 'sample_time');
% spectra = ncread(fileMRU, 'wave_spectra');
% 
% timeMRU = ncread(fileMRU, 'TIME') + datenum(1950,1,1);
% 
% swh = ncread(fileMRU, 'SWH');
% fs = 5;
% 
% % use sample 2000, 2885 as an example
% i=2885;
% q1 = quaternion(imuquant(:,:,i));
% vr = RotateVector(q1,imuaccel(:,:,i));
% vr(3,isnan(vr(3,:)))=-9.81;
% [pxx,f] = pwelch(vr(3,:),512,256,512,fs,'power');
% 
% use = find(f >= 0.03 & f <= 1);
% 
% plot(f,10*log10(pxx)); grid on; hold on
% plot(f(use),10*log10(pxx(use))); hold off
% 
% xlabel('Frequency (Hz)')
% ylabel('Magnitude (dB)')
% 
% swh1 = 4*sqrt(sum(pxx(use)./((2*pi()*f(use)).^4)))/1.16
% 
