% SOFS float current

file='IMOS_ABOS-ASFS_RVT_20150324T023434Z_SOFS_FV00_SOFS-5-2015-Aquapro-1-MHz-AQP-5830-AQD-11011-1m_END-20160413T201719Z_C-20170314T105153Z_PART01.nc';

cur=ncread(file, 'VELOCITY');
time=ncread(file, 'TIME') + datenum(1950,1,1);

transform_mat = ncreadatt(file, '/', 'nortek_headconfig_transformation_matrix');

tm = reshape(transform_mat, [3 3]);

cur_pur = permute(cur, [3 1 2]);

head=ncread(file, 'HEAD');

cur_xyz = tm' * cur_pur(:,:,1)';
cur_spd = sqrt(cur_xyz(1,:).^2 + cur_xyz(2,:).^2);
dir = atan2d(cur_xyz(2,:), cur_xyz(1,:));

plot(time, dir,'.', time, head-180,'.'); ylim([-180 180]);
plot(time, dir,'.'); ylim([-180 180]);

figure(2); histogram(dir, 'DisplayStyle', 'stairs', 'Binwidth', 2, 'Normalization', 'count'); grid on
