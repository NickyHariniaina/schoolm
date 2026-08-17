-- Two exams per course (1/2 + 1/2), except PRO4 (stage, no exams).
-- Dates are cosmetic: one set per semester, shared by all cohorts.

insert into exam (course_id, date_exam, coef_numerator, coef_denominator)
select c.id, ed.date_exam, 1, 2
from course c
join (values
  ('S1', date '2025-12-12'),
  ('S1', date '2026-01-23'),
  ('S2', date '2026-06-12'),
  ('S2', date '2026-07-10'),
  ('S3', date '2025-12-12'),
  ('S3', date '2026-01-23'),
  ('S4', date '2026-06-12'),
  ('S4', date '2026-07-10'),
  ('S5', date '2026-12-12'),
  ('S5', date '2027-01-23'),
  ('S6', date '2027-06-12'),
  ('S6', date '2027-07-10')
) as ed(semester, date_exam) on ed.semester = c.semester
where c.ref <> 'PRO4';
