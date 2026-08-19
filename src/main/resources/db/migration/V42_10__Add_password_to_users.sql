alter table student add column if not exists password varchar(255);
alter table teacher add column if not exists password varchar(255);
alter table admin add column if not exists password varchar(255);

update student set password = '$2a$10$vQ.M0zvBtLJuyrSadV0q8uarFVF8LIMCct7Jeq9xBhLPOzhyEDtae' where password is null;
update teacher set password = '$2a$10$vQ.M0zvBtLJuyrSadV0q8uarFVF8LIMCct7Jeq9xBhLPOzhyEDtae' where password is null;
update admin set password = '$2a$10$vQ.M0zvBtLJuyrSadV0q8uarFVF8LIMCct7Jeq9xBhLPOzhyEDtae' where password is null;

insert into admin (email, first_name, last_name, role, password)
select 'admin@hei.school', 'Admin', 'Principal', 'ADMIN', '$2a$10$vQ.M0zvBtLJuyrSadV0q8uarFVF8LIMCct7Jeq9xBhLPOzhyEDtae'
where not exists (select 1 from admin where lower(email) = 'admin@hei.school');