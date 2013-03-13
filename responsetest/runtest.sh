#!/bin/bash

export FLIPTV_HOME=${0/runtest.sh/}target/deploy/usr/local/flip.tv
export CONFIG_FILE="/etc/flip.tv/responsetest.xml"
export DIRECT=true
export PATHDELIM=":"

$FLIPTV_HOME/runresponsetest
