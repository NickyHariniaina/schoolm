-- Courses : 30 credits per semester, real HEI UE codes.
-- track is only metadata; shared courses are taken by both EL and TN groups.

insert into course (ref, title, credit, track, semester) values
  -- Tronc commun S1
  ('PROG1',   'Programmation 1 - Bases de la programmation',         6, 'COMMON', 'S1'),
  ('THEORIE1','Theorie 1 - Logique, ensembles et algebre de Boole',  4, 'COMMON', 'S1'),
  ('DONNEES1','Donnees 1 - Bases de donnees relationnelles',         4, 'COMMON', 'S1'),
  ('WEB1',    'Web 1 - Bases du developpement web',                  6, 'COMMON', 'S1'),
  ('SYS1',    'Systemes 1 - Systemes d exploitation',                6, 'COMMON', 'S1'),
  ('LV1',     'LV 1 - Anglais 1',                                    4, 'COMMON', 'S1'),
  -- Tronc commun S2
  ('PROG2',   'Programmation 2 - Programmation orientee objet',      6, 'COMMON', 'S2'),
  ('MGT1',    'Management 1 - Gestion d equipe',                     4, 'COMMON', 'S2'),
  ('DONNEES2','Donnees 2 - Optimisation des requetes',               4, 'COMMON', 'S2'),
  ('WEB2',    'Web 2 - Developpement web avance',                    8, 'COMMON', 'S2'),
  ('SYS2',    'Systemes 2 - Reseaux',                                8, 'COMMON', 'S2'),
  -- Tronc commun S3
  ('PROG3',   'Programmation 3 - Structuration',                     8, 'COMMON', 'S3'),
  ('MGT2',    'Management 2 - Gestion de projet',                    5, 'COMMON', 'S3'),
  ('WEB3',    'Web 3 - Backend',                                     8, 'COMMON', 'S3'),
  ('SYS3',    'Systemes 3 - Infrastructure cloud',                   6, 'COMMON', 'S3'),
  ('PRO2',    'Experience professionnelle 2 - Alternance',           3, 'COMMON', 'S3'),
  -- S4 partage EL / TN
  ('SECU1',   'Securite 1 - Securite informatique',                  7, 'COMMON', 'S4'),
  ('METIER1', 'Metier 1 - Decouverte du monde professionnel',        6, 'COMMON', 'S4'),
  ('DONNEES4','Donnees 4 - Science des donnees',                     6, 'COMMON', 'S4'),
  ('PRO3',    'Entrepreneuriat',                                     3, 'COMMON', 'S4'),
  -- S5 partage EL / TN
  ('SECU2',   'Securite 2 - Securite offensive',                     7, 'COMMON', 'S5'),
  ('IA1',     'IA 1 - Apprentissage automatique',                    4, 'COMMON', 'S5'),
  ('MOB1',    'Projet de mobilite internationale',                   6, 'COMMON', 'S5'),
  ('PRO1',    'Experience professionnelle 1',                        6, 'COMMON', 'S5'),
  -- S6 partage EL / TN
  ('PRO4',    'Experience professionnelle 4 - Stage de fin d etudes',14, 'COMMON', 'S6'),
  ('PROJET1', 'Projet de specialisation',                           12, 'COMMON', 'S6'),
  ('LV2',     'LV 2 - Anglais 2',                                    4, 'COMMON', 'S6'),
  -- Track EL
  ('PROG4',   'Programmation 4 - Qualite et surete',                 8, 'EL',     'S4'),
  ('PROG5',   'Maintenabilite du code',                              7, 'EL',     'S5'),
  -- Track TN
  ('TN1',     'TN 1 - Les fondamentaux de la numerisation',          3, 'TN',     'S4'),
  ('TN4',     'TN 4 - Projet numerique',                             5, 'TN',     'S4'),
  ('TN2',     'TN 2 - Introduction aux reseaux et a la technologie', 3, 'TN',     'S5'),
  ('TN3',     'TN 3 - Competences numeriques et vie de l entreprise',4, 'TN',     'S5');

-- Assign courses to groups.
-- EL groups (K1, K2, J1, J2): tronc commun S1-S3 + track EL S4-S6.
-- TN group (K3): tronc commun S1-S3 + track TN S4-S6.
-- COMMON groups (N1-N4): tronc commun S1-S2 only.

insert into course_group (course_id, group_id)
select c.id, g.id
from course c
cross join (select id from "group" where ref in ('K1', 'K2', 'J1', 'J2')) g
where c.semester in ('S1', 'S2', 'S3', 'S4', 'S5', 'S6')
  and not (c.ref in ('TN1', 'TN2', 'TN3', 'TN4'));

insert into course_group (course_id, group_id)
select c.id, g.id
from course c
cross join (select id from "group" where ref = 'K3') g
where c.semester in ('S1', 'S2', 'S3', 'S4', 'S5', 'S6')
  and not (c.ref in ('PROG4', 'PROG5'));

insert into course_group (course_id, group_id)
select c.id, g.id
from course c
cross join (select id from "group" where ref in ('N1', 'N2', 'N3', 'N4')) g
where c.semester in ('S1', 'S2');

-- Assign teachers to courses (2 teachers per course, deterministic).

insert into course_teacher (course_id, teacher_id)
select c.id, t.id
from (select id, row_number() over (order by ref) as idx from course) c
cross join (select id, row_number() over (order by email) as idx from teacher) t
where t.idx in (mod(c.idx - 1, 12) + 1, mod(c.idx, 12) + 1);
