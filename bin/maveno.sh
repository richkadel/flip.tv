#!/bin/bash

if [[ $0 =~ "^(.*)maveno.sh$" ]]; then
	echo maven -Dmaven.test.skip $*
	. ${BASH_REMATCH[1]}/maveno $*
else
	echo Error in script $0
fi
