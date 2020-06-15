% Transform.m is a Matlab script that shows how velocity data can be 
% transformed between beam coordinates and ENU coordinates. Beam 
% coordinates are defined as the velocity measured along the three 
% beams of the instrument.
% ENU coordinates are defined in an earth coordinate system, where
% E represents the East-West component, N represents the North-South
% component and U represents the Up-Down component.

% Note that the transformation matrix must be recalculated every time
% the orientation, heading, pitch or roll changes.

% Transformation matrix for beam to xyz coordinates,
% the values can be found from the header file that is generated in the
% conversion of data to ASCII format
% This example shows the transformation matrix for a standard Aquadopp head
T =[ 2896  2896    0 ;...
    -2896  2896    0 ;...
    -2896 -2896 5792  ];

T = T/4096;   % Scale the transformation matrix correctly to floating point numbers

% Aquadopp Serial number                         AQD11011
% Head configuration
% ---------------------------------------------------------------------
% Pressure sensor                       YES
% Compass                               YES
% Tilt sensor                           YES
% System 1                              1
% Head frequency                        1000 kHz
% Serial number                         AQP 5830
% Transformation matrix                 1.5774 -0.7891 -0.7891
%                                       0.0000 -1.3662 1.3662
%                                       0.3677 0.3677 0.3677
% Pressure sensor calibration           0 0 18853 9057
% Number of beams                       3
% System5                               25 25 25 0
% System7                               -16874 663 11173 -267
%                                       -17241 -1131 11287 -2
% System8                               0 0 -1
%                                       0 1 0
%                                       1 0 0
% System9                               0 0 -1
%                                       0 -1 0
%                                       -1 0 0
% System10                              0 -1 1 0
% System11                              0 -1 -1 0
% System13                              1481 259 7128 10841
% System14                              -12431 111 9910 -11
%                                       -12130 -631 9863 -70
% System15                              30531 557 414 557
%                                       32767 306 -487 -676
%                                       32113 -64 -2 -8
%                                       0 0 0 0
% System16                              0 0 0 0
% System17                              5461
% System18                              3600
% System19                              3600
% System20                              10000


T_org = T;

% If instrument is pointing down (bit 0 in status equal to 1)
% rows 2 and 3 must change sign
% NOTE: For the Vector the instrument is defined to be in 
%       the UP orientation when the communication cable 
%       end of the canister is on the top of the canister, ie when 
%       the probe is pointing down.
%       For the other instruments that are used vertically, the UP 
%       orientation is defined to be when the head is on the upper
%       side of the canister.


if statusbit0 == 1,
   T(2,:) = -T(2,:);   
   T(3,:) = -T(3,:);   
end

% heading, pitch and roll are the angles output in the data in degrees

hh = pi*(heading-90)/180;
pp = pi*pitch/180;
rr = pi*roll/180;

% Make heading matrix
H = [cos(hh) sin(hh) 0; -sin(hh) cos(hh) 0; 0 0 1]

% Make tilt matrix
P = [cos(pp) -sin(pp)*sin(rr) -cos(rr)*sin(pp);...
      0             cos(rr)          -sin(rr);  ...
      sin(pp) sin(rr)*cos(pp)  cos(pp)*cos(rr)]

% Make resulting transformation matrix
R = H*P*T;


% beam is beam coordinates, for example beam = [0.23 ; -0.52 ; 0.12]
% enu is ENU coordinates

% Given beam velocities, ENU coordinates are calculated as
enu = R*beam

% Given ENU velocities, beam coordinates are calculated as
beam = inv(R)*enu


% Transformation between beam and xyz coordinates are done using
% the original T matrix 
xyz = T_org*beam
beam = inv(T_org)*xyz

% Given ENU velocities, xyz coordinates are calculated as
xyz = T_org*inv(R)*enu