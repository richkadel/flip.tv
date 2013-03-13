
    alter table channels 
        drop 
        foreign key FK556423D0BBC0BB0B;

    alter table channels 
        drop 
        foreign key FK556423D0C3683A9;

    alter table headend_communities 
        drop 
        foreign key FK1A4ED703A0C82BC9;

    alter table headend_communities 
        drop 
        foreign key FK1A4ED703283DDA49;

    alter table lineups 
        drop 
        foreign key FKA8BA0C4283DDA49;

    alter table network_lineup_network 
        drop 
        foreign key FKC47245AFB0727186;

    alter table network_lineup_network 
        drop 
        foreign key FKC47245AFD4892969;

    alter table network_schedules 
        drop 
        foreign key FKE3DB3EBB0727186;

    alter table network_schedules 
        drop 
        foreign key FKE3DB3EBC2DD95CB;

    alter table program_credits 
        drop 
        foreign key FK30B9B5DF72A9C1A9;

    alter table program_credits 
        drop 
        foreign key FK30B9B5DF3EEE4ACB;

    alter table schedules 
        drop 
        foreign key FKF66BC0BC72A9C1A9;

    alter table schedules 
        drop 
        foreign key FKF66BC0BCD4892969;

    drop table if exists channels;

    drop table if exists communities;

    drop table if exists credits;

    drop table if exists headend_communities;

    drop table if exists headends;

    drop table if exists lineups;

    drop table if exists network_lineup_network;

    drop table if exists network_lineups;

    drop table if exists network_schedules;

    drop table if exists networks;

    drop table if exists program_credits;

    drop table if exists programs;

    drop table if exists schedules;

    drop table if exists stations;
