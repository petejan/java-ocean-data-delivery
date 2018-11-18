from netCDF4 import Dataset
from netCDF4 import num2date
import datetime as dt
import numpy as np
import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import sys
import os
from matplotlib import rc


def ncdump(nc_fid, verb=True):
    '''
    ncdump outputs dimensions, variables and their attribute information.
    The information is similar to that of NCAR's ncdump utility.
    ncdump requires a valid instance of Dataset.

    Parameters
    ----------
    nc_fid : netCDF4.Dataset
        A netCDF4 dateset object
    verb : Boolean
        whether or not nc_attrs, nc_dims, and nc_vars are printed

    Returns
    -------
    nc_attrs : list
        A Python list of the NetCDF file global attributes
    nc_dims : list
        A Python list of the NetCDF file dimensions
    nc_vars : list
        A Python list of the NetCDF file variables
    '''

    def print_ncattr(key):
        """
        Prints the NetCDF file attributes for a given key

        Parameters
        ----------
        key : unicode
            a valid netCDF4.Dataset.variables key
        """
        try:
            print "\t\ttype:", repr(nc_fid.variables[key].dtype)
            for ncattr in nc_fid.variables[key].ncattrs():
                print '\t\t%s:' % ncattr, \
                    repr(nc_fid.variables[key].getncattr(ncattr))
        except KeyError:
            print "\t\tWARNING: %s does not contain variable attributes" % key

    # NetCDF global attributes
    nc_attrs = nc_fid.ncattrs()
    if verb:
        print "NetCDF Global Attributes:"
        for nc_attr in nc_attrs:
            print '\t%s:' % nc_attr, repr(nc_fid.getncattr(nc_attr))
    nc_dims = [dim for dim in nc_fid.dimensions]  # list of nc dimensions
    # Dimension shape information.
    if verb:
        print "NetCDF dimension information:"
        for dim in nc_dims:
            print "\tName:", dim
            print "\t\tsize:", len(nc_fid.dimensions[dim])
            print_ncattr(dim)
    # Variable information.
    nc_vars = [var for var in nc_fid.variables]  # list of nc variables
    if verb:
        print "NetCDF variable information:"
        for var in nc_vars:
            if var not in nc_dims:
                print '\tName:', var
                print "\t\tdimensions:", nc_fid.variables[var].dimensions
                print "\t\tsize:", nc_fid.variables[var].size
                print_ncattr(var)
    return nc_attrs, nc_dims, nc_vars


# rc('text', usetex=True)

