% plot PULSE-9 RAS samples

file = 'data/IMOS_ABOS-SOTS_RWOBKFGTPCS_20120619_Pulse_FV02_Pulse-9-2012-Gridded-Data_END-20130523_C-20180529.nc';

time1 = ncread(file, 'TIME') + datetime(1950,1,1);
pres = ncread(file, 'PRES') ;
pres_qc = ncread(file, 'PRES_quality_control') ;
mld = ncread(file, 'MLD') ;

pres_qc_sum = sum(pres_qc,2);

grid on
axis 'ij'

ylim([20 100]);

time2 = ncread('data/IMOS_ABOS-SOTS_SKOPTR_20120722_Pulse_FV01_Pulse-9-2012--RAS-3-48-500-12709-01-39m_END-20130418_C-20180529.nc', 'TIME') + datetime(1950,1,1);
hold on
for i=1:size(time2,1)
    idx(i)=find(time1>=time2(i),1);
end

pres_mean = mean(pres(pres_qc(:,1)<=1,1), 'omitnan');
p1 = pres(:,1);
p1(isnan(p1)) = pres_mean;
p1(p1<20) = pres_mean;
p1(p1>100) = pres_mean;

plot(time2, p1(idx), '*r');
hold on
plot(time1(pres_qc(:,2)<=1), pres(pres_qc(:,2)<=1,2:5));
plot(time1(pres_qc(:,1)<=1), pres(pres_qc(:,1)<=1,1));
axis 'ij';
grid on
ylim([20 100]);
plot(time2, p1(idx), '*r');
title('Pulse-9 RAS sample time pressure'); ylabel('pressure (dbar)'); xlabel('date');

figure(2);
hold on
plot(time1, mld)
plot(time2, p1(idx), '*r')
axis 'ij'; grid on

title('Pulse-9 RAS sample time mix layer depth'); ylabel('mixlayer depth (dbar)'); xlabel('date');
