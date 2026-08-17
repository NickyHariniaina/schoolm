with student_base as (
  select
    'STD' || to_char(grp.entry_year % 100, 'FM00')
         || lpad(((grp.grp_idx - 1) * 40 + s.n)::text, 3, '0') as reference,
    grp.id                                             as group_id,
    (row_number() over (order by grp.entry_year, grp.ref, s.n) - 1) as idx
  from (
    select g.id, g.ref, c.entry_year,
           row_number() over (partition by c.id order by g.ref) as grp_idx
    from "group" g
    join cohort c on c.id = g.cohort_id
  ) grp
  cross join generate_series(1, 40) as s(n)
),
with_names as (
  select *,
    (array[
      'Jean','Pierre','Marie','Claire','Sophie','Lucas','Hugo','Lea',
      'Emma','Louis','Camille','Nathan','Chloe','Theo','Manon','Jules',
      'Ines','Paul','Alice','Noah','Romane','Tom','Mathis','Sarah',
      'Eva','Antoine','Julie','Maxime','Laura','Nicolas','Amelie','Baptiste',
      'Charlotte','Guillaume','Margaux','Quentin'
    ])[1 + idx % 36] as first_name,
    (array[
      'Dupont','Martin','Bernard','Thomas','Petit','Robert','Richard','Durand',
      'Moreau','Simon','Laurent','Lefebvre','Michel','Garcia','David','Bertrand',
      'Roux','Vincent','Fournier','Morel','Girard','Andre','Mercier','Blanc',
      'Guerin','Boyer','Garnier','Chevalier','Francois','Legrand','Gauthier','Nicolas',
      'Perrin','Robin','Clement','Morin','Mathieu','Fontaine','Rousseau','Maillard'
    ])[1 + idx % 40] as last_name
  from student_base
),
with_emails as (
  select *,
    row_number() over (partition by first_name order by reference) as dup
  from with_names
)
insert into student (reference, group_id, email, first_name, last_name, role)
select reference,
       group_id,
       case
         when dup = 1 then lower(first_name)
         else lower(first_name) || '.' || (dup - 1)
       end || '@mail.hei.school' as email,
       first_name,
       last_name,
       'STUDENT' as role
from with_emails;
