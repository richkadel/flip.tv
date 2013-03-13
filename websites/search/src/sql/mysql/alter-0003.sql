create table favorite (
    favorite_id bigint not null auto_increment,    
    show_title varchar(255) not null,
    program_id varchar(255),
    episode_title varchar(255),
    created timestamp not null,
    deleted boolean not null,
    rank double not null,
    user_id bigint not null,
    primary key (favorite_id)
);

create index rank_index on favorite (rank);
create index show_title_index on favorite (show_title);
create index program_index on favorite (program_id);

create table rating (
    rating_id bigint not null auto_increment,    
    show_title varchar(255) not null,
    program_id varchar(255),
    episode_title varchar(255),
    rating double not null,
    created timestamp not null,
    last_modified timestamp,
    deleted boolean not null,
    user_id bigint not null,
    primary key (rating_id)
);

create index show_title_index on rating (show_title);
create index program_index on rating (program_id);

alter table user
	add birth_month_year date null;
	
update user set birth_month_year=MAKEDATE(YEAR(current_date)-((high_age + low_age) / 2), 1);

create table to_notify (
    id bigint not null auto_increment,
    email varchar(64) not null,
    created timestamp not null default CURRENT_TIMESTAMP,
    primary key (id)
);

alter table to_notify add constraint email_unique unique (email);
create index email_index on to_notify (email);

