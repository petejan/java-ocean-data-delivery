format long

%Constants from sheet

Soc = 0.4873;
Voffset = -0.4886;
Tau20 = 5.56;

A = -0.0024093;
B = 0.0001208;
C = -0.0000024562;
E = 0.036;

D1 = 0.000192634;
D2 = -.0464803;

H1 = -0.033;
H2 = 5000;
H3 = 1450;

%Input from Seabird

V = csvread('Pulse-7-SBE16-6331-decimal-C.csv',[1 4 5917 4]);

T = csvread('Pulse-7-SBE16-6331-decimal-C.csv',[1 1 5917 1]);

K = T + 273.15;

Depth = csvread('Pulse-7-SBE16-6331-decimal-C.csv',[1 3 5917 3]);

P = Depth;

Cond = csvread('Pulse-7-SBE16-6331-decimal-C.csv',[1 2 5917 2]);

%Practical and absolute salinities

PS = gsw_SP_from_C(10*Cond,T,P); 

AS = gsw_SA_from_SP(PS,P,140.673,-46.312);

%Gordon and Garcia 1992 

% convert T to scaled temperature
temp_S = log((298.15 - T)./(273.15 + T));

% constants from Table 1 of Garcia &amp; Gordon for the fit to Benson and Krause (1984)
A0_o2 = 5.80871; 
A1_o2 = 3.20291;
A2_o2 = 4.17887;
A3_o2 = 5.10006;
A4_o2 = -9.86643e-2;
A5_o2 = 3.80369;
B0_o2 = -7.01577e-3;
B1_o2 = -7.70028e-3;
B2_o2 = -1.13864e-2;
B3_o2 = -9.51519e-3;
C0_o2 = -2.75915e-7;

% Corrected Eqn (8) of Garcia and Gordon 1992
OxSol_um_kg = exp(A0_o2 + A1_o2*temp_S + A2_o2*temp_S.^2 + A3_o2*temp_S.^3 + A4_o2*temp_S.^4 + A5_o2*temp_S.^5 + PS.*(B0_o2 + B1_o2*temp_S + B2_o2*temp_S.^2 + B3_o2*temp_S.^3) + C0_o2*PS.^2);

OxSol_ml_L = OxSol_um_kg.*(gsw_rho(AS,T,P)+1000)./44660;

%Formula

Oxygen_ml_L = Soc*(V+Voffset).*(1+A*T+B*T.^2+C*T.^3).*OxSol_ml_L.*exp(E.*P./K);

Oxygen_um_kg = Oxygen_ml_L.*(44660./(gsw_rho(AS,T,P)+1000));

plot(Oxygen_um_kg)
