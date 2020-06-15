DROP TRIGGER raw_measurement_trigger ON public.raw_instrument_data;

DROP FUNCTION public.raw_insert_trigger();

CREATE OR REPLACE FUNCTION public.raw_insert_trigger() RETURNS trigger AS
$$
BEGIN
    IF (NEW.mooring_id = 'SOFS-2-2011') THEN
	INSERT INTO raw_instrument_data_sofs2 VALUES (NEW.*);
    ELSEIF (NEW.mooring_id = 'SAZ47-15-2012') THEN
	INSERT INTO raw_instrument_data_saz47 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'mooring_id out of range, fix the raw_insert_trigger() function!';
    END IF;
    RETURN NULL;
END;
$$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE TRIGGER raw_measurement_trigger BEFORE INSERT ON public.raw_instrument_data FOR EACH ROW EXECUTE PROCEDURE public.raw_insert_trigger();
