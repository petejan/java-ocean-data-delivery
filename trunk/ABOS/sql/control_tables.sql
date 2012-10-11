begin work;

CREATE TABLE siteinfo
(
  site_id character(20) NOT NULL,
  site_name character(60),
  abn_acn character(20),
  addr1 character(60),
  addr2 character(60),
  addr3 character(60),
  phone character(20),
  fax character(20),
  email_address character(80),
  contact character(30),
  supervisor character(80),
  manager character(80),
  spool_path character(80),
  CONSTRAINT siteinfo_pkey PRIMARY KEY (site_id)
)
WITH (OIDS=FALSE);
ALTER TABLE siteinfo OWNER TO postgres;
CREATE TABLE staff
(
  staff_id character(10) NOT NULL,
  fam_name character(60),
  addr1 character(60),
  addr2 character(60),
  addr3 character(60),
  addr4 character(60),
  phone character(20),
  fax character(20),
  contact character(40),
  coll_title character(10),
  giv_name character(20),
  mobilephone character(20),
  pager character(20),
  email character(60),
  access_level integer,
  "password" character(20) NOT NULL,
  dbms_login character(20) NOT NULL,
  display_code character(1),
  notes character varying(1024),
  CONSTRAINT staff_pkey PRIMARY KEY (staff_id),
  CONSTRAINT staff_dbms_login_key UNIQUE (dbms_login)
)
WITH (OIDS=FALSE);
ALTER TABLE staff OWNER TO postgres;

-- Index: staff_pk

-- DROP INDEX staff_pk;

CREATE UNIQUE INDEX staff_pk
  ON staff
  USING btree
  (staff_id);


CREATE TABLE system_params
(
  param_code character(40) NOT NULL,
  description character varying(255) NOT NULL,
  data_type character(10) NOT NULL,
  data_value character(120) NOT NULL,
  CONSTRAINT system_params_pkey PRIMARY KEY (param_code)
)
WITH (OIDS=FALSE);
ALTER TABLE system_params OWNER TO postgres;
CREATE TABLE organisation
(
  code character(20) NOT NULL,
  description character(80),
  display_code character(1) DEFAULT 'Y'::bpchar,
  CONSTRAINT organisation_pkey PRIMARY KEY (code)
)
WITH (OIDS=FALSE);
ALTER TABLE organisation OWNER TO postgres;


CREATE TABLE nnsmenuheader
(
  menu_code character(20) NOT NULL,
  short_description character(60) NOT NULL,
  full_description character(255),
  sort_order integer NOT NULL,
  access_level integer NOT NULL,
  CONSTRAINT nnsmenuheader_pkey PRIMARY KEY (menu_code)
)
WITH (OIDS=FALSE);
ALTER TABLE nnsmenuheader OWNER TO postgres;
CREATE TABLE nnsmenuitem
(
  menu_code character(20) NOT NULL,
  action_code character(20) NOT NULL,
  short_description character(60),
  full_description character(255),
  class_name character(255) NOT NULL,
  sort_order integer NOT NULL,
  access_level integer NOT NULL,
  CONSTRAINT nnsmenuitem_pkey PRIMARY KEY (action_code),
  CONSTRAINT nnsmenuitem_menu_code_fkey FOREIGN KEY (menu_code)
      REFERENCES nnsmenuheader (menu_code) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE nnsmenuitem OWNER TO postgres;
CREATE TABLE nnsmenuparam
(
  action_code character(20) NOT NULL,
  param_code character(20) NOT NULL,
  param_value character(20) NOT NULL,
  CONSTRAINT nnsmenuparam_pkey PRIMARY KEY (action_code, param_code),
  CONSTRAINT nnsmenuparam_action_code_fkey FOREIGN KEY (action_code)
      REFERENCES nnsmenuitem (action_code) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE nnsmenuparam OWNER TO postgres;
CREATE TABLE manufacturer
(
  code character(20) NOT NULL,
  description character(80),
  display_code character(1) DEFAULT 'Y'::bpchar,
  CONSTRAINT manufacturer_pkey PRIMARY KEY (code)
)
WITH (OIDS=FALSE);
ALTER TABLE manufacturer OWNER TO postgres;
CREATE TABLE form_location
(
  program_code character(120) NOT NULL,
  ip_address character(60) NOT NULL,
  logon_id character(20) NOT NULL,
  x_coord integer NOT NULL,
  y_coord integer NOT NULL,
  frame_height integer NOT NULL,
  frame_width integer NOT NULL,
  CONSTRAINT form_location_pkey PRIMARY KEY (program_code, ip_address, logon_id)
)
WITH (OIDS=FALSE);
ALTER TABLE form_location OWNER TO postgres;


commit work;