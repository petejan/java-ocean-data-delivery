% SAZ47-15-2012 depth calculation

file_aqd = 'IMOS_ABOS-SOTS_AETVZ_20120720T000000Z_SAZ47_FV01_SAZ47-15-2012-Aquadopp-Current-Meter-1215_END-20131030T024000Z_C-20160609T041846Z.nc';
file_sbe = 'IMOS_ABOS-SOTS_CSTZ_20120710T000001Z_SAZ47_FV01_SAZ47-15-2012-SBE37SM-RS232-4422_END-20131012T023501Z_C-20160609T044138Z.nc';
file_star = 'IMOS_ABOS-SOTS_ETZ_20120714T000000Z_SAZ47_FV01_SAZ47-15-2012-Starmon-Mini-1000_END-20131028T235000Z_C-20161205T040250Z.nc';

pres_aqd = ncread(file_aqd, 'PRES_REL');
depth_sbe = ncread(file_sbe, 'DEPTH');
pres_DST = ncread(file_star, 'PRES_REL');
pres_aqd_qc = ncread(file_aqd, 'PRES_REL_quality_control');
depth_sbe_qc = ncread(file_sbe, 'DEPTH_quality_control');
pres_DST_qc = ncread(file_star, 'PRES_REL_quality_control');

time_aqd = ncread(file_aqd, 'TIME') + datenum(1950,1,1);
time_sbe = ncread(file_sbe, 'TIME') + datenum(1950,1,1);
time_DST = ncread(file_star, 'TIME') + datenum(1950,1,1);

pres_aqd = ncread(file_aqd, 'PRES_REL');
depth_sbe = ncread(file_sbe, 'DEPTH');
depth_DST = ncread(file_star, 'PRES_REL');

figure (1); clf
plot(time_aqd(pres_aqd_qc<=1), pres_aqd(pres_aqd_qc<=1)); hold on; grid on
plot(time_sbe(depth_sbe_qc<=1), depth_sbe(depth_sbe_qc<=1)); hold on; grid on
plot(time_DST(pres_DST_qc<=1), depth_DST(pres_DST_qc<=1)); hold on; grid on

mean_pres_aqd = mean(pres_aqd(pres_aqd_qc<=1));
mean_depth_sbe = mean(depth_sbe(depth_sbe_qc<=1));
mean_pres_DST = mean(pres_DST(pres_DST_qc<=1));
