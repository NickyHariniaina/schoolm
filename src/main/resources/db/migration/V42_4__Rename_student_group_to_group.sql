alter table student_group rename to "group";

alter table "group" rename constraint student_group_cohort_id_fk to group_cohort_id_fk;

alter table student rename constraint student_group_id_fk to group_id_fk;

alter index student_group_pkey rename to group_pkey;
