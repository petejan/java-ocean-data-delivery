function aqdpCurrMaskCdf2nc(cdfFileRoot, badBursts, badCells);
% aqdpCurrMaskCdf2nc.m  A function to write Aquadopp velocity data to a Best
%                       Basic Version (BBV) netCDF (.nc) file.  
%
%    usage:  aqdpCurrMaskCdf2nc(cdfFileRoot);
%
%        where:  cdfFileRoot - the name of the .cdf file which contains
%                               masked Aquadopp velocity data, surrounded
%                               by single quotes with no file extension 
%                badBursts - a list of bad bursts, which are collected
%                             during instrument deployment and recovery
%                badCells - a list of bad cells, which are located above
%                             MSL and contaminated by sidelobe reflection
%                             errors
%
% Copyright 2004 
% USGS Woods Hole Field Center
% Written by Charlene Sullivan
% csullivan@usgs.gov
% 
% Dependencies:
%   gmin.m
%   gmax.m
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

% C. Sullivan   06/17/05,   version 1.0
% This mfile writes Aquadopp velocity data to a (BBV) netCDF file for
% distribution. The data in this BBV netCDF file has gone through QA/QC
% measures including removal of bad data collected during deployment and
% recovery of the instrument, and removal of data in cells contaminated by
% sidelobe reflection errors.


version = '1.0';

% There may be more than one .cdf file containing masked data if the
% user has done additional masking.  Assuming the user used appended
% incremental #'s to the file name (*-mask2.cdf,...,*-maskN.cdf) with
% each masking session, use the file w/ the highest number.  Best though to
% ask if this is the correct file.
if isunix
    cdfFiles = ls([cdfFileRoot,'-mask*.cdf']);
    if length(cdfFiles)>1
        cdfFile = cdfFiles(end);
    else
        cdfFile = cdfFiles(1);
    end
else
    cdfFiles = dir([cdfFileRoot,'-mask*.cdf']);
    if length(cdfFiles)>1
        cdfFile = cdfFiles(end).name;
    else
        cdfFile = cdfFiles(1).name;
    end
end
disp(' ')
disp(['The file ',cdfFile,' will be converted to an EPIC-compliant netCDF file'])
answer = input('Is this the correct .cdf file?:  ','s');
if ~strcmp(answer,'Y') & ~strcmp(answer,'y') & ...
   ~strcmp(answer,'N') & ~strcmp(answer,'n')
        disp(['Valid answers are either yes ("y") or no ("n")'])
        answer = input('Is this the correct .cdf file?:  ','s');
end
if strcmp(answer,'N') || strcmp(answer,'n')
    disp(' ')
    disp('The .cdf files in this directory are:')
    disp(' ')
    if isunix
        disp(cdfFiles)
    else
        for i=1:length(cdfFiles)
            disp(['      ',cdfFiles(i).name])
        end
    end
    disp(' ')
    cdfFile = input('Enter the name of the correct .cdf file to convert:  ','s');
end

% Open the .cdf netCDF file
cdf = netcdf(cdfFile);

% Extract the data from the .cdf file and trim it to remove data collected
% during deployment and recovery of the instrument, and data in cells
% contaminated by sidelobes.
theVars = var(cdf);
for v = 1:length(theVars)
    varName = name(theVars{v});
    if ~strcmp(varName,'lat') && ~strcmp(varName,'lon')
            data = theVars{v}(:);
            [row, col] = size(data);
            if col > 1 %2D array
                if ~isempty(badBursts)
                    data(badBursts,:) = [];
                end
                if ~isempty(badCells)
                    data(:,badCells) = [];
                end
            else %1D array
                if ~isempty(badBursts) && ~strcmp(varName,'height')
                   data(badBursts) = [];
                elseif ~isempty(badCells) && strcmp(varName,'height')
                   data(badCells) = [];
                end
            end
            eval([varName,' = data;'])
    end
end

%depth calculation using pressure sensor. this depth is relative to MSL.
%the first value in the depth array is closest to the transducer head.
mean_P = cdf.WATER_DEPTH(:);
depth_head = mean_P;
center_first_bin=height(1);
bin_size=diff(height);
depth_head_corrected = depth_head + cdf.sensor_height(:);
depth = depth_head_corrected - height;
dnote = 'Depth values were calculated using the Aquadopp Pressure Sensor';
wdepth = depth_head_corrected;

% Get number of records (bursts) and cells
nRec = length(time);
nCells = length(height); 

% Open the .nc netCDF file 
nc = netcdf([cdfFileRoot,'-cal.nc'],'clobber');

% Copy global attributes from the .cdf to the .nc
theCdfAtts = att(cdf);
for a = 1:length(theCdfAtts)
    copy(theCdfAtts{a}, nc);
end
nc.DATA_TYPE = ncchar('Nortek Aquadopp calibrated data file');

%define dimensions
nc('time') = 0;                               
nc('depth')= nCells; 
nc('lat') = 1;
nc('lon') = 1;

%define variables
nc{'time'} = nclong('time');
nc{'time'}.FORTRAN_format = ncchar('F10.2');
nc{'time'}.units = ncchar('True Julian Day');
nc{'time'}.type = ncchar('UNEVEN');
nc{'time'}.epic_code = nclong(624);
nc{'time'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'time2'} = nclong('time') ;
nc{'time2'}.FORTRAN_format = ncchar('F10.2');
nc{'time2'}.units = ncchar('msec since 0:00 GMT');
nc{'time2'}.type = ncchar('UNEVEN');
nc{'time2'}.epic_code = nclong(624);
nc{'time2'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'depth'} = ncfloat('depth'); 
nc{'depth'}.FORTRAN_format = ncchar('F10.2');
nc{'depth'}.units = ncchar('m');
nc{'depth'}.type = ncchar('EVEN');
nc{'depth'}.epic_code = nclong(3);
nc{'depth'}.long_name = ncchar('DEPTH (m)');
nc{'depth'}.blanking_distance = ncchar(nc.AQDP_Blanking_distance);
nc{'depth'}.bin_size = ncchar(nc.AQDP_Cell_size);
nc{'depth'}.xducer_offset_from_bottom = ncfloat(nc.sensor_height);
nc{'depth'}.FillValue_ = ncfloat(1.00000004091848e+035);
nc{'depth'}.NOTE = ncchar(dnote);

nc{'lon'} = ncfloat('lon'); %% 1 element.
nc{'lon'}.FORTRAN_format = ncchar('f10.4');
nc{'lon'}.units = ncchar('dd');
nc{'lon'}.type = ncchar('EVEN');
nc{'lon'}.epic_code = nclong(502);
nc{'lon'}.name = ncchar('LON');
nc{'lon'}.long_name = ncchar('LONGITUDE');
nc{'lon'}.generic_name = ncchar('lon');
nc{'lon'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'lat'} = ncfloat('lat'); %% 1 element.
nc{'lat'}.FORTRAN_format = ncchar('F10.2');
nc{'lat'}.units = ncchar('dd');
nc{'lat'}.type = ncchar('EVEN');
nc{'lat'}.epic_code = nclong(500);
nc{'lat'}.name = ncchar('LAT');
nc{'lat'}.long_name = ncchar('LATITUDE');
nc{'lat'}.generic_name = ncchar('lat');
nc{'lat'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'u_1205'} = ncfloat('time', 'depth', 'lat', 'lon');
nc{'u_1205'}.name = ncchar('u');
nc{'u_1205'}.long_name = ncchar('Eastward Velocity');
nc{'u_1205'}.generic_name = ncchar('u');
nc{'u_1205'}.FORTRAN_format = ncchar(' ');
nc{'u_1205'}.units = ncchar('cm/s');
nc{'u_1205'}.epic_code = nclong(1205);
nc{'u_1205'}.sensor_type = ncchar(nc.INST_TYPE(:));
nc{'u_1205'}.sensor_depth = ncfloat(nc.WATER_DEPTH(:) - nc.sensor_height(:));
nc{'u_1205'}.serial_number = ncchar(nc.AQDP_Serial_number);
nc{'u_1205'}.minimum = ncfloat(gmin(VelEast(:)*100));
nc{'u_1205'}.maximum = ncfloat(gmax(VelEast(:)*100));
nc{'u_1205'}.valid_range = ncfloat([1000 1000]);
nc{'u_1205'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'v_1206'} = ncfloat('time', 'depth', 'lat', 'lon'); 
nc{'v_1206'}.name = ncchar('v');
nc{'v_1206'}.long_name = ncchar('Northward Velocity');
nc{'v_1206'}.generic_name = ncchar('v');
nc{'v_1206'}.FORTRAN_format = ncchar(' ');
nc{'v_1206'}.units = ncchar('cm/s');
nc{'v_1206'}.epic_code = nclong(1206);
nc{'v_1206'}.sensor_type = ncchar(nc.INST_TYPE(:));
nc{'v_1206'}.sensor_depth = ncfloat(nc.WATER_DEPTH(:) - nc.sensor_height(:));
nc{'v_1206'}.serial_number = ncchar(nc.AQDP_Serial_number);
nc{'v_1206'}.minimum = ncfloat(gmin(VelNorth(:)*100));
nc{'v_1206'}.maximum = ncfloat(gmax(VelNorth(:)*100));
nc{'v_1206'}.valid_range = ncfloat([1000 1000]);
nc{'v_1206'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'w_1204'} = ncfloat('time', 'depth', 'lat', 'lon'); 
nc{'w_1204'}.name = ncchar('w');
nc{'w_1204'}.long_name = ncchar('Vertical Velocity');
nc{'w_1204'}.generic_name = ncchar('w');
nc{'w_1204'}.FORTRAN_format = ncchar(' ');
nc{'w_1204'}.units = ncchar('cm/s');
nc{'w_1204'}.epic_code = nclong(1204);
nc{'w_1204'}.sensor_type = ncchar(nc.INST_TYPE(:));
nc{'w_1204'}.sensor_depth = ncfloat(nc.WATER_DEPTH(:) - nc.sensor_height(:));
nc{'w_1204'}.serial_number = ncchar(nc.AQDP_Serial_number);
nc{'w_1204'}.minimum = ncfloat(gmin(VelUp(:)*100));
nc{'w_1204'}.maximum = ncfloat(gmax(VelUp(:)*100));
nc{'w_1204'}.valid_range = ncfloat([1000 1000]);
nc{'w_1204'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'P_4'} = ncfloat('time', 'lat', 'lon'); 
nc{'P_4'}.name = ncchar('P');
nc{'P_4'}.long_name = ncchar('PRESSURE (PASCALS)');
nc{'P_4'}.generic_name = ncchar('depth');
nc{'P_4'}.units = ncchar('Pa');
nc{'P_4'}.epic_code = nclong(4);
nc{'P_4'}.sensor_type = ncchar(nc.INST_TYPE(:));
nc{'P_4'}.sensor_depth = ncfloat(nc.WATER_DEPTH(:) - nc.sensor_height(:));
nc{'P_4'}.serial_number = ncchar(nc.AQDP_Serial_number);
nc{'P_4'}.minimum = ncfloat(gmin(pressure * 9806.65));
nc{'P_4'}.maximum = ncfloat(gmax(pressure * 9806.65));
nc{'P_4'}.valid_range = [0 4294967295]; %ADCP TOOLBOX
nc{'P_4'}.FillValue_ = ncfloat(1.00000004091848e+035);
nc{'P_4'}.NOTE = ncchar('Pressure of the water at the transducer head relative to one atmosphere (sea level)');

