select  s.search_id, (UNIX_TIMESTAMP(created_time)-UNIX_TIMESTAMP('2007-01-27 11:10:30')) as offset, channel, headend_id, program_id, lineup_device, keyword_text, weight 
into outfile '/tmp/script.txt'
from search as s left join keyword as k 
on s.search_id=k.search_id 
where created_time >= '2007-01-27 11:10:30' && created_time <= '2007-01-27 12:10:30'  and channel=41
order by created_time, search_id