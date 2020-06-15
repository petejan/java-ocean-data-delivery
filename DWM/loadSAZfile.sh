#!/bin/sh

java -cp dist/ABOS.jar:lib/poi-3.15/poi-3.15.jar:lib/poi-3.15/poi-ooxml-3.15.jar:lib/poi-3.15/ooxml-lib/xmlbeans-2.6.0.jar:lib/poi-3.15/lib/commons-collections4-4.1.jar:lib/poi-3.15/poi-ooxml-schemas-3.15.jar org.imos.abos.parsers.saz.LoadSAZfile "$1"
