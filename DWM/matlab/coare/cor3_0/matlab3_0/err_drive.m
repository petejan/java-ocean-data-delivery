disp('err_drive');
%Input environmental conditions
u=5;
Ts=29.2;
ta=28;
qa=18;
%Input sensor heights
zu=18;
zt=18;
zq=18;

%error specs  [intercept error   slope error]
%Xtrue=+-interror+(1+-sloperror)*Xmeas
ue=[.2 .03];%'true' wind speed relative to sea surface
te=[.3 0];%sea-air temperature difference
qe=[.3 0];%sea-air difference
%%%%%%%%%%%%

bulk_err_3;