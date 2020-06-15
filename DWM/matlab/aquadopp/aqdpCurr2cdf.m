function aqdpCurr2cdf(metaFile,cdfFileRoot);
% aqdpCurr2cdf.m  A function to write Aquadopp velocity data, downloaded 
%                 from the instrument, to a netCDF (.cdf) file.  
%
%    usage:  aqdpCurr2cdf(metaFile,cdfFileRoot);
%
%        where:  metaFile    - an ascii file in which metadata is defined,
%                               in single quotes including the file extension
%                cdfFileRoot - the name given to the output .cdf file, no file
%                               extension necessary
%
% Copyright 2004 
% USGS Woods Hole Field Center
% Written by Charlene Sullivan
% csullivan@usgs.gov
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
%
% Dependencies:
%   get_meta_nortek.m
%   READ_aquapro_beam.m
%   gregorian.m
%   gmin.m
%   gmax.m

% C. Sullivan   06/14/05,   version 1.3
% Start using version #'s to keep track of changes to mfiles. Run
% READ_Aquapro_beam.m (which is now a function) directly from this mfile.
% Write all AquaPro velocity output (files .v1, .v2, .v3, .a1, .a2, .a3,
% and .sen) to a .cdf file. The data in this file is the data as downloaded
% from the instrument, ergo the units of the data will be the units the
% data was collected in. Instrument setup information is ncluded as
% metadata. (Note that all QA/QC will take place AFTER this file is created.
% The QA'd/QC'd data will be converted into EPIC-compliant variables and
% written to a BBV .nc file.)
% C. Sullivan   01/27/05,   version 1.2
% Pressure is output from the Aquadopp in dBars. Deep-water Aquadopp
% outputs pressure in both meters and dbars and assumes a scaling of 1025
% kg/m^3 seawater).  Per communication Nortek messageboards.
% C. Sullivan   01/21/05,   version 1.1
% Add depth calculation. Write temperature of transducer head to TX_1211
% C. Sullivan   01/19/05,   version 1.0
% This function assumes the user ran both READ_aquapro_beam.m (from GV and
% BG at USC) and aqdp_clean.m on an Aquadopp data set.  It assumes the user
% is in the directory with this. data, has created a text file with
% important metadata in the directory, and has the netcdf toolbox installed
% and included on their Matlab path.

more off

version = 1.3;

% Check inputs
if ~ischar(metaFile) || ~ischar(cdfFileRoot)
    error('File names should be surrounded in single quotes');
end

% Check existence of metadata file and Aquapro v. 1.25 output
if isunix
    l = ls(metaFile);
    if isempty(l)
        error(['The metafile ',metaFile,' does not exist in this directory']);
    end

    senFile = ls('*.sen');
    hdrFile = ls('*.hdr');
    if isempty(senFile) || isempty(hdrFile) 
        error(['AquaPro output files do not exist in this directory'])
    end
else
    l = dir(metaFile);
    if isempty(l)
        error(['The metafile ',metaFile,' does not exist in this directory']);
    end

    sen = dir('*.sen');
    hdr = dir('*.hdr');
    if isempty(sen) || isempty(hdr) 
        error(['AquaPro output files do not exist in this directory'])
    elseif length(sen) > 1 || length(hdr) > 1
        error(['Too many .wad or .hdr files exist in this directory'])
    else
        senFile = sen.name;
        hdrFile = hdr.name;
    end
end

% Gather user-defined and instrument metadata. Add AquaPro output file
% names to metadata.
[userMeta, aqdpMeta] = get_meta_nortek(metaFile, hdrFile);
aqdpMeta.metaFile = metaFile;
aqdpMeta.hdrFile = hdrFile;
aqdpMeta.senFile = senFile;

% Load the velocities with READ_aquapro_beam.m.  This mfile is a function
% that loads the data from the AquaPro files *.v1, *.v2, *.v2, converts
% them from BEAM to ENU coordinates (if necessary), and corrects for
% magnetic variation.
[aqdpData] = READ_aquapro_beam(userMeta, aqdpMeta);