nc{'Tx_1211'} = ncfloat('time', 'lat', 'lon');
nc{'Tx_1211'}.name = ncchar('Tx');
nc{'Tx_1211'}.long_name = ncchar('ADCP Transducer Temp.');
nc{'Tx_1211'}.generic_name = ncchar('temp');
nc{'Tx_1211'}.units = ncchar('degrees.C');
nc{'Tx_1211'}.epic_code = nclong(1211);
nc{'Tx_1211'}.sensor_type = ncchar(nc.INST_TYPE(:));
nc{'Tx_1211'}.sensor_depth = ncfloat(nc.WATER_DEPTH(:) - nc.sensor_height(:));
nc{'Tx_1211'}.serial_number = ncchar(nc.AQDP_Serial_number);
nc{'Tx_1211'}.minimum = ncfloat(gmin(temperature(:)));
nc{'Tx_1211'}.maximum = ncfloat(gmax(temperature(:)));
nc{'Tx_1211'}.valid_range = [-5 45];
nc{'Tx_1211'}.FillValue_ = ncfloat(1.00000004091848e+035);

endef(nc);

nc{'time'}(1:nRec) = time;
nc{'time2'}(1:nRec) = time2;
nc{'depth'}(1:nCells) = depth;
nc{'lat'}(1) = nc.latitude(:);
nc{'lon'}(1) = nc.longitude(:);
nc{'u_1205'}(1:nRec, 1:nCells) = VelEast * 100; %convert m/s to cm/s
nc{'v_1206'}(1:nRec, 1:nCells) = VelNorth * 100; %convert m/s to cm/s
nc{'w_1204'}(1:nRec, 1:nCells) = VelUp * 100; %convert m/s to cm/s
nc{'P_4'}(1:nRec) = pressure * 9806.65; %convert meters to pascals assuming 9806.65 Pa/m (ADCP TOOLBOX)
nc{'Tx_1211'}(1:nRec) = temperature;

% Update some attributes
try
    temp_note = cdf{'temperature'}.NOTE(:);
    nc{'Tx_1211'}.NOTE = ncchar(temp_note);
catch
end

hist = nc.history(:);
hist_new = ['Data trimmed and converted to EPIC-compliant variables ',...
            'by aqdpCurrMaskCdf2nc.m V ',version,'; ',hist];
nc.history = ncchar(hist_new);
nc.CREATION_DATE = ncchar(datestr(now,0));
nc.VAR_DESC = ncchar('u:v:w:P:Tx');
nc.start_time = datestr(gregorian(time(1)+time2(1)/3600/1000/24),0);
nc.stop_time = datestr(gregorian(time(end)+time2(end)/3600/1000/24),0);

close(cdf)
close(nc)