-- Author: J. Andrew Key
-- Objective:  Demonstrate the ability to pre-aggregate relational data
--	to create a "running total" of sales for each day of business by
--	"ad_id".  Pre-aggregating values can be extrememly useful when
--	preparing reports for business intelligence.

-- (0)  Load data for the demo so that the answers can be seen.
create database running_total;
use running_total;
create table visitor_sales (`date` DATETIME
	, `ad_id` INT
	, `sales` INT
);
insert into visitor_sales (`date`,`ad_id`,`sales`) VALUES 
	  ('2010-01-01',111,1)
	, ('2010-01-01',111,1)
	, ('2010-01-01',222,1)
	, ('2010-01-01',333,1)
	
	, ('2010-01-02',111,0)
	, ('2010-01-02',222,4)
	, ('2010-01-02',222,7)
	, ('2010-01-02',333,9)
	
	, ('2010-01-03',111,6)
	, ('2010-01-03',111,4)
	, ('2010-01-03',222,1)
	, ('2010-01-03',333,8)
	
	, ('2010-01-04',111,1)
	, ('2010-01-04',111,2)
	, ('2010-01-04',222,2)
	, ('2010-01-04',333,0)
;
-- (1) Write a query to generate the DAILY_AD_SUMMARY table, based on the
-- VISITOR_SALES table.
create table daily_ad_summary(`date` DATETIME
	, `ad_id` INT
	, `total_sales` INT
);    
insert into daily_ad_summary(`date`,`ad_id`,`total_sales`) 
select 
	`date`, `ad_id`, SUM(`sales`) 
from 
	visitor_sales 
group by `date`, `ad_id` 
order by `date`, `ad_id`;

-- (2) Write a query to generate the RUNNING_TOTAL table based on either or
-- both of the other tables.
create table running_total (`date` DATETIME
	, `ad_id` INT
	, `cumulative_total_sales` INT
);
insert into running_total (`date`,`ad_id`,`cumulative_total_sales`)
select
	x.`date`
	, x.`ad_id`
	, sum(das.`total_sales`) b
from
	daily_ad_summary das
	INNER JOIN daily_ad_summary x
		ON das.`ad_id` = x.`ad_id`
		AND DATEDIFF(das.`date`, x.`date`) <= 0
group by x.`date`, x.`ad_id`
order by x.`date`, x.`ad_id`
;
