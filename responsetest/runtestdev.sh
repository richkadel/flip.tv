#!/bin/bash

export FLIPTV_HOME=${0/runtestdev.sh/}target/deploy/usr/local/flip.tv
export CONFIG_FILE="/etc/flip.tv/responsetestdev.xml"
export DIRECT=true
export PATHDELIM=":"

$FLIPTV_HOME/runresponsetest
