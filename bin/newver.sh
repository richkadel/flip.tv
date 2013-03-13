#!/bin/bash

if [[ $0 =~ ^(.*)newver.sh$ ]]; then
	#echo maven -Dmaven.test.skip $* I THINK THIS SHOULD BE REMOVED
	. ${BASH_REMATCH[1]}/newver $*
else
	echo Error in script $0
fi
