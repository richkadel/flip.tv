
    create table channels (
        channel_id varchar(255) not null,
        tms_chan varchar(5),
        service_tier integer,
        effective_date datetime,
        expiration_date datetime,
        lineup_id varchar(255) not null,
        station_id bigint not null,
        primary key (channel_id)
    );

    create table communities (
        community_id varchar(255) not null,
        communityName varchar(28),
        county_name varchar(25),
        county_size varchar(1),
        county_code integer,
        state varchar(2),
        zip_code varchar(12),
        primary key (community_id)
    );

    create table credits (
        credit_id bigint not null auto_increment,
        type integer,
        firstName varchar(20),
        lastName varchar(20),
        role_description varchar(30),
        primary key (credit_id)
    );

    create table headend_communities (
        community_id varchar(255) not null,
        headend_id varchar(255) not null,
        primary key (headend_id, community_id)
    );

    create table headends (
        headend_id varchar(255) not null,
        dma_code integer,
        dma_name varchar(70),
        mso_code integer,
        dma_rank integer,
        headend_name varchar(42),
        headend_location varchar(28),
        mso_name varchar(42),
        time_zone_code integer,
        primary key (headend_id)
    );

    create table lineups (
        lineup_id varchar(255) not null,
        name varchar(50),
        device integer,
        headend_id varchar(255) not null,
        primary key (lineup_id)
    );

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

    create table program_credits (
        credit_id bigint not null,
        program_id varchar(255) not null,
        primary key (program_id, credit_id)
    );

    create table programs (
        program_id varchar(255) not null,
        title varchar(120),
        reduced_title_70 varchar(70),
        reduced_title_40 varchar(40),
        reduced_title_20 varchar(20),
        reduced_title_10 varchar(10),
        alt_title varchar(120),
        reduced_description_120 varchar(120),
        reduced_description_60 varchar(60),
        reduced_description_40 varchar(40),
        adult_situations varchar(30),
        graphic_language varchar(30),
        brief_nudity varchar(30),
        graphic_violence varchar(30),
        ssc varchar(30),
        rape varchar(30),
        genre_description varchar(100),
        description varchar(255),
        year date,
        mpaa_rating varchar(5),
        star_rating float,
        run_time integer,
        color_code varchar(20),
        program_language varchar(20),
        org_country varchar(15),
        made_for_tv bit,
        source_type varchar(10),
        show_type varchar(30),
        holiday varchar(30),
        syn_epi_num varchar(11),
        alt_syn_epi_num varchar(11),
        epi_title varchar(150),
        net_syn_source varchar(10),
        net_syn_type varchar(21),
        description_actors varchar(255),
        reduced_description_actors varchar(100),
        org_studio varchar(25),
        game_date date,
        game_time datetime,
        game_time_zome varchar(30),
        orginal_air_date date,
        unique_id varchar(8),
        last_modified datetime,
        program_type integer,
        primary key (program_id)
    );

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

    create table stations (
        station_id bigint not null,
        time_zone varchar(30),
        name varchar(40),
        call_sign varchar(10),
        affiliation varchar(25),
        city varchar(20),
        state varchar(15),
        zip_code varchar(12),
        country varchar(15),
        dma_name varchar(70),
        dma_num integer,
        fcc_channel_number integer,
        primary key (station_id)
    );

    create index channelNumberIndex on channels (tms_chan);

    alter table channels 
        add index FK556423D0BBC0BB0B (lineup_id), 
        add constraint FK556423D0BBC0BB0B 
        foreign key (lineup_id) 
        references lineups (lineup_id);

    alter table channels 
        add index FK556423D0C3683A9 (station_id), 
        add constraint FK556423D0C3683A9 
        foreign key (station_id) 
        references stations (station_id);

    alter table headend_communities 
        add index FK1A4ED703A0C82BC9 (community_id), 
        add constraint FK1A4ED703A0C82BC9 
        foreign key (community_id) 
        references communities (community_id);

    alter table headend_communities 
        add index FK1A4ED703283DDA49 (headend_id), 
        add constraint FK1A4ED703283DDA49 
        foreign key (headend_id) 
        references headends (headend_id);

    alter table lineups 
        add index FKA8BA0C4283DDA49 (headend_id), 
        add constraint FKA8BA0C4283DDA49 
        foreign key (headend_id) 
        references headends (headend_id);

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

    create index callSignIndex on networks (call_sign);

    alter table program_credits 
        add index FK30B9B5DF72A9C1A9 (program_id), 
        add constraint FK30B9B5DF72A9C1A9 
        foreign key (program_id) 
        references programs (program_id);

    alter table program_credits 
        add index FK30B9B5DF3EEE4ACB (credit_id), 
        add constraint FK30B9B5DF3EEE4ACB 
        foreign key (credit_id) 
        references credits (credit_id);

    create index airTimeIndex on schedules (air_time);
    create index endTimeIndex on schedules (end_time);

    alter table schedules 
        add index FKF66BC0BC72A9C1A9 (program_id), 
        add constraint FKF66BC0BC72A9C1A9 
        foreign key (program_id) 
        references programs (program_id);

    alter table schedules 
        add index FKF66BC0BCD4892969 (network_id), 
        add constraint FKF66BC0BCD4892969 
        foreign key (network_id) 
        references networks (network_id);
