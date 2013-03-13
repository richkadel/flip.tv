alter table message
	add defer_until timestamp null,
	add expires timestamp null,
	drop index message_tosend_index;

alter table user
	add lineup varchar(16) not null default 'P-DC',
	add alert_minutes_default int not null default 15,
	add using_primary_email_default boolean not null default true,
	add using_alternate_email_default boolean not null default false,
	add using_sms_default boolean not null default true,
	add using_im_default boolean not null default false;

create index message_tosend_index on message (attempts, sent, defer_until, expires, priority);

delete from pending_alerts;

alter table pending_alerts
	drop channel,
	change program_id program_id varchar(255) null,
	add call_sign varchar(10) not null,
	add manual boolean not null default false,
	drop index pending_alert_time_index;
	
create index pending_alert_user_manual_index on pending_alerts (user_id, manual, deleted, fired);
create index pending_alert_time_index on pending_alerts (deleted, fired, alert_time, user_id, pending_alert_id);

alter table program_alerts
	change program_id program_id varchar(255) null,
	add program_title varchar(120) null,
	add new_episodes boolean not null default false,
	drop index user_program_alerts;
	
create index user_program_alerts on program_alerts (user_id, program_id, program_title);
	
create table friends (
    friend_id bigint not null auto_increment,
    email varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    status integer,
    deleted bit,
    created datetime not null,
    recent bit not null,
    friend_user_id bigint,
    user_id bigint not null,
    primary key (friend_id)
);

create index friend_email_index on friends (email);
create index friend_User_index on friends (friend_user_id);
create index user_index on friends (user_id);
create index status_index on friends (status);
create index deleted_index on friends (deleted);


    