for path_file in sys.argv[1:len(sys.argv)]:

    nc = Dataset(path_file)

    nc_attrs, nc_dims, nc_vars = ncdump(nc)

    nctime = nc.variables['TIME'][:]
    t_unit = nc.variables['TIME'].units  # get unit  "days since 1950-01-01T00:00:00Z"

    try:

        t_cal = nc.variables['TIME'].calendar

    except AttributeError:  # Attribute doesn't exist

        t_cal = u"gregorian"  # or standard

    t1 = num2date(nctime[0], units=t_unit, calendar=t_cal)

    print 'time ', t1

    dt_time = [num2date(t, units=t_unit, calendar=t_cal) for t in nctime]

    # remove any dimensions from the list to plot
    nc_vars_to_plot = nc_vars;
    for i in nc_dims:
        try:
            nc_vars_to_plot.remove(i)
        except ValueError:
            print 'did not remove ', i

    # remove an auxiliary variables from the list to plot
    aux_vars = list();
    for var in nc.variables:
        try:
            aux_vars.append(nc.variables[var].getncattr('ancillary_variables'))
        except AttributeError:
            pass

    # remove any variables without a TIME dimension from the list to plot
    to_plot = list();

    for var in nc.variables:
        # print var
        if var in nc_dims:
            continue
        if var in aux_vars:
            continue
        if 'TIME' in nc.variables[var].dimensions:
            print 'to plot ', var
            to_plot.append(var)

    # pdffile = path_file[path_file.rfind('/')+1:len(path_file)] + '-' + nc.getncattr('deployment_code') + '-plot.pdf'

    pdffile = path_file + '.pdf'

    pp = PdfPages(pdffile)

    fig = plt.figure(figsize=(11.69, 8.27))

    text = 'file name : ' + os.path.basename(path_file) + '\n'

    # print "NetCDF Global Attributes:"
    for nc_attr in nc_attrs:
        # print '\t%s:' % nc_attr, repr(nc.getncattr(nc_attr))
        text += nc_attr + ' : ' + str(nc.getncattr(nc_attr)) + '\n'

    plt.text(-0.1, -0.1, text, fontsize=8, family='monospace')
    plt.axis('off')
    pp.savefig(fig)

    for plot in to_plot:

        plot_var = nc.variables[plot]

        var = plot_var[:]
        shape_len = len(var.shape)

        fig = plt.figure(figsize=(11.69, 8.27))

        text = "Variable : " + plot_var.name + str(plot_var.dimensions) + "\n"
        nc_attrs = plot_var.ncattrs()
        # print "NetCDF Variable Attributes:"
        for nc_attr in nc_attrs:
            attrVal = plot_var.getncattr(nc_attr)
            print '\t%s:' % nc_attr, repr(plot_var.getncattr(nc_attr)), type(attrVal)
            if type(attrVal) != unicode:
                text += nc_attr.encode('utf-8') + ' : ' + str(attrVal) + '\n'
            else:
                text += nc_attr.encode('utf-8') + ' : ' + attrVal + '\n'

        if hasattr(plot_var, 'ancillary_variables'):
            qc_var_name = plot_var.getncattr('ancillary_variables')
            qc_var = nc.variables[qc_var_name];

            text += "\nAUX : " + qc_var.name + str(qc_var.dimensions) + "\n"

            nc_attrs = qc_var.ncattrs()
            # print "NetCDF AUX Variable Attributes:"
            for nc_attr in nc_attrs:
                # print '\t%s:' % nc_attr, repr(nc.getncattr(nc_attr))
                text += nc_attr + ' : ' + str(qc_var.getncattr(nc_attr)) + '\n'

            qc = nc.variables[qc_var_name][:]

            if plot_var.dimensions[0] != 'TIME':
                qc = np.transpose(qc)

            qc = np.squeeze(qc)
        else:
            qc = 0

        plt.text(-0.1, 0.0, text, fontsize=8, family='monospace')
        plt.axis('off')
        pp.savefig(fig)
        plt.close(fig)

        print plot_var.name, " shape ", var.shape, " len ", shape_len

        fig = plt.figure(figsize=(11.69, 8.27))
        if plot_var.dimensions[0] != 'TIME':
            var = np.transpose(var)
        var = np.squeeze(var)

        qc_m = np.ma.masked_where(qc > 1, var)
        mx = qc_m.max()
        mi = qc_m.min()

        marg = (mx - mi) * 0.1
        print "max ", mx, " min ", mi

        plt.ylim([mi - marg, mx + marg])

        if hasattr(plot_var, 'sensor_serial_number'):
            sn = plot_var.getncattr('sensor_serial_number').split('; ')
        else:
            sn = nc.getncattr('instrument_serial_number')

        if hasattr(plot_var, 'sensor_depth'):
            dpth = plot_var.getncattr('sensor_depth').split('; ')
        elif hasattr(plot_var, 'sensor_height'):
            dpth = plot_var.getncattr('sensor_height').split('; ')
        else:
            dpth = 'unknown'

        leg = [x + ' (' + y + ' m)' for x, y in zip(sn, dpth)]

        plot_marks = '-'
        if len(dt_time) < 200:
            plot_marks = '.-'
        pl = plt.plot(dt_time, qc_m, plot_marks)
        # plt.legend(iter(pl), leg)
        plt.legend(iter(pl), leg, bbox_to_anchor=(0.0, -0.2, 1.0, -0.15), loc=3, ncol=6, mode="expand",
                   borderaxespad=0.0, fontsize='x-small')

        qc_m = np.ma.masked_where(qc <= 1, var)
        plt.plot(dt_time, qc_m, 'yo')
        qc_m = np.ma.masked_where(qc <= 3, var)
        plt.plot(dt_time, qc_m, 'ro')

        fig.autofmt_xdate()
        plt.grid()

        # add deployment/instrument/standard name as title

        # plt.title(nc.getncattr('deployment_code') + ' : ' + plot_var.sensor_name + ' ' + \
        #          plot_var.sensor_serial_number + ' : ' + plot_var.name, fontsize=10)

        # plt.title(nc.getncattr('deployment_code') + ' : ' + plot_var.getncattr('name'), fontsize=10)
        plt.title(nc.getncattr('deployment_code'), fontsize=10)

        # add units to Y axis

        plt.ylabel(plot + ' (' + plot_var.units + ')')

        # plot only the time of deployment
        # date_time_start = dt.datetime.strptime(nc.getncattr('time_deployment_start'), '%Y-%m-%dT%H:%M:%SZ')
        # date_time_end = dt.datetime.strptime(nc.getncattr('time_deployment_end'), '%Y-%m-%dT%H:%M:%SZ')
        date_time_start = dt.datetime.strptime(nc.getncattr('time_coverage_start'), '%Y-%m-%dT%H:%M:%SZ')
        date_time_end = dt.datetime.strptime(nc.getncattr('time_coverage_end'), '%Y-%m-%dT%H:%M:%SZ')

        plt.xlim(date_time_start, date_time_end)

        # plt.savefig(plot + '.pdf')
        pp.savefig(fig, papertype='a4')
        plt.close(fig)

    # plt.show()

    pp.close()

    nc.close();
