file='IMOS_ABOS-SOTS_RA_20160316T130318Z_FluxPulse_FV00_FluxPulse-1-2016-MF-AWCP-55052-0m_END-20160623T010000Z_C-20170511T074224Z_PART01.nc';

sv_38 = ncread(file, 'Sv38');
time = ncread(file, 'TIME') + datenum(1950,1,1);

d = 10000/40000*1500/2;
x=0:d/10000:(d-d/10000);

figure(2)
plot(x,sv_38(:,2259))
grid on;
figure(2);
xlabel('distance (m)');
ylabel('SV (dB rel 1 uPa at 1m)');

figure(1)
imagesc(time,x,sv_38);
colormap('jet')
caxis([-100 -60]);
title('FluxPulse-1 AZFP 38 kHz data');
datetick('x', 'keeplimits');
