create table if not exists group_flow
(
    id              uuid primary key default gen_random_uuid(),
    student_id      uuid   not null
        constraint group_flow_student_id_fk references student (id),
    group_id        uuid   not null
        constraint group_flow_group_id_fk references "group" (id),
    group_flow_type varchar not null,
    created_at      timestamptz not null default now()
);