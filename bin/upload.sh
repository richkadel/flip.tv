#!/bin/bash
#
# upload.sh: uploads a file over sftp to the maven server.
#

username=$USER
if [ "$4" != "" ]; then
	username=$1
	shift;
fi

hostname=knowbout.tv
maven_dir=/maven
dir_perm=775
file_perm=664


#
#create the sftp script file
#
if [ "$3" == "" ]; then
	echo "Usage: upload.sh [remoteuser] <type> <groupId> <filename>"
	exit 0;
fi

echo $username
echo $1 $2 $3

tmp_file=~sftp_maven.tmp
src_file=$(echo "$3" | sed -e 's/\\/\//g')
filename=`basename $3`

if [ -f $tmp_file ]; then
	rm $tmp_file;
fi
echo "mkdir $maven_dir/$2" >> $tmp_file
echo "chmod $dir_perm $maven_dir/$2" >> $tmp_file
echo "mkdir $maven_dir/$2/$1s" >> $tmp_file
echo "chmod $dir_perm $maven_dir/$2/$1s" >> $tmp_file
echo "cd $maven_dir/$2/$1s" >> $tmp_file
echo "put $src_file $filename" >> $tmp_file
echo "chmod $file_perm $maven_dir/$2/$1s/$filename" >> $tmp_file

sftp $username@$hostname < $tmp_file

#
#remove script file
#
rm $tmp_file
