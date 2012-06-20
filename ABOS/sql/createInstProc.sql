-- Table: instrument_data_processors

-- DROP TABLE instrument_data_processors;

CREATE TABLE instrument_data_processors
(
	  mooring_id character(20) NOT NULL,
	  class_name character varying(255) NOT NULL,
	  parameters character varying(255),
	  processing_date timestamp without time zone,
	  display_code character(10) DEFAULT 'Y'::bpchar,
	  CONSTRAINT instrument_data_processors_pkey PRIMARY KEY (class_name ),
	  CONSTRAINT instrument_data_processors_mooring_id_fkey FOREIGN KEY (mooring_id)
	      REFERENCES mooring (mooring_id) MATCH SIMPLE
	      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
	  OIDS=FALSE
);
ALTER TABLE instrument_data_processors
  OWNER TO postgres;

