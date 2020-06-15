
%file = 'Pulse-11-2015/IMOS_ABOS-SOTS_RTSCP_20150213T070000Z_Pulse_FV01_Pulse-11-2015-SubSurface_END-20160319T220000Z_C-20160528T101902Z.nc';
file = '../data/IMOS_ABOS-SOTS_FREMVSOBTCP_20091130_SOFS_FV02_SOFS-1-2010-Gridded-Data_END-20110523_C-20190611.nc';
%file = '../data/IMOS_ABOS-SOTS_REFOBKGTPCS_20100817_Pulse_FV02_Pulse-7-2010-Gridded-Data_END-20110708_C-20190522.nc';

deployment_code = ncreadatt(file, '/', 'deployment_code');
startt = datenum(ncreadatt(file, '/', 'time_deployment_start'), 'yyyy-mm-ddTHH:MM:SSZ');
endt = datenum(ncreadatt(file, '/', 'time_deployment_end'), 'yyyy-mm-ddTHH:MM:SSZ');

ts = ncread(file, 'TIME') + datenum('1950-01-01 00:00:00');

temp = ncread(file, 'TEMP')';
%vinfo = ncinfo(file, 'TEMP');
%depth_temp = ncread(file, vinfo.Dimensions(1).Name);
depth_temp = ncread(file, 'DEPTH_TEMP');

pres = ncread(file, 'PRES')';
%vinfo = ncinfo(file, 'PRES');
%depth_p = ncread(file, vinfo.Dimensions(1).Name);
depth_p = ncread(file, 'DEPTH_PRES');

depth_p = [0 depth_p']';
pres = [zeros(size(ts,1),1) pres']';

% generate interpolated depths

d1 = repmat(depth_temp,1,length(ts));
p1 = repmat(depth_p,1,length(ts));

p = zeros(size(d1,1), length(ts));

for I=1:length(ts)
    p(:,I) = interp1(p1(:,I), pres(:,I),d1(:,I));
end

t1=repmat(ts',size(temp,1),1);

% Generate a step based mixed layer depth

MLD_step_threshold = 0.3;

mld_temps = temp;
mld_step = zeros(length(mld_temps),1);
mld = p(size(p,1),:);

for i = 1:length(mld_step)
    
    mld_i = find((mld_temps(:,i)<(mld_temps(1,i)-MLD_step_threshold)|(mld_temps(:,i)>(mld_temps(1,i)+MLD_step_threshold))),1);

    if ~isempty(mld_i)           
        mld_step(i) = mld_i(1);     
    else        
        mld_step(i) = size(mld_temps,1);
    end
    mld(i) = p(mld_step(i),i);
end

% calculate mean daily, pressure, temperature profile, and mixlayer depth

tmi = floor(ts-floor(ts(1)));
temp_mean_day=zeros(size(temp,1), tmi(length(tmi)) - tmi(1) + 1);
pres_mean_day=zeros(size(p,1), tmi(length(tmi)) - tmi(1) + 1);
mld_mean_day=zeros(size(mld,1), tmi(length(tmi)) - tmi(1) + 1);
time_days=zeros(1, tmi(length(tmi)) - tmi(1) + 1);
j = 1;
for i=tmi(1):tmi(length(tmi))
    temp_mean_day(:,j)=mean(temp(:,tmi==i),2);
    pres_mean_day(:,j)=mean(p(:,tmi==i),2);
    mld_mean_day(:,j)=mean(mld(:,tmi==i),2);
    time_days(j)=i+floor(ts(1))+0.5;
    j = j + 1;
end

% calculate overall mean temperature to offset plot to

temp_mean = mean(mean(temp),'omitnan');

t2=repmat(time_days,size(temp_mean_day,1),1);

clf;
x = 5*(temp_mean_day-temp_mean)+t2; % temperature * 5 + ts offsets line by temperature
plot(x, pres_mean_day, '.:b'); axis 'ij'; grid()

hold on

% plot the daily mean mixed layer depth

plot(time_days, mld_mean_day, '*red'); axis 'ij'
title([deployment_code ' : daily mean Temperature profile, Mix Layer depth']);
ylabel('depth (dbar)'); 
xlim([startt endt])
datetick('x', 'yyyy-mm-dd', 'KeepLimits'); 

fid = fopen([deployment_code '-MLD.txt'], 'w+');
for i=1:length(ts)
    fprintf(fid,'%s,MLD=%4.3f\n', datestr(ts(i), 'yyyy-mm-dd HH:MM:SS'), mld(i));
end
fclose(fid);

print( '-dpdf', [deployment_code '-WaterFall.pdf'], '-fillpage');