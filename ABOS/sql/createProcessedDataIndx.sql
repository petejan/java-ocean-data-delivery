CREATE INDEX procesed_data_ix0
  ON public.processed_instrument_data
  USING btree
  (source_file_id);

-- Index: public.procesed_data_ix1

-- DROP INDEX public.procesed_data_ix1;

CREATE INDEX procesed_data_ix1
  ON public.processed_instrument_data
  USING btree
  (instrument_id);

-- Index: public.procesed_data_ix2

-- DROP INDEX public.procesed_data_ix2;

CREATE INDEX procesed_data_ix2
  ON public.processed_instrument_data
  USING btree
  (mooring_id COLLATE pg_catalog."default");

-- Index: public.procesed_data_ix3

-- DROP INDEX public.procesed_data_ix3;

CREATE INDEX procesed_data_ix3
  ON public.processed_instrument_data
  USING btree
  (parameter_code COLLATE pg_catalog."default", data_timestamp);

-- Index: public.procesed_data_ix4

-- DROP INDEX public.procesed_data_ix4;

CREATE INDEX procesed_data_ix4
  ON public.processed_instrument_data
  USING btree
  (source_file_id, mooring_id COLLATE pg_catalog."default");

CREATE INDEX procesed_data_ix5 ON  public.processed_instrument_data (DATE(data_timestamp AT TIME ZONE 'UTC'));
