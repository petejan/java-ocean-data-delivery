format long;

%Raw data ----> B phase ----> D phase

optode_V = csvread('Pulse-7-SBE16-6331-decimal-C.csv',1,8,[1 8 5917 8]);

optode_T = csvread('Pulse-7-SBE16-6331-decimal-C.csv',1,1,[1 1 5917 1]);

Depth = csvread('Pulse-7-SBE16-6331-decimal-C.csv',1,3,[1 3 5917 3]);

B_phase = 12*optode_V + 10;

phase_coeff_0 = 0.894295;

phase_coeff_1 = 1.10633;

D_phase = phase_coeff_0 + B_phase*phase_coeff_1;

%Coefficients of C from Aanderaa calibration sheet

C0_coeff = [5.27602e03 -1.78336e02 3.60337 -3.17257e-02]';

C1_coeff = [-2.83515e02 8.53926 -1.70712e-01 1.51927e-03]';

C2_coeff = [6.14613 -1.62949e-01 3.25579e-03 -2.94146e-05]';

C3_coeff = [-6.20004e-02 1.43629e-03 -2.90879e-05 2.67188e-07]';

C4_coeff = [2.39283e-04 -4.79250e-06 1.00060e-07 -9.33184e-10]';


%Calculating C0...4

C0 = C0_coeff(1) + C0_coeff(2)*optode_T + C0_coeff(3)*(optode_T.^2) + C0_coeff(4)*(optode_T.^3);

C1 = C1_coeff(1) + C1_coeff(2)*optode_T + C1_coeff(3)*(optode_T.^2) + C1_coeff(4)*(optode_T.^3);

C2 = C2_coeff(1) + C2_coeff(2)*optode_T + C2_coeff(3)*(optode_T.^2) + C2_coeff(4)*(optode_T.^3);

C3 = C3_coeff(1) + C3_coeff(2)*optode_T + C3_coeff(3)*(optode_T.^2) + C3_coeff(4)*(optode_T.^3);

C4 = C4_coeff(1) + C4_coeff(2)*optode_T + C4_coeff(3)*(optode_T.^2) + C4_coeff(4)*(optode_T.^3);

%Calculating uncorrected oxygen in um/kg

O2_optode_uncorr = C0 + C1.*D_phase + C2.*(D_phase.^2) + C3.*(D_phase.^3) + C4.*(D_phase.^4);

%Salinity correction using Gordon and Garcia 1992

B0 = -6.24097e-3;

B1 = -6.93498e-3;

B2 = -6.90358e-3;

B3 = -4.29155e-3;

C0 = -3.11680e-7;

scaled_optode_T = log((298.15 - optode_T)./(273.15 + optode_T));

Cond = csvread('Pulse-7-SBE16-6331-decimal-C.csv',1,2,[1 2 5917 2]);

PS = gsw_SP_from_C(10*Cond,optode_T,Depth); 

Sal_corr = exp(PS.*(B0 + B1*scaled_optode_T + B2*(scaled_optode_T.^2) + B3*(scaled_optode_T.^3)) + C0*(PS.^2));

%Depth correction

Depth_corr = 1 + (0.04/1000)*Depth;

%Final corrected oxygen

O2_optode = O2_optode_uncorr.*Sal_corr.*Depth_corr;

