insert into "group" (ref, track, cohort_id) values
  ('K1', 'EL',     (select id from cohort where ref = 'K')),
  ('K2', 'EL',     (select id from cohort where ref = 'K')),
  ('K3', 'TN',     (select id from cohort where ref = 'K')),
  ('J1', 'EL',     (select id from cohort where ref = 'J')),
  ('J2', 'EL',     (select id from cohort where ref = 'J')),
  ('N1', 'COMMON', (select id from cohort where ref = 'N')),
  ('N2', 'COMMON', (select id from cohort where ref = 'N')),
  ('N3', 'COMMON', (select id from cohort where ref = 'N')),
  ('N4', 'COMMON', (select id from cohort where ref = 'N'));
