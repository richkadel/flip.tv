alter table user
	drop alerts_enabled,
	add last_login timestamp not null default '2007-05-10 12:00:00',
    add first_name varchar(64),
    add last_name varchar(64),
    add search_type varchar(16) default 'ALL',
    add enabled boolean not null default true;

create index enabled_index on user (enabled);

alter table message
	add priority int not null default 1;
	
create index message_tosend_index			on message (attempts, sent, priority);
