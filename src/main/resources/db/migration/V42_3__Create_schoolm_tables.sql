create table cohort
(
    id         varchar primary key,
    ref        varchar not null,
    entry_year integer  not null,
    created_at timestamptz,
    updated_at timestamptz
);

create table student_group
(
    id         varchar primary key,
    ref        varchar not null,
    track      varchar not null,
    cohort_id  varchar   not null
        constraint student_group_cohort_id_fk references cohort (id),
    created_at timestamptz,
    updated_at timestamptz
);

create table student
(
    id         varchar primary key,
    reference  varchar not null
        constraint student_reference_unique unique,
    group_id   varchar
        constraint student_group_id_fk references student_group (id),
    email      varchar not null
        constraint student_email_unique unique,
    first_name varchar not null,
    last_name  varchar not null,
    role       varchar not null,
    created_at timestamptz,
    updated_at timestamptz
);

create table teacher
(
    id         varchar primary key,
    email      varchar not null
        constraint teacher_email_unique unique,
    first_name varchar not null,
    last_name  varchar not null,
    role       varchar not null,
    created_at timestamptz,
    updated_at timestamptz
);

create table admin
(
    id         varchar primary key,
    email      varchar not null
        constraint admin_email_unique unique,
    first_name varchar not null,
    last_name  varchar not null,
    role       varchar not null,
    created_at timestamptz,
    updated_at timestamptz
);

create table course
(
    id         varchar primary key,
    ref        varchar not null
        constraint course_ref_unique unique,
    title      varchar not null,
    credit     integer  not null,
    track      varchar not null,
    semester   varchar not null,
    created_at timestamptz,
    updated_at timestamptz
);

create table course_group
(
    course_id varchar not null
        constraint course_group_course_id_fk references course (id),
    group_id  varchar not null
        constraint course_group_group_id_fk references student_group (id),
    primary key (course_id, group_id)
);

create table course_teacher
(
    course_id  varchar not null
        constraint course_teacher_course_id_fk references course (id),
    teacher_id varchar not null
        constraint course_teacher_teacher_id_fk references teacher (id),
    primary key (course_id, teacher_id)
);

create table exam
(
    id               varchar primary key,
    course_id        varchar   not null
        constraint exam_course_id_fk references course (id),
    date_exam        date      not null,
    coef_numerator   integer   not null,
    coef_denominator integer   not null,
    created_at       timestamptz,
    updated_at       timestamptz
);

create table grade
(
    id            varchar primary key,
    student_id    varchar        not null
        constraint grade_student_id_fk references student (id),
    exam_id       varchar        not null
        constraint grade_exam_id_fk references exam (id),
    value         numeric(4, 2)  not null,
    change_reason varchar,
    created_at    timestamptz,
    updated_at    timestamptz
);
