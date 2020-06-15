--
-- PostgreSQL database dump
--

-- Dumped from database version 9.2.2
-- Dumped by pg_dump version 9.2.2
-- Started on 2013-09-24 20:23:30 EST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- TOC entry 2317 (class 0 OID 40642)
-- Dependencies: 192
-- Data for Name: instrument_data_processors; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO instrument_data_processors VALUES (100367, 'Pulse-6-2009        ', 'org.imos.abos.forms.SBE16CalculationForm', 'MOORING=Pulse-6-2009,SRC_INST=4,SBE43_INST=621,PAR_INST=619,FLNTUS_INST=622,OPTODE_INST=620', '2013-09-24 03:19:39.894', 'Y         ', 4228);
INSERT INTO instrument_data_processors VALUES (100368, 'Pulse-6-2009        ', 'org.imos.abos.forms.SeaWaterCalculationForm', 'MOORING=Pulse-6-2009', '2013-09-24 03:40:12.962', 'Y         ', 266320);
INSERT INTO instrument_data_processors VALUES (100369, 'Pulse-6-2009        ', 'org.imos.abos.forms.InterpolatedWaterParamsDataCreationForm', 'MOORING=Pulse-6-2009,SRC_INST=4,TGT_INST=621', '2013-09-24 03:42:07.971', 'Y         ', 4226);
INSERT INTO instrument_data_processors VALUES (100370, 'Pulse-6-2009        ', 'org.imos.abos.forms.SeabirdSBE43CalculationForm', 'MOORING=Pulse-6-2009,SRC_INST=621,TGT_INST=621,FILE=100209', '2013-09-24 03:42:43.272', 'Y         ', 4228);
INSERT INTO instrument_data_processors VALUES (100371, 'Pulse-6-2009        ', 'org.imos.abos.forms.WETLabsPARCalculationForm', 'MOORING=Pulse-6-2009,SRC_INST=619,TGT_INST=619,FILE=200400', '2013-09-24 04:03:22.769', 'Y         ', 4228);
INSERT INTO instrument_data_processors VALUES (100372, 'Pulse-6-2009        ', 'org.imos.abos.forms.WETLabsFLNTUSCalculationForm', 'MOORING=Pulse-6-2009,SRC_INST=622,TGT_INST=622,FILE=200402', '2013-09-24 04:06:20.833', 'Y         ', 4228);
INSERT INTO instrument_data_processors VALUES (100373, 'Pulse-6-2009        ', 'org.imos.abos.forms.InterpolatedProcessedDataCreationForm', 'MOORING=Pulse-6-2009', '2013-09-24 04:12:44.121', 'Y         ', 258261);
INSERT INTO instrument_data_processors VALUES (100374, 'Pulse-6-2009        ', 'org.imos.abos.forms.MarkBadDataForMooringDeploymentForm', 'MOORING=Pulse-6-2009', '2013-09-24 04:24:43.364', 'Y         ', -1);
INSERT INTO instrument_data_processors VALUES (100375, 'Pulse-7-2010        ', 'org.imos.abos.forms.SBE16CalculationForm', 'MOORING=Pulse-7-2010,SRC_INST=4,SBE43_INST=621,PAR_INST=619,FLNTUS_INST=622,OPTODE_INST=620', '2013-09-24 04:32:06.031', 'Y         ', 5918);
INSERT INTO instrument_data_processors VALUES (100376, 'Pulse-7-2010        ', 'org.imos.abos.forms.SeaWaterCalculationForm', 'MOORING=Pulse-7-2010', '2013-09-24 04:52:18.116', 'Y         ', 344184);
INSERT INTO instrument_data_processors VALUES (100377, 'Pulse-7-2010        ', 'org.imos.abos.forms.InterpolatedWaterParamsDataCreationForm', 'MOORING=Pulse-7-2010,SRC_INST=4,TGT_INST=620', '2013-09-24 04:54:56.493', 'Y         ', 5916);
INSERT INTO instrument_data_processors VALUES (100378, 'Pulse-7-2010        ', 'org.imos.abos.forms.InterpolatedWaterParamsDataCreationForm', 'MOORING=Pulse-7-2010,SRC_INST=4,TGT_INST=621', '2013-09-24 04:55:45.701', 'Y         ', 5916);
INSERT INTO instrument_data_processors VALUES (100379, 'Pulse-7-2010        ', 'org.imos.abos.forms.SeabirdSBE43CalculationForm', 'MOORING=Pulse-7-2010,SRC_INST=621,TGT_INST=621,FILE=100209', '2013-09-24 04:56:54.745', 'Y         ', 5918);
INSERT INTO instrument_data_processors VALUES (100380, 'Pulse-7-2010        ', 'org.imos.abos.forms.AanderraOptodeCalculationForm', 'MOORING=Pulse-7-2010,SRC_INST=620,TGT_INST=620,ALGO=Uchida,FILE=100217', '2013-09-24 04:57:24.491', 'Y         ', 5918);
INSERT INTO instrument_data_processors VALUES (100381, 'Pulse-7-2010        ', 'org.imos.abos.forms.WETLabsPARCalculationForm', 'MOORING=Pulse-7-2010,SRC_INST=619,TGT_INST=619,FILE=200400', '2013-09-24 04:58:48.278', 'Y         ', 5918);
INSERT INTO instrument_data_processors VALUES (100382, 'Pulse-7-2010        ', 'org.imos.abos.forms.WETLabsFLNTUSCalculationForm', 'MOORING=Pulse-7-2010,SRC_INST=622,TGT_INST=622,FILE=200402', '2013-09-24 04:59:04.541', 'Y         ', 5918);
INSERT INTO instrument_data_processors VALUES (100383, 'Pulse-7-2010        ', 'org.imos.abos.forms.ISUSDataCreationForm', 'MOORING=Pulse-7-2010', '2013-09-24 05:25:26.772', 'Y         ', 0);
INSERT INTO instrument_data_processors VALUES (100384, 'Pulse-7-2010        ', 'org.imos.abos.forms.InterpolatedProcessedDataCreationForm', 'MOORING=Pulse-7-2010', '2013-09-24 05:37:34.737', 'Y         ', 394086);
INSERT INTO instrument_data_processors VALUES (100385, 'Pulse-7-2010        ', 'org.imos.abos.forms.MarkBadDataForMooringDeploymentForm', 'MOORING=Pulse-7-2010', '2013-09-24 05:40:22.364', 'Y         ', -1);
INSERT INTO instrument_data_processors VALUES (100388, 'Pulse-8-2011        ', 'org.imos.abos.forms.SBE16CalculationForm', 'MOORING=Pulse-8-2011,SRC_INST=740,SBE43_INST=742,PAR_INST=660,FLNTUS_INST=622,OPTODE_INST=727', '2013-09-24 06:23:39.576', 'Y         ', 5436);
INSERT INTO instrument_data_processors VALUES (100389, 'Pulse-8-2011        ', 'org.imos.abos.forms.SeaWaterCalculationForm', 'MOORING=Pulse-8-2011', '2013-09-24 07:17:04.787', 'Y         ', 519693);
INSERT INTO instrument_data_processors VALUES (100390, 'Pulse-8-2011        ', 'org.imos.abos.forms.InterpolatedWaterParamsDataCreationForm', 'MOORING=Pulse-8-2011,SRC_INST=740,TGT_INST=742', '2013-09-24 07:18:12.25', 'Y         ', 5434);
INSERT INTO instrument_data_processors VALUES (100391, 'Pulse-8-2011        ', 'org.imos.abos.forms.InterpolatedWaterParamsDataCreationForm', 'MOORING=Pulse-8-2011,SRC_INST=740,TGT_INST=727', '2013-09-24 07:19:04.519', 'Y         ', 5434);
INSERT INTO instrument_data_processors VALUES (100392, 'Pulse-8-2011        ', 'org.imos.abos.forms.SeabirdSBE43CalculationForm', 'MOORING=Pulse-8-2011,SRC_INST=742,TGT_INST=742,FILE=100228', '2013-09-24 07:19:38.876', 'Y         ', 5436);
INSERT INTO instrument_data_processors VALUES (100393, 'Pulse-8-2011        ', 'org.imos.abos.forms.AanderraOptodeCalculationForm', 'MOORING=Pulse-8-2011,SRC_INST=727,TGT_INST=727,ALGO=Uchida,FILE=100234', '2013-09-24 07:20:20.346', 'Y         ', 5436);
INSERT INTO instrument_data_processors VALUES (100394, 'Pulse-8-2011        ', 'org.imos.abos.forms.ISUSDataCreationForm', 'MOORING=Pulse-8-2011', '2013-09-24 07:20:39.633', 'Y         ', 0);
INSERT INTO instrument_data_processors VALUES (100395, 'Pulse-8-2011        ', 'org.imos.abos.forms.WETLabsPARCalculationForm', 'MOORING=Pulse-8-2011,SRC_INST=660,TGT_INST=660,FILE=200404', '2013-09-24 07:25:16.248', 'Y         ', 5436);
INSERT INTO instrument_data_processors VALUES (100396, 'Pulse-8-2011        ', 'org.imos.abos.forms.WETLabsFLNTUSCalculationForm', 'MOORING=Pulse-8-2011,SRC_INST=622,TGT_INST=622,FILE=200402', '2013-09-24 07:25:38.595', 'Y         ', 5436);
INSERT INTO instrument_data_processors VALUES (100397, 'Pulse-8-2011        ', 'org.imos.abos.forms.InterpolatedProcessedDataCreationForm', 'MOORING=Pulse-8-2011', '2013-09-24 07:42:45.606', 'Y         ', 274565);
INSERT INTO instrument_data_processors VALUES (100398, 'Pulse-8-2011        ', 'org.imos.abos.forms.MarkBadDataForMooringDeploymentForm', 'MOORING=Pulse-8-2011', '2013-09-24 07:42:57.626', 'Y         ', -1);
INSERT INTO instrument_data_processors VALUES (100399, 'Pulse-9-2012        ', 'org.imos.abos.forms.SBE16CalculationForm', 'MOORING=Pulse-9-2012,SRC_INST=4,SBE43_INST=621,PAR_INST=619,FLNTUS_INST=748,OPTODE_INST=857', '2013-09-24 07:45:24.434', 'Y         ', 5429);
INSERT INTO instrument_data_processors VALUES (100400, 'Pulse-9-2012        ', 'org.imos.abos.forms.SeaWaterCalculationForm', 'MOORING=Pulse-9-2012', '2013-09-24 07:56:32.284', 'Y         ', 20433);
INSERT INTO instrument_data_processors VALUES (100401, 'Pulse-9-2012        ', 'org.imos.abos.forms.InterpolatedWaterParamsDataCreationForm', 'MOORING=Pulse-9-2012,SRC_INST=4,TGT_INST=857', '2013-09-24 07:57:17.464', 'Y         ', 5427);
INSERT INTO instrument_data_processors VALUES (100402, 'Pulse-9-2012        ', 'org.imos.abos.forms.InterpolatedWaterParamsDataCreationForm', 'MOORING=Pulse-9-2012,SRC_INST=4,TGT_INST=621', '2013-09-24 07:57:59.443', 'Y         ', 5427);
INSERT INTO instrument_data_processors VALUES (100403, 'Pulse-9-2012        ', 'org.imos.abos.forms.SeabirdSBE43CalculationForm', 'MOORING=Pulse-9-2012,SRC_INST=621,TGT_INST=621,FILE=100224', '2013-09-24 07:58:41.148', 'Y         ', 5429);
INSERT INTO instrument_data_processors VALUES (100404, 'Pulse-9-2012        ', 'org.imos.abos.forms.AanderraOptodeCalculationForm', 'MOORING=Pulse-9-2012,SRC_INST=857,TGT_INST=857,ALGO=Uchida,FILE=100238', '2013-09-24 07:59:29.137', 'Y         ', 5429);
INSERT INTO instrument_data_processors VALUES (100405, 'Pulse-9-2012        ', 'org.imos.abos.forms.WETLabsPARCalculationForm', 'MOORING=Pulse-9-2012,SRC_INST=619,TGT_INST=619,FILE=200400', '2013-09-24 07:59:46.36', 'Y         ', 5429);
INSERT INTO instrument_data_processors VALUES (100406, 'Pulse-9-2012        ', 'org.imos.abos.forms.WETLabsFLNTUSCalculationForm', 'MOORING=Pulse-9-2012,SRC_INST=748,TGT_INST=748,FILE=200398', '2013-09-24 08:00:01.585', 'Y         ', 5429);
INSERT INTO instrument_data_processors VALUES (100407, 'Pulse-9-2012        ', 'org.imos.abos.forms.ISUSDataCreationForm', 'MOORING=Pulse-9-2012', '2013-09-24 08:00:27.44', 'Y         ', 0);
INSERT INTO instrument_data_processors VALUES (100408, 'Pulse-9-2012        ', 'org.imos.abos.forms.InterpolatedProcessedDataCreationForm', 'MOORING=Pulse-9-2012', '2013-09-24 08:04:57.321', 'Y         ', 641564);
INSERT INTO instrument_data_processors VALUES (100409, 'Pulse-9-2012        ', 'org.imos.abos.forms.MarkBadDataForMooringDeploymentForm', 'MOORING=Pulse-9-2012', '2013-09-24 08:05:38.982', 'Y         ', -1);


-- Completed on 2013-09-24 20:23:30 EST

--
-- PostgreSQL database dump complete
--

