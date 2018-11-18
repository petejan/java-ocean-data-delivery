% plot java-ocean-data-delivery generated data

file='IMOS_ABOS-ASFS_RA_20120714T000000Z_SOFS_FV00_SOFS-3-2012-MF-AWCP-55052-30m_END-20130102T000000Z_C-20170404T104537Z_PART01.nc';

time = ncread(file, 'TIME') + datenum(1950,1,1);
mooring = ncreadatt(file, '/', 'deployment_code');

sv38 = ncread(file, 'Sv38');
sv125 = ncread(file, 'Sv125');
sv200 = ncread(file, 'Sv200');
sv455 = ncread(file, 'Sv455');

plot(sv38(:,2000)); grid on
hold on
plot(sv125(:,2000)); grid on
plot(sv200(:,2000)); grid on
plot(sv455(:,2000)); grid on

legend('38', '125', '200', '455');
title([mooring ' : ping : ' datestr(time(2000), 'yyyy-mm-dd') ' : jodd data processing' ]);

count = 10.^(sv38/20);
count_min = min(count,[], 2);
sv_lessnoise = 20*log10(count(:,2000) - count_min);
figure(2)
plot(sv_lessnoise); grid on;

ch = 2;
sv_mean = ncread('IMOS_SOOP-BA_AE_20120712T171419Z_VLHJ_FV02_Southern-Surveyor-EK60-38-120_END-20120713T095640Z_C-20150618T013040Z.nc', 'Sv');
sv1 = permute(sv_mean, [3 2 1]);
figure(3);
depth = ncread('IMOS_SOOP-BA_AE_20120712T171419Z_VLHJ_FV02_Southern-Surveyor-EK60-38-120_END-20120713T095640Z_C-20150618T013040Z.nc', 'DEPTH');
time = ncread('IMOS_SOOP-BA_AE_20120712T171419Z_VLHJ_FV02_Southern-Surveyor-EK60-38-120_END-20120713T095640Z_C-20150618T013040Z.nc', 'TIME') + datenum(1950,1,1);
imagesc(time, depth, 10*log10(sv1(:,:,ch)')); colormap(jet); colorbar()
datetick('x', 'keeplimits');
figure(2); plot(depth, 10*log10(sv1(140,:,ch)), depth, 10*log10(sv1(1,:,ch))); grid on
ttl = ncreadatt('IMOS_SOOP-BA_AE_20120712T171419Z_VLHJ_FV02_Southern-Surveyor-EK60-38-120_END-20120713T095640Z_C-20150618T013040Z.nc', '/', 'deployment_id');
ttl2 = strrep(ttl, '_', ' ');
title(ttl2)
xlabel('depth (m)'); ylabel('Sv (dB)');
legend('day', 'night');
