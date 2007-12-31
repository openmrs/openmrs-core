select count(*) from orders
where discontinued_date > auto_expire_date
   or discontinued_date < start_date
   or auto_expire_date < start_date;

select count(*) from orders
where discontinued = false
  and (discontinued_date is not null or discontinued_by is not null or discontinued_reason is not null);

select dt.name, count(*)
from obs o
	inner join concept c on o.concept_id = c.concept_id
	inner join concept_datatype dt on c.datatype_id = dt.concept_datatype_id
where (dt.hl7_abbreviation in ('NM', 'SN') and o.value_numeric is null)
   or (dt.hl7_abbreviation in ('DT', 'TM', 'TS') and o.value_datetime is null)
   or (dt.hl7_abbreviation in ('CWE') and o.value_coded is null)
   or (dt.hl7_abbreviation in ('ST') and o.value_text is null)
group by dt.name;