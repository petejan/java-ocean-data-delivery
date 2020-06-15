function [badBursts, badCells] = mask_aqdpCurr_cdf(cdfFileRoot);
% mask_aqdpCurr_cdf.m  A function to QA/QC Aquadopp velocity data in a netCDF
%                     (.cdf) file.  
%
%    usage:  mask_aqdpCurr_cdf(cdfFileRoot);
%
%        where:  cdfFileRoot - the name of the .cdf file which contains
%                               Aquadopp velocity data as output by 
%                               AquaPro, surrounded by single quotes with
%                               no file extension 
%                badBursts - a list of bad bursts, which are collected
%                             during instrument deployment and recovery
%                badCells - a list of bad cells, which are located above
%                             MSL and contaminated by sidelobe reflection
%                             errors
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
%   smart_interp.m
%   gmin.m
%   gmax.m

% C. Sullivan   06/17/05,   version 1.1
% If the Aquadopp's wave interval is longer than the current profile
% interval, the waves take precedence!! I believe the AquaPro software 
% interpolates where current profiles are skipped, but something weird is
% happening to the temperature. I now NaN the profiles that are skipped 
% (interpolated) b/c of waves, and do my own interpolation.
% C. Sullivan   06/16/05,   version 1.0
% This mfile applies QA/QC measures including removal of bad data collected
% during deployment and recovery of the instrument, and removal of cells
% (bins) contaminated by sidelobe reflection errors.

version = '1.1';

% Copy cdfFileRoot.cdf to cdfFileRoot-mask.cdf. In the 'mask' version
% all bad data will be masked (set to FILL). Masked data should NOT be
% written to the BBV .nc file. The mask file is an intermediate file,
% between the .cdf file created from AquaPro output, and the .nc file
% created for distribution.
copyfile([cdfFileRoot,'.cdf'],[cdfFileRoot,'-mask.cdf'])

cdf = netcdf([cdfFileRoot,'-mask.cdf'],'w');

% Get the dimensions
theBurstDim = cdf('burst');
nBursts = max(size(theBurstDim));

% Data is interpolated internally by AquaPro for those bursts
% that occur during a wave burst. Ask the user if they'd like to loose
% AquaPro's interpolation, and do their own linear interpolation FOR
% TEMPERATURE DATA ONLY (velocity and pressure data has been okay).
temperature = cdf{'temperature'}(:);
burst = cdf{'burst'}(:);
time = cdf{'time'}(:) + (cdf{'time2'}(:)/3600/1000/24);
prof_int = cdf.AQDP_Profile_interval(:);
nums = ~isletter(prof_int);
prof_int = str2num(prof_int(nums));
wave_int = cdf.AQDP_Wave__Interval(:);
nums = ~isletter(wave_int);
wave_int = str2num(wave_int(nums));
if wave_int > prof_int
    figure
    plot(burst, temperature)
    hold on
    xlabel('Burst number')
    ylabel('Temperature (\circC)')
    disp(' ')
    disp('Please look at temperature data and look for evidence of bad')
    disp('interpolations by AquaPro')
    answer = input('Would you like to re-interpolate temperature data? y/n:  ','s');
    if strcmp(answer,'Y') || strcmp(answer,'y')
        first_bad_interp = input('Enter the burst # with the 1st bad interplolation:  ');
        bad_interps = [first_bad_interp:wave_int/prof_int:nBursts]';
        temperature(bad_interps) = nan;
        disp('Re-interpolating bad temperature data ...')
        temperature = smart_interp(time, temperature, time, 1);
        plot(burst, temperature, 'r')
        legend('AquaPro-interpolated data','self-interpolated data')
        bads = find(isnan(temperature));
        temperature(bads) = 1e35; %set nans to fill value
        cdf{'temperature'}(:) = temperature;
        cdf{'temperature'}.NOTE = ncchar('Temperature data re-interpolated at those bursts that occured during wave bursts');
        hist = cdf.history(:);
        hist_new = ['Temperature data re-interpolated by mask_aqdpCurr_cdf.m V ',...
                     version,'; ',hist];
        cdf.history = ncchar(hist_new);
    end
end

% Identify bad data collected during instrument deployment and 
% recovery by looking at a plot of pressure. Ask the user to enter the
% first and last good bursts of data. Data up to the first good burst and
% data after the last good burst will be masked.
figure
plot(cdf{'burst'}(:), cdf{'pressure'}(:))
hold on
ylabel('Pressure (m)')
xlabel('Burst number')
disp(' ')
disp('Please view the figure (feel free to zoom!) and look for bad')
disp('bursts collected during instrument deployment and recovery')
answer = input(['Would you like to mask any bad bursts? y/n :  '],'s');
if ~strcmp(answer,'Y') & ~strcmp(answer,'y') & ...
   ~strcmp(answer,'N') & ~strcmp(answer,'n')
        disp(['Valid answers are either yes ("y") or no ("n")'])
        answer = input(['Would you like to mask any bad bursts? y/n :  '],'s');
end
if strcmp(answer,'N') || strcmp(answer,'n')
    first_good = 1;
    last_good = nBursts;
    bad_bursts = [];
    disp(' ')
    disp('Not masking any bursts')
    disp(['All bursts (1:',num2str(nBursts),') remain un-masked in ',...
          cdfFileRoot,'-mask.cdf'])
