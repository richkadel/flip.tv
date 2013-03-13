create database guide;

grant select,insert,update,delete
	on guide.*
	to 'knowbout'@'localhost'
	identified by 'C0tt3r';
