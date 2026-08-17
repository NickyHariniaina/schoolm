-- Deterministic grades for every student on every exam of their group's courses.
-- ~2/3 of students (profile 0/1) score >= 10 everywhere -> graduate candidates.
-- ~1/3 (profile 2) have at least one grade < 10.
-- PRO4 has no exams, so no grades for it.

with student_exams as (
  select s.id as student_id, e.id as exam_id, s.reference
  from student s
  join course_group cg on cg.group_id = s.group_id
  join exam e on e.course_id = cg.course_id
),
profiles as (
  select distinct
    student_id,
    mod(('x' || substr(md5(reference || ':profile'), 1, 8))::bit(32)::bigint, 3) as profile
  from student_exams
),
generated as (
  select se.student_id,
         se.exam_id,
         (case
           when p.profile in (0, 1)
             then 10 + mod(('x' || substr(md5(se.reference || ':' || se.exam_id::text), 1, 8))::bit(32)::bigint, 11)
           else 5 + mod(('x' || substr(md5(se.reference || ':' || se.exam_id::text), 1, 8))::bit(32)::bigint, 16)
         end)::numeric(4, 2) as value
  from student_exams se
  join profiles p on p.student_id = se.student_id
)
insert into grade (student_id, exam_id, value, change_reason)
select student_id, exam_id, value, null
from generated;