elseif strcmp(answer,'Y') || strcmp(answer,'y')
    first_good = input('Please view the figure and enter the first GOOD burst:  ');
    if isempty(first_good)
        disp('Valid answers are numeric only')
        first_good = input('Please view the figure and enter the first GOOD burst:  ');
    end
    last_good = input('Please view the figure and enter the last GOOD burst:  ');
    if isempty(last_good)
        disp('Valid answers are numeric only')
        last_good = input('Please view the figure and enter the last GOOD burst:  ');
    end
    if isequal(first_good,1)
        disp(' ')
        disp(['Bursts ',num2str(last_good),':',num2str(nBursts),...
              ' will be masked in ',cdfFileRoot,'-mask.cdf'])
        badBursts = [last_good:nBursts]';
    elseif isequal(last_good,nBursts)
        disp(['Bursts 1:',num2str(first_good),' will be masked in ',...
               cdfFileRoot,'-mask.cdf'])
        badBursts = [1:first_good]';
    else
        disp(['Bursts 1:',num2str(first_good),' and bursts ',num2str(last_good), ...
              ':',num2str(nBursts),' will be masked in ',cdfFileRoot,'-mask.cdf'])
        badBursts = [1:first_good last_good:nBursts]';
    end
end
plot(cdf{'burst'}(1:first_good), cdf{'pressure'}(1:first_good),'r')
plot(cdf{'burst'}(last_good:nBursts), cdf{'pressure'}(last_good:nBursts),'r')
legend('good data','masked data')

% Calculate MSL and 1/2 the tidal range
press = cdf{'pressure'}(:);
press(badBursts) = nan;
MSL = gmean(press);
tide = gstd(press);
disp(' ')
disp(['The calculated MSL is ',num2str(MSL), ' meters'])
disp(['The calculated tidal range is ',num2str(tide),' meters'])

% Data within approx. 6% of the surface is contaminated by sidelobe
% reflection errors.  We will mask the data in these cells.
% Reference: http://www.nortekusa.com/principles/Doppler.html
cellHeight = cdf{'height'}(:); %cell height above bottom, m
badCells = find( cellHeight > (0.94*(MSL+(tide))));
disp(' ')
if isempty(badCells)
    disp('No cells found within the specified depth range')
else
    disp(['Cells [',num2str(badCells'),'] are above MSL'])
    disp('Data in these cells will be masked')
end

% Mask data, recalculate min/max values, and
% set NaN's to fill
disp(' ')
disp('Masking data ...')
theVars = var(cdf);
if ~isempty(badBursts) || ~isempty(badCells)
    for i = 1:length(theVars),
    
        if ~strcmp(ncnames(theVars{i}),'time') & ...
           ~strcmp(ncnames(theVars{i}),'time2') & ...
           ~strcmp(ncnames(theVars{i}),'burst') & ...
           ~strcmp(ncnames(theVars{i}),'lat') & ...
           ~strcmp(ncnames(theVars{i}),'lon') & ...
           ~strcmp(ncnames(theVars{i}),'height')
            theFillVal = theVars{i}.FillValue_(:);
            data = theVars{i}(:);
            [nR, nC] = size(data);
            fills = find(data >= theFillVal);
            if ~isempty(fills)
                data(fills) = nan;
            end
            if nC > 1
                if ~isempty(badBursts)
                    data(badBursts,:) = nan;
                end
                if ~isempty(badCells)
                    data(:,badCells) = nan;
                end
            else
                if ~isempty(badBursts)
                    data(badBursts) = nan;
                end
            end
            theVars{i}.minimum = gmin(data(:));
            theVars{i}.maximum = gmax(data(:));
            bads = find(isnan(data));
            data(bads) = theFillVal;
            theVars{i}(:) = data;
        end
    end
else
    disp(' ')
    disp(['Not masking any data in the file ',cdfFileRoot,'-mask.cdf'])
end

% Set attribute WATER_DEPTH to MSL
cdf.WATER_DEPTH = ncfloat(MSL);
cdf.WATER_DEPTH_NOTE = ncchar('Water depth is MSL');

% Update the file history
if ~isempty(badBursts) && ~isempty(badCells)
    hist = cdf.history(:);
    hist_new = ['Bad data collected during instrument deployment and recovery, ',...
                'and data collected above MSL masked by mask_aqdpCurr_cdf.m V ',...
                version,'; ',hist];
    cdf.history = ncchar(hist_new);
    
elseif ~isempty(badBursts)
    hist = cdf.history(:);
    hist_new = ['Bad data collected during instrument deployment and recovery ',...
                'masked by mask_aqdpCurr_cdf.m V ',version,'; ',hist];
    cdf.history = ncchar(hist_new);
elseif ~isempty(badCells)
    hist = cdf.history(:);
    hist_new = ['Data collected above MSL ',...
                'masked by mask_aqdpCurr_cdf.m V ',version,'; ',hist];
     cdf.history = ncchar(hist_new);
end

% Update the file creation date
cdf.CREATION_DATE = ncchar(datestr(now,0));

close(cdf);

disp(' ')
disp('Finished masking data')
disp(' ')
disp('NOTE:  Masking data for sidelobes does not necessarily remove all')
disp('       contaminated cells.  The user should load the file ')
disp(['       ',cdfFileRoot,'-mask.cdf and manually look for and remove if'])
disp('       necessary additional contaminated cells')
