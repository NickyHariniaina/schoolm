insert into cohort (id, ref, entry_year, created_at, updated_at)
values ('cohort-2024', 'C2024', 2024, now(), now());

insert into student_group (id, ref, track, cohort_id, created_at, updated_at)
values ('group-g1', 'G1', 'COMMON', 'cohort-2024', now(), now()),
       ('group-g2', 'G2', 'EL', 'cohort-2024', now(), now());

insert into course (id, ref, title, credit, track, semester, created_at, updated_at)
values ('course-prog1', 'PROG1', 'Programmation 1', 4, 'COMMON', 'S1', now(), now()),
       ('course-prog2', 'PROG2', 'Programmation 2', 4, 'COMMON', 'S2', now(), now()),
       ('course-algo1', 'ALGO1', 'Algorithmique 1', 3, 'EL', 'S1', now(), now());

insert into teacher (id, email, first_name, last_name, role, created_at, updated_at)
values ('teacher-jean', 'jean.dupont@schoolm.hei', 'Jean', 'Dupont', 'TEACHER', now(), now()),
       ('teacher-marie', 'marie.curie@schoolm.hei', 'Marie', 'Curie', 'TEACHER', now(), now());

insert into admin (id, email, first_name, last_name, role, created_at, updated_at)
values ('admin-boss', 'boss@schoolm.hei', 'Boss', 'Admin', 'ADMIN', now(), now());

insert into student (id, reference, group_id, email, first_name, last_name, role, created_at, updated_at)
values ('student-alice', 'STU-001', 'group-g1', 'alice@schoolm.hei', 'Alice', 'Martin', 'STUDENT', now(), now()),
       ('student-bob', 'STU-002', 'group-g1', 'bob@schoolm.hei', 'Bob', 'Martin', 'STUDENT', now(), now()),
       ('student-carol', 'STU-003', 'group-g2', 'carol@schoolm.hei', 'Carol', 'Durand', 'STUDENT', now(), now()),
       ('student-dave', 'STU-004', 'group-g2', 'dave@schoolm.hei', 'Dave', 'Durand', 'STUDENT', now(), now());

insert into course_group (course_id, group_id)
values ('course-prog1', 'group-g1'),
       ('course-prog1', 'group-g2'),
       ('course-prog2', 'group-g1'),
       ('course-algo1', 'group-g2');

insert into course_teacher (course_id, teacher_id)
values ('course-prog1', 'teacher-jean'),
       ('course-prog2', 'teacher-jean'),
       ('course-algo1', 'teacher-marie');

insert into exam (id, course_id, date_exam, coef_numerator, coef_denominator, created_at, updated_at)
values ('exam-prog1-1', 'course-prog1', '2024-01-15', 1, 2, now(), now()),
       ('exam-prog1-2', 'course-prog1', '2024-06-15', 1, 2, now(), now()),
       ('exam-algo1-1', 'course-algo1', '2024-03-01', 1, 1, now(), now());

insert into grade (id, student_id, exam_id, value, change_reason, created_at, updated_at)
values ('grade-1', 'student-alice', 'exam-prog1-1', 14.5, null, now(), now()),
       ('grade-2', 'student-bob', 'exam-prog1-1', 12.0, null, now(), now()),
       ('grade-3', 'student-alice', 'exam-prog1-2', 16.0, null, now(), now()),
       ('grade-4', 'student-bob', 'exam-prog1-2', 10.5, null, now(), now()),
       ('grade-5', 'student-carol', 'exam-algo1-1', 16.0, null, now(), now()),
       ('grade-6', 'student-dave', 'exam-algo1-1', 9.5, null, now(), now());
