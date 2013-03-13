
update
	search.program_alerts,
	(	select DISTINCT 
    		search.program_alerts.program_alert_id						as alertid,
    		concat('SH',substring(guide.programs.program_id,3,8),'0000')	as showid 
		from search.program_alerts,guide.programs 
		where
			 search.program_alerts.program_title=guide.programs.title and
			 search.program_alerts.program_id is null
	) a 
	set search.program_alerts.program_id = a.showid
	where search.program_alerts.program_alert_id = a.alertid;
	
alter table program_alerts
	drop column program_title;
	
alter table program_alerts drop index user_program_alerts;
	
alter table favorite
	drop column show_title,
	drop column episode_title,
	modify program_id varchar(255) not null,
	add label varchar(275) not null,
	drop index show_title_index;    
	
alter table rating
	drop column show_title,
	drop column episode_title,
	modify program_id varchar(255) not null,
	add label varchar(275) not null,
    drop index show_title_index;    
    
update user set timezone='PST8PDT' where timezone='PST';
update user set timezone='MST7MDT' where timezone='MST';
update user set timezone='CST6CDT' where timezone='CST';
update user set timezone='EST5EDT' where timezone='EST';
update user set timezone='US/Alaska' where timezone='AKST';
update user set timezone='US/Hawaii' where timezone='HST';
	
