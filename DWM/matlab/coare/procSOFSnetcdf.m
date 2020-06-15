pkg load octcdf
addpath('coare/flux-cor3_5')
;
nc = netcdf ('IMOS_ABOS-ASFS_RTSCP_20100318T000000Z_SOFS_FV00_SOFS-1-2010_END-20110420T040000Z_C-20140720T003341Z.nc', 'r');
;
t = nc{'AIRT'}(:,1); zu=nc{'HEIGHT_AI_CA_RE'}(1,:);
u = nc{'WSPD'}(:,1); zt=nc{'HEIGHT_AI_CA_RE'}(1,:);
rh = nc{'RELH'}(:,2); zq=nc{'HEIGHT_AI_CA_RE'}(2,:);
P = nc{'CAPH'}(:,1);
ts = nc{'TEMP'}(:,1);
Rs = nc{'SW'}(:,1);
Rl = nc{'LW'}(:,1);
lat=-47;
cp=NaN;
sigH=NaN;
zi=NaN;
rait = nc{'RAIT'}(:,1);
rain=0;
;
A = coare35vn(u,zu,t,zu,rh,zu,P,ts,Rs,Rl,lat,zi,rain,cp,sigH);
;
time=nc{'TIME'}(:);
tunit=nc{'TIME'}.units;
toffset=datenum('1950-01-01');
;
more off
fout = fopen('coare-out.csv', 'w');
;
fprintf(fout, "time,usr tau hsb hlb hbb hsbb hlwebb tsr qsr zot zoq Cd Ch Ce  L zet dter dqer tkt Urf Trf Qrf RHrf UrfN Rnl Le rhoa UN U10 U10N Cdn_10 Chn_10 Cen_10 RF Qs Evap T10 Q10 RH10\n");
for i=1:length(time)
    fprintf(fout, "%s", datestr(toffset+time(i), 'yyyy-mm-dd HH:MM:SS'));
    fprintf(fout, ",%f", A(i,:));
    fprintf(fout, "\n");
end
fclose(fout);