% Create and define the .cdf file
nc=netcdf([cdfFileRoot,'.cdf'],'clobber');                        

%write user metadata
theAtts = fieldnames(userMeta);
for a=1:length(theAtts)
    eval(['theDef = userMeta.',theAtts{a},';'])
    if ischar(theDef)
        eval(['nc.',theAtts{a},' = ncchar(''',theDef,''');']);
    else
        eval(['nc.',theAtts{a},' = ncfloat([theDef]);']);
    end
end

%write instrument setup information
theAtts = fieldnames(aqdpMeta);
for a=1:length(theAtts)
    eval(['theDef = aqdpMeta.',theAtts{a},';'])
    if ischar(theDef)
        eval(['nc.AQDP_',theAtts{a},' = ncchar(''',theDef,''');']);
    else
        eval(['nc.AQDP_',theAtts{a},' = ncfloat([theDef]);']);
    end
end


%define dimensions
nc('burst') = 0;                               
nc('cell')= aqdpData.nCells; 
nc('lat') = 1;
nc('lon') = 1;

%define variables
nc{'time'} = nclong('burst');
nc{'time'}.FORTRAN_format = ncchar('F10.2');
nc{'time'}.units = ncchar('True Julian Day');
nc{'time'}.type = ncchar('UNEVEN');
nc{'time'}.epic_code = nclong(624);
nc{'time'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'time2'} = nclong('burst') ;
nc{'time2'}.FORTRAN_format = ncchar('F10.2');
nc{'time2'}.units = ncchar('msec since 0:00 GMT');
nc{'time2'}.type = ncchar('UNEVEN');
nc{'time2'}.epic_code = nclong(624);
nc{'time2'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'burst'} = nclong('burst');
nc{'burst'}.FORTRAN_format = ncchar('F10.2');
nc{'burst'}.units = ncchar('counts');
nc{'burst'}.type = ncchar('EVEN');
nc{'burst'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'height'} = ncfloat('cell'); 
nc{'height'}.FORTRAN_format = ncchar('F10.2');
nc{'height'}.units = ncchar('m');
nc{'height'}.type = ncchar('EVEN');
nc{'height'}.epic_code = nclong(3);
nc{'height'}.long_name = ncchar('DEPTH (m)');
nc{'height'}.blanking_distance = ncchar(aqdpMeta.Blanking_distance);
nc{'height'}.cell_size = ncchar(aqdpMeta.Cell_size);
nc{'height'}.xducer_offset_from_bottom = ncfloat(userMeta.sensor_height);
nc{'height'}.FillValue_ = ncfloat(1.00000004091848e+035);
nc{'height'}.NOTE = ncchar('Centered cell heights above the bed');

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

nc{'VelEast'} = ncfloat('burst', 'cell', 'lat', 'lon');
nc{'VelEast'}.name = ncchar('u');
nc{'VelEast'}.long_name = ncchar('eastward velocity (m/s)');
nc{'VelEast'}.generic_name = ncchar('u');
nc{'VelEast'}.FORTRAN_format = ncchar(' ');
nc{'VelEast'}.units = ncchar('m s-1');
nc{'VelEast'}.sensor_type = nc.INST_TYPE(:);
nc{'VelEast'}.height = ncfloat(userMeta.sensor_height);
nc{'VelEast'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'VelEast'}.valid_range = ncfloat([10 10]);
nc{'VelEast'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'VelNorth'} = ncfloat('burst', 'cell', 'lat', 'lon'); 
nc{'VelNorth'}.name = ncchar('v');
nc{'VelNorth'}.long_name = ncchar('northward velocity (m/s)');
nc{'VelNorth'}.generic_name = ncchar('v');
nc{'VelNorth'}.FORTRAN_format = ncchar(' ');
nc{'VelNorth'}.units = ncchar('m s-1');
nc{'VelNorth'}.sensor_type = nc.INST_TYPE(:);
nc{'VelNorth'}.height = ncfloat(userMeta.sensor_height);
nc{'VelNorth'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'VelNorth'}.valid_range = ncfloat([10 10]);
nc{'VelNorth'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'VelUp'} = ncfloat('burst', 'cell', 'lat', 'lon'); 
nc{'VelUp'}.name = ncchar('w');
nc{'VelUp'}.long_name = ncchar('vertical velocity (m/s)');
nc{'VelUp'}.generic_name = ncchar('w');
nc{'VelUp'}.FORTRAN_format = ncchar(' ');
nc{'VelUp'}.units = ncchar('m s-1');
nc{'VelUp'}.sensor_type = nc.INST_TYPE(:);
nc{'VelUp'}.height = ncfloat(userMeta.sensor_height);
nc{'VelUp'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'VelUp'}.valid_range = ncfloat([10 10]);
nc{'VelUp'}.FillValue_ = ncfloat(1.00000004091848e+035);

if strcmp(aqdpMeta.Coordinate_system,'BEAM')
    nc{'VelBeam1'} = ncfloat('burst', 'cell', 'lat', 'lon'); 
    nc{'VelBeam1'}.long_name = ncchar('velocity along beam 1 (m/s)');
    nc{'VelBeam1'}.units = ncchar('m s-1');
    nc{'VelBeam1'}.valid_range = ncfloat([10 10]);
    nc{'VelBeam1'}.sensor_type = nc.INST_TYPE(:);
    nc{'VelBeam1'}.height = ncfloat(userMeta.sensor_height);
    nc{'VelBeam1'}.serial = ncchar(aqdpMeta.Serial_number);
    nc{'VelBeam1'}.FillValue_ = ncfloat(1.00000004091848e+035);

    nc{'VelBeam2'} = ncfloat('burst', 'cell', 'lat', 'lon');
    nc{'VelBeam2'}.long_name = ncchar('velocity along beam 2 (m/s)');
    nc{'VelBeam2'}.units = ncchar('m s-1');
    nc{'VelBeam2'}.valid_range = ncfloat([10 10]);
    nc{'VelBeam2'}.sensor_type = nc.INST_TYPE(:);
    nc{'VelBeam2'}.height = ncfloat(userMeta.sensor_height);
    nc{'VelBeam2'}.serial = ncchar(aqdpMeta.Serial_number);
    nc{'VelBeam2'}.FillValue_ = ncfloat(1.00000004091848e+035);

    nc{'VelBeam3'} = ncfloat('burst', 'cell', 'lat', 'lon');
    nc{'VelBeam3'}.long_name = ncchar('velocity along beam 3 (m/s)');
    nc{'VelBeam3'}.units = ncchar('m s-1');
    nc{'VelBeam3'}.valid_range = ncfloat([10 10]);
    nc{'VelBeam3'}.sensor_type = nc.INST_TYPE(:);
    nc{'VelBeam3'}.height = ncfloat(userMeta.sensor_height);
    nc{'VelBeam3'}.serial = ncchar(aqdpMeta.Serial_number);
    nc{'VelBeam3'}.FillValue_ = ncfloat(1.00000004091848e+035);
end

nc{'ampBeam1'} = ncfloat('burst', 'cell', 'lat', 'lon');
nc{'ampBeam1'}.units = ncchar('counts');
nc{'ampBeam1'}.long_name = ncchar('beam 1 amplitude (counts)');
nc{'ampBeam1'}.valid_range = ncfloat([-32768 32767]);
nc{'ampBeam1'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'ampBeam2'} = ncfloat('burst', 'cell', 'lat', 'lon');
nc{'ampBeam2'}.units = ncchar('counts');
nc{'ampBeam2'}.long_name = ncchar('beam 2 amplitude (counts)');
nc{'ampBeam2'}.valid_range = ncfloat([-32768 32767]);
nc{'ampBeam2'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'ampBeam3'} = ncfloat('burst', 'cell', 'lat', 'lon');
nc{'ampBeam3'}.units = ncchar('counts');
nc{'ampBeam3'}.long_name = ncchar('beam 3 amplitude (counts)');
nc{'ampBeam3'}.valid_range = ncfloat([-32768 32767]);
nc{'ampBeam3'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'err_code'} = ncfloat('burst', 'lat', 'lon');
nc{'err_code'}.units = ncchar('');
nc{'err_code'}.long_name = ncchar('error code');
nc{'err_code'}.valid_range = ncfloat([0 111111]);
nc{'err_code'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'stat_code'} = ncfloat('burst', 'lat', 'lon');
nc{'stat_code'}.units = ncchar('');
nc{'stat_code'}.long_name = ncchar('status code');
nc{'stat_code'}.valid_range = ncfloat([0 111111]);
nc{'stat_code'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'batt'} = ncfloat('burst', 'lat', 'lon');
nc{'batt'}.units = ncchar('volts');
nc{'batt'}.long_name = ncchar('battery voltage (volts)');
nc{'batt'}.valid_range = ncfloat([-32768 32767]);
nc{'batt'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'sound_spd'} = ncfloat('burst', 'lat', 'lon');
nc{'sound_spd'}.units = ncchar('m s-1');
nc{'sound_spd'}.long_name = ncchar('soundspeed (m/s)');
nc{'sound_spd'}.valid_range = ncfloat([10 10]);
nc{'sound_spd'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'heading'} = ncfloat('burst', 'lat', 'lon');
nc{'heading'}.units = ncchar('degrees, magnetic');
nc{'heading'}.long_name = ncchar('INST Heading (degrees, magnetic)');
nc{'heading'}.epic_code = nclong(1215);
nc{'heading'}.valid_range = ncfloat([0 359.999908447266]);
nc{'heading'}.height = ncfloat(userMeta.sensor_height);
nc{'heading'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'heading'}.FillValue_ = ncfloat(1.00000004091848e+035);
 
nc{'pitch'} = ncfloat('burst', 'lat', 'lon');
nc{'pitch'}.units = ncchar('degrees');
nc{'pitch'}.long_name = ncchar('INST Pitch (degrees)');
nc{'pitch'}.epic_code = nclong(1216);
nc{'pitch'}.valid_range = ncfloat([0 359.999908447266]);
nc{'pitch'}.height = ncfloat(userMeta.sensor_height);
nc{'pitch'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'pitch'}.FillValue_ = ncfloat(1.00000004091848e+035);
 
nc{'roll'} = ncfloat('burst', 'lat', 'lon');
nc{'roll'}.units = ncchar('degrees');
nc{'roll'}.long_name = ncchar('INST roll (degrees)');
nc{'roll'}.epic_code = nclong(1217);
nc{'roll'}.valid_range = ncfloat([0 359.999908447266]);
nc{'roll'}.height = ncfloat(userMeta.sensor_height);
nc{'roll'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'roll'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'temperature'} = ncfloat('burst', 'lat', 'lon');
nc{'temperature'}.units = ncchar('degrees');
nc{'temperature'}.long_name = ncchar(['Aquadopp transducer temperature (degrees C)']);
nc{'temperature'}.valid_range = ncfloat([-5 40]);
nc{'temperature'}.height = ncfloat(userMeta.sensor_height);
nc{'temperature'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'temperature'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'pressure'} = ncfloat('burst', 'lat', 'lon');
nc{'pressure'}.units = ncchar('m');
nc{'pressure'}.long_name = ncchar('pressure (m)');
nc{'pressure'}.valid_range = ncfloat([0 20]);
nc{'pressure'}.height = ncfloat(userMeta.sensor_height);
nc{'pressure'}.serial = ncchar(aqdpMeta.Serial_number);
nc{'pressure'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'extsen1'} = ncfloat('burst', 'lat', 'lon');
nc{'extsen1'}.units = ncchar('');
nc{'extsen1'}.long_name = ncchar('analog input 1');
nc{'extsen1'}.height = ncfloat([]);
nc{'extsen1'}.serial = ncchar([]);
nc{'extsen1'}.FillValue_ = ncfloat(1.00000004091848e+035);

nc{'extsen2'} = ncfloat('burst', 'lat', 'lon');
nc{'extsen2'}.units = ncchar('');
nc{'extsen2'}.long_name = ncchar('analog input 2');
nc{'extsen2'}.height = ncfloat([]);
nc{'extsen2'}.serial = ncchar([]);
nc{'extsen2'}.FillValue_ = ncfloat(1.00000004091848e+035);

endef(nc);

% Get dimensions
nRec = aqdpData.nBursts;
nCells = aqdpData.nCells;

% Write the data to the .cdf file
nc{'time'}(1:nRec) = floor(aqdpData.time);
nc{'time2'}(1:nRec) = (aqdpData.time-floor(aqdpData.time)).*(24*3600*1000); 
nc{'height'}(1:nCells) = aqdpData.cell_height;
nc{'burst'}(1:nRec) = aqdpData.burst;
nc{'lat'}(1)=nc.latitude(:);
nc{'lon'}(1)=nc.longitude(:);
nc{'VelEast'}(1:nRec, 1:nCells) = aqdpData.u;
nc{'VelNorth'}(1:nRec, 1:nCells) = aqdpData.v;
nc{'VelUp'}(1:nRec, 1:nCells) = aqdpData.w;
nc{'VelBeam1'}(1:nRec, 1:nCells) = aqdpData.velBeam1;
nc{'VelBeam2'}(1:nRec, 1:nCells) = aqdpData.velBeam2;
nc{'VelBeam3'}(1:nRec, 1:nCells) = aqdpData.velBeam3;
nc{'ampBeam1'}(1:nRec, 1:nCells) = aqdpData.ampBeam1;
nc{'ampBeam2'}(1:nRec, 1:nCells) = aqdpData.ampBeam2;
nc{'ampBeam3'}(1:nRec, 1:nCells) = aqdpData.ampBeam3;
nc{'err_code'}(1:nRec) = aqdpData.err_code;
nc{'stat_code'}(1:nRec) = aqdpData.sta_code;
nc{'batt'}(1:nRec) = aqdpData.batt;
nc{'sound_spd'}(1:nRec) = aqdpData.sound_spd;
nc{'heading'}(1:nRec) = aqdpData.hdg;
nc{'pitch'}(1:nRec) = aqdpData.ptch;
nc{'roll'}(1:nRec) = aqdpData.roll;
nc{'temperature'}(1:nRec) = aqdpData.tC;
nc{'pressure'}(1:nRec) = aqdpData.pres;
nc{'extsen1'}(1:nRec) = aqdpData.xsen1;
nc{'extsen2'}(1:nRec) = aqdpData.xsen2;

% Calculate min/max values
% Calculate min/max values and replace NaNs
% with the fill value
theVars = var(nc);
for i = 1:length(theVars),
    data = theVars{i}(:);
    if ~isempty(data) & ...
       ~strcmp(ncnames(theVars{i}),'time') & ...
       ~strcmp(ncnames(theVars{i}),'time2') & ...
       ~strcmp(ncnames(theVars{i}),'depth') & ...
       ~strcmp(ncnames(theVars{i}),'lat') & ...
       ~strcmp(ncnames(theVars{i}),'lon')
            theVars{i}.minimum = gmin(data(:));
            theVars{i}.maximum = gmax(data(:));
            theFillVal = theVars{i}.FillValue_(:);
            bads = find(isnan(data));
            data(bads) = theFillVal;
            theVars{i}(:) = data;
    end
end

% Update/add some attributes
nc.DATA_TYPE = ncchar('Aquadopp velocity data output from AquaPro');
history = nc.history(:);
if strcmp(aqdpMeta.Coordinate_system,'BEAM')
    history_new = ['Data converted to netCDF via MATLAB by aqdpCurr2cdf.m V ',...
                   num2str(version),'; Velocities rotated and corrected for ',...
                   'magnetic variation by READ_aquapro_beam.m; ',history];
else
    history_new = [history,'; Data converted to netCDF via MATLAB by aqdpCurr2cdf.m V ',...
                num2str(version)];
end
nc.history = ncchar(history_new);
nc.DELTA_T = ncfloat(aqdpData.delta_t * 24 * 3600); %seconds
nc.CREATION_DATE = datestr(now,0);
nc.start_time = datestr(gregorian(aqdpData.time(1)),0);
nc.stop_time = datestr(gregorian(aqdpData.time(end)),0);

nc=close(nc);