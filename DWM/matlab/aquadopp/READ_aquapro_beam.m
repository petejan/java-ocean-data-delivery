function [aqdpData] = READ_aquapro_beam(userMeta, aqdpMeta, senFile);
% READ_aquapro_beam.m  A function to read Aquadopp velocities, convert
%                      them from BEAM to ENU coordinates (if necessary),
%                      and correct for magnetic variation.
%
% usage:    [aqdpData] = READ_aquapro_beam(userMeta, aqdpMeta);
%
%               aqdpData  - a structure w/ the following fields:
%                     nBursts - number of bursts
%                      nCells - number of velocity cells
%                    err_code - error code
%                    sta_code - status code
%                        batt - Battery, volts
%                   sound_spd - soundspeed, m/s
%                         hdg - Heading, deg
%                        ptch - Pitch, deg
%                        roll - Roll, deg
%                        pres - Pressure, dBar
%                          tC - temperature, degrees C
%                       xsen1 - analog input 1
%                       xsen2 - analog input 2
%                    velBeam1 - velocity along beam 1, m/s
%                    velBeam2 - velocity along beam 2, m/s
%                    velBeam3 - velocity along beam 3, m/s
%                    ampBeam1 - amplitude along beam 1, counts
%                    ampBeam2 - amplitude along beam 2, counts
%                    ampBeam3 - amplitude along beam3, counts
%                           u - eastward velocity, m/s
%                           v - northward velocity, m/s
%                           w - vertical velocity, m/s
%               userMeta - a structure with user-defined metadata
%               aqdpMeta - a structure with instrument metadata
%               senFile - the name of the ascii file with data output from
%                         AquaPro
%
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Use of this program is described in:
%
% Sullivan, C.M., Warner, J.C., Martini, M.A., Voulgaris, G., 
% Work, P.A., Haas, K.A., and Hanes, D.H. (2006) 
% South Carolina Coastal Erosion Study Data Report for Observations
% October 2003 - April 2004., USGS Open-File Report 2005-1429.
%
% Program written in Matlab v7.1.0 SP3
% Program ran on PC with Windows XP Professional OS.
%
% "Although this program has been used by the USGS, no warranty, 
% expressed or implied, is made by the USGS or the United States 
% Government as to the accuracy and functioning of the program 
% and related program material nor shall the fact of distribution 
% constitute any such warranty, and no responsibility is assumed 
% by the USGS in connection therewith."
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Copyright 2004 
% USGS Woods Hole Field Center
% Written by Charlene Sullivan
% csullivan@usgs.gov
%
% Dependencies:
%   julian.m
%   gmean.m
%   beam2enu.m

% C. Sullivan   06/13/05,   version 1.1
% Turn GV's and BG's script into a function. Metadata is loaded by running
% get_meta_nortek.m
% C. Sullivan   01/14/05,   version 1.0
% Modify GV's and BG's script for South Carolina site 8

[pathstr, name, ext, versn] = fileparts(aqdpMeta.senFile);
dataset = name; 

% metadata
nums = ~isletter(aqdpMeta.Cell_size);
cellsize = str2num(aqdpMeta.Cell_size(nums))/100; % convert cm to m
nums = ~isletter(aqdpMeta.Blanking_distance);
blanking = str2num(aqdpMeta.Blanking_distance(nums)); % m 
serialnum = aqdpMeta.Serial_number;
mount_h = userMeta.sensor_height; % m
MagDev = userMeta.magnetic_variation * pi/180;

% load velocities in beam coordinates. these are dimensioned
% [nBursts x nCells]
aqdpData.velBeam1 = load([dataset,'.v1']);
aqdpData.velBeam2 = load([dataset,'.v2']);
aqdpData.velBeam3 = load([dataset '.v3']);
[nRows, nCol] = size(aqdpData.velBeam1);
aqdpData.nBursts = nRows;
aqdpData.burst = [1:nRows]';
aqdpData.nCells = nCol;

% load amplitudes. these are also dimensioned
% [nBursts x nCells]
aqdpData.ampBeam1 = load([dataset '.a1']);
aqdpData.ampBeam2 = load([dataset '.a2']);
aqdpData.ampBeam3 = load([dataset '.a3']);

% load time and sensor information. time is the
% time at the beginning of the burst. there are 17
% columns in the .sen file. the last 2 columns may
% contain analog input
senData = load([dataset '.sen']);
yyyy = senData(:,3);
mm = senData(:,1);
dd = senData(:,2);
hr = senData(:,4);
min = senData(:,5);
sec = senData(:,6);
aqdpData.err_code = senData(:,7);    %error code
aqdpData.sta_code = senData(:,8);    %status code
aqdpData.batt = senData(:,9);        %Battery (volts)
aqdpData.sound_spd = senData(:,10);  %soundspeed (m/s)
aqdpData.hdg = senData(:,11);        %Heading (deg)
aqdpData.ptch = senData(:,12);       %Pitch (deg)
aqdpData.roll = senData(:,13);       %Roll (deg)
aqdpData.pres = senData(:,14);       %Pressure (dBar)
aqdpData.tC = senData(:,15);         %temperature (degrees C)
aqdpData.xsen1 = senData(:,16);      %analog input 1
aqdpData.xsen2 = senData(:,17);      %analog input 2

% convert time to julian days
aqdpData.time = julian([yyyy mm dd hr min sec]); 
aqdpData.delta_t = gmean(diff(aqdpData.time)); %time step, julian days

% shift time from the time at the beginning of the burst to the time at
% the center of the burst
delta_t = aqdpData.delta_t; %profile interval, seconds
delta_t = delta_t / (3600*24); % profile interval, days
aqdpData.time = aqdpData.time + 0.5*delta_t;

% calculate the height of the center of each cell
% above the bed
aqdpData.cell_height = mount_h + blanking + cellsize/2 + ...
                      ([0:aqdpData.nCells-1])*cellsize;

% convert velocities from BEAM to ENU (if necessary)
vv1 = nan(aqdpData.nBursts, aqdpData.nCells);
vv2 = nan(aqdpData.nBursts, aqdpData.nCells);
vv3 = nan(aqdpData.nBursts, aqdpData.nCells);
if strcmp(aqdpMeta.Coordinate_system,'BEAM')
    for ii = 1:aqdpData.nBursts,
        for j = 1:aqdpData.nCells,
            vbeam = [aqdpData.velBeam1(ii,j) aqdpData.velBeam2(ii,j) aqdpData.velBeam3(ii,j)];
            venu = beam2enu(vbeam,aqdpData.hdg(ii),aqdpData.ptch(ii),aqdpData.roll(ii),0);
            vv1(ii,j) = venu(1);
            vv2(ii,j) = venu(2);
            vv3(ii,j) = venu(3);
        end
    end
elseif strcmp(aqdpMeta.Coordinate_system,'ENU')
    vv1 = aqdpData.velBeam1;
    vv2 = aqdpData.velBeam2;
    vv3 = aqdpData.velBeam3;
else
    error('Data must be in either BEAM or ENU coordinates')
end

%correct for magnetic variation
East =  vv1*cos(MagDev) + vv2*sin(MagDev);
North = -vv1*sin(MagDev) + vv2*cos(MagDev);

aqdpData.u = East;  %Eastward velocity (m/s)
aqdpData.v = North; %Northward velocity (m/s)
aqdpData.w = vv3;   %Upward velocity (m/s)

%%AAA=(a1(1:ncells,:)+a2(1:ncells,:)+a3(1:ncells,:))/3;