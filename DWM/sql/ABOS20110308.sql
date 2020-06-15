--
-- PostgreSQL database dump
--

-- Started on 2012-03-08 11:42:03 EST

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 579 (class 2612 OID 16389)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

--
-- TOC entry 465 (class 1247 OID 16585)
-- Dependencies: 6 1770
-- Name: breakpoint; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE breakpoint AS (
	func oid,
	linenumber integer,
	targetname text
);


ALTER TYPE public.breakpoint OWNER TO postgres;

--
-- TOC entry 419 (class 0 OID 0)
-- Name: cube; Type: SHELL TYPE; Schema: public; Owner: postgres
--

CREATE TYPE cube;


--
-- TOC entry 29 (class 1255 OID 16399)
-- Dependencies: 6 419
-- Name: cube_in(cstring); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_in(cstring) RETURNS cube
    AS '$libdir/cube', 'cube_in'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_in(cstring) OWNER TO postgres;

--
-- TOC entry 32 (class 1255 OID 16402)
-- Dependencies: 6 419
-- Name: cube_out(cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_out(cube) RETURNS cstring
    AS '$libdir/cube', 'cube_out'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_out(cube) OWNER TO postgres;

--
-- TOC entry 418 (class 1247 OID 16398)
-- Dependencies: 32 29 6
-- Name: cube; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE cube (
    INTERNALLENGTH = variable,
    INPUT = cube_in,
    OUTPUT = cube_out,
    ALIGNMENT = double,
    STORAGE = plain
);


ALTER TYPE public.cube OWNER TO postgres;

--
-- TOC entry 2234 (class 0 OID 0)
-- Dependencies: 418
-- Name: TYPE cube; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE cube IS 'multi-dimensional cube ''(FLOAT-1, FLOAT-2, ..., FLOAT-N), (FLOAT-1, FLOAT-2, ..., FLOAT-N)''';


--
-- TOC entry 421 (class 1247 OID 16497)
-- Dependencies: 6 1766
-- Name: dblink_pkey_results; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE dblink_pkey_results AS (
	"position" integer,
	colname text
);


ALTER TYPE public.dblink_pkey_results OWNER TO postgres;

--
-- TOC entry 467 (class 1247 OID 16588)
-- Dependencies: 6 1771
-- Name: frame; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE frame AS (
	level integer,
	targetname text,
	func oid,
	linenumber integer,
	args text
);


ALTER TYPE public.frame OWNER TO postgres;

--
-- TOC entry 473 (class 1247 OID 16597)
-- Dependencies: 6 1774
-- Name: proxyinfo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE proxyinfo AS (
	serverversionstr text,
	serverversionnum integer,
	proxyapiver integer,
	serverprocessid integer
);


ALTER TYPE public.proxyinfo OWNER TO postgres;

--
-- TOC entry 469 (class 1247 OID 16591)
-- Dependencies: 6 1772
-- Name: targetinfo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE targetinfo AS (
	target oid,
	schema oid,
	nargs integer,
	argtypes oidvector,
	targetname name,
	argmodes "char"[],
	argnames text[],
	targetlang oid,
	fqname text,
	returnsset boolean,
	returntype oid
);


ALTER TYPE public.targetinfo OWNER TO postgres;

--
-- TOC entry 471 (class 1247 OID 16594)
-- Dependencies: 6 1773
-- Name: var; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE var AS (
	name text,
	varclass character(1),
	linenumber integer,
	isunique boolean,
	isconst boolean,
	isnotnull boolean,
	dtype oid,
	value text
);


ALTER TYPE public.var OWNER TO postgres;

--
-- TOC entry 135 (class 1255 OID 16556)
-- Dependencies: 6
-- Name: armor(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION armor(bytea) RETURNS text
    AS '$libdir/pgcrypto', 'pg_armor'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.armor(bytea) OWNER TO postgres;

--
-- TOC entry 180 (class 1255 OID 4649116)
-- Dependencies: 579 6
-- Name: cmdfindtopdeploymentid(integer, integer, date); Type: FUNCTION; Schema: public; Owner: csiro
--

CREATE FUNCTION cmdfindtopdeploymentid(integer, integer, date) RETURNS integer
    AS $_$ 

DECLARE 

-- Declare aliases for user input.
childId ALIAS FOR $1; 
deployId ALIAS FOR $2; 
onBeforeDate ALIAS FOR $3; 

-- Declare a variable to hold the returned record & deployment ID number.
itemLink RECORD; 
deploymentId INTEGER; 

BEGIN 

  -- Find the Parent of the given Child if they exist
  SELECT INTO itemLink CMDIL_ParentIDid, CMDIL_ChildIDid, CMDIL_DDid 
    FROM CMDItemLink 
    WHERE CMDIL_ChildIDid = childId and CMDILdate = (SELECT max(cmdildate) 
                                                     FROM CMDItemLink 
                                                     WHERE CMDIL_ChildIDid = childId and CMDILdate <= onBeforeDate); 

  -- if no record is returned or parent and child are the same then return passed in DeploymentId 
  IF itemLink.CMDIL_ParentIDid is null OR (itemLink.CMDIL_ParentIDid = itemLink.CMDIL_ChildIDid) THEN 
    deploymentId := itemLink.CMDIL_DDid;
  ELSE 
    -- if a parent exists for the given child look to see if this parent has a parent
    IF itemLink.CMDIL_ParentIDid <> childId THEN 
      deploymentId := cmdfindtopdeploymentid(itemLink.CMDIL_ParentIDid, itemLink.CMDIL_DDid, onBeforeDate); 
    END IF; 
  END IF; 

  RETURN deploymentId; 

END; 

$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.cmdfindtopdeploymentid(integer, integer, date) OWNER TO csiro;

--
-- TOC entry 181 (class 1255 OID 4649117)
-- Dependencies: 579 6
-- Name: cmdfindtopparentid(integer, date); Type: FUNCTION; Schema: public; Owner: csiro
--

CREATE FUNCTION cmdfindtopparentid(integer, date) RETURNS integer
    AS $_$ 

DECLARE 

-- Declare aliases for user input.
childId ALIAS FOR $1; 
onBeforeDate ALIAS FOR $2; 

-- Declare a variable to hold the parent ID number.
parentId INTEGER; 

BEGIN 

  -- Find the Parent of the given Child if they exist
  SELECT INTO parentId CMDIL_ParentIDid 
    FROM CMDItemLink 
    WHERE CMDIL_ChildIDid = childId and CMDILdate = (SELECT max(cmdildate) 
                                                     FROM CMDItemLink 
                                                     WHERE CMDIL_ChildIDid = childId and CMDILdate <= onBeforeDate); 

  IF parentId <> childId and parentId > 0 THEN 
    -- if a parent exists for the given child look to see if this parent has a parent
    parentId := CMDfindTopParentId(ParentId, $2); 
  ELSE 
    -- otherwise just return the child id as being the top parent
    parentId = childId; 
  END IF; 

  RETURN parentId; 

END; 

$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.cmdfindtopparentid(integer, date) OWNER TO csiro;

--
-- TOC entry 108 (class 1255 OID 16529)
-- Dependencies: 6
-- Name: crypt(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION crypt(text, text) RETURNS text
    AS '$libdir/pgcrypto', 'pg_crypt'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.crypt(text, text) OWNER TO postgres;

--
-- TOC entry 30 (class 1255 OID 16400)
-- Dependencies: 418 6
-- Name: cube(double precision[], double precision[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube(double precision[], double precision[]) RETURNS cube
    AS '$libdir/cube', 'cube_a_f8_f8'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube(double precision[], double precision[]) OWNER TO postgres;

--
-- TOC entry 31 (class 1255 OID 16401)
-- Dependencies: 418 6
-- Name: cube(double precision[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube(double precision[]) RETURNS cube
    AS '$libdir/cube', 'cube_a_f8'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube(double precision[]) OWNER TO postgres;

--
-- TOC entry 51 (class 1255 OID 16422)
-- Dependencies: 6 418
-- Name: cube(double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube(double precision) RETURNS cube
    AS '$libdir/cube', 'cube_f8'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube(double precision) OWNER TO postgres;

--
-- TOC entry 52 (class 1255 OID 16423)
-- Dependencies: 6 418
-- Name: cube(double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube(double precision, double precision) RETURNS cube
    AS '$libdir/cube', 'cube_f8_f8'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube(double precision, double precision) OWNER TO postgres;

--
-- TOC entry 53 (class 1255 OID 16424)
-- Dependencies: 418 6 418
-- Name: cube(cube, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube(cube, double precision) RETURNS cube
    AS '$libdir/cube', 'cube_c_f8'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube(cube, double precision) OWNER TO postgres;

--
-- TOC entry 54 (class 1255 OID 16425)
-- Dependencies: 418 6 418
-- Name: cube(cube, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube(cube, double precision, double precision) RETURNS cube
    AS '$libdir/cube', 'cube_c_f8_f8'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube(cube, double precision, double precision) OWNER TO postgres;

--
-- TOC entry 39 (class 1255 OID 16410)
-- Dependencies: 418 418 6
-- Name: cube_cmp(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_cmp(cube, cube) RETURNS integer
    AS '$libdir/cube', 'cube_cmp'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_cmp(cube, cube) OWNER TO postgres;

--
-- TOC entry 2235 (class 0 OID 0)
-- Dependencies: 39
-- Name: FUNCTION cube_cmp(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_cmp(cube, cube) IS 'btree comparison function';


--
-- TOC entry 41 (class 1255 OID 16412)
-- Dependencies: 418 418 6
-- Name: cube_contained(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_contained(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_contained'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_contained(cube, cube) OWNER TO postgres;

--
-- TOC entry 2236 (class 0 OID 0)
-- Dependencies: 41
-- Name: FUNCTION cube_contained(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_contained(cube, cube) IS 'contained in';


--
-- TOC entry 40 (class 1255 OID 16411)
-- Dependencies: 418 6 418
-- Name: cube_contains(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_contains(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_contains'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_contains(cube, cube) OWNER TO postgres;

--
-- TOC entry 2237 (class 0 OID 0)
-- Dependencies: 40
-- Name: FUNCTION cube_contains(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_contains(cube, cube) IS 'contains';


--
-- TOC entry 48 (class 1255 OID 16419)
-- Dependencies: 6 418
-- Name: cube_dim(cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_dim(cube) RETURNS integer
    AS '$libdir/cube', 'cube_dim'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_dim(cube) OWNER TO postgres;

--
-- TOC entry 47 (class 1255 OID 16418)
-- Dependencies: 418 6 418
-- Name: cube_distance(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_distance(cube, cube) RETURNS double precision
    AS '$libdir/cube', 'cube_distance'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_distance(cube, cube) OWNER TO postgres;

--
-- TOC entry 56 (class 1255 OID 16427)
-- Dependencies: 418 418 6
-- Name: cube_enlarge(cube, double precision, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_enlarge(cube, double precision, integer) RETURNS cube
    AS '$libdir/cube', 'cube_enlarge'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_enlarge(cube, double precision, integer) OWNER TO postgres;

--
-- TOC entry 33 (class 1255 OID 16404)
-- Dependencies: 418 6 418
-- Name: cube_eq(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_eq(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_eq'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_eq(cube, cube) OWNER TO postgres;

--
-- TOC entry 2238 (class 0 OID 0)
-- Dependencies: 33
-- Name: FUNCTION cube_eq(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_eq(cube, cube) IS 'same as';


--
-- TOC entry 38 (class 1255 OID 16409)
-- Dependencies: 6 418 418
-- Name: cube_ge(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_ge(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_ge'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_ge(cube, cube) OWNER TO postgres;

--
-- TOC entry 2239 (class 0 OID 0)
-- Dependencies: 38
-- Name: FUNCTION cube_ge(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_ge(cube, cube) IS 'greater than or equal to';


--
-- TOC entry 36 (class 1255 OID 16407)
-- Dependencies: 418 418 6
-- Name: cube_gt(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_gt(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_gt'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_gt(cube, cube) OWNER TO postgres;

--
-- TOC entry 2240 (class 0 OID 0)
-- Dependencies: 36
-- Name: FUNCTION cube_gt(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_gt(cube, cube) IS 'greater than';


--
-- TOC entry 44 (class 1255 OID 16415)
-- Dependencies: 6 418 418 418
-- Name: cube_inter(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_inter(cube, cube) RETURNS cube
    AS '$libdir/cube', 'cube_inter'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_inter(cube, cube) OWNER TO postgres;

--
-- TOC entry 55 (class 1255 OID 16426)
-- Dependencies: 418 6
-- Name: cube_is_point(cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_is_point(cube) RETURNS boolean
    AS '$libdir/cube', 'cube_is_point'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_is_point(cube) OWNER TO postgres;

--
-- TOC entry 37 (class 1255 OID 16408)
-- Dependencies: 418 418 6
-- Name: cube_le(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_le(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_le'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_le(cube, cube) OWNER TO postgres;

--
-- TOC entry 2241 (class 0 OID 0)
-- Dependencies: 37
-- Name: FUNCTION cube_le(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_le(cube, cube) IS 'lower than or equal to';


--
-- TOC entry 49 (class 1255 OID 16420)
-- Dependencies: 6 418
-- Name: cube_ll_coord(cube, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_ll_coord(cube, integer) RETURNS double precision
    AS '$libdir/cube', 'cube_ll_coord'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_ll_coord(cube, integer) OWNER TO postgres;

--
-- TOC entry 35 (class 1255 OID 16406)
-- Dependencies: 418 6 418
-- Name: cube_lt(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_lt(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_lt'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_lt(cube, cube) OWNER TO postgres;

--
-- TOC entry 2242 (class 0 OID 0)
-- Dependencies: 35
-- Name: FUNCTION cube_lt(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_lt(cube, cube) IS 'lower than';


--
-- TOC entry 34 (class 1255 OID 16405)
-- Dependencies: 418 6 418
-- Name: cube_ne(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_ne(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_ne'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_ne(cube, cube) OWNER TO postgres;

--
-- TOC entry 2243 (class 0 OID 0)
-- Dependencies: 34
-- Name: FUNCTION cube_ne(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_ne(cube, cube) IS 'different';


--
-- TOC entry 42 (class 1255 OID 16413)
-- Dependencies: 6 418 418
-- Name: cube_overlap(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_overlap(cube, cube) RETURNS boolean
    AS '$libdir/cube', 'cube_overlap'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_overlap(cube, cube) OWNER TO postgres;

--
-- TOC entry 2244 (class 0 OID 0)
-- Dependencies: 42
-- Name: FUNCTION cube_overlap(cube, cube); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION cube_overlap(cube, cube) IS 'overlaps';


--
-- TOC entry 45 (class 1255 OID 16416)
-- Dependencies: 418 6
-- Name: cube_size(cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_size(cube) RETURNS double precision
    AS '$libdir/cube', 'cube_size'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_size(cube) OWNER TO postgres;

--
-- TOC entry 46 (class 1255 OID 16417)
-- Dependencies: 6 418 418
-- Name: cube_subset(cube, integer[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_subset(cube, integer[]) RETURNS cube
    AS '$libdir/cube', 'cube_subset'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_subset(cube, integer[]) OWNER TO postgres;

--
-- TOC entry 43 (class 1255 OID 16414)
-- Dependencies: 418 6 418 418
-- Name: cube_union(cube, cube); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_union(cube, cube) RETURNS cube
    AS '$libdir/cube', 'cube_union'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_union(cube, cube) OWNER TO postgres;

--
-- TOC entry 50 (class 1255 OID 16421)
-- Dependencies: 6 418
-- Name: cube_ur_coord(cube, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION cube_ur_coord(cube, integer) RETURNS double precision
    AS '$libdir/cube', 'cube_ur_coord'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.cube_ur_coord(cube, integer) OWNER TO postgres;

--
-- TOC entry 81 (class 1255 OID 16487)
-- Dependencies: 6
-- Name: dblink(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink(text, text) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_record'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink(text, text) OWNER TO postgres;

--
-- TOC entry 82 (class 1255 OID 16488)
-- Dependencies: 6
-- Name: dblink(text, text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink(text, text, boolean) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_record'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink(text, text, boolean) OWNER TO postgres;

--
-- TOC entry 83 (class 1255 OID 16489)
-- Dependencies: 6
-- Name: dblink(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink(text) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_record'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink(text) OWNER TO postgres;

--
-- TOC entry 84 (class 1255 OID 16490)
-- Dependencies: 6
-- Name: dblink(text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink(text, boolean) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_record'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink(text, boolean) OWNER TO postgres;

--
-- TOC entry 91 (class 1255 OID 16500)
-- Dependencies: 6
-- Name: dblink_build_sql_delete(text, int2vector, integer, text[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_build_sql_delete(text, int2vector, integer, text[]) RETURNS text
    AS '$libdir/dblink', 'dblink_build_sql_delete'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_build_sql_delete(text, int2vector, integer, text[]) OWNER TO postgres;

--
-- TOC entry 90 (class 1255 OID 16499)
-- Dependencies: 6
-- Name: dblink_build_sql_insert(text, int2vector, integer, text[], text[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_build_sql_insert(text, int2vector, integer, text[], text[]) RETURNS text
    AS '$libdir/dblink', 'dblink_build_sql_insert'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_build_sql_insert(text, int2vector, integer, text[], text[]) OWNER TO postgres;

--
-- TOC entry 92 (class 1255 OID 16501)
-- Dependencies: 6
-- Name: dblink_build_sql_update(text, int2vector, integer, text[], text[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_build_sql_update(text, int2vector, integer, text[], text[]) RETURNS text
    AS '$libdir/dblink', 'dblink_build_sql_update'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_build_sql_update(text, int2vector, integer, text[], text[]) OWNER TO postgres;

--
-- TOC entry 99 (class 1255 OID 16508)
-- Dependencies: 6
-- Name: dblink_cancel_query(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_cancel_query(text) RETURNS text
    AS '$libdir/dblink', 'dblink_cancel_query'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_cancel_query(text) OWNER TO postgres;

--
-- TOC entry 77 (class 1255 OID 16483)
-- Dependencies: 6
-- Name: dblink_close(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_close(text) RETURNS text
    AS '$libdir/dblink', 'dblink_close'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_close(text) OWNER TO postgres;

--
-- TOC entry 78 (class 1255 OID 16484)
-- Dependencies: 6
-- Name: dblink_close(text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_close(text, boolean) RETURNS text
    AS '$libdir/dblink', 'dblink_close'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_close(text, boolean) OWNER TO postgres;

--
-- TOC entry 79 (class 1255 OID 16485)
-- Dependencies: 6
-- Name: dblink_close(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_close(text, text) RETURNS text
    AS '$libdir/dblink', 'dblink_close'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_close(text, text) OWNER TO postgres;

--
-- TOC entry 80 (class 1255 OID 16486)
-- Dependencies: 6
-- Name: dblink_close(text, text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_close(text, text, boolean) RETURNS text
    AS '$libdir/dblink', 'dblink_close'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_close(text, text, boolean) OWNER TO postgres;

--
-- TOC entry 15 (class 1255 OID 16469)
-- Dependencies: 6
-- Name: dblink_connect(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_connect(text) RETURNS text
    AS '$libdir/dblink', 'dblink_connect'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_connect(text) OWNER TO postgres;

--
-- TOC entry 64 (class 1255 OID 16470)
-- Dependencies: 6
-- Name: dblink_connect(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_connect(text, text) RETURNS text
    AS '$libdir/dblink', 'dblink_connect'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_connect(text, text) OWNER TO postgres;

--
-- TOC entry 66 (class 1255 OID 16471)
-- Dependencies: 6
-- Name: dblink_connect_u(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_connect_u(text) RETURNS text
    AS '$libdir/dblink', 'dblink_connect'
    LANGUAGE c STRICT SECURITY DEFINER;


ALTER FUNCTION public.dblink_connect_u(text) OWNER TO postgres;

--
-- TOC entry 65 (class 1255 OID 16472)
-- Dependencies: 6
-- Name: dblink_connect_u(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_connect_u(text, text) RETURNS text
    AS '$libdir/dblink', 'dblink_connect'
    LANGUAGE c STRICT SECURITY DEFINER;


ALTER FUNCTION public.dblink_connect_u(text, text) OWNER TO postgres;

--
-- TOC entry 93 (class 1255 OID 16502)
-- Dependencies: 6
-- Name: dblink_current_query(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_current_query() RETURNS text
    AS '$libdir/dblink', 'dblink_current_query'
    LANGUAGE c;


ALTER FUNCTION public.dblink_current_query() OWNER TO postgres;

--
-- TOC entry 67 (class 1255 OID 16473)
-- Dependencies: 6
-- Name: dblink_disconnect(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_disconnect() RETURNS text
    AS '$libdir/dblink', 'dblink_disconnect'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_disconnect() OWNER TO postgres;

--
-- TOC entry 68 (class 1255 OID 16474)
-- Dependencies: 6
-- Name: dblink_disconnect(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_disconnect(text) RETURNS text
    AS '$libdir/dblink', 'dblink_disconnect'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_disconnect(text) OWNER TO postgres;

--
-- TOC entry 100 (class 1255 OID 16509)
-- Dependencies: 6
-- Name: dblink_error_message(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_error_message(text) RETURNS text
    AS '$libdir/dblink', 'dblink_error_message'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_error_message(text) OWNER TO postgres;

--
-- TOC entry 85 (class 1255 OID 16491)
-- Dependencies: 6
-- Name: dblink_exec(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_exec(text, text) RETURNS text
    AS '$libdir/dblink', 'dblink_exec'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_exec(text, text) OWNER TO postgres;

--
-- TOC entry 86 (class 1255 OID 16492)
-- Dependencies: 6
-- Name: dblink_exec(text, text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_exec(text, text, boolean) RETURNS text
    AS '$libdir/dblink', 'dblink_exec'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_exec(text, text, boolean) OWNER TO postgres;

--
-- TOC entry 87 (class 1255 OID 16493)
-- Dependencies: 6
-- Name: dblink_exec(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_exec(text) RETURNS text
    AS '$libdir/dblink', 'dblink_exec'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_exec(text) OWNER TO postgres;

--
-- TOC entry 88 (class 1255 OID 16494)
-- Dependencies: 6
-- Name: dblink_exec(text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_exec(text, boolean) RETURNS text
    AS '$libdir/dblink', 'dblink_exec'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_exec(text, boolean) OWNER TO postgres;

--
-- TOC entry 73 (class 1255 OID 16479)
-- Dependencies: 6
-- Name: dblink_fetch(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_fetch(text, integer) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_fetch'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_fetch(text, integer) OWNER TO postgres;

--
-- TOC entry 74 (class 1255 OID 16480)
-- Dependencies: 6
-- Name: dblink_fetch(text, integer, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_fetch(text, integer, boolean) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_fetch'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_fetch(text, integer, boolean) OWNER TO postgres;

--
-- TOC entry 75 (class 1255 OID 16481)
-- Dependencies: 6
-- Name: dblink_fetch(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_fetch(text, text, integer) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_fetch'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_fetch(text, text, integer) OWNER TO postgres;

--
-- TOC entry 76 (class 1255 OID 16482)
-- Dependencies: 6
-- Name: dblink_fetch(text, text, integer, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_fetch(text, text, integer, boolean) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_fetch'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_fetch(text, text, integer, boolean) OWNER TO postgres;

--
-- TOC entry 98 (class 1255 OID 16507)
-- Dependencies: 6
-- Name: dblink_get_connections(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_get_connections() RETURNS text[]
    AS '$libdir/dblink', 'dblink_get_connections'
    LANGUAGE c;


ALTER FUNCTION public.dblink_get_connections() OWNER TO postgres;

--
-- TOC entry 89 (class 1255 OID 16498)
-- Dependencies: 6 421
-- Name: dblink_get_pkey(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_get_pkey(text) RETURNS SETOF dblink_pkey_results
    AS '$libdir/dblink', 'dblink_get_pkey'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_get_pkey(text) OWNER TO postgres;

--
-- TOC entry 96 (class 1255 OID 16505)
-- Dependencies: 6
-- Name: dblink_get_result(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_get_result(text) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_get_result'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_get_result(text) OWNER TO postgres;

--
-- TOC entry 97 (class 1255 OID 16506)
-- Dependencies: 6
-- Name: dblink_get_result(text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_get_result(text, boolean) RETURNS SETOF record
    AS '$libdir/dblink', 'dblink_get_result'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_get_result(text, boolean) OWNER TO postgres;

--
-- TOC entry 95 (class 1255 OID 16504)
-- Dependencies: 6
-- Name: dblink_is_busy(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_is_busy(text) RETURNS integer
    AS '$libdir/dblink', 'dblink_is_busy'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_is_busy(text) OWNER TO postgres;

--
-- TOC entry 69 (class 1255 OID 16475)
-- Dependencies: 6
-- Name: dblink_open(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_open(text, text) RETURNS text
    AS '$libdir/dblink', 'dblink_open'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_open(text, text) OWNER TO postgres;

--
-- TOC entry 70 (class 1255 OID 16476)
-- Dependencies: 6
-- Name: dblink_open(text, text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_open(text, text, boolean) RETURNS text
    AS '$libdir/dblink', 'dblink_open'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_open(text, text, boolean) OWNER TO postgres;

--
-- TOC entry 71 (class 1255 OID 16477)
-- Dependencies: 6
-- Name: dblink_open(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_open(text, text, text) RETURNS text
    AS '$libdir/dblink', 'dblink_open'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_open(text, text, text) OWNER TO postgres;

--
-- TOC entry 72 (class 1255 OID 16478)
-- Dependencies: 6
-- Name: dblink_open(text, text, text, boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_open(text, text, text, boolean) RETURNS text
    AS '$libdir/dblink', 'dblink_open'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_open(text, text, text, boolean) OWNER TO postgres;

--
-- TOC entry 94 (class 1255 OID 16503)
-- Dependencies: 6
-- Name: dblink_send_query(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dblink_send_query(text, text) RETURNS integer
    AS '$libdir/dblink', 'dblink_send_query'
    LANGUAGE c STRICT;


ALTER FUNCTION public.dblink_send_query(text, text) OWNER TO postgres;

--
-- TOC entry 136 (class 1255 OID 16557)
-- Dependencies: 6
-- Name: dearmor(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dearmor(text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_dearmor'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.dearmor(text) OWNER TO postgres;

--
-- TOC entry 112 (class 1255 OID 16533)
-- Dependencies: 6
-- Name: decrypt(bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION decrypt(bytea, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_decrypt'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.decrypt(bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 114 (class 1255 OID 16535)
-- Dependencies: 6
-- Name: decrypt_iv(bytea, bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION decrypt_iv(bytea, bytea, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_decrypt_iv'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.decrypt_iv(bytea, bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 104 (class 1255 OID 16525)
-- Dependencies: 6
-- Name: digest(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION digest(text, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_digest'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.digest(text, text) OWNER TO postgres;

--
-- TOC entry 105 (class 1255 OID 16526)
-- Dependencies: 6
-- Name: digest(bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION digest(bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_digest'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.digest(bytea, text) OWNER TO postgres;

--
-- TOC entry 111 (class 1255 OID 16532)
-- Dependencies: 6
-- Name: encrypt(bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION encrypt(bytea, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_encrypt'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.encrypt(bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 113 (class 1255 OID 16534)
-- Dependencies: 6
-- Name: encrypt_iv(bytea, bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION encrypt_iv(bytea, bytea, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_encrypt_iv'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.encrypt_iv(bytea, bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 58 (class 1255 OID 16440)
-- Dependencies: 6
-- Name: g_cube_compress(internal); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION g_cube_compress(internal) RETURNS internal
    AS '$libdir/cube', 'g_cube_compress'
    LANGUAGE c IMMUTABLE;


ALTER FUNCTION public.g_cube_compress(internal) OWNER TO postgres;

--
-- TOC entry 57 (class 1255 OID 16439)
-- Dependencies: 418 6
-- Name: g_cube_consistent(internal, cube, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION g_cube_consistent(internal, cube, integer) RETURNS boolean
    AS '$libdir/cube', 'g_cube_consistent'
    LANGUAGE c IMMUTABLE;


ALTER FUNCTION public.g_cube_consistent(internal, cube, integer) OWNER TO postgres;

--
-- TOC entry 59 (class 1255 OID 16441)
-- Dependencies: 6
-- Name: g_cube_decompress(internal); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION g_cube_decompress(internal) RETURNS internal
    AS '$libdir/cube', 'g_cube_decompress'
    LANGUAGE c IMMUTABLE;


ALTER FUNCTION public.g_cube_decompress(internal) OWNER TO postgres;

--
-- TOC entry 60 (class 1255 OID 16442)
-- Dependencies: 6
-- Name: g_cube_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION g_cube_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/cube', 'g_cube_penalty'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.g_cube_penalty(internal, internal, internal) OWNER TO postgres;

--
-- TOC entry 61 (class 1255 OID 16443)
-- Dependencies: 6
-- Name: g_cube_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION g_cube_picksplit(internal, internal) RETURNS internal
    AS '$libdir/cube', 'g_cube_picksplit'
    LANGUAGE c IMMUTABLE;


ALTER FUNCTION public.g_cube_picksplit(internal, internal) OWNER TO postgres;

--
-- TOC entry 63 (class 1255 OID 16445)
-- Dependencies: 418 6 418
-- Name: g_cube_same(cube, cube, internal); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION g_cube_same(cube, cube, internal) RETURNS internal
    AS '$libdir/cube', 'g_cube_same'
    LANGUAGE c IMMUTABLE;


ALTER FUNCTION public.g_cube_same(cube, cube, internal) OWNER TO postgres;

--
-- TOC entry 62 (class 1255 OID 16444)
-- Dependencies: 418 6
-- Name: g_cube_union(internal, internal); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION g_cube_union(internal, internal) RETURNS cube
    AS '$libdir/cube', 'g_cube_union'
    LANGUAGE c IMMUTABLE;


ALTER FUNCTION public.g_cube_union(internal, internal) OWNER TO postgres;

--
-- TOC entry 115 (class 1255 OID 16536)
-- Dependencies: 6
-- Name: gen_random_bytes(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION gen_random_bytes(integer) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_random_bytes'
    LANGUAGE c STRICT;


ALTER FUNCTION public.gen_random_bytes(integer) OWNER TO postgres;

--
-- TOC entry 109 (class 1255 OID 16530)
-- Dependencies: 6
-- Name: gen_salt(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION gen_salt(text) RETURNS text
    AS '$libdir/pgcrypto', 'pg_gen_salt'
    LANGUAGE c STRICT;


ALTER FUNCTION public.gen_salt(text) OWNER TO postgres;

--
-- TOC entry 110 (class 1255 OID 16531)
-- Dependencies: 6
-- Name: gen_salt(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION gen_salt(text, integer) RETURNS text
    AS '$libdir/pgcrypto', 'pg_gen_salt_rounds'
    LANGUAGE c STRICT;


ALTER FUNCTION public.gen_salt(text, integer) OWNER TO postgres;

--
-- TOC entry 106 (class 1255 OID 16527)
-- Dependencies: 6
-- Name: hmac(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION hmac(text, text, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_hmac'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.hmac(text, text, text) OWNER TO postgres;

--
-- TOC entry 107 (class 1255 OID 16528)
-- Dependencies: 6
-- Name: hmac(bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION hmac(bytea, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pg_hmac'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.hmac(bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 182 (class 1255 OID 4650876)
-- Dependencies: 6 579
-- Name: optode_data_selector(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION optode_data_selector() RETURNS refcursor
    AS $$
DECLARE 
mycurs refcursor; 
BEGIN

drop table if exists dphase;
drop table if exists optode_temp;
drop table if exists sbe16_temperature;
drop table if exists sbe16_pressure;
drop table if exists sbe16_conductivity;
drop table if exists set1;
drop table if exists set2;
drop table if exists set3;

create temp table dphase as
select distinct on (date_trunc('hour',data_timestamp)) 
date_trunc('hour',data_timestamp) as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and parameter_code = 'OPTODE_DPHASE'
;

create temp table optode_temp as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and parameter_code = 'OPTODE_TEMP'
;

create temp table sbe16_temperature as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'WATER_TEMP'
;


create temp table sbe16_pressure as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'WATER_PRESSURE'
;

create temp table sbe16_conductivity as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'CONDUCTIVITY'
;

create temp table set1 as
select 
	dphase.obs_time,
	dphase.depth,
	dphase.parameter_value as optode_dphase,
	optode_temp.parameter_value as optode_temperature
from dphase full join optode_temp on 
(
dphase.obs_time = optode_temp.obs_time
AND
dphase.depth = optode_temp.depth
)
;

create temp table set2 as
select 
	set1.obs_time,
	set1.depth,
	set1.optode_dphase,
	set1.optode_temperature,
	sbe16_temperature.parameter_value as sbe16_temperature
from set1 full join sbe16_temperature on 
(
set1.obs_time = sbe16_temperature.obs_time
AND
set1.depth = sbe16_temperature.depth
)
;

create temp table set3 as
select 
	set2.obs_time,
	set2.depth,
	set2.optode_dphase,
	set2.optode_temperature,
        set2.sbe16_temperature,
	sbe16_pressure.parameter_value as sbe16_pressure
from set2 full join sbe16_pressure on 
(
set2.obs_time = sbe16_pressure.obs_time
AND
set2.depth = sbe16_pressure.depth
)
;

create temp table set4 as
select 
	set3.obs_time,
	set3.depth,
	set3.optode_dphase,
	set3.optode_temperature,
        set3.sbe16_temperature,
	set3.sbe16_pressure,
	sbe16_conductivity.parameter_value as sbe16_conductivity
from set3 full join sbe16_conductivity on 
(
set3.obs_time = sbe16_conductivity.obs_time
AND
set3.depth = sbe16_conductivity.depth
)
;

OPEN mycurs FOR  select * from set4 order by obs_time;
RETURN mycurs; 
END;
$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.optode_data_selector() OWNER TO postgres;

--
-- TOC entry 101 (class 1255 OID 16510)
-- Dependencies: 6
-- Name: pg_buffercache_pages(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pg_buffercache_pages() RETURNS SETOF record
    AS '$libdir/pg_buffercache', 'pg_buffercache_pages'
    LANGUAGE c;


ALTER FUNCTION public.pg_buffercache_pages() OWNER TO postgres;

--
-- TOC entry 102 (class 1255 OID 16515)
-- Dependencies: 6
-- Name: pg_freespacemap_pages(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pg_freespacemap_pages() RETURNS SETOF record
    AS '$libdir/pg_freespacemap', 'pg_freespacemap_pages'
    LANGUAGE c;


ALTER FUNCTION public.pg_freespacemap_pages() OWNER TO postgres;

--
-- TOC entry 103 (class 1255 OID 16516)
-- Dependencies: 6
-- Name: pg_freespacemap_relations(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pg_freespacemap_relations() RETURNS SETOF record
    AS '$libdir/pg_freespacemap', 'pg_freespacemap_relations'
    LANGUAGE c;


ALTER FUNCTION public.pg_freespacemap_relations() OWNER TO postgres;

--
-- TOC entry 140 (class 1255 OID 16561)
-- Dependencies: 6
-- Name: pg_relpages(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pg_relpages(text) RETURNS integer
    AS '$libdir/pgstattuple', 'pg_relpages'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pg_relpages(text) OWNER TO postgres;

--
-- TOC entry 134 (class 1255 OID 16555)
-- Dependencies: 6
-- Name: pgp_key_id(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_key_id(bytea) RETURNS text
    AS '$libdir/pgcrypto', 'pgp_key_id_w'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_key_id(bytea) OWNER TO postgres;

--
-- TOC entry 128 (class 1255 OID 16549)
-- Dependencies: 6
-- Name: pgp_pub_decrypt(bytea, bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_decrypt(bytea, bytea) RETURNS text
    AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_pub_decrypt(bytea, bytea) OWNER TO postgres;

--
-- TOC entry 130 (class 1255 OID 16551)
-- Dependencies: 6
-- Name: pgp_pub_decrypt(bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_decrypt(bytea, bytea, text) RETURNS text
    AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_pub_decrypt(bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 132 (class 1255 OID 16553)
-- Dependencies: 6
-- Name: pgp_pub_decrypt(bytea, bytea, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_decrypt(bytea, bytea, text, text) RETURNS text
    AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text) OWNER TO postgres;

--
-- TOC entry 129 (class 1255 OID 16550)
-- Dependencies: 6
-- Name: pgp_pub_decrypt_bytea(bytea, bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_decrypt_bytea(bytea, bytea) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea) OWNER TO postgres;

--
-- TOC entry 131 (class 1255 OID 16552)
-- Dependencies: 6
-- Name: pgp_pub_decrypt_bytea(bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 133 (class 1255 OID 16554)
-- Dependencies: 6
-- Name: pgp_pub_decrypt_bytea(bytea, bytea, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text) OWNER TO postgres;

--
-- TOC entry 124 (class 1255 OID 16545)
-- Dependencies: 6
-- Name: pgp_pub_encrypt(text, bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_encrypt(text, bytea) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_pub_encrypt_text'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_pub_encrypt(text, bytea) OWNER TO postgres;

--
-- TOC entry 126 (class 1255 OID 16547)
-- Dependencies: 6
-- Name: pgp_pub_encrypt(text, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_encrypt(text, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_pub_encrypt_text'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_pub_encrypt(text, bytea, text) OWNER TO postgres;

--
-- TOC entry 125 (class 1255 OID 16546)
-- Dependencies: 6
-- Name: pgp_pub_encrypt_bytea(bytea, bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_encrypt_bytea(bytea, bytea) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_pub_encrypt_bytea'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea) OWNER TO postgres;

--
-- TOC entry 127 (class 1255 OID 16548)
-- Dependencies: 6
-- Name: pgp_pub_encrypt_bytea(bytea, bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_pub_encrypt_bytea(bytea, bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_pub_encrypt_bytea'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text) OWNER TO postgres;

--
-- TOC entry 120 (class 1255 OID 16541)
-- Dependencies: 6
-- Name: pgp_sym_decrypt(bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_decrypt(bytea, text) RETURNS text
    AS '$libdir/pgcrypto', 'pgp_sym_decrypt_text'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_sym_decrypt(bytea, text) OWNER TO postgres;

--
-- TOC entry 122 (class 1255 OID 16543)
-- Dependencies: 6
-- Name: pgp_sym_decrypt(bytea, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_decrypt(bytea, text, text) RETURNS text
    AS '$libdir/pgcrypto', 'pgp_sym_decrypt_text'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_sym_decrypt(bytea, text, text) OWNER TO postgres;

--
-- TOC entry 121 (class 1255 OID 16542)
-- Dependencies: 6
-- Name: pgp_sym_decrypt_bytea(bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_decrypt_bytea(bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_sym_decrypt_bytea'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_sym_decrypt_bytea(bytea, text) OWNER TO postgres;

--
-- TOC entry 123 (class 1255 OID 16544)
-- Dependencies: 6
-- Name: pgp_sym_decrypt_bytea(bytea, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_decrypt_bytea(bytea, text, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_sym_decrypt_bytea'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text) OWNER TO postgres;

--
-- TOC entry 116 (class 1255 OID 16537)
-- Dependencies: 6
-- Name: pgp_sym_encrypt(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_encrypt(text, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_sym_encrypt_text'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_sym_encrypt(text, text) OWNER TO postgres;

--
-- TOC entry 118 (class 1255 OID 16539)
-- Dependencies: 6
-- Name: pgp_sym_encrypt(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_encrypt(text, text, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_sym_encrypt_text'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_sym_encrypt(text, text, text) OWNER TO postgres;

--
-- TOC entry 117 (class 1255 OID 16538)
-- Dependencies: 6
-- Name: pgp_sym_encrypt_bytea(bytea, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_encrypt_bytea(bytea, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_sym_encrypt_bytea'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_sym_encrypt_bytea(bytea, text) OWNER TO postgres;

--
-- TOC entry 119 (class 1255 OID 16540)
-- Dependencies: 6
-- Name: pgp_sym_encrypt_bytea(bytea, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgp_sym_encrypt_bytea(bytea, text, text) RETURNS bytea
    AS '$libdir/pgcrypto', 'pgp_sym_encrypt_bytea'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text) OWNER TO postgres;

--
-- TOC entry 139 (class 1255 OID 16560)
-- Dependencies: 6
-- Name: pgstatindex(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgstatindex(relname text, OUT version integer, OUT tree_level integer, OUT index_size integer, OUT root_block_no integer, OUT internal_pages integer, OUT leaf_pages integer, OUT empty_pages integer, OUT deleted_pages integer, OUT avg_leaf_density double precision, OUT leaf_fragmentation double precision) RETURNS record
    AS '$libdir/pgstattuple', 'pgstatindex'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgstatindex(relname text, OUT version integer, OUT tree_level integer, OUT index_size integer, OUT root_block_no integer, OUT internal_pages integer, OUT leaf_pages integer, OUT empty_pages integer, OUT deleted_pages integer, OUT avg_leaf_density double precision, OUT leaf_fragmentation double precision) OWNER TO postgres;

--
-- TOC entry 137 (class 1255 OID 16558)
-- Dependencies: 6
-- Name: pgstattuple(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgstattuple(relname text, OUT table_len bigint, OUT tuple_count bigint, OUT tuple_len bigint, OUT tuple_percent double precision, OUT dead_tuple_count bigint, OUT dead_tuple_len bigint, OUT dead_tuple_percent double precision, OUT free_space bigint, OUT free_percent double precision) RETURNS record
    AS '$libdir/pgstattuple', 'pgstattuple'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgstattuple(relname text, OUT table_len bigint, OUT tuple_count bigint, OUT tuple_len bigint, OUT tuple_percent double precision, OUT dead_tuple_count bigint, OUT dead_tuple_len bigint, OUT dead_tuple_percent double precision, OUT free_space bigint, OUT free_percent double precision) OWNER TO postgres;

--
-- TOC entry 138 (class 1255 OID 16559)
-- Dependencies: 6
-- Name: pgstattuple(oid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pgstattuple(reloid oid, OUT table_len bigint, OUT tuple_count bigint, OUT tuple_len bigint, OUT tuple_percent double precision, OUT dead_tuple_count bigint, OUT dead_tuple_len bigint, OUT dead_tuple_percent double precision, OUT free_space bigint, OUT free_percent double precision) RETURNS record
    AS '$libdir/pgstattuple', 'pgstattuplebyid'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pgstattuple(reloid oid, OUT table_len bigint, OUT tuple_count bigint, OUT tuple_len bigint, OUT tuple_percent double precision, OUT dead_tuple_count bigint, OUT dead_tuple_len bigint, OUT dead_tuple_percent double precision, OUT free_space bigint, OUT free_percent double precision) OWNER TO postgres;

--
-- TOC entry 161 (class 1255 OID 16599)
-- Dependencies: 6
-- Name: pldbg_abort_target(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_abort_target(session integer) RETURNS SETOF boolean
    AS '$libdir/pldbgapi', 'pldbg_abort_target'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_abort_target(session integer) OWNER TO postgres;

--
-- TOC entry 162 (class 1255 OID 16600)
-- Dependencies: 6
-- Name: pldbg_attach_to_port(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_attach_to_port(portnumber integer) RETURNS integer
    AS '$libdir/pldbgapi', 'pldbg_attach_to_port'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_attach_to_port(portnumber integer) OWNER TO postgres;

--
-- TOC entry 163 (class 1255 OID 16601)
-- Dependencies: 6 465
-- Name: pldbg_continue(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_continue(session integer) RETURNS breakpoint
    AS '$libdir/pldbgapi', 'pldbg_continue'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_continue(session integer) OWNER TO postgres;

--
-- TOC entry 164 (class 1255 OID 16602)
-- Dependencies: 6
-- Name: pldbg_create_listener(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_create_listener() RETURNS integer
    AS '$libdir/pldbgapi', 'pldbg_create_listener'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_create_listener() OWNER TO postgres;

--
-- TOC entry 165 (class 1255 OID 16603)
-- Dependencies: 6
-- Name: pldbg_deposit_value(integer, text, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_deposit_value(session integer, varname text, linenumber integer, value text) RETURNS boolean
    AS '$libdir/pldbgapi', 'pldbg_deposit_value'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_deposit_value(session integer, varname text, linenumber integer, value text) OWNER TO postgres;

--
-- TOC entry 166 (class 1255 OID 16604)
-- Dependencies: 6
-- Name: pldbg_drop_breakpoint(integer, oid, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_drop_breakpoint(session integer, func oid, linenumber integer) RETURNS boolean
    AS '$libdir/pldbgapi', 'pldbg_drop_breakpoint'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_drop_breakpoint(session integer, func oid, linenumber integer) OWNER TO postgres;

--
-- TOC entry 167 (class 1255 OID 16605)
-- Dependencies: 6 465
-- Name: pldbg_get_breakpoints(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_get_breakpoints(session integer) RETURNS SETOF breakpoint
    AS '$libdir/pldbgapi', 'pldbg_get_breakpoints'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_get_breakpoints(session integer) OWNER TO postgres;

--
-- TOC entry 170 (class 1255 OID 16608)
-- Dependencies: 473 6
-- Name: pldbg_get_proxy_info(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_get_proxy_info() RETURNS proxyinfo
    AS '$libdir/pldbgapi', 'pldbg_get_proxy_info'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_get_proxy_info() OWNER TO postgres;

--
-- TOC entry 168 (class 1255 OID 16606)
-- Dependencies: 6
-- Name: pldbg_get_source(integer, oid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_get_source(session integer, func oid) RETURNS text
    AS '$libdir/pldbgapi', 'pldbg_get_source'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_get_source(session integer, func oid) OWNER TO postgres;

--
-- TOC entry 169 (class 1255 OID 16607)
-- Dependencies: 6 467
-- Name: pldbg_get_stack(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_get_stack(session integer) RETURNS SETOF frame
    AS '$libdir/pldbgapi', 'pldbg_get_stack'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_get_stack(session integer) OWNER TO postgres;

--
-- TOC entry 179 (class 1255 OID 16617)
-- Dependencies: 469 6
-- Name: pldbg_get_target_info(text, "char"); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_get_target_info(signature text, targettype "char") RETURNS targetinfo
    AS '$libdir/targetinfo', 'pldbg_get_target_info'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_get_target_info(signature text, targettype "char") OWNER TO postgres;

--
-- TOC entry 171 (class 1255 OID 16609)
-- Dependencies: 6 471
-- Name: pldbg_get_variables(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_get_variables(session integer) RETURNS SETOF var
    AS '$libdir/pldbgapi', 'pldbg_get_variables'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_get_variables(session integer) OWNER TO postgres;

--
-- TOC entry 172 (class 1255 OID 16610)
-- Dependencies: 465 6
-- Name: pldbg_select_frame(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_select_frame(session integer, frame integer) RETURNS breakpoint
    AS '$libdir/pldbgapi', 'pldbg_select_frame'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_select_frame(session integer, frame integer) OWNER TO postgres;

--
-- TOC entry 173 (class 1255 OID 16611)
-- Dependencies: 6
-- Name: pldbg_set_breakpoint(integer, oid, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_set_breakpoint(session integer, func oid, linenumber integer) RETURNS boolean
    AS '$libdir/pldbgapi', 'pldbg_set_breakpoint'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_set_breakpoint(session integer, func oid, linenumber integer) OWNER TO postgres;

--
-- TOC entry 174 (class 1255 OID 16612)
-- Dependencies: 6
-- Name: pldbg_set_global_breakpoint(integer, oid, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_set_global_breakpoint(session integer, func oid, linenumber integer, targetpid integer) RETURNS boolean
    AS '$libdir/pldbgapi', 'pldbg_set_global_breakpoint'
    LANGUAGE c;


ALTER FUNCTION public.pldbg_set_global_breakpoint(session integer, func oid, linenumber integer, targetpid integer) OWNER TO postgres;

--
-- TOC entry 175 (class 1255 OID 16613)
-- Dependencies: 465 6
-- Name: pldbg_step_into(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_step_into(session integer) RETURNS breakpoint
    AS '$libdir/pldbgapi', 'pldbg_step_into'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_step_into(session integer) OWNER TO postgres;

--
-- TOC entry 176 (class 1255 OID 16614)
-- Dependencies: 465 6
-- Name: pldbg_step_over(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_step_over(session integer) RETURNS breakpoint
    AS '$libdir/pldbgapi', 'pldbg_step_over'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_step_over(session integer) OWNER TO postgres;

--
-- TOC entry 177 (class 1255 OID 16615)
-- Dependencies: 465 6
-- Name: pldbg_wait_for_breakpoint(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_wait_for_breakpoint(session integer) RETURNS breakpoint
    AS '$libdir/pldbgapi', 'pldbg_wait_for_breakpoint'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_wait_for_breakpoint(session integer) OWNER TO postgres;

--
-- TOC entry 178 (class 1255 OID 16616)
-- Dependencies: 6
-- Name: pldbg_wait_for_target(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pldbg_wait_for_target(session integer) RETURNS integer
    AS '$libdir/pldbgapi', 'pldbg_wait_for_target'
    LANGUAGE c STRICT;


ALTER FUNCTION public.pldbg_wait_for_target(session integer) OWNER TO postgres;

--
-- TOC entry 160 (class 1255 OID 16598)
-- Dependencies: 6
-- Name: plpgsql_oid_debug(oid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION plpgsql_oid_debug(functionoid oid) RETURNS integer
    AS '$libdir/plugins/plugin_debugger', 'plpgsql_oid_debug'
    LANGUAGE c STRICT;


ALTER FUNCTION public.plpgsql_oid_debug(functionoid oid) OWNER TO postgres;

--
-- TOC entry 183 (class 1255 OID 4650875)
-- Dependencies: 6 579
-- Name: refcursorfunc(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION refcursorfunc() RETURNS refcursor
    AS $$
DECLARE 
mycurs refcursor; 
BEGIN

drop table if exists dphase;
drop table if exists optode_temp;
drop table if exists sbe16_temperature;
drop table if exists sbe16_pressure;
drop table if exists sbe16_conductivity;
drop table if exists set1;
drop table if exists set2;
drop table if exists set3;

create temp table dphase as
select distinct on (date_trunc('hour',data_timestamp)) 
date_trunc('hour',data_timestamp) as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and parameter_code = 'OPTODE_DPHASE'
;

create temp table optode_temp as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and parameter_code = 'OPTODE_TEMP'
;

create temp table sbe16_temperature as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'WATER_TEMP'
;


create temp table sbe16_pressure as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'WATER_PRESSURE'
;

create temp table sbe16_conductivity as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where mooring_id = 'PULSE_7'
and instrument_id = 4
and parameter_code = 'CONDUCTIVITY'
;

create temp table set1 as
select 
	dphase.obs_time,
	dphase.depth,
	dphase.parameter_value as optode_dphase,
	optode_temp.parameter_value as optode_temperature
from dphase full join optode_temp on 
(
dphase.obs_time = optode_temp.obs_time
AND
dphase.depth = optode_temp.depth
)
;

create temp table set2 as
select 
	set1.obs_time,
	set1.depth,
	set1.optode_dphase,
	set1.optode_temperature,
	sbe16_temperature.parameter_value as sbe16_temperature
from set1 full join sbe16_temperature on 
(
set1.obs_time = sbe16_temperature.obs_time
AND
set1.depth = sbe16_temperature.depth
)
;

create temp table set3 as
select 
	set2.obs_time,
	set2.depth,
	set2.optode_dphase,
	set2.optode_temperature,
        set2.sbe16_temperature,
	sbe16_pressure.parameter_value as sbe16_pressure
from set2 full join sbe16_pressure on 
(
set2.obs_time = sbe16_pressure.obs_time
AND
set2.depth = sbe16_pressure.depth
)
;

create temp table set4 as
select 
	set3.obs_time,
	set3.depth,
	set3.optode_dphase,
	set3.optode_temperature,
        set3.sbe16_temperature,
	set3.sbe16_pressure,
	sbe16_conductivity.parameter_value as sbe16_conductivity
from set3 full join sbe16_conductivity on 
(
set3.obs_time = sbe16_conductivity.obs_time
AND
set3.depth = sbe16_conductivity.depth
)
;

OPEN mycurs FOR  select * from set4 order by obs_time;
RETURN mycurs; 
END;
$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.refcursorfunc() OWNER TO postgres;

--
-- TOC entry 143 (class 1255 OID 16564)
-- Dependencies: 6
-- Name: ssl_client_cert_present(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ssl_client_cert_present() RETURNS boolean
    AS '$libdir/sslinfo', 'ssl_client_cert_present'
    LANGUAGE c STRICT;


ALTER FUNCTION public.ssl_client_cert_present() OWNER TO postgres;

--
-- TOC entry 146 (class 1255 OID 16567)
-- Dependencies: 6
-- Name: ssl_client_dn(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ssl_client_dn() RETURNS text
    AS '$libdir/sslinfo', 'ssl_client_dn'
    LANGUAGE c STRICT;


ALTER FUNCTION public.ssl_client_dn() OWNER TO postgres;

--
-- TOC entry 144 (class 1255 OID 16565)
-- Dependencies: 6
-- Name: ssl_client_dn_field(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ssl_client_dn_field(text) RETURNS text
    AS '$libdir/sslinfo', 'ssl_client_dn_field'
    LANGUAGE c STRICT;


ALTER FUNCTION public.ssl_client_dn_field(text) OWNER TO postgres;

--
-- TOC entry 141 (class 1255 OID 16562)
-- Dependencies: 6
-- Name: ssl_client_serial(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ssl_client_serial() RETURNS numeric
    AS '$libdir/sslinfo', 'ssl_client_serial'
    LANGUAGE c STRICT;


ALTER FUNCTION public.ssl_client_serial() OWNER TO postgres;

--
-- TOC entry 142 (class 1255 OID 16563)
-- Dependencies: 6
-- Name: ssl_is_used(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ssl_is_used() RETURNS boolean
    AS '$libdir/sslinfo', 'ssl_is_used'
    LANGUAGE c STRICT;


ALTER FUNCTION public.ssl_is_used() OWNER TO postgres;

--
-- TOC entry 147 (class 1255 OID 16568)
-- Dependencies: 6
-- Name: ssl_issuer_dn(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ssl_issuer_dn() RETURNS text
    AS '$libdir/sslinfo', 'ssl_issuer_dn'
    LANGUAGE c STRICT;


ALTER FUNCTION public.ssl_issuer_dn() OWNER TO postgres;

--
-- TOC entry 145 (class 1255 OID 16566)
-- Dependencies: 6
-- Name: ssl_issuer_field(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ssl_issuer_field(text) RETURNS text
    AS '$libdir/sslinfo', 'ssl_issuer_field'
    LANGUAGE c STRICT;


ALTER FUNCTION public.ssl_issuer_field(text) OWNER TO postgres;

--
-- TOC entry 150 (class 1255 OID 16571)
-- Dependencies: 6
-- Name: xml_encode_special_chars(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xml_encode_special_chars(text) RETURNS text
    AS '$libdir/pgxml', 'xml_encode_special_chars'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xml_encode_special_chars(text) OWNER TO postgres;

--
-- TOC entry 148 (class 1255 OID 16569)
-- Dependencies: 6
-- Name: xml_is_well_formed(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xml_is_well_formed(text) RETURNS boolean
    AS '$libdir/pgxml', 'xml_is_well_formed'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xml_is_well_formed(text) OWNER TO postgres;

--
-- TOC entry 149 (class 1255 OID 16570)
-- Dependencies: 6
-- Name: xml_valid(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xml_valid(text) RETURNS boolean
    AS '$libdir/pgxml', 'xml_is_well_formed'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xml_valid(text) OWNER TO postgres;

--
-- TOC entry 184 (class 1255 OID 4650905)
-- Dependencies: 6 579
-- Name: xoptode_data_selector(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xoptode_data_selector(integer, text) RETURNS refcursor
    AS $_$
DECLARE 

instrumentID alias for $1;
mooringID alias for $2;
mycurs refcursor; 
BEGIN

drop table if exists dphase;
drop table if exists optode_temp;
drop table if exists sbe16_temperature;
drop table if exists sbe16_pressure;
drop table if exists sbe16_conductivity;
drop table if exists set1;
drop table if exists set2;
drop table if exists set3;

create temp table dphase as
select distinct on (date_trunc('hour',data_timestamp)) 
date_trunc('hour',data_timestamp) as obs_time, depth, parameter_value 
from raw_instrument_data
where instrument_id = instrumentID
and mooring_id = mooringID
and parameter_code = 'OPTODE_DPHASE'
;

create temp table optode_temp as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where instrument_id = instrumentID
and mooring_id = mooringID
and parameter_code = 'OPTODE_TEMP'
;

create temp table sbe16_temperature as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where instrument_id = instrumentID
and mooring_id = mooringID
and parameter_code = 'WATER_TEMP'
;


create temp table sbe16_pressure as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where instrument_id = instrumentID
and mooring_id = mooringID
and parameter_code = 'WATER_PRESSURE'
;

create temp table sbe16_conductivity as
select distinct on (date_trunc('hour',data_timestamp) )
date_trunc('hour',data_timestamp)as obs_time, depth, parameter_value 
from raw_instrument_data
where instrument_id = instrumentID
and mooring_id = mooringID
and parameter_code = 'CONDUCTIVITY'
;

create temp table set1 as
select 
	dphase.obs_time,
	dphase.depth,
	dphase.parameter_value as optode_dphase,
	optode_temp.parameter_value as optode_temperature
from dphase full join optode_temp on 
(
dphase.obs_time = optode_temp.obs_time
)
;

create temp table set2 as
select 
	set1.obs_time,
	set1.depth,
	set1.optode_dphase,
	set1.optode_temperature,
	sbe16_temperature.parameter_value as sbe16_temperature
from set1 full join sbe16_temperature on 
(
set1.obs_time = sbe16_temperature.obs_time
)
;

create temp table set3 as
select 
	set2.obs_time,
	set2.depth,
	set2.optode_dphase,
	set2.optode_temperature,
        set2.sbe16_temperature,
	sbe16_pressure.parameter_value as sbe16_pressure
from set2 full join sbe16_pressure on 
(
set2.obs_time = sbe16_pressure.obs_time
)
;

create temp table set4 as
select 
	set3.obs_time,
	set3.depth,
	set3.optode_temperature,
	set3.optode_dphase,
        set3.sbe16_temperature,
	set3.sbe16_pressure,
	sbe16_conductivity.parameter_value as sbe16_conductivity
from set3 full join sbe16_conductivity on 
(
set3.obs_time = sbe16_conductivity.obs_time
)
;

OPEN mycurs FOR  select * from set4 order by obs_time;
RETURN mycurs; 
END;
$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.xoptode_data_selector(integer, text) OWNER TO postgres;

--
-- TOC entry 154 (class 1255 OID 16575)
-- Dependencies: 6
-- Name: xpath_bool(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_bool(text, text) RETURNS boolean
    AS '$libdir/pgxml', 'xpath_bool'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_bool(text, text) OWNER TO postgres;

--
-- TOC entry 155 (class 1255 OID 16576)
-- Dependencies: 6
-- Name: xpath_list(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_list(text, text, text) RETURNS text
    AS '$libdir/pgxml', 'xpath_list'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_list(text, text, text) OWNER TO postgres;

--
-- TOC entry 156 (class 1255 OID 16577)
-- Dependencies: 6
-- Name: xpath_list(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_list(text, text) RETURNS text
    AS $_$SELECT xpath_list($1,$2,',')$_$
    LANGUAGE sql IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_list(text, text) OWNER TO postgres;

--
-- TOC entry 152 (class 1255 OID 16573)
-- Dependencies: 6
-- Name: xpath_nodeset(text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_nodeset(text, text, text, text) RETURNS text
    AS '$libdir/pgxml', 'xpath_nodeset'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_nodeset(text, text, text, text) OWNER TO postgres;

--
-- TOC entry 157 (class 1255 OID 16578)
-- Dependencies: 6
-- Name: xpath_nodeset(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_nodeset(text, text) RETURNS text
    AS $_$SELECT xpath_nodeset($1,$2,'','')$_$
    LANGUAGE sql IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_nodeset(text, text) OWNER TO postgres;

--
-- TOC entry 158 (class 1255 OID 16579)
-- Dependencies: 6
-- Name: xpath_nodeset(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_nodeset(text, text, text) RETURNS text
    AS $_$SELECT xpath_nodeset($1,$2,'',$3)$_$
    LANGUAGE sql IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_nodeset(text, text, text) OWNER TO postgres;

--
-- TOC entry 153 (class 1255 OID 16574)
-- Dependencies: 6
-- Name: xpath_number(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_number(text, text) RETURNS real
    AS '$libdir/pgxml', 'xpath_number'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_number(text, text) OWNER TO postgres;

--
-- TOC entry 151 (class 1255 OID 16572)
-- Dependencies: 6
-- Name: xpath_string(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_string(text, text) RETURNS text
    AS '$libdir/pgxml', 'xpath_string'
    LANGUAGE c IMMUTABLE STRICT;


ALTER FUNCTION public.xpath_string(text, text) OWNER TO postgres;

--
-- TOC entry 159 (class 1255 OID 16580)
-- Dependencies: 6
-- Name: xpath_table(text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xpath_table(text, text, text, text, text) RETURNS SETOF record
    AS '$libdir/pgxml', 'xpath_table'
    LANGUAGE c STABLE STRICT;


ALTER FUNCTION public.xpath_table(text, text, text, text, text) OWNER TO postgres;

--
-- TOC entry 1286 (class 2617 OID 16432)
-- Dependencies: 418 6 418 42
-- Name: &&; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR && (
    PROCEDURE = cube_overlap,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = &&,
    RESTRICT = areasel,
    JOIN = areajoinsel
);


ALTER OPERATOR public.&& (cube, cube) OWNER TO postgres;

--
-- TOC entry 1282 (class 2617 OID 16430)
-- Dependencies: 35 418 418 6
-- Name: <; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR < (
    PROCEDURE = cube_lt,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = >,
    NEGATOR = >=,
    RESTRICT = scalarltsel,
    JOIN = scalarltjoinsel
);


ALTER OPERATOR public.< (cube, cube) OWNER TO postgres;

--
-- TOC entry 1284 (class 2617 OID 16431)
-- Dependencies: 6 418 37 418
-- Name: <=; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR <= (
    PROCEDURE = cube_le,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = >=,
    NEGATOR = >,
    RESTRICT = scalarltsel,
    JOIN = scalarltjoinsel
);


ALTER OPERATOR public.<= (cube, cube) OWNER TO postgres;

--
-- TOC entry 1288 (class 2617 OID 16433)
-- Dependencies: 418 34 6 418
-- Name: <>; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR <> (
    PROCEDURE = cube_ne,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = <>,
    NEGATOR = =,
    RESTRICT = neqsel,
    JOIN = neqjoinsel
);


ALTER OPERATOR public.<> (cube, cube) OWNER TO postgres;

--
-- TOC entry 1290 (class 2617 OID 16435)
-- Dependencies: 41 6 418 418
-- Name: <@; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR <@ (
    PROCEDURE = cube_contained,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = @>,
    RESTRICT = contsel,
    JOIN = contjoinsel
);


ALTER OPERATOR public.<@ (cube, cube) OWNER TO postgres;

--
-- TOC entry 1287 (class 2617 OID 16434)
-- Dependencies: 418 33 418 6
-- Name: =; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR = (
    PROCEDURE = cube_eq,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = =,
    NEGATOR = <>,
    MERGES,
    RESTRICT = eqsel,
    JOIN = eqjoinsel
);


ALTER OPERATOR public.= (cube, cube) OWNER TO postgres;

--
-- TOC entry 1283 (class 2617 OID 16428)
-- Dependencies: 418 418 36 6
-- Name: >; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR > (
    PROCEDURE = cube_gt,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = <,
    NEGATOR = <=,
    RESTRICT = scalargtsel,
    JOIN = scalargtjoinsel
);


ALTER OPERATOR public.> (cube, cube) OWNER TO postgres;

--
-- TOC entry 1285 (class 2617 OID 16429)
-- Dependencies: 38 6 418 418
-- Name: >=; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR >= (
    PROCEDURE = cube_ge,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = <=,
    NEGATOR = <,
    RESTRICT = scalargtsel,
    JOIN = scalargtjoinsel
);


ALTER OPERATOR public.>= (cube, cube) OWNER TO postgres;

--
-- TOC entry 1291 (class 2617 OID 16438)
-- Dependencies: 418 40 418 6
-- Name: @; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR @ (
    PROCEDURE = cube_contains,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = ~,
    RESTRICT = contsel,
    JOIN = contjoinsel
);


ALTER OPERATOR public.@ (cube, cube) OWNER TO postgres;

--
-- TOC entry 1289 (class 2617 OID 16436)
-- Dependencies: 418 40 418 6
-- Name: @>; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR @> (
    PROCEDURE = cube_contains,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = <@,
    RESTRICT = contsel,
    JOIN = contjoinsel
);


ALTER OPERATOR public.@> (cube, cube) OWNER TO postgres;

--
-- TOC entry 1292 (class 2617 OID 16437)
-- Dependencies: 6 41 418 418
-- Name: ~; Type: OPERATOR; Schema: public; Owner: postgres
--

CREATE OPERATOR ~ (
    PROCEDURE = cube_contained,
    LEFTARG = cube,
    RIGHTARG = cube,
    COMMUTATOR = @,
    RESTRICT = contsel,
    JOIN = contjoinsel
);


ALTER OPERATOR public.~ (cube, cube) OWNER TO postgres;

--
-- TOC entry 1405 (class 2616 OID 16447)
-- Dependencies: 6 1514 418
-- Name: cube_ops; Type: OPERATOR CLASS; Schema: public; Owner: postgres
--

CREATE OPERATOR CLASS cube_ops
    DEFAULT FOR TYPE cube USING btree AS
    OPERATOR 1 <(cube,cube) ,
    OPERATOR 2 <=(cube,cube) ,
    OPERATOR 3 =(cube,cube) ,
    OPERATOR 4 >=(cube,cube) ,
    OPERATOR 5 >(cube,cube) ,
    FUNCTION 1 cube_cmp(cube,cube);


ALTER OPERATOR CLASS public.cube_ops USING btree OWNER TO postgres;

--
-- TOC entry 1406 (class 2616 OID 16455)
-- Dependencies: 6 418 1515
-- Name: gist_cube_ops; Type: OPERATOR CLASS; Schema: public; Owner: postgres
--

CREATE OPERATOR CLASS gist_cube_ops
    DEFAULT FOR TYPE cube USING gist AS
    OPERATOR 3 &&(cube,cube) ,
    OPERATOR 6 =(cube,cube) ,
    OPERATOR 7 @>(cube,cube) ,
    OPERATOR 8 <@(cube,cube) ,
    OPERATOR 13 @(cube,cube) ,
    OPERATOR 14 ~(cube,cube) ,
    FUNCTION 1 g_cube_consistent(internal,cube,integer) ,
    FUNCTION 2 g_cube_union(internal,internal) ,
    FUNCTION 3 g_cube_compress(internal) ,
    FUNCTION 4 g_cube_decompress(internal) ,
    FUNCTION 5 g_cube_penalty(internal,internal,internal) ,
    FUNCTION 6 g_cube_picksplit(internal,internal) ,
    FUNCTION 7 g_cube_same(cube,cube,internal);


ALTER OPERATOR CLASS public.gist_cube_ops USING gist OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1775 (class 1259 OID 4649118)
-- Dependencies: 6
-- Name: cmdattachedfiles; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdattachedfiles (
    cmdafid integer NOT NULL,
    cmdaf_ddid integer,
    cmdafattachedid integer,
    cmdafattachedfrom character varying(2),
    cmdafdate timestamp without time zone,
    cmdaftype integer,
    cmdaffilepath character varying(500),
    cmdaffilename character varying(500),
    cmdafnotes character varying(500)
);


ALTER TABLE public.cmdattachedfiles OWNER TO csiro;

--
-- TOC entry 1776 (class 1259 OID 4649124)
-- Dependencies: 6
-- Name: cmdattachedfilessequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdattachedfilessequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdattachedfilessequence OWNER TO csiro;

--
-- TOC entry 1777 (class 1259 OID 4649126)
-- Dependencies: 6
-- Name: cmdattachednotes; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdattachednotes (
    cmdanid integer NOT NULL,
    cmdanattachedid integer,
    cmdanattachedfrom character varying(2),
    cmdandatetime timestamp without time zone,
    cmdannotes character varying(5000),
    cmdanstatus integer
);


ALTER TABLE public.cmdattachednotes OWNER TO csiro;

--
-- TOC entry 1778 (class 1259 OID 4649132)
-- Dependencies: 6
-- Name: cmdattachednotessequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdattachednotessequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdattachednotessequence OWNER TO csiro;

--
-- TOC entry 1779 (class 1259 OID 4649134)
-- Dependencies: 6
-- Name: cmdattachedtype; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdattachedtype (
    cmdatid integer NOT NULL,
    cmdattype character varying(30)
);


ALTER TABLE public.cmdattachedtype OWNER TO csiro;

--
-- TOC entry 1780 (class 1259 OID 4649137)
-- Dependencies: 6
-- Name: cmdattachedtypesequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdattachedtypesequence
    START WITH 9
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdattachedtypesequence OWNER TO csiro;

--
-- TOC entry 1781 (class 1259 OID 4649139)
-- Dependencies: 6
-- Name: cmdcontrolledby; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdcontrolledby (
    cmdcbid integer NOT NULL,
    cmdcbsectionname character varying(50)
);


ALTER TABLE public.cmdcontrolledby OWNER TO csiro;

--
-- TOC entry 1782 (class 1259 OID 4649142)
-- Dependencies: 6
-- Name: cmdcontrolledbysequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdcontrolledbysequence
    START WITH 7
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdcontrolledbysequence OWNER TO csiro;

--
-- TOC entry 1783 (class 1259 OID 4649144)
-- Dependencies: 2097 6
-- Name: cmddeploymentdetails; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmddeploymentdetails (
    cmdddid integer NOT NULL,
    cmddd_slid integer,
    cmddd_cbid integer,
    cmddddeploymentdate date,
    cmdddrecoverydate date,
    cmddddateinposition timestamp without time zone,
    cmddddateoutposition timestamp without time zone,
    cmdddlatitude numeric(13,10),
    cmdddlongitude numeric(13,10),
    cmdddstatus integer,
    cmddddepth numeric(8,2) DEFAULT 0
);


ALTER TABLE public.cmddeploymentdetails OWNER TO csiro;

--
-- TOC entry 1784 (class 1259 OID 4649148)
-- Dependencies: 6
-- Name: cmddeploymentdetailssequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmddeploymentdetailssequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmddeploymentdetailssequence OWNER TO csiro;

--
-- TOC entry 1785 (class 1259 OID 4649150)
-- Dependencies: 2098 6
-- Name: cmdfieldpersonnel; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdfieldpersonnel (
    cmdfpid integer NOT NULL,
    cmdfp_ftid integer,
    cmdfp_plid integer,
    cmdfpstatus integer DEFAULT 0
);


ALTER TABLE public.cmdfieldpersonnel OWNER TO csiro;

--
-- TOC entry 1786 (class 1259 OID 4649154)
-- Dependencies: 6
-- Name: cmdfieldpersonnelsequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdfieldpersonnelsequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdfieldpersonnelsequence OWNER TO csiro;

--
-- TOC entry 1787 (class 1259 OID 4649156)
-- Dependencies: 6
-- Name: cmdfieldsite; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdfieldsite (
    cmdfsid integer NOT NULL,
    cmdfs_cbid integer,
    cmdfsshortname character varying(20),
    cmdfslongname character varying(200),
    cmdfsdescription character varying(5000)
);


ALTER TABLE public.cmdfieldsite OWNER TO csiro;

--
-- TOC entry 1788 (class 1259 OID 4649162)
-- Dependencies: 6
-- Name: cmdfieldsitesequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdfieldsitesequence
    START WITH 13
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdfieldsitesequence OWNER TO csiro;

--
-- TOC entry 1789 (class 1259 OID 4649164)
-- Dependencies: 6
-- Name: cmdfieldtrip; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdfieldtrip (
    cmdftid integer NOT NULL,
    cmdft_cbid integer,
    cmdftstartdate date,
    cmdftfinishdate date,
    cmdftdescription character varying(5000)
);


ALTER TABLE public.cmdfieldtrip OWNER TO csiro;

--
-- TOC entry 1790 (class 1259 OID 4649170)
-- Dependencies: 6
-- Name: cmdfieldtripsequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdfieldtripsequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdfieldtripsequence OWNER TO csiro;

--
-- TOC entry 1791 (class 1259 OID 4649172)
-- Dependencies: 6
-- Name: cmditemdetail; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmditemdetail (
    cmdidid integer NOT NULL,
    cmdid_ownercbid integer,
    cmdid_controllercbid integer,
    cmdidassetid character varying(50),
    cmdidbrand character varying(50),
    cmdidmodel character varying(50),
    cmdidserialnumber character varying(50),
    cmdidcapability character varying(200),
    cmdidsampletype character varying(100),
    cmdidrange character varying(100),
    cmdidnotes character varying(2000)
);


ALTER TABLE public.cmditemdetail OWNER TO csiro;

--
-- TOC entry 1792 (class 1259 OID 4649178)
-- Dependencies: 6
-- Name: cmditemdetailsequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmditemdetailsequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmditemdetailsequence OWNER TO csiro;

--
-- TOC entry 1793 (class 1259 OID 4649180)
-- Dependencies: 6
-- Name: cmditemlink; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmditemlink (
    cmdilid integer NOT NULL,
    cmdil_parentidid integer,
    cmdil_childidid integer,
    cmdil_ddid integer,
    cmdildate date,
    cmdildepth numeric(7,2),
    cmdilshortnotes character varying(100),
    cmdilstatus integer
);


ALTER TABLE public.cmditemlink OWNER TO csiro;

--
-- TOC entry 1794 (class 1259 OID 4649183)
-- Dependencies: 6
-- Name: cmditemlinksequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmditemlinksequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmditemlinksequence OWNER TO csiro;

--
-- TOC entry 1795 (class 1259 OID 4649185)
-- Dependencies: 1905 6
-- Name: cmditemlinkunlinkview; Type: VIEW; Schema: public; Owner: csiro
--

CREATE VIEW cmditemlinkunlinkview AS
    SELECT cmdil.cmdilid, cmdil.cmdil_childidid, cmdil.cmdil_parentidid, (((cmdid.cmdidmodel)::text || ' '::text) || (cmdid.cmdidserialnumber)::text) AS item, cmdil.cmdildate, cmdil.cmdildepth, CASE WHEN ((SELECT min(cmditemlink.cmdildate) AS min FROM cmditemlink WHERE ((cmditemlink.cmdil_childidid = cmdil.cmdil_childidid) AND (cmditemlink.cmdildate > cmdil.cmdildate)) GROUP BY cmditemlink.cmdil_childidid) IS NULL) THEN '2999-12-31'::date ELSE (SELECT min(cmditemlink.cmdildate) AS min FROM cmditemlink WHERE ((cmditemlink.cmdil_childidid = cmdil.cmdil_childidid) AND (cmditemlink.cmdildate > cmdil.cmdildate)) GROUP BY cmditemlink.cmdil_childidid) END AS dateunlinked, cmdil.cmdil_ddid FROM ((cmditemlink cmdil LEFT JOIN cmddeploymentdetails cmddd ON ((cmdil.cmdil_ddid = cmddd.cmdddid))) LEFT JOIN cmditemdetail cmdid ON ((cmdil.cmdil_childidid = cmdid.cmdidid))) ORDER BY cmdil.cmdilid;


ALTER TABLE public.cmditemlinkunlinkview OWNER TO csiro;

--
-- TOC entry 1796 (class 1259 OID 4649190)
-- Dependencies: 6
-- Name: cmdpersonnellist; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdpersonnellist (
    cmdplid integer NOT NULL,
    cmdpladminlevel integer,
    cmdpl_cbid integer,
    cmdplstaffid character varying(50),
    cmdpllastname character varying(50),
    cmdplfirstname character varying(50),
    cmdplorganisation character varying(100),
    cmdplfilelocation character varying(500),
    cmdplstafflogin character varying(100),
    cmdpldefaultdb character varying(100)
);


ALTER TABLE public.cmdpersonnellist OWNER TO csiro;

--
-- TOC entry 1797 (class 1259 OID 4649196)
-- Dependencies: 6
-- Name: cmdpersonnellistsequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdpersonnellistsequence
    START WITH 21
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdpersonnellistsequence OWNER TO csiro;

--
-- TOC entry 1798 (class 1259 OID 4649198)
-- Dependencies: 6
-- Name: cmdsitelocation; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdsitelocation (
    cmdslid integer NOT NULL,
    cmdsl_fsid integer,
    cmdslname character varying(100),
    cmdsldepth numeric(7,2),
    cmdsldepthdescription character varying(5000)
);


ALTER TABLE public.cmdsitelocation OWNER TO csiro;

--
-- TOC entry 1799 (class 1259 OID 4649204)
-- Dependencies: 6
-- Name: cmdsitelocationsequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdsitelocationsequence
    START WITH 29
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdsitelocationsequence OWNER TO csiro;

--
-- TOC entry 1800 (class 1259 OID 4649206)
-- Dependencies: 6
-- Name: cmdtripdeployment; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdtripdeployment (
    cmdtdid integer NOT NULL,
    cmdtddeployment character(1),
    cmdtd_ftid integer,
    cmdtd_ddid integer
);


ALTER TABLE public.cmdtripdeployment OWNER TO csiro;

--
-- TOC entry 1801 (class 1259 OID 4649209)
-- Dependencies: 6
-- Name: cmdtripdeploymentsequence; Type: SEQUENCE; Schema: public; Owner: csiro
--

CREATE SEQUENCE cmdtripdeploymentsequence
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.cmdtripdeploymentsequence OWNER TO csiro;

--
-- TOC entry 1802 (class 1259 OID 4649211)
-- Dependencies: 6
-- Name: cmdversiondetail; Type: TABLE; Schema: public; Owner: csiro; Tablespace: 
--

CREATE TABLE cmdversiondetail (
    cmdvddbversion character varying(10) NOT NULL,
    cmdvddbupdateok character varying(1),
    cmdvddbreadonly character varying(1)
);


ALTER TABLE public.cmdversiondetail OWNER TO csiro;

--
-- TOC entry 1822 (class 1259 OID 4650323)
-- Dependencies: 2107 2108 6
-- Name: corrected_instrument_data; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE corrected_instrument_data (
    source_file_id integer NOT NULL,
    calibration_file_id integer NOT NULL,
    instrument_id integer NOT NULL,
    mooring_id character(20) NOT NULL,
    data_timestamp timestamp without time zone NOT NULL,
    latitude numeric(16,4) NOT NULL,
    longitude numeric(16,4) NOT NULL,
    depth numeric(16,4) DEFAULT 0 NOT NULL,
    parameter_code character(20) NOT NULL,
    parameter_value numeric(16,4) NOT NULL,
    quality_code character(20) DEFAULT 'N/A'::bpchar NOT NULL
);


ALTER TABLE public.corrected_instrument_data OWNER TO postgres;

--
-- TOC entry 1819 (class 1259 OID 4650151)
-- Dependencies: 6
-- Name: datafile_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE datafile_sequence
    INCREMENT BY 1
    NO MAXVALUE
    MINVALUE 100000
    CACHE 1;


ALTER TABLE public.datafile_sequence OWNER TO postgres;

--
-- TOC entry 1815 (class 1259 OID 4649842)
-- Dependencies: 6
-- Name: form_location; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE form_location (
    program_code character(120) NOT NULL,
    ip_address character(60) NOT NULL,
    logon_id character(20) NOT NULL,
    x_coord integer NOT NULL,
    y_coord integer NOT NULL,
    frame_height integer NOT NULL,
    frame_width integer NOT NULL
);


ALTER TABLE public.form_location OWNER TO postgres;

--
-- TOC entry 1805 (class 1259 OID 4649656)
-- Dependencies: 2099 2100 2101 6
-- Name: instrument; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE instrument (
    instrument_id integer NOT NULL,
    make character(50) NOT NULL,
    model character(50) NOT NULL,
    serial_number character(50),
    asset_code character(50),
    date_acquired timestamp without time zone DEFAULT '2000-01-01 00:00:00'::timestamp without time zone NOT NULL,
    date_disposed timestamp without time zone,
    instrument_type character(20) DEFAULT 'SIMPLE'::bpchar NOT NULL,
    instrument_status character(20) DEFAULT 'AVAILABLE'::bpchar NOT NULL
);


ALTER TABLE public.instrument OWNER TO postgres;

--
-- TOC entry 1817 (class 1259 OID 4649993)
-- Dependencies: 2105 6
-- Name: instrument_calibration_files; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE instrument_calibration_files (
    datafile_pk integer NOT NULL,
    instrument_id integer NOT NULL,
    file_path character varying(255) NOT NULL,
    file_name character varying(255) NOT NULL,
    validity_start timestamp without time zone NOT NULL,
    validity_end timestamp without time zone DEFAULT '2099-12-31 23:59:59'::timestamp without time zone NOT NULL,
    file_data bytea NOT NULL,
    processing_date timestamp without time zone,
    processing_class character varying(255)
);


ALTER TABLE public.instrument_calibration_files OWNER TO postgres;

--
-- TOC entry 1826 (class 1259 OID 4650759)
-- Dependencies: 6
-- Name: instrument_calibration_values; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE instrument_calibration_values (
    instrument_id integer NOT NULL,
    mooring_id character(20) NOT NULL,
    datafile_pk integer NOT NULL,
    param_code character(40) NOT NULL,
    description character varying(255) NOT NULL,
    data_type character(10) NOT NULL,
    data_value character(120) NOT NULL
);


ALTER TABLE public.instrument_calibration_values OWNER TO postgres;

--
-- TOC entry 1816 (class 1259 OID 4649847)
-- Dependencies: 2104 6
-- Name: instrument_data_files; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE instrument_data_files (
    datafile_pk integer NOT NULL,
    instrument_id integer NOT NULL,
    mooring_id character(20) NOT NULL,
    file_path character varying(255) NOT NULL,
    file_name character varying(255) NOT NULL,
    file_data bytea NOT NULL,
    processing_status character(20) DEFAULT 'NEW'::bpchar NOT NULL,
    processing_date timestamp without time zone,
    processing_class character varying(255),
    instrument_depth numeric(16,4)
);


ALTER TABLE public.instrument_data_files OWNER TO postgres;

--
-- TOC entry 1824 (class 1259 OID 4650388)
-- Dependencies: 2111 6
-- Name: instrument_data_parsers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE instrument_data_parsers (
    class_name character varying(255) NOT NULL,
    description character(60),
    display_code character(10) DEFAULT 'Y'::bpchar
);


ALTER TABLE public.instrument_data_parsers OWNER TO postgres;

--
-- TOC entry 1825 (class 1259 OID 4650430)
-- Dependencies: 6
-- Name: instrument_datafile_headers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE instrument_datafile_headers (
    datafile_pk integer NOT NULL,
    line integer NOT NULL,
    line_text character varying(1024) NOT NULL
);


ALTER TABLE public.instrument_datafile_headers OWNER TO postgres;

--
-- TOC entry 1806 (class 1259 OID 4649664)
-- Dependencies: 6
-- Name: instrument_notes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE instrument_notes (
    instrument_id integer NOT NULL,
    recording_timestamp timestamp without time zone NOT NULL,
    notes_data character varying(4096) NOT NULL
);


ALTER TABLE public.instrument_notes OWNER TO postgres;

--
-- TOC entry 1818 (class 1259 OID 4650149)
-- Dependencies: 6
-- Name: instrument_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE instrument_sequence
    INCREMENT BY 1
    NO MAXVALUE
    MINVALUE 100000
    CACHE 1;


ALTER TABLE public.instrument_sequence OWNER TO postgres;

--
-- TOC entry 1814 (class 1259 OID 4649836)
-- Dependencies: 2103 6
-- Name: manufacturer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE manufacturer (
    code character(20) NOT NULL,
    description character(80),
    display_code character(1) DEFAULT 'Y'::bpchar
);


ALTER TABLE public.manufacturer OWNER TO postgres;

--
-- TOC entry 1803 (class 1259 OID 4649638)
-- Dependencies: 6
-- Name: mooring; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE mooring (
    mooring_id character(20) NOT NULL,
    short_description character(80) NOT NULL,
    timestamp_in timestamp without time zone,
    timestamp_out timestamp without time zone,
    latitude_in numeric(16,4),
    longitude_in numeric(16,4),
    latitude_out numeric(16,4),
    longitude_out numeric(16,4)
);


ALTER TABLE public.mooring OWNER TO postgres;

--
-- TOC entry 1804 (class 1259 OID 4649643)
-- Dependencies: 6
-- Name: mooring_notes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE mooring_notes (
    mooring_id character(20) NOT NULL,
    recording_timestamp timestamp without time zone NOT NULL,
    notes_data character varying(4096) NOT NULL
);


ALTER TABLE public.mooring_notes OWNER TO postgres;

--
-- TOC entry 1811 (class 1259 OID 4649808)
-- Dependencies: 6
-- Name: nnsmenuheader; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE nnsmenuheader (
    menu_code character(20) NOT NULL,
    short_description character(60) NOT NULL,
    full_description character(255),
    sort_order integer NOT NULL,
    access_level integer NOT NULL
);


ALTER TABLE public.nnsmenuheader OWNER TO postgres;

--
-- TOC entry 1812 (class 1259 OID 4649813)
-- Dependencies: 6
-- Name: nnsmenuitem; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE nnsmenuitem (
    menu_code character(20) NOT NULL,
    action_code character(20) NOT NULL,
    short_description character(60),
    full_description character(255),
    class_name character(255) NOT NULL,
    sort_order integer NOT NULL,
    access_level integer NOT NULL
);


ALTER TABLE public.nnsmenuitem OWNER TO postgres;

--
-- TOC entry 1813 (class 1259 OID 4649826)
-- Dependencies: 6
-- Name: nnsmenuparam; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE nnsmenuparam (
    action_code character(20) NOT NULL,
    param_code character(20) NOT NULL,
    param_value character(20) NOT NULL
);


ALTER TABLE public.nnsmenuparam OWNER TO postgres;

--
-- TOC entry 1810 (class 1259 OID 4649802)
-- Dependencies: 2102 6
-- Name: organisation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE organisation (
    code character(20) NOT NULL,
    description character(80),
    display_code character(1) DEFAULT 'Y'::bpchar
);


ALTER TABLE public.organisation OWNER TO postgres;

--
-- TOC entry 1821 (class 1259 OID 4650290)
-- Dependencies: 2106 6
-- Name: parameters; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE parameters (
    code character(20) NOT NULL,
    description character(60),
    display_code character(10) DEFAULT 'Y'::bpchar
);


ALTER TABLE public.parameters OWNER TO postgres;

--
-- TOC entry 1767 (class 1259 OID 16511)
-- Dependencies: 1902 6
-- Name: pg_buffercache; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW pg_buffercache AS
    SELECT p.bufferid, p.relfilenode, p.reltablespace, p.reldatabase, p.relblocknumber, p.isdirty, p.usagecount FROM pg_buffercache_pages() p(bufferid integer, relfilenode oid, reltablespace oid, reldatabase oid, relblocknumber bigint, isdirty boolean, usagecount smallint);


ALTER TABLE public.pg_buffercache OWNER TO postgres;

--
-- TOC entry 1768 (class 1259 OID 16517)
-- Dependencies: 1903 6
-- Name: pg_freespacemap_pages; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW pg_freespacemap_pages AS
    SELECT p.reltablespace, p.reldatabase, p.relfilenode, p.relblocknumber, p.bytes FROM pg_freespacemap_pages() p(reltablespace oid, reldatabase oid, relfilenode oid, relblocknumber bigint, bytes integer);


ALTER TABLE public.pg_freespacemap_pages OWNER TO postgres;

--
-- TOC entry 1769 (class 1259 OID 16521)
-- Dependencies: 1904 6
-- Name: pg_freespacemap_relations; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW pg_freespacemap_relations AS
    SELECT p.reltablespace, p.reldatabase, p.relfilenode, p.avgrequest, p.interestingpages, p.storedpages, p.nextpage FROM pg_freespacemap_relations() p(reltablespace oid, reldatabase oid, relfilenode oid, avgrequest integer, interestingpages integer, storedpages integer, nextpage integer);


ALTER TABLE public.pg_freespacemap_relations OWNER TO postgres;

--
-- TOC entry 1823 (class 1259 OID 4650356)
-- Dependencies: 2109 2110 6
-- Name: raw_instrument_data; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE raw_instrument_data (
    source_file_id integer NOT NULL,
    instrument_id integer NOT NULL,
    mooring_id character(20) NOT NULL,
    data_timestamp timestamp without time zone NOT NULL,
    latitude numeric(16,4) NOT NULL,
    longitude numeric(16,4) NOT NULL,
    depth numeric(16,4) DEFAULT 0 NOT NULL,
    parameter_code character(20) NOT NULL,
    parameter_value numeric(16,4) NOT NULL,
    quality_code character(20) DEFAULT 'N/A'::bpchar NOT NULL
);


ALTER TABLE public.raw_instrument_data OWNER TO postgres;

--
-- TOC entry 1807 (class 1259 OID 4649778)
-- Dependencies: 6
-- Name: siteinfo; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE siteinfo (
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
    spool_path character(80)
);


ALTER TABLE public.siteinfo OWNER TO postgres;

--
-- TOC entry 1808 (class 1259 OID 4649786)
-- Dependencies: 6
-- Name: staff; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE staff (
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
    password character(20) NOT NULL,
    dbms_login character(20) NOT NULL,
    display_code character(1),
    notes character varying(1024)
);


ALTER TABLE public.staff OWNER TO postgres;

--
-- TOC entry 1809 (class 1259 OID 4649797)
-- Dependencies: 6
-- Name: system_params; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE system_params (
    param_code character(40) NOT NULL,
    description character varying(255) NOT NULL,
    data_type character(10) NOT NULL,
    data_value character(120) NOT NULL
);


ALTER TABLE public.system_params OWNER TO postgres;

--
-- TOC entry 1820 (class 1259 OID 4650164)
-- Dependencies: 6
-- Name: user_params; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_params (
    logon_id character(20) NOT NULL,
    param_code character(40) NOT NULL,
    description character varying(255) NOT NULL,
    data_type character(10) NOT NULL,
    data_value character(120) NOT NULL
);


ALTER TABLE public.user_params OWNER TO postgres;

--
-- TOC entry 2113 (class 2606 OID 4649215)
-- Dependencies: 1775 1775
-- Name: cmdattachedfiles_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdattachedfiles
    ADD CONSTRAINT cmdattachedfiles_pkey PRIMARY KEY (cmdafid);


--
-- TOC entry 2115 (class 2606 OID 4649217)
-- Dependencies: 1777 1777
-- Name: cmdattachednotes_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdattachednotes
    ADD CONSTRAINT cmdattachednotes_pkey PRIMARY KEY (cmdanid);


--
-- TOC entry 2117 (class 2606 OID 4649219)
-- Dependencies: 1779 1779
-- Name: cmdattachedtype_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdattachedtype
    ADD CONSTRAINT cmdattachedtype_pkey PRIMARY KEY (cmdatid);


--
-- TOC entry 2119 (class 2606 OID 4649221)
-- Dependencies: 1781 1781
-- Name: cmdcontrolledby_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdcontrolledby
    ADD CONSTRAINT cmdcontrolledby_pkey PRIMARY KEY (cmdcbid);


--
-- TOC entry 2121 (class 2606 OID 4649223)
-- Dependencies: 1783 1783
-- Name: cmddeploymentdetails_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmddeploymentdetails
    ADD CONSTRAINT cmddeploymentdetails_pkey PRIMARY KEY (cmdddid);


--
-- TOC entry 2123 (class 2606 OID 4649225)
-- Dependencies: 1785 1785
-- Name: cmdfieldpersonnel_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdfieldpersonnel
    ADD CONSTRAINT cmdfieldpersonnel_pkey PRIMARY KEY (cmdfpid);


--
-- TOC entry 2125 (class 2606 OID 4649227)
-- Dependencies: 1787 1787
-- Name: cmdfieldsite_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdfieldsite
    ADD CONSTRAINT cmdfieldsite_pkey PRIMARY KEY (cmdfsid);


--
-- TOC entry 2127 (class 2606 OID 4649229)
-- Dependencies: 1789 1789
-- Name: cmdfieldtrip_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdfieldtrip
    ADD CONSTRAINT cmdfieldtrip_pkey PRIMARY KEY (cmdftid);


--
-- TOC entry 2129 (class 2606 OID 4649231)
-- Dependencies: 1791 1791
-- Name: cmditemdetail_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmditemdetail
    ADD CONSTRAINT cmditemdetail_pkey PRIMARY KEY (cmdidid);


--
-- TOC entry 2131 (class 2606 OID 4649233)
-- Dependencies: 1793 1793
-- Name: cmditemlink_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmditemlink
    ADD CONSTRAINT cmditemlink_pkey PRIMARY KEY (cmdilid);


--
-- TOC entry 2133 (class 2606 OID 4649235)
-- Dependencies: 1796 1796
-- Name: cmdpersonnellist_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdpersonnellist
    ADD CONSTRAINT cmdpersonnellist_pkey PRIMARY KEY (cmdplid);


--
-- TOC entry 2135 (class 2606 OID 4649237)
-- Dependencies: 1798 1798
-- Name: cmdsitelocation_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdsitelocation
    ADD CONSTRAINT cmdsitelocation_pkey PRIMARY KEY (cmdslid);


--
-- TOC entry 2137 (class 2606 OID 4649239)
-- Dependencies: 1800 1800
-- Name: cmdtripdeployment_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdtripdeployment
    ADD CONSTRAINT cmdtripdeployment_pkey PRIMARY KEY (cmdtdid);


--
-- TOC entry 2139 (class 2606 OID 4649241)
-- Dependencies: 1802 1802
-- Name: cmdversiondetail_pkey; Type: CONSTRAINT; Schema: public; Owner: csiro; Tablespace: 
--

ALTER TABLE ONLY cmdversiondetail
    ADD CONSTRAINT cmdversiondetail_pkey PRIMARY KEY (cmdvddbversion);


--
-- TOC entry 2182 (class 2606 OID 4650329)
-- Dependencies: 1822 1822 1822 1822
-- Name: corrected_instrument_data_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY corrected_instrument_data
    ADD CONSTRAINT corrected_instrument_data_pkey PRIMARY KEY (instrument_id, data_timestamp, parameter_code);


--
-- TOC entry 2168 (class 2606 OID 4649846)
-- Dependencies: 1815 1815 1815 1815
-- Name: form_location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY form_location
    ADD CONSTRAINT form_location_pkey PRIMARY KEY (program_code, ip_address, logon_id);


--
-- TOC entry 2174 (class 2606 OID 4650003)
-- Dependencies: 1817 1817 1817 1817 1817
-- Name: instrument_calibration_files_constraint; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_calibration_files
    ADD CONSTRAINT instrument_calibration_files_constraint UNIQUE (instrument_id, file_path, file_name, validity_start);


--
-- TOC entry 2176 (class 2606 OID 4650001)
-- Dependencies: 1817 1817
-- Name: instrument_calibration_files_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_calibration_files
    ADD CONSTRAINT instrument_calibration_files_pkey PRIMARY KEY (datafile_pk);


--
-- TOC entry 2194 (class 2606 OID 4650763)
-- Dependencies: 1826 1826 1826
-- Name: instrument_calibration_values_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_calibration_values
    ADD CONSTRAINT instrument_calibration_values_pkey PRIMARY KEY (datafile_pk, param_code);


--
-- TOC entry 2170 (class 2606 OID 4649856)
-- Dependencies: 1816 1816 1816 1816 1816
-- Name: instrument_data_files_constraint; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_data_files
    ADD CONSTRAINT instrument_data_files_constraint UNIQUE (instrument_id, mooring_id, file_path, file_name);


--
-- TOC entry 2172 (class 2606 OID 4649854)
-- Dependencies: 1816 1816
-- Name: instrument_data_files_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_data_files
    ADD CONSTRAINT instrument_data_files_pkey PRIMARY KEY (datafile_pk);


--
-- TOC entry 2190 (class 2606 OID 4650393)
-- Dependencies: 1824 1824
-- Name: instrument_data_parsers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_data_parsers
    ADD CONSTRAINT instrument_data_parsers_pkey PRIMARY KEY (class_name);


--
-- TOC entry 2192 (class 2606 OID 4650437)
-- Dependencies: 1825 1825 1825
-- Name: instrument_datafile_headers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_datafile_headers
    ADD CONSTRAINT instrument_datafile_headers_pkey PRIMARY KEY (datafile_pk, line);


--
-- TOC entry 2147 (class 2606 OID 4649671)
-- Dependencies: 1806 1806 1806
-- Name: instrument_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument_notes
    ADD CONSTRAINT instrument_notes_pkey PRIMARY KEY (instrument_id, recording_timestamp);


--
-- TOC entry 2145 (class 2606 OID 4649663)
-- Dependencies: 1805 1805
-- Name: instrument_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY instrument
    ADD CONSTRAINT instrument_pkey PRIMARY KEY (instrument_id);


--
-- TOC entry 2166 (class 2606 OID 4649841)
-- Dependencies: 1814 1814
-- Name: manufacturer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY manufacturer
    ADD CONSTRAINT manufacturer_pkey PRIMARY KEY (code);


--
-- TOC entry 2143 (class 2606 OID 4649650)
-- Dependencies: 1804 1804 1804
-- Name: mooring_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY mooring_notes
    ADD CONSTRAINT mooring_notes_pkey PRIMARY KEY (mooring_id, recording_timestamp);


--
-- TOC entry 2141 (class 2606 OID 4649642)
-- Dependencies: 1803 1803
-- Name: mooring_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY mooring
    ADD CONSTRAINT mooring_pkey PRIMARY KEY (mooring_id);


--
-- TOC entry 2160 (class 2606 OID 4649812)
-- Dependencies: 1811 1811
-- Name: nnsmenuheader_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY nnsmenuheader
    ADD CONSTRAINT nnsmenuheader_pkey PRIMARY KEY (menu_code);


--
-- TOC entry 2162 (class 2606 OID 4649820)
-- Dependencies: 1812 1812
-- Name: nnsmenuitem_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY nnsmenuitem
    ADD CONSTRAINT nnsmenuitem_pkey PRIMARY KEY (action_code);


--
-- TOC entry 2164 (class 2606 OID 4649830)
-- Dependencies: 1813 1813 1813
-- Name: nnsmenuparam_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY nnsmenuparam
    ADD CONSTRAINT nnsmenuparam_pkey PRIMARY KEY (action_code, param_code);


--
-- TOC entry 2158 (class 2606 OID 4649807)
-- Dependencies: 1810 1810
-- Name: organisation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY organisation
    ADD CONSTRAINT organisation_pkey PRIMARY KEY (code);


--
-- TOC entry 2180 (class 2606 OID 4650295)
-- Dependencies: 1821 1821
-- Name: parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY parameters
    ADD CONSTRAINT parameters_pkey PRIMARY KEY (code);


--
-- TOC entry 2188 (class 2606 OID 4650743)
-- Dependencies: 1823 1823 1823 1823 1823
-- Name: raw_instrument_data_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY raw_instrument_data
    ADD CONSTRAINT raw_instrument_data_pkey PRIMARY KEY (source_file_id, instrument_id, data_timestamp, parameter_code);


--
-- TOC entry 2149 (class 2606 OID 4649785)
-- Dependencies: 1807 1807
-- Name: siteinfo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY siteinfo
    ADD CONSTRAINT siteinfo_pkey PRIMARY KEY (site_id);


--
-- TOC entry 2151 (class 2606 OID 4649795)
-- Dependencies: 1808 1808
-- Name: staff_dbms_login_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY staff
    ADD CONSTRAINT staff_dbms_login_key UNIQUE (dbms_login);


--
-- TOC entry 2154 (class 2606 OID 4649793)
-- Dependencies: 1808 1808
-- Name: staff_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY staff
    ADD CONSTRAINT staff_pkey PRIMARY KEY (staff_id);


--
-- TOC entry 2156 (class 2606 OID 4649801)
-- Dependencies: 1809 1809
-- Name: system_params_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY system_params
    ADD CONSTRAINT system_params_pkey PRIMARY KEY (param_code);


--
-- TOC entry 2178 (class 2606 OID 4650168)
-- Dependencies: 1820 1820 1820
-- Name: user_params_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_params
    ADD CONSTRAINT user_params_pkey PRIMARY KEY (logon_id, param_code);


--
-- TOC entry 2183 (class 1259 OID 4650440)
-- Dependencies: 1823
-- Name: raw_data_ix0; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX raw_data_ix0 ON raw_instrument_data USING btree (source_file_id);


--
-- TOC entry 2184 (class 1259 OID 4650441)
-- Dependencies: 1823
-- Name: raw_data_ix1; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX raw_data_ix1 ON raw_instrument_data USING btree (instrument_id);


--
-- TOC entry 2185 (class 1259 OID 4650442)
-- Dependencies: 1823
-- Name: raw_data_ix2; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX raw_data_ix2 ON raw_instrument_data USING btree (mooring_id);


--
-- TOC entry 2186 (class 1259 OID 4650904)
-- Dependencies: 1823 1823
-- Name: raw_data_ix3; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX raw_data_ix3 ON raw_instrument_data USING btree (parameter_code, data_timestamp);


--
-- TOC entry 2152 (class 1259 OID 4649796)
-- Dependencies: 1808
-- Name: staff_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX staff_pk ON staff USING btree (staff_id);


--
-- TOC entry 2195 (class 2606 OID 4649242)
-- Dependencies: 2134 1783 1798
-- Name: cmddeploymentdetailsfk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmddeploymentdetails
    ADD CONSTRAINT cmddeploymentdetailsfk FOREIGN KEY (cmddd_slid) REFERENCES cmdsitelocation(cmdslid);


--
-- TOC entry 2196 (class 2606 OID 4649247)
-- Dependencies: 1783 1781 2118
-- Name: cmddeploymentdetailsfk2; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmddeploymentdetails
    ADD CONSTRAINT cmddeploymentdetailsfk2 FOREIGN KEY (cmddd_cbid) REFERENCES cmdcontrolledby(cmdcbid);


--
-- TOC entry 2197 (class 2606 OID 4649252)
-- Dependencies: 1785 2126 1789
-- Name: cmdfieldpersonnelfk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmdfieldpersonnel
    ADD CONSTRAINT cmdfieldpersonnelfk FOREIGN KEY (cmdfp_ftid) REFERENCES cmdfieldtrip(cmdftid);


--
-- TOC entry 2198 (class 2606 OID 4649257)
-- Dependencies: 1796 2132 1785
-- Name: cmdfieldpersonnelfk2; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmdfieldpersonnel
    ADD CONSTRAINT cmdfieldpersonnelfk2 FOREIGN KEY (cmdfp_plid) REFERENCES cmdpersonnellist(cmdplid);


--
-- TOC entry 2199 (class 2606 OID 4649262)
-- Dependencies: 2118 1787 1781
-- Name: cmdfieldsitefk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmdfieldsite
    ADD CONSTRAINT cmdfieldsitefk FOREIGN KEY (cmdfs_cbid) REFERENCES cmdcontrolledby(cmdcbid);


--
-- TOC entry 2200 (class 2606 OID 4649267)
-- Dependencies: 2118 1781 1791
-- Name: cmditemdetailfk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmditemdetail
    ADD CONSTRAINT cmditemdetailfk FOREIGN KEY (cmdid_ownercbid) REFERENCES cmdcontrolledby(cmdcbid);


--
-- TOC entry 2201 (class 2606 OID 4649272)
-- Dependencies: 1791 1781 2118
-- Name: cmditemdetailfk2; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmditemdetail
    ADD CONSTRAINT cmditemdetailfk2 FOREIGN KEY (cmdid_controllercbid) REFERENCES cmdcontrolledby(cmdcbid);


--
-- TOC entry 2202 (class 2606 OID 4649277)
-- Dependencies: 2128 1793 1791
-- Name: cmditemlinkfk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmditemlink
    ADD CONSTRAINT cmditemlinkfk FOREIGN KEY (cmdil_parentidid) REFERENCES cmditemdetail(cmdidid);


--
-- TOC entry 2203 (class 2606 OID 4649282)
-- Dependencies: 1791 1793 2128
-- Name: cmditemlinkfk2; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmditemlink
    ADD CONSTRAINT cmditemlinkfk2 FOREIGN KEY (cmdil_childidid) REFERENCES cmditemdetail(cmdidid);


--
-- TOC entry 2204 (class 2606 OID 4649287)
-- Dependencies: 1783 1793 2120
-- Name: cmditemlinkfk3; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmditemlink
    ADD CONSTRAINT cmditemlinkfk3 FOREIGN KEY (cmdil_ddid) REFERENCES cmddeploymentdetails(cmdddid);


--
-- TOC entry 2205 (class 2606 OID 4649292)
-- Dependencies: 1781 1796 2118
-- Name: cmdpersonnellistfk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmdpersonnellist
    ADD CONSTRAINT cmdpersonnellistfk FOREIGN KEY (cmdpl_cbid) REFERENCES cmdcontrolledby(cmdcbid);


--
-- TOC entry 2206 (class 2606 OID 4649297)
-- Dependencies: 1787 1798 2124
-- Name: cmdsitelocationfk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmdsitelocation
    ADD CONSTRAINT cmdsitelocationfk FOREIGN KEY (cmdsl_fsid) REFERENCES cmdfieldsite(cmdfsid);


--
-- TOC entry 2207 (class 2606 OID 4649302)
-- Dependencies: 1789 1800 2126
-- Name: cmdtriplocationfk; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmdtripdeployment
    ADD CONSTRAINT cmdtriplocationfk FOREIGN KEY (cmdtd_ftid) REFERENCES cmdfieldtrip(cmdftid);


--
-- TOC entry 2208 (class 2606 OID 4649307)
-- Dependencies: 1783 1800 2120
-- Name: cmdtriplocationfk2; Type: FK CONSTRAINT; Schema: public; Owner: csiro
--

ALTER TABLE ONLY cmdtripdeployment
    ADD CONSTRAINT cmdtriplocationfk2 FOREIGN KEY (cmdtd_ddid) REFERENCES cmddeploymentdetails(cmdddid);


--
-- TOC entry 2217 (class 2606 OID 4650330)
-- Dependencies: 1822 2175 1817
-- Name: corrected_instrument_data_calibration_file_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY corrected_instrument_data
    ADD CONSTRAINT corrected_instrument_data_calibration_file_id_fkey FOREIGN KEY (calibration_file_id) REFERENCES instrument_calibration_files(datafile_pk);


--
-- TOC entry 2218 (class 2606 OID 4650335)
-- Dependencies: 1822 2144 1805
-- Name: corrected_instrument_data_instrument_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY corrected_instrument_data
    ADD CONSTRAINT corrected_instrument_data_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id);


--
-- TOC entry 2219 (class 2606 OID 4650340)
-- Dependencies: 2140 1803 1822
-- Name: corrected_instrument_data_mooring_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY corrected_instrument_data
    ADD CONSTRAINT corrected_instrument_data_mooring_id_fkey FOREIGN KEY (mooring_id) REFERENCES mooring(mooring_id);


--
-- TOC entry 2220 (class 2606 OID 4650345)
-- Dependencies: 1821 2179 1822
-- Name: corrected_instrument_data_parameter_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY corrected_instrument_data
    ADD CONSTRAINT corrected_instrument_data_parameter_code_fkey FOREIGN KEY (parameter_code) REFERENCES parameters(code);


--
-- TOC entry 2221 (class 2606 OID 4650350)
-- Dependencies: 1816 2171 1822
-- Name: corrected_instrument_data_source_file_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY corrected_instrument_data
    ADD CONSTRAINT corrected_instrument_data_source_file_id_fkey FOREIGN KEY (source_file_id) REFERENCES instrument_data_files(datafile_pk);


--
-- TOC entry 2215 (class 2606 OID 4650004)
-- Dependencies: 2144 1817 1805
-- Name: instrument_calibration_files_instrument_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY instrument_calibration_files
    ADD CONSTRAINT instrument_calibration_files_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id);


--
-- TOC entry 2228 (class 2606 OID 4650774)
-- Dependencies: 2175 1826 1817
-- Name: instrument_calibration_values_datafile_pk_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY instrument_calibration_values
    ADD CONSTRAINT instrument_calibration_values_datafile_pk_fkey FOREIGN KEY (datafile_pk) REFERENCES instrument_calibration_files(datafile_pk);


--
-- TOC entry 2226 (class 2606 OID 4650764)
-- Dependencies: 1805 1826 2144
-- Name: instrument_calibration_values_instrument_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY instrument_calibration_values
    ADD CONSTRAINT instrument_calibration_values_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id);


--
-- TOC entry 2227 (class 2606 OID 4650769)
-- Dependencies: 2140 1803 1826
-- Name: instrument_calibration_values_mooring_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY instrument_calibration_values
    ADD CONSTRAINT instrument_calibration_values_mooring_id_fkey FOREIGN KEY (mooring_id) REFERENCES mooring(mooring_id);


--
-- TOC entry 2213 (class 2606 OID 4649857)
-- Dependencies: 1805 1816 2144
-- Name: instrument_data_files_instrument_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY instrument_data_files
    ADD CONSTRAINT instrument_data_files_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id);


--
-- TOC entry 2214 (class 2606 OID 4649862)
-- Dependencies: 2140 1816 1803
-- Name: instrument_data_files_mooring_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY instrument_data_files
    ADD CONSTRAINT instrument_data_files_mooring_id_fkey FOREIGN KEY (mooring_id) REFERENCES mooring(mooring_id);


--
-- TOC entry 2210 (class 2606 OID 4649672)
-- Dependencies: 2144 1806 1805
-- Name: instrument_notes_instrument_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY instrument_notes
    ADD CONSTRAINT instrument_notes_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id);


--
-- TOC entry 2209 (class 2606 OID 4649651)
-- Dependencies: 1804 1803 2140
-- Name: mooring_notes_mooring_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY mooring_notes
    ADD CONSTRAINT mooring_notes_mooring_id_fkey FOREIGN KEY (mooring_id) REFERENCES mooring(mooring_id);


--
-- TOC entry 2211 (class 2606 OID 4649821)
-- Dependencies: 1811 1812 2159
-- Name: nnsmenuitem_menu_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY nnsmenuitem
    ADD CONSTRAINT nnsmenuitem_menu_code_fkey FOREIGN KEY (menu_code) REFERENCES nnsmenuheader(menu_code);


--
-- TOC entry 2212 (class 2606 OID 4649831)
-- Dependencies: 1812 1813 2161
-- Name: nnsmenuparam_action_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY nnsmenuparam
    ADD CONSTRAINT nnsmenuparam_action_code_fkey FOREIGN KEY (action_code) REFERENCES nnsmenuitem(action_code);


--
-- TOC entry 2222 (class 2606 OID 4650363)
-- Dependencies: 1823 1805 2144
-- Name: raw_instrument_data_instrument_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY raw_instrument_data
    ADD CONSTRAINT raw_instrument_data_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id);


--
-- TOC entry 2223 (class 2606 OID 4650368)
-- Dependencies: 1823 1803 2140
-- Name: raw_instrument_data_mooring_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY raw_instrument_data
    ADD CONSTRAINT raw_instrument_data_mooring_id_fkey FOREIGN KEY (mooring_id) REFERENCES mooring(mooring_id);


--
-- TOC entry 2224 (class 2606 OID 4650373)
-- Dependencies: 1823 1821 2179
-- Name: raw_instrument_data_parameter_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY raw_instrument_data
    ADD CONSTRAINT raw_instrument_data_parameter_code_fkey FOREIGN KEY (parameter_code) REFERENCES parameters(code);


--
-- TOC entry 2225 (class 2606 OID 4650378)
-- Dependencies: 2171 1816 1823
-- Name: raw_instrument_data_source_file_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY raw_instrument_data
    ADD CONSTRAINT raw_instrument_data_source_file_id_fkey FOREIGN KEY (source_file_id) REFERENCES instrument_data_files(datafile_pk);


--
-- TOC entry 2216 (class 2606 OID 4650169)
-- Dependencies: 1808 2150 1820
-- Name: user_params_logon_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_params
    ADD CONSTRAINT user_params_logon_id_fkey FOREIGN KEY (logon_id) REFERENCES staff(dbms_login);


--
-- TOC entry 2233 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- TOC entry 2245 (class 0 OID 0)
-- Dependencies: 66
-- Name: dblink_connect_u(text); Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON FUNCTION dblink_connect_u(text) FROM PUBLIC;
REVOKE ALL ON FUNCTION dblink_connect_u(text) FROM postgres;
GRANT ALL ON FUNCTION dblink_connect_u(text) TO postgres;


--
-- TOC entry 2246 (class 0 OID 0)
-- Dependencies: 65
-- Name: dblink_connect_u(text, text); Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON FUNCTION dblink_connect_u(text, text) FROM PUBLIC;
REVOKE ALL ON FUNCTION dblink_connect_u(text, text) FROM postgres;
GRANT ALL ON FUNCTION dblink_connect_u(text, text) TO postgres;


--
-- TOC entry 2247 (class 0 OID 0)
-- Dependencies: 101
-- Name: pg_buffercache_pages(); Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON FUNCTION pg_buffercache_pages() FROM PUBLIC;
REVOKE ALL ON FUNCTION pg_buffercache_pages() FROM postgres;
GRANT ALL ON FUNCTION pg_buffercache_pages() TO postgres;


--
-- TOC entry 2248 (class 0 OID 0)
-- Dependencies: 102
-- Name: pg_freespacemap_pages(); Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON FUNCTION pg_freespacemap_pages() FROM PUBLIC;
REVOKE ALL ON FUNCTION pg_freespacemap_pages() FROM postgres;
GRANT ALL ON FUNCTION pg_freespacemap_pages() TO postgres;


--
-- TOC entry 2249 (class 0 OID 0)
-- Dependencies: 103
-- Name: pg_freespacemap_relations(); Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON FUNCTION pg_freespacemap_relations() FROM PUBLIC;
REVOKE ALL ON FUNCTION pg_freespacemap_relations() FROM postgres;
GRANT ALL ON FUNCTION pg_freespacemap_relations() TO postgres;


--
-- TOC entry 2250 (class 0 OID 0)
-- Dependencies: 1795
-- Name: cmditemlinkunlinkview; Type: ACL; Schema: public; Owner: csiro
--

REVOKE ALL ON TABLE cmditemlinkunlinkview FROM PUBLIC;
REVOKE ALL ON TABLE cmditemlinkunlinkview FROM csiro;
GRANT ALL ON TABLE cmditemlinkunlinkview TO csiro;


--
-- TOC entry 2251 (class 0 OID 0)
-- Dependencies: 1767
-- Name: pg_buffercache; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE pg_buffercache FROM PUBLIC;
REVOKE ALL ON TABLE pg_buffercache FROM postgres;
GRANT ALL ON TABLE pg_buffercache TO postgres;


--
-- TOC entry 2252 (class 0 OID 0)
-- Dependencies: 1768
-- Name: pg_freespacemap_pages; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE pg_freespacemap_pages FROM PUBLIC;
REVOKE ALL ON TABLE pg_freespacemap_pages FROM postgres;
GRANT ALL ON TABLE pg_freespacemap_pages TO postgres;


--
-- TOC entry 2253 (class 0 OID 0)
-- Dependencies: 1769
-- Name: pg_freespacemap_relations; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE pg_freespacemap_relations FROM PUBLIC;
REVOKE ALL ON TABLE pg_freespacemap_relations FROM postgres;
GRANT ALL ON TABLE pg_freespacemap_relations TO postgres;


-- Completed on 2012-03-08 11:42:06 EST

--
-- PostgreSQL database dump complete
--

