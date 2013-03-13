
insert into user
	(user_id, username, password, primary_email, gender, timezone, zipcode,
	 city, state, low_age, high_age, sms_email, earliest_sms, latest_sms, alerts_enabled, registration_complete, sms_verified, sms_verification_code) 
		values (1,'Tester','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','','M','PST','92128',
		'San Diego','CA',20,30,'','08:00','20:00',false, true, false, 'foobar');
	
insert into user 
	(user_id, username, password, primary_email, gender, timezone, zipcode,
	 city, state, low_age, high_age, sms_email, earliest_sms, latest_sms, alerts_enabled, registration_complete, sms_verified, sms_verification_code)
		values (2,'exline','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','chris.exline@appeligo.com','M','PST','92128',
		'San Diego','CA',20,30,'6198469544@messaging.sprintpcs.com','08:00','20:00',true, true, false, 'foobar');

insert into user
	(user_id, username, password, primary_email, gender, timezone, zipcode,
	 city, state, low_age, high_age, sms_email, earliest_sms, latest_sms, alerts_enabled, registration_complete, sms_verified, sms_verification_code)
		values (3,'kadel','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','rich.kadel@appeligo.com','M','PST','92128',
		'San Diego','CA',20,30,'6198463324@messaging.sprintpcs.com','08:00','20:00',true, true, false, 'foobar');

insert into user (user_id, username, password, timezone,zipcode, primary_email, alerts_enabled, registration_complete, sms_verified, sms_verification_code) 
	values (4, 'almilli', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'PST','00000', 'david.almilli@appeligo.com', true, true, false, 'foobar');

insert into user (user_id, username, password, timezone, zipcode, primary_email, alerts_enabled, registration_complete, sms_verified, sms_verification_code) 
	values (5, 'fear', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'PST','92119', 'jake.fear@appeligo.com', true, true, false, 'foobar');
	
insert into groups (group_id, name) values (1, 'admin');
insert into groups (group_id, name) values (2, 'demo');
insert into groups (group_id, name) values (3, 'user');

insert into user_groups (user_id, username, group_name) values (5, 'fear', 'admin');
insert into user_groups (user_id, username, group_name) values (5, 'fear', 'demo');
insert into user_groups (user_id, username, group_name) values (5, 'fear', 'user');
insert into user_groups (user_id, username, group_name) values (3, 'kadel', 'admin');
insert into user_groups (user_id, username, group_name) values (3, 'kadel', 'demo');
insert into user_groups (user_id, username, group_name) values (3, 'kadel', 'user');
insert into user_groups (user_id, username, group_name) values (2, 'exline', 'admin');
insert into user_groups (user_id, username, group_name) values (2, 'exline', 'demo');
insert into user_groups (user_id, username, group_name) values (2, 'exline', 'user');
insert into user_groups (user_id, username, group_name) values (4, 'almilli', 'admin');
insert into user_groups (user_id, username, group_name) values (4, 'almilli', 'demo');
insert into user_groups (user_id, username, group_name) values (4, 'almilli', 'user');

