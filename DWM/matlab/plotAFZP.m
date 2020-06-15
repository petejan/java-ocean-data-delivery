
fn = '08';

file=['data/IMOS_ABOS-SOTS_RA_20170319_SOFS_FV01_SOFS-6-2017-MF-AWCP-55052-0m_END-20171101_C-20180218_PART' fn '.nc'];

time=ncread(file, 'TIME') + datenum(1950,1,1);
deployment = ncreadatt(file, '/', 'deployment_code');

hfig=figure(1);
sh(1) = subplot(2,1,1);
sv125=ncread(file, 'Sv125');
depth125=ncread(file, 'depth_125');
imagesc(time,depth125,sv125)
colorbar
caxis([-100 -70])
grid on
%xticks(([ 0 find(diff(time) > 2)']+1))
%xticklabels(datestr(time([ 0 find(diff(time) > 2)']+1)))
title([deployment ' : 125 kHz'])

sh(2)=subplot(2,1,2);
sv38=ncread(file, 'Sv38');
depth38=ncread(file, 'depth_38');
imagesc(time,depth38,sv38)
colorbar
caxis([-100 -70])
grid on
%xticks(([ 0 find(diff(time) > 2)']+1))
%xticklabels(datestr(time([ 0 find(diff(time) > 2)']+1)))
title([deployment ' : 38 kHz'])

linkaxes(sh,'x');

subplot(2,1,1);
%xticks(1:59:size(time,1))
%xticklabels(datestr(time(1:59:size(time,1)), 'HH:MM'))
%xlim([0 1400])

%hfig.PaperOrientation='landscape';
%saveas(hfig, ['sofs-6-azfp-1d-' fn '.pdf'])

%tightfig(hfig)