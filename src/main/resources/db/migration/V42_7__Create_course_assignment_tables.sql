create table if not exists course_assignment
(
    id            uuid primary key default gen_random_uuid(),
    course_id     uuid   not null
        constraint course_assignment_course_id_fk references course (id),
    group_id      uuid   not null
        constraint course_assignment_group_id_fk references "group" (id),
    academic_year integer not null,
    semester      varchar not null,
    credits       integer not null,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    constraint course_assignment_uk unique (course_id, group_id, academic_year, semester)
);

create table if not exists course_assignment_teacher
(
    course_assignment_id uuid not null
        constraint course_assignment_teacher_assignment_id_fk references course_assignment (id),
    teacher_id           uuid not null
        constraint course_assignment_teacher_teacher_id_fk references teacher (id),
    primary key (course_assignment_id, teacher_id)
);