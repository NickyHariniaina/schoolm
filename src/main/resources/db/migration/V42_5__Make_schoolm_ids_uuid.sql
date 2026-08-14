alter table student
    drop constraint group_id_fk;

alter table "group"
    drop constraint group_cohort_id_fk;

alter table course_group
    drop constraint course_group_course_id_fk;

alter table course_group
    drop constraint course_group_group_id_fk;

alter table course_teacher
    drop constraint course_teacher_course_id_fk;

alter table course_teacher
    drop constraint course_teacher_teacher_id_fk;

alter table exam
    drop constraint exam_course_id_fk;

alter table grade
    drop constraint grade_student_id_fk;

alter table grade
    drop constraint grade_exam_id_fk;

alter table cohort
    alter column id type uuid using id::uuid;

alter table "group"
    alter column id type uuid using id::uuid;

alter table student
    alter column id type uuid using id::uuid;

alter table student
    alter column group_id type uuid using group_id::uuid;

alter table teacher
    alter column id type uuid using id::uuid;

alter table admin
    alter column id type uuid using id::uuid;

alter table course
    alter column id type uuid using id::uuid;

alter table course_group
    alter column course_id type uuid using course_id::uuid;

alter table course_group
    alter column group_id type uuid using group_id::uuid;

alter table course_teacher
    alter column course_id type uuid using course_id::uuid;

alter table course_teacher
    alter column teacher_id type uuid using teacher_id::uuid;

alter table exam
    alter column id type uuid using id::uuid;

alter table exam
    alter column course_id type uuid using course_id::uuid;

alter table grade
    alter column id type uuid using id::uuid;

alter table grade
    alter column student_id type uuid using student_id::uuid;

alter table grade
    alter column exam_id type uuid using exam_id::uuid;

alter table student
    add constraint group_id_fk foreign key (group_id) references "group" (id);

alter table "group"
    add constraint group_cohort_id_fk foreign key (cohort_id) references cohort (id);

alter table course_group
    add constraint course_group_course_id_fk foreign key (course_id) references course (id);

alter table course_group
    add constraint course_group_group_id_fk foreign key (group_id) references "group" (id);

alter table course_teacher
    add constraint course_teacher_course_id_fk foreign key (course_id) references course (id);

alter table course_teacher
    add constraint course_teacher_teacher_id_fk foreign key (teacher_id) references teacher (id);

alter table exam
    add constraint exam_course_id_fk foreign key (course_id) references course (id);

alter table grade
    add constraint grade_student_id_fk foreign key (student_id) references student (id);

alter table grade
    add constraint grade_exam_id_fk foreign key (exam_id) references exam (id);