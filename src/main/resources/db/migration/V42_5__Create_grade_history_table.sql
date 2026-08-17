CREATE TABLE IF NOT EXISTS grade_history
(
    id            UUID         NOT NULL,
    grade_id      UUID         NOT NULL,
    student_id    UUID         NOT NULL,
    exam_id       UUID         NOT NULL,
    old_value     DECIMAL      NOT NULL,
    new_value     DECIMAL      NOT NULL,
    change_reason VARCHAR(255) NOT NULL,
    changed_at    TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_grade_history PRIMARY KEY (id)
);