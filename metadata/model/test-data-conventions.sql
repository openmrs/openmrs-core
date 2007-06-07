select count(*) from orders
where discontinued_date > auto_expire_date
   or discontinued_date < start_date
   or auto_expire_date < start_date;

select count(*) from orders
where discontinued = false
  and (discontinued_date is not null or discontinued_by is not null or discontinued_reason is not null);