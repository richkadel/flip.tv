create table user (
    user_id bigint not null auto_increment,
    username varchar(32) not null,
    password varchar(64) not null,
    primary_email varchar(64),
    gender char(1),
    timezone varchar(64),
    zipcode varchar(32),
    city varchar(128),
    state varchar(128),
    low_age int,
    high_age int,
    sms_email varchar(64),
    earliest_sms time,
    latest_sms time,
    alerts_enabled integer,
    registration_secret varchar(32),
    registration_complete boolean not null default false,
    sms_verified boolean not null default false,
    sms_verification_code varchar(32),
    max_entries int not null default 25,
    creation_time timestamp not null default CURRENT_TIMESTAMP,
    primary key (user_id)
);

alter table user add constraint username_unique unique (username);
create index username_index on user (username);
create index username_primary_email_index on user (primary_email);
create index reg_secret_index on user (registration_secret);

create table groups (
	group_id bigint not null auto_increment,
    name varchar(255) not null,
    primary key (group_id)
);    

create index groupname_index on groups (name);

create table user_groups (
	user_id bigint not null,
	group_name varchar(255) not null,
    username varchar(255) not null,
    primary key (username, group_name)
);   

create index usergroups_index on user_groups (username);

create table feedback (
    feedback_id bigint not null auto_increment,
    user_id bigint,
	subject varchar(255),
	message text not null,
	url varchar(255) not null,
	email varchar(255),
	received timestamp  not null default CURRENT_TIMESTAMP,
	primary key (feedback_id)
); 

create table keyword_alerts (
    keyword_alert_id bigint not null auto_increment,
    user_id bigint,
	user_query varchar(512) not null,
	normalized_query varchar(512) not null,
	using_primary_email_realtime boolean not null,
	using_alternate_email_realtime boolean not null,
	using_sms_realtime boolean not null,
	using_im_realtime boolean not null,
	max_alerts_per_day int not null default 5,
	todays_alert_count int not null,
	last_alert_day date,
    disabled boolean not null default false,
    creation_time timestamp not null default CURRENT_TIMESTAMP,
    last_modified timestamp,
    deleted boolean not null default false,
	primary key (keyword_alert_id)
);

create index user_keyword_alerts					on keyword_alerts (user_id);
create index keyword_alert_query_index				on keyword_alerts (normalized_query);
create index keyword_alert_disabled_by_normal_index	on keyword_alerts (disabled, normalized_query);
create index keyword_alerts_deleted_index			on keyword_alerts (deleted);

create table program_alert_templates (
    template_id bigint not null auto_increment,
    keyword_alert_id bigint not null,
	alert_minutes int not null,
	using_primary_email boolean not null,
	using_alternate_email boolean not null,
	using_sms boolean not null,
	using_im boolean not null,
    creation_time timestamp not null default CURRENT_TIMESTAMP,
    last_modified timestamp,
	primary key (template_id)
);

create index keyword_alert_program_alert_templates	on program_alert_templates (keyword_alert_id);

create table program_alerts (
    program_alert_id bigint not null auto_increment,
    user_id bigint,
    keyword_alert_id bigint,
	program_id varchar(255) not null,
	alert_minutes int not null,
	using_primary_email boolean not null,
	using_alternate_email boolean not null,
	using_sms boolean not null,
	using_im boolean not null,
    disabled boolean not null default false,
    creation_time timestamp not null default CURRENT_TIMESTAMP,
    last_modified timestamp,
    deleted boolean not null default false,
	primary key (program_alert_id)
);

create index user_program_alerts				on program_alerts (user_id, program_id);
create index keyword_alert_program_alerts		on program_alerts (keyword_alert_id);
create index program_alerts_deleted_index		on program_alerts (deleted);
create index program_alerts_creation_time_index	on program_alerts (creation_time);

create table pending_alerts (
    pending_alert_id bigint not null auto_increment,
    program_alert_id bigint,
    user_id bigint,
	program_id varchar(255) not null,
	channel varchar(255) not null,
	program_start_time datetime not null,
	alert_time datetime not null,
    fired boolean not null default false,
    deleted boolean not null default false,
	primary key (pending_alert_id)
);

create index pending_alert_program_time_index	on pending_alerts (program_alert_id, alert_time);
create index pending_alert_time_index			on pending_alerts (deleted, fired, alert_time, user_id, program_id);
create index pending_start_time_index			on pending_alerts (fired, program_start_time);

create table keyword_matches (
    keyword_alert_id bigint not null,
	program_id varchar(255) not null,
	program_end_time datetime not null,
    creation_time timestamp not null default CURRENT_TIMESTAMP,
	primary key (keyword_alert_id, program_id)
);

create index keyword_match_end_time_index					on keyword_matches (program_end_time);

create table message (
	message_id bigint not null auto_increment,
    user_id bigint,
	subject text not null,
	body text not null,
	to_address varchar(255) not null,
	from_address varchar(255) not null,
	sent datetime,
	mime_type varchar(32) not null,
	attempts int not null default 0,
    sms boolean not null default false,
	primary key (message_id)
);
