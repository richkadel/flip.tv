#Programs
alter table programs
	add program_type int default 0;
create index program_type_index on programs (program_type);

#Drop old schedule table since the data is old bad at this point.
DROP TABLE IF EXISTS `schedules`;

    create table network_lineup_network (
        network_id bigint not null,
        network_lineup_id varchar(255) not null,
        primary key (network_id, network_lineup_id)
    );

    create table network_lineups (
        network_lineup_id varchar(255) not null,
        title varchar(120),
        digital bit,
        delay integer,
        affiliate_delay integer,
        primary key (network_lineup_id)
    );

    create table network_schedules (
        schedule_id bigint not null,
        network_lineup_id varchar(16) not null,
        air_time datetime not null,
        primary key (schedule_id, network_lineup_id)
    );

    create table networks (
        network_id bigint not null auto_increment,
        name varchar(40),
        call_sign varchar(10),
        affiliation varchar(25),
        logo varchar(100),
        primary key (network_id)
    );


#Create the new structure of the schedule table
    create table schedules (
        schedule_id bigint not null auto_increment,
        air_time datetime,
        end_time datetime,
        duration integer,
        part_number integer,
        number_of_parts integer,
        cc bit,
        stereo bit,
        new_episode bit,
        live_tape_delay varchar(25),
        subtitled bit,
        premiere_finale varchar(25),
        joined_in_progress bit,
        cable_classroom bit,
        tv_rating varchar(4),
        sap bit,
        lineup_id varchar(255),
        sex_rating bit,
        violence_rating bit,
        language_rating bit,
        dialog_rating bit,
        fv_rating bit,
        three_d bit,
        letterbox bit,
        hdtv bit,
        dolby varchar(5),
        dvs bit,
        network_id bigint not null,
        program_id varchar(255) not null,
        primary key (schedule_id)
    );


    alter table network_lineup_network 
        add index FKC47245AFB0727186 (network_lineup_id), 
        add constraint FKC47245AFB0727186 
        foreign key (network_lineup_id) 
        references network_lineups (network_lineup_id);

    alter table network_lineup_network 
        add index FKC47245AFD4892969 (network_id), 
        add constraint FKC47245AFD4892969 
        foreign key (network_id) 
        references networks (network_id);

    alter table network_schedules 
        add index FKE3DB3EBB0727186 (network_lineup_id), 
        add constraint FKE3DB3EBB0727186 
        foreign key (network_lineup_id) 
        references network_lineups (network_lineup_id);

    alter table network_schedules 
        add index FKE3DB3EBC2DD95CB (schedule_id), 
        add constraint FKE3DB3EBC2DD95CB 
        foreign key (schedule_id) 
        references schedules (schedule_id);

    create index airTimeIndex on network_schedules (air_time);

    create index callSignIndex on networks (call_sign);

    create index airTimeIndex on schedules (air_time);
    create index endTimeIndex on schedules (end_time);
    create index program_idIndex on schedules ( program_id);
    
    alter table schedules 
        add index FKF66BC0BCD4892969 (network_id), 
        add constraint FKF66BC0BCD4892969 
        foreign key (network_id) 
        references networks (network_id);
