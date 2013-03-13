alter table programs add column game_time_zone varchar(30) null;
update programs set game_time_zone = game_time_zome;
alter table programs drop column game_time_zome;

create index program_last_modified_index on programs (last_modified);
create index program_team_index on programs (title, epi_title, program_id);
