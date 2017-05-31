DROP INDEX public.raw_data_sofs2_ix0;
DROP INDEX public.raw_data_sofs2_ix1;
DROP INDEX public.raw_data_sofs2_ix2;
DROP INDEX public.raw_data_sofs2_ix3;
DROP INDEX public.raw_data_sofs2_ix4;

--CREATE INDEX raw_data_sofs2_ix0 ON public.raw_instrument_data_sofs2 USING btree (source_file_id);
--CREATE INDEX raw_data_sofs2_ix2 ON public.raw_instrument_data_sofs2 USING btree (mooring_id COLLATE pg_catalog."default");
--CREATE INDEX raw_data_sofs2_ix1 ON public.raw_instrument_data_sofs2 USING btree (instrument_id);
--CREATE INDEX raw_data_sofs2_ix3 ON public.raw_instrument_data_sofs2 USING btree (parameter_code COLLATE pg_catalog."default", data_timestamp);
--CREATE INDEX raw_data_sofs2_ix4 ON public.raw_instrument_data_sofs2 USING btree (source_file_id, mooring_id COLLATE pg_catalog."default");

