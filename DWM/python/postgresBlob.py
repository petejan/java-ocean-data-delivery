#!/usr/bin/python
import psycopg2
import sys

def write_blob(instrument_id, file_path, file_name):
    """ insert a BLOB into a table """
    conn = None
    datafile_pk = None
    validity_start = "2000-01-01"
    validity_end = "2019-12-31"
    try:
        # read data from a picture
        drawing = open("tmp.txt", 'rb').read()
        # connect to the PostgresQL database
        conn = psycopg2.connect(host="localhost", database="ABOS", user="pete", password="password")
        # create a new cursor object
        cur = conn.cursor()
        # execute the INSERT statement
        cur.execute("INSERT INTO instrument_calibration_files(instrument_id, file_path, file_name, validity_start, validity_end, file_data) " +
                    "VALUES(%s,%s,%s,%s,%s,%s) RETURNING datafile_pk",
                    (instrument_id, file_path, file_name, validity_start, validity_end, psycopg2.Binary(drawing)))
        # commit the changes to the database
        conn.commit()
        datafile_pk = cur.fetchone()[0]
        # close the communication with the PostgresQL database
        cur.close()
    except (Exception, psycopg2.DatabaseError) as error:
        print(error)
    finally:
        if conn is not None:
            conn.close()

    return datafile_pk


def read_blob(part_id):
    """ read BLOB data from a table """
    conn = None
    file_name = None
    file_path = None
    mooring_id = None
    instrument_id = None

    try:
        # connect to the PostgresQL database
        conn = psycopg2.connect(host="localhost", database="ABOS", user="pete", password="password")
        # create a new cursor object
        cur = conn.cursor()
        # execute the SELECT statement
        cur.execute("SELECT file_name, file_path, mooring_id, instrument_id, file_data FROM instrument_data_files WHERE datafile_pk = %s", (part_id,))

        blob = cur.fetchone()

        file_name = blob[0].strip()
        file_path = blob[1].strip()
        mooring_id = blob[2].strip()
        instrument_id = blob[3]

        print("instrument_id %d name : %s" % (instrument_id, file_name))

        open(blob[0], 'wb').write(blob[4])
        # close the communication with the PostgresQL database
        cur.close()
    except (Exception, psycopg2.DatabaseError) as error:
        print(error)
    finally:
        if conn is not None:
            conn.close()

    return file_name, file_path, mooring_id, instrument_id


if __name__ == '__main__':
    inst = sys.argv[1]

    file_name, file_path, mooring_id, instrument_id = read_blob(inst)

    print("mooring_id %s instrument_id %d name : %s" % (mooring_id, instrument_id, file_name))

    # file_path = sys.argv[1]

    N = 30
    lines =[]
    with open(file_path) as f:
        lines.extend(f.readline() for i in range(N))

    calvalues = []
    sn = None
    x = 0
    f = open('tmp.txt', 'w')
    for i in lines:
        l = i.strip()
        f.write(l)
        f.write('\n')

        lsplit = l.split('=')
        #print(i.strip())
        if i.startswith("CoefDate"):
            print("CoefDate : %s" % l)
            calvalues.append(('CoefDate', 'Calibration Date', 'STRING', lsplit[1].strip(",")))
        elif i.startswith("ImmersionEN"):
            print("Im : %s" % l)
            calvalues.append((lsplit[0], 'Immersion Enabled', 'VALUE', lsplit[1]))
        elif i.startswith("Immersion_Effect"):
            print("Im : %s" % l)
            calvalues.append((lsplit[0], 'Immersion Effect Correction', 'VALUE', lsplit[1]))
        elif i.startswith("Inst_No"):
            print("Inst_No : %s" % l)
            sn = lsplit[1].strip()
        elif i.startswith("InstNo"):
            print("InstNo : %s" % lsplit[1].strip())
            sn = lsplit[1].strip()
            print('Serial number %s' % sn)
        elif i.startswith("SondeNo"):
            print("SondeNo : %s" % l)
            sn = lsplit[1].strip()
        elif i.startswith("Ch1"):
            print("Ch1 : %s" % l)
            calvalues.append(('Ch1', 'Coefficents', 'STRING', lsplit[1].strip(",")))
            v = lsplit[1].split(",")
            calvalues.append(('A', 'intercept', 'VALUE', v[0]))
            calvalues.append(('B', 'slope', 'VALUE', v[1]))
        elif i.startswith("[Coef]"):
            print("[Coef] : %s : %s" % (l, lines[x+1].strip()))
            calvalues.append(('Coef', 'Coefficents', 'STRING', lines[x+1].strip(",\r\n '")))
            coeff = lines[x+1].split(",")
            calvalues.append(('A', 'intercept', 'VALUE', coeff[0]))
            calvalues.append(('B', 'slope', 'VALUE', coeff[1]))

        x = x + 1

    f.close()

    print("extracted values: serial %s" % sn)
    for v in calvalues:
        print(v)

    file_split = file_path.split("/")
    file_name = file_split[-1]
    mooring_id = file_split[-2]

    # try:
    #     # connect to the PostgresQL database
    #     conn = psycopg2.connect(host="localhost", database="ABOS", user="pete", password="password")
    #     # create a new cursor object
    #     cur = conn.cursor()
    #     # execute the SELECT statement
    #     cur.execute("SELECT instrument_id FROM instrument WHERE serial_number = %s AND make = 'Alec Electronics'", (sn,))
    #
    #     blob = cur.fetchone()
    #
    #     instrument_id = blob[0]
    #
    #     open(blob[0], 'wb').write(blob[4])
    #     # close the communication with the PostgresQL database
    #     cur.close()
    # except (Exception, psycopg2.DatabaseError) as error:
    #     print(error)
    # finally:
    #     if conn is not None:
    #         conn.close()

    print("filename %s instrument_id %d" % (file_name, instrument_id))

    writeId = -1
    writeId = write_blob(instrument_id, file_path, file_name)

    print("wrote id : %d" % writeId)

    print("extracted values:")

    try:
        # connect to the PostgresQL database
        conn = psycopg2.connect(host="localhost", database="ABOS", user="pete", password="password")
        # create a new cursor object
        cur = conn.cursor()
        # execute the INSERT statement
        for v in calvalues:
            print(v)
            cur.execute("INSERT INTO instrument_calibration_values(instrument_id, mooring_id, datafile_pk, param_code, description, data_type, data_value) " +
                        "VALUES(%s, %s, %s, %s, %s, %s, %s)",
                (instrument_id, mooring_id, writeId, v[0], v[1], v[2], v[3]))
        # commit the changes to the database
        conn.commit()
        # close the communication with the PostgresQL database
        cur.close()
    except (Exception, psycopg2.DatabaseError) as error:
        print(error)
    finally:
        if conn is not None:
            conn.close()

