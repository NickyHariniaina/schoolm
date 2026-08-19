insert into course_assignment (course_id, group_id, academic_year, semester, credits)
select cg.course_id,
       cg.group_id,
       (co.entry_year + (case c.semester when 'S1' then 0 when 'S2' then 0 when 'S3' then 1 when 'S4' then 1 when 'S5' then 2 when 'S6' then 2 end)) as academic_year,
       c.semester,
       c.credit
from course_group cg
         join course c on c.id = cg.course_id
         join "group" g on g.id = cg.group_id
         join cohort co on co.id = g.cohort_id;

insert into course_assignment_teacher (course_assignment_id, teacher_id)
select ca.id, ct.teacher_id
from course_teacher ct
         join course_assignment ca
              on ca.course_id = ct.course_id;