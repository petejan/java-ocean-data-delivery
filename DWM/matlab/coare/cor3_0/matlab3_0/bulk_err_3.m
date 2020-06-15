disp('bulk_err')
%uses vector form of coare3.0 and coare stability functions
%analytical error functions for normalized error (delX/X): 
%dus for u*t
%dzet for z/L
%dwth for sensible HF
%dwq for latent HF
%dwu for stress

qs=qsea(Ts);
bet=1.25;
k=0.4;
al=9.8/(ta+273.15);

%********   specify error in sqrt of neutral xfer coefficients
%************    .001 corresponds to .001/.035=0.0286 or 5.7% in Cd, Ce, Ch

ecu=.001/10;
ect=.001/10;
ecq=.001/10;
%************   

x=[5 0 Ts ta qs qa 203 413 0 600 1010 zu zt zq  0 1 0 5 1 ];%coare30, cool on, wave off
dq=qs-qa;
%dt=-.5:-1:-4;
dt=.5:1:4;

[b,m]=size(dt);

u=.5:.5:15;
[b,n]=size(u);
%x(12)=z;x(13)=z;x(14)=z;

for j=1:m,
dth=dt(j)+6.1e-4*(Ts-dt(j)+273.16)*dq;

for i=1:n,
    zi=x(10);
    x(1)=u(i);
    x(3)=Ts;
    x(4)=Ts-dt(j);
    x(5)=qs;
    x(6)=qs-dq;

    eu=sqrt(ue(1)^2+(ue(2)*u(i))^2);
    et=sqrt(te(1)^2+(te(2)*dt(j))^2);
    eq=sqrt(qe(1)^2+(qe(2)*dq)^2);

    y=cor30a(x);

  %y=[hsb hlb tau zo zot zoq L usr tsr qsr dter dqer tkt RF wbar Cd Ch Ce Cdn_10 Chn_10 Cen_10 ug ];
   %   1   2   3   4  5   6  7  8   9  10   11   12  13  14  15  16 17 18    19      20    21  22

    hs(j,i)=y(1);
    hl(j,i)=y(2);
    tau(j,i)=y(3);
    hb(j,i)=hs(j,i)+6.1e-4*300*hl(j,i)/2.5;
    L=y(7);
    zetu=zu/L;
    zett=zt/L;
    zetq=zq/L;

    ztn(j,i)=10/L;
    cunh=k/log(zu/y(4));
    ctnh=k/log(zt/y(5));
    cqnh=k/log(zq/y(6));

    dcu=(psiu_30(zetu)-psiu_30(.99*zetu))/.01;
    dct=(psit_30(zett)-psit_30(.99*zett))/.01;
    dcq=(psit_30(zetq)-psit_30(.99*zetq))/.01;;

    usr=y(8);
    ust(j,i)=usr;
    tsr=y(9);
    qsr=y(10);
    ws=(al*usr*abs(tsr+6.1e-4*300*qsr)*zi)^.333;

    su=(1-cunh/k*psiu_30(zetu));
    st=(1-ctnh/k*psit_30(zett));
    sq=(1-cqnh/k*psit_30(zetq));
    uw=sqrt(u(i)*u(i)+(bet*ws)^2);
    a=((bet*ws)/uw)^2;

    fs=cunh/k*dcu/su+a/3;
    fu=cunh/k*dcu/su;
    ft=ctnh/k*dct/st;
    fq=cqnh/k*dcq/sq;

    %***** xfer coeff error contribution
    xs1=ecu/su/ctnh;
    xt1=ect/st/ctnh;
    xq1=ecq/sq/cqnh;
    xu1=ecu/su/cunh;
    %***** sensor error contribution

    xs2=eu/u(i)*(u(i)/uw)^2;
    xt2=et/dt(j);
    xq2=eq/dq;
    xu2=eu/u(i);
    
    %************** analytical error functions
    d(j,i)=(1-ft)*(1-a)+2*fs;%universal denominator
    dus(j,i)=sqrt((1-ft)^2*(xs1^2+xs2^2)+fs^2*(xt1^2+xt2^2))/d(j,i);
    dzet(j,i)=sqrt((1-a)^2*(xt1^2+xt2^2)+4*(xs1^2+xs2^2))/d(j,i);
    dwth(j,i)=sqrt(((1-a)+3*fs)^2*(xt1^2+xt2^2)+(xs1^2+xs2^2)*(1-3*ft)^2)/d(j,i);
    dwq(j,i)=sqrt(xq1^2+xq2^2+((fq*(1-a)+fs)^2*(xt1^2+xt2^2)+((1-ft)-2*fq)^2*(xs1^2+xs2^2))/d(j,i).^2);
    dwu(j,i)= sqrt((xu1*((1-ft)*(2-a)+.67*a))^2+(xu2*((1-ft)*(1+(u(i)/uw)^2-a)+2*fu*(1-(u(i)/uw)^2)+.67*a))^2+(xt1^2+xt2^2)*(fu*(2-a)+.33*a)^2)/d(j,i);

    end;%for i
end;%for j

figure;
plot(u,hl);
xlabel('Wind speed (m/s)');
ylabel('H_l (W/m^2) ');
text(5,275,'{\Delta\Theta}_v=.5, 1.5, 2.5, 3.5');
title('Latent heat flux');

figure;
plot(u,dwq.*hl);
xlabel('Wind speed (m/s)');
ylabel('{\delta}H_l ');
text(10,5.8,'{\Delta\Theta}=.5, 1.5, 2.5, 3.5');
title('Error in Latent heat flux');

figure;
plot(u,tau);
axis([0 u(n) 0 .4]);
xlabel('Wind speed (m/s)');
ylabel('Tau (N/m^2)');
text(10,.38,'{\Delta\Theta}=.5, 1.5, 2.5, 3.5');
title('Stress');

figure;
plot(u,dwu.*tau);
axis([0 u(n) 0 .1]);
xlabel('Wind speed (m/s)');
ylabel('{\delta}Tau');
text(10,.05,'{\Delta\Theta}=.5, 1.5, 2.5, 3.5');
title('Error in Stress');

figure;
plot(u,hs);
axis([0 u(n) 0 70]);
xlabel('Wind speed (m/s)');
ylabel('H_{s} (W/m^2) ');
text(5,65,'{\Delta\Theta}=.5, 1.5, 2.5, 3.5');
title('Sensible heat flux');

figure;
plot(u,dwth.*hs);
%axis([0 u(n) 0 1]);
xlabel('Wind speed (m/s)');
ylabel('{\delta}H_{sb} ');
text(10,.90,'{\Delta\Theta}=.5, 1.5, 2.5, 3.5');
title('Error in sensible heat flux');
