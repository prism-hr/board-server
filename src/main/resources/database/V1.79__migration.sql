SET @uclId = (
  SELECT resource.id
  FROM resource
  WHERE resource.scope = 'UNIVERSITY');

INSERT INTO resource (parent_id, scope, state, previous_state, name, summary, created_timestamp, updated_timestamp)
VALUES (@uclId, 'DEPARTMENT', 'ACCEPTED', 'ACCEPTED', 'Biochemical Engineering',
        'The UCL Department of Biochemical Engineering is unique - both in the diversity of our programmes and the outstanding facilities we offer.We work in close partnership with industry in support of a dynamic, innovative sector and our alumni continue to pioneer the discipline around the world.\n\nOur excellent international reputation derives from a shared passion to understand how life science discoveries can be converted into commercially viable products on a truly global scale.',
        '2015-10-16 08:11:41', '2017-09-14 09:44:50'),
  (@uclId, 'DEPARTMENT', 'ACCEPTED', 'ACCEPTED', 'Electronic & Electrical Engineering',
   'We are proud of our heritage as the first Electrical Engineering department in England and our ethos of constant development and innovation continues a tradition inspired by our first Head of Department, Professor Sir Ambrose Fleming, inventor of the thermionic valve and hence the founder of the discipline of electronics. We offer both three year (BEng) and four year (MEng) undergraduate programmes, which are continually updated and shaped by both industrial and academic requirements, as well as a range of specialist MSc, PhD and Engineering Doctorate programmes.',
   '2015-10-16 08:34:11', '2016-07-23 07:50:54'),
  (@uclId, 'DEPARTMENT', 'ACCEPTED', 'ACCEPTED', 'Computer Science',
   'In the 2014 Research Excellence Framework (REF) evaluation UCL was ranked in first place for Computer Science, out of 89 Universities assessed, and considerably ahead of other Institutions. 61% of its research work is rated as world-leading (the highest possible category) and 96% of its research work is rated as internationally excellent.',
   '2015-11-13 17:04:09', '2017-05-17 20:37:12');

INSERT INTO resource (parent_id, scope, state, previous_state, name, summary, created_timestamp, updated_timestamp)
  SELECT
    id,
    'BOARD',
    'ACCEPTED',
    'ACCEPTED',
    'Careers',
    CONCAT('Forum for partner organizations and staff to share career opportunities with ', name, ' students.'),
    created_timestamp,
    updated_timestamp
  FROM resource
  WHERE scope = 'DEPARTMENT';

INSERT INTO resource_category (resource_id, name, type, ordinal, created_timestamp, updated_timestamp)
  SELECT
    resource.id,
    member_category.name,
    'MEMBER',
    member_category.ordinal,
    resource.created_timestamp,
    resource.updated_timestamp
  FROM resource
    INNER JOIN (
                 SELECT
                   'UNDERGRADUATE_STUDENT' AS name,
                   0                       AS ordinal
                 UNION
                 SELECT
                   'MASTER_STUDENT' AS name,
                   1                AS ordinal
                 UNION
                 SELECT
                   'RESEARCH_STUDENT' AS name,
                   2                  AS ordinal) AS member_category
  WHERE resource.scope = 'DEPARTMENT';

INSERT INTO resource_category (resource_id, name, type, ordinal, created_timestamp, updated_timestamp)
  SELECT
    resource.id,
    post_category.name,
    'POST',
    post_category.ordinal,
    resource.created_timestamp,
    resource.updated_timestamp
  FROM resource
    INNER JOIN (
                 SELECT
                   'Employment' AS name,
                   0            AS ordinal
                 UNION
                 SELECT
                   'Internship' AS name,
                   1            AS ordinal
                 UNION
                 SELECT
                   'Volunteering' AS name,
                   2              AS ordinal) AS post_category
  WHERE resource.scope = 'BOARD';

INSERT INTO resource_relation (resource1_id, resource2_id, created_timestamp, updated_timestamp)
  SELECT
    parent_id,
    id,
    created_timestamp,
    updated_timestamp
  FROM resource
  WHERE resource.scope = 'DEPARTMENT'
  UNION
  SELECT
    id,
    id,
    created_timestamp,
    updated_timestamp
  FROM resource
  WHERE scope = 'DEPARTMENT'
  UNION
  SELECT
    @uclId,
    id,
    created_timestamp,
    updated_timestamp
  FROM resource
  WHERE scope = 'BOARD'
  UNION
  SELECT
    parent_id,
    id,
    created_timestamp,
    updated_timestamp
  FROM resource
  WHERE scope = 'BOARD'
  UNION
  SELECT
    id,
    id,
    created_timestamp,
    updated_timestamp
  FROM resource
  WHERE scope = 'BOARD';

INSERT INTO resource_operation (resource_id, action, created_timestamp, updated_timestamp)
  SELECT
    id,
    'EXTEND',
    created_timestamp,
    updated_timestamp
  FROM resource
  WHERE scope IN ('DEPARTMENT', 'BOARD')
  ORDER BY id;

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES
  ('b169293b-ac76-11e7-b423-2c600c86e54c', 'Alastair', 'Knowles', 'alastair@prism.hr', '1aff72f1f781a27944da350abd81e9eaf76fa12cf32f251ad446fbbfd8bc867c', 'SHA256', NULL, NULL,
   '2017-10-08 23:13:31', '2017-10-08 23:13:31');

SET @alastairAdminId = (
  SELECT LAST_INSERT_ID());

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES
  ('25bd8327-ac77-11e7-b423-2c600c86e54c', 'Alastair', 'Knowles', 'alastairknowles@gmail.com', '1aff72f1f781a27944da350abd81e9eaf76fa12cf32f251ad446fbbfd8bc867c', 'SHA256', NULL,
   NULL, '2017-10-08 23:13:31', '2017-10-08 23:13:31');

SET @alastairStudentId = (
  SELECT LAST_INSERT_ID());

SET @biochemicalId = (
  SELECT id
  FROM resource
  WHERE name = 'Biochemical Engineering');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES
  ('1d41fd36-ac76-11e7-b423-2c600c86e54c', 'Iris', 'Luke', 'i.luke@ucl.ac.uk', '0aa56ebedfacaa0086294733c7f44712', 'MD5', NULL, NULL, '2017-10-08 23:13:31', '2017-10-08 23:13:31');

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp, updated_timestamp)
VALUES (@biochemicalId, @alastairAdminId, 'ADMINISTRATOR', 'ACCEPTED', NOW(), NOW()),
  (@biochemicalId, last_insert_id(), 'ADMINISTRATOR', 'ACCEPTED', NOW(), NOW());

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae3f8-ac78-11e7-b423-2c600c86e54c', 'SOPHIE', 'MAYRBAEURL', 'sophie.mayrbaeurl.13@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'f1RJFsZSIo', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae5bc-ac78-11e7-b423-2c600c86e54c', 'MEHDI', 'MOHAMADI', 'mehdi.mohamadi.14@ucl.ac.uk', '4e5bd69202cf836a1e25db70f95f6f27', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae641-ac78-11e7-b423-2c600c86e54c', 'SANTINO', 'ONYEME', 'santino.onyeme.13@ucl.ac.uk', '465e82f98818644199c61bb335d32fdc', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae6ae-ac78-11e7-b423-2c600c86e54c', 'ELSA', 'NOAKS', 'elsa.noaks.12@ucl.ac.uk', '216d617c1578545695714f600f612dfd', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae721-ac78-11e7-b423-2c600c86e54c', 'Shahidul', 'ISLAM', 'shahidul.islam.11@ucl.ac.uk', '7db66710ad487cec00afe5b3bdbbe231', 'MD5', 'LINKEDIN', 'b0_y4xo9zP',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae800-ac78-11e7-b423-2c600c86e54c', 'SEOKYOUNG', 'KIM', 'seokyoung.kim.14@ucl.ac.uk', 'fb07ce1fe90dce810f7f427586a89a98', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae855-ac78-11e7-b423-2c600c86e54c', 'MICHALINA', 'PECZKOWSKA', 'michalina.peczkowska.13@ucl.ac.uk', '422b6aab8dcfba1077fa37762fabbce7', 'MD5', NULL, NULL,
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92ae9e9-ac78-11e7-b423-2c600c86e54c', 'HARI', 'DADDAR', 'hari.daddar.13@ucl.ac.uk', 'b40b8f862d816d65b4c1dcdaf6accfc1', 'MD5', 'LINKEDIN', 'Pq9SLkx5QX', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aeae1-ac78-11e7-b423-2c600c86e54c', 'MATTHEW', 'AKITA', 'matthew.akita.13@ucl.ac.uk', 'd37c8e20a6b84540831edeb3c5351833', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aeb59-ac78-11e7-b423-2c600c86e54c', 'BABATUNDE', 'LAMUREN', 'richard.lamuren.14@ucl.ac.uk', '904b7040b54153b0875fd82d206d44fa', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aec5d-ac78-11e7-b423-2c600c86e54c', 'FELIX', 'ROBINSON', 'felix.robinson.12@ucl.ac.uk', '3431b16c51b51e670cf8ddfb07f8321f', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aed91-ac78-11e7-b423-2c600c86e54c', 'ALIREZA', 'MEGHDADI', 'alireza.meghdadi.13@ucl.ac.uk', '568630e794227cbaac159f3f827518cd', 'MD5', 'LINKEDIN', 'opQ37_reWu',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aee28-ac78-11e7-b423-2c600c86e54c', 'MAARIYAH', 'SAMAD', 'maariyah.samad.14@ucl.ac.uk', '623f1eca405412accc5808df345a5dbe', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aee8e-ac78-11e7-b423-2c600c86e54c', 'HARSHAL', 'DESAI', 'harshal.desai.12@ucl.ac.uk', '6e5a3a5fe6961bcaaa7d6133c0a57f49', 'MD5', 'LINKEDIN', 'FZohBJtJSL',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aef01-ac78-11e7-b423-2c600c86e54c', 'SAMUEL', 'OBIORAH AGBALAKA', 'sammy.agbalaka.13@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'JFy5tkcSW1', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af00b-ac78-11e7-b423-2c600c86e54c', 'Amandeep', 'Varia', 'amandeep.varia.14@ucl.ac.uk', 'bf4e8ecbbd96416b1d2e09df234f645b', 'MD5', 'LINKEDIN', 'ARNciU6rEY',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af212-ac78-11e7-b423-2c600c86e54c', 'GEORGINA', 'HUNTER', 'georgina.hunter.13@ucl.ac.uk', 'dd9be9d755046ad4a4e96ccf28730b85', 'MD5', 'LINKEDIN', 'qhhpr7gdUq',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af2a3-ac78-11e7-b423-2c600c86e54c', 'MARIA', 'PARAU', 'maria.parau.11@ucl.ac.uk', '965fe7ffd9338dd48247a8906ffd87e7', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af370-ac78-11e7-b423-2c600c86e54c', 'THOMAS', 'BENGE', 'thomas.benge.14@ucl.ac.uk', 'a6b9992366412a808a70dda9938cc73f', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af3c5-ac78-11e7-b423-2c600c86e54c', 'ARTHUR', 'RADLEY', 'arthur.radley.11@ucl.ac.uk', 'eeb4ca3a6aedbe63963f64735b85cad1', 'MD5', 'LINKEDIN', 'imLvG2VDu8',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af4ce-ac78-11e7-b423-2c600c86e54c', 'PAULINA', 'VACHET', 'paulina.vachet.11@ucl.ac.uk', 'ffb7b940accccbddd8bf754b79a2a797', 'MD5', 'LINKEDIN', 'PlhA_zacLn',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af53b-ac78-11e7-b423-2c600c86e54c', 'HANNA', 'MAHAL', 'hanna.mahal.14@ucl.ac.uk', '4b0994c84ca644d9320987b57397b869', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af58a-ac78-11e7-b423-2c600c86e54c', 'DALE', 'STIBBS', 'dale.stibbs.14@ucl.ac.uk', 'e5b04461fe6def32dc9bcbbfd518c519', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af5f6-ac78-11e7-b423-2c600c86e54c', 'SIDDIQUE', 'UDDIN', 'siddique.uddin.12@ucl.ac.uk', 'f4764f09328837a08debcf68a5b5f458', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af669-ac78-11e7-b423-2c600c86e54c', 'CHARLOTTE', 'COLES', 'charlotte.coles.13@ucl.ac.uk', 'a52bf1071e8aa33496eec3f0fb81fbb7', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af6fa-ac78-11e7-b423-2c600c86e54c', 'Yash', 'Mishra', 'yash.mishra.13@ucl.ac.uk', '3257859213f1cdeb523ae5c7b01b1abe', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af760-ac78-11e7-b423-2c600c86e54c', 'Thomas', 'van Tergouw', 'thomas.tergouw.12@ucl.ac.uk', '4d87b83da5f0b3aef62a98545e4d31f2', 'MD5', 'LINKEDIN', 'pAxMcoQjms',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af7b5-ac78-11e7-b423-2c600c86e54c', 'RADHA', 'PATEL', 'radha.patel.14@ucl.ac.uk', 'faa7576d26f5b9478ce0eca021d6cf08', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af85e-ac78-11e7-b423-2c600c86e54c', 'Hamza', 'Patel', 'hamza.patel.14@ucl.ac.uk', 'b56ba14b5d55b24922c1d3e5e888e868', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92af95c-ac78-11e7-b423-2c600c86e54c', 'LEWIS', 'MOFFAT', 'lewis.moffat.13@ucl.ac.uk', 'ddfa997db831b195f54e1179463e24fd', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92afa47-ac78-11e7-b423-2c600c86e54c', 'DHAMAYANTHY', 'PARAMESWARAN', 'dhamayanthy.parameswaran.11@ucl.ac.uk', '6cbf893ef66274a3704eef63302ba847', 'MD5', 'LINKEDIN',
   '7g4ugf-FbD', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92afaf0-ac78-11e7-b423-2c600c86e54c', 'NUR', 'BADROL HISHAM', 'nur.hisham.14@ucl.ac.uk', 'f8eb082ad3db101836780bf243231bb5', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92afcbb-ac78-11e7-b423-2c600c86e54c', 'NING', 'LU', 'ning.lu.12@ucl.ac.uk', 'ffce402a7e84ef7d4b5aa6c3aaf08977', 'MD5', 'LINKEDIN', 'cKvsp-H7OB', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92afd70-ac78-11e7-b423-2c600c86e54c', 'LIAM', 'GONZALEZ', 'liam.gonzalez.12@ucl.ac.uk', 'b2b35946901ce983ab6ff0b6b0930228', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92afe3d-ac78-11e7-b423-2c600c86e54c', 'SAMUEL', 'HEMPHILL', 'samuel.hemphill.14@ucl.ac.uk', '55aa313840dd95bbcbfe2b1fe1eda8b0', 'MD5', 'LINKEDIN', 'igjmsObvwu',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92aff83-ac78-11e7-b423-2c600c86e54c', 'BRANDON', 'TUCK', 'brandon.tuck.14@ucl.ac.uk', '1a7fdf4e82ccc88713b6cb1b67a1130f', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b000e-ac78-11e7-b423-2c600c86e54c', 'KIT YING', 'LAM', 'kit.lam.12@ucl.ac.uk', '59abdaf8d8b0a59c2a6ae8f0944ae172', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0081-ac78-11e7-b423-2c600c86e54c', 'MOHIT', 'SANTILAL', 'mohit.santilal.15@ucl.ac.uk', 'c78f7b1d5b8c5f45695e9d7ab1b4ba62', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('c92b00cf-ac78-11e7-b423-2c600c86e54c', 'RAN', 'MO', 'ran.mo.15@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'etYkFBRGkb', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b01d9-ac78-11e7-b423-2c600c86e54c', 'ARMAN', 'AMINI', 'arman.amini.12@ucl.ac.uk', 'f3d46d180f290104436f9206d224865e', 'MD5', 'LINKEDIN', 'ZfbiX9Y43V', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0246-ac78-11e7-b423-2c600c86e54c', 'MUHAMMAD', 'MUSTAQIM', 'muhammad.mustaqim.15@ucl.ac.uk', '04efa26cde3f4e4ff976a0fe779facbe', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0294-ac78-11e7-b423-2c600c86e54c', 'EMMANUELLA', 'EKADI', 'emmanuella.ekadi.15@ucl.ac.uk', '743103d580a1994ee6fe96d4e6abf8e8', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b02e9-ac78-11e7-b423-2c600c86e54c', 'Samuel', 'Winborne', 'samuel.winborne.15@ucl.ac.uk', '51740b87397a8546cacf1f416eab6e19', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0337-ac78-11e7-b423-2c600c86e54c', 'PAUL', 'CACHERA', 'paul.cachera.15@ucl.ac.uk', '8ffe70b5ed96fd5253232125f6c11ff3', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b03a4-ac78-11e7-b423-2c600c86e54c', 'SANJAY', 'JOSHI', 'sanjay.joshi.12@ucl.ac.uk', '4fa0b97f768e2ba491cff10e926962b2', 'MD5', 'LINKEDIN', 'lT0OJsvsQw',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0465-ac78-11e7-b423-2c600c86e54c', 'SHREYA', 'SHAH', 'shreya.shah.15@ucl.ac.uk', 'cf6e7d23327e12a59e03d94cbf5acc10', 'MD5', 'LINKEDIN', 'DDoQVOqkpM', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b04b4-ac78-11e7-b423-2c600c86e54c', 'OSCAR', 'CRENTSIL', 'oscar.crentsil.15@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'WBDKmtyOmV', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0502-ac78-11e7-b423-2c600c86e54c', 'Patcharaporn', 'Chainiwatana', 'patcharaporn.chainiwatana.15@ucl.ac.uk', 'e824b086f702ad3a19b2bc94690e0d2d', 'MD5', 'LINKEDIN',
   'P7ph3J9Cxy', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0575-ac78-11e7-b423-2c600c86e54c', 'KUAN-LIN', 'CHIANG', 'kuan-lin.chiang.15@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'awojVXtKzC', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('c92b06c7-ac78-11e7-b423-2c600c86e54c', 'HANWEN', 'LIU', 'hanwen.liu.15@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'CmhmLiJ4Cc', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b070f-ac78-11e7-b423-2c600c86e54c', 'ELENA', 'PARISI', 'elena.parisi.15@ucl.ac.uk', 'dfcb629e414d3fbb40a4e61cce44d836', 'MD5', 'LINKEDIN', 'GWYMOD7OYL',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b075e-ac78-11e7-b423-2c600c86e54c', 'XUAN', 'ZHAO', 'xuan.zhao.15@ucl.ac.uk', 'a4072f96388b13ad779526983d7a0157', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b07b2-ac78-11e7-b423-2c600c86e54c', 'RANA', 'KESHIK', 'rana.keshik.15@ucl.ac.uk', 'c27985b5d0b45f191604cace8da4157c', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b07fb-ac78-11e7-b423-2c600c86e54c', 'PABLO', 'LUBROTH', 'pablo.lubroth.12@ucl.ac.uk', 'f5422c28dac1073b8f08f058d0a5405e', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b08aa-ac78-11e7-b423-2c600c86e54c', 'LINTONG', 'CAO', 'lin.cao.15@ucl.ac.uk', 'd7653f373c51aa13d8ec2d310c2ddb14', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b08f8-ac78-11e7-b423-2c600c86e54c', 'JAVIER', 'LARRAGOITI KURI', 'javier.kuri.15@ucl.ac.uk', '2b69f9a35a9045d7355e10873d1b8cbb', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0965-ac78-11e7-b423-2c600c86e54c', 'MOEKO', 'SAITO', 'moeko.saito.12@ucl.ac.uk', 'af93fa7c8fcd4847a196fbf6cc04a8e2', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0a4a-ac78-11e7-b423-2c600c86e54c', 'KYLE', 'MAIN', 'kyle.main.15@ucl.ac.uk', '281ed9da4ef9080ff2006b1570e7012b', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0ab7-ac78-11e7-b423-2c600c86e54c', 'PINELOPI-ELEFTHERIA', 'STAMOU', 'pinelopi-eleftheria.stamou.15@ucl.ac.uk', 'b2103e480e49da224d6ff622dd38ff48', 'MD5', NULL, NULL,
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0bf7-ac78-11e7-b423-2c600c86e54c', 'PHILLIPPA', 'JAMAL EL DIN SHMEIS', 'phillippa.shmeis.15@ucl.ac.uk', 'd5225e326778fc42ae1a0dfe6522e2be', 'MD5', NULL, NULL,
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0c52-ac78-11e7-b423-2c600c86e54c', 'Pablo', 'Lubroth', 'pablolubroth@gmail.com', 'f5422c28dac1073b8f08f058d0a5405e', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0ca0-ac78-11e7-b423-2c600c86e54c', 'Jakub', 'Domaradzki', 'zcbejdo@ucl.ac.uk', '9066ce171d18c735b511d545c4957886', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0cf5-ac78-11e7-b423-2c600c86e54c', 'Fairooza', 'Alam', 'zcbeala@ucl.ac.uk', '1c961d4b30395c921afc345f20a6434f', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0d3d-ac78-11e7-b423-2c600c86e54c', 'TOLULOPE', 'AGBEBI', 'tolulope.agbebi.14@ucl.ac.uk', '07b20e6dc4a1fbca9592061977205104', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0dec-ac78-11e7-b423-2c600c86e54c', 'YANIKA', 'BORG', 'yanika.borg.11@ucl.ac.uk', 'dd2538478bd4c50492b8d05fe5509565', 'MD5', 'LINKEDIN', 'jVtJBL9aCQ', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('c92b0e9b-ac78-11e7-b423-2c600c86e54c', 'MOHD', 'SANI', 'm.sani.11@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'IOQvGm4le4', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0eea-ac78-11e7-b423-2c600c86e54c', 'BEATRICE', 'MELINEK', 'b.melinek.12@ucl.ac.uk', '8e0f805244e0cdcc50b0b358f9d47a3c', 'MD5', 'LINKEDIN', 'cQBW_t1Y4d',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0f38-ac78-11e7-b423-2c600c86e54c', 'SHAHIN', 'HESHMATIFAR', 's.heshmatifar.12@ucl.ac.uk', '3c9922e3179bf5fd637c456143b4bfd7', 'MD5', 'LINKEDIN', 'GjqBnyhpka',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b0fe7-ac78-11e7-b423-2c600c86e54c', 'YI', 'LI', 'yi.li.14@ucl.ac.uk', '72b73eeccdfb1e1a3b3806727923e3f8', 'MD5', NULL, NULL, '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1329-ac78-11e7-b423-2c600c86e54c', 'SUSHOBHAN', 'BANDYOPADHYAY', 'sushobhan.bandyopadhyay.14@ucl.ac.uk', '048b297e1be1fda3cdf9173c14859e03', 'MD5', 'LINKEDIN',
   'QmnrgP604L', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b139b-ac78-11e7-b423-2c600c86e54c', 'MARY', 'LUNSON', 'mary.lunson.12@ucl.ac.uk', '3eff4dda719e812877f1a5db803b1acc', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1408-ac78-11e7-b423-2c600c86e54c', 'NIHAL', 'BAYIR', 'n.bayir@ucl.ac.uk', 'c9fa25bdb95fef9157f0d64ce686fb90', 'MD5', 'LINKEDIN', 'eNzIyAAOOm', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1475-ac78-11e7-b423-2c600c86e54c', 'ROMAN', 'ZAKRZEWSKI', 'roman.zakrzewski@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', '8l12OMCIGh', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b14ff-ac78-11e7-b423-2c600c86e54c', 'STEFAN', 'WOODHOUSE', 's.woodhouse@ucl.ac.uk', '90072393610abbb84f6f743557028f12', 'MD5', 'LINKEDIN', 'PLhlhk-ULW',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b15e5-ac78-11e7-b423-2c600c86e54c', 'OLUSEGUN', 'FOLARIN', 'olusegun.folarin.14@ucl.ac.uk', '1df017c9c18e674e7e3579c09d2579a1', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1688-ac78-11e7-b423-2c600c86e54c', 'ALMA MONA', 'ANTEMIE', 'a.antemie@ucl.ac.uk', '64173af605ae66e5ba3647e76d2f896e', 'MD5', 'LINKEDIN', 'xGdT0hh3NA',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1737-ac78-11e7-b423-2c600c86e54c', 'HAI-YUAN', 'GOH', 'hai.goh.14@ucl.ac.uk', '1d8f14aa5e1ba830c25dd8b0d000999b', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1810-ac78-11e7-b423-2c600c86e54c', 'SADFER', 'ALI', 'sadfer.ali.13@ucl.ac.uk', '1119cb8b04317b84ade2363e06aed8af', 'MD5', 'LINKEDIN', 'PxBKRamd2i', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b18a1-ac78-11e7-b423-2c600c86e54c', 'RACHAEL', 'WOOD', 'rachael.wood.14@ucl.ac.uk', 'ca3820bd5c888efe3924460413a529fd', 'MD5', 'LINKEDIN', 'M1evAfazHU',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1969-ac78-11e7-b423-2c600c86e54c', 'NEHAL', 'PATEL', 'nehal.patel.12@ucl.ac.uk', 'e773719cace54e92bdb22a4307c1825b', 'MD5', 'LINKEDIN', 'ahWO8sH5mj', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b19cf-ac78-11e7-b423-2c600c86e54c', 'AISHA', 'ASRA', 'aisha.asra.13@ucl.ac.uk', '108a8bc86e6a29d591243d8515cbe270', 'MD5', 'LINKEDIN', 'z-4isfnB1y', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1a1e-ac78-11e7-b423-2c600c86e54c', 'STEWART', 'DODS', 'stewart.dods.10@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'oPEGRjTWga', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1ac7-ac78-11e7-b423-2c600c86e54c', 'NELSON', 'BARRIENTOS LOBOS', 'n.lobos.12@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'VsvwaGCWKO', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1b33-ac78-11e7-b423-2c600c86e54c', 'RANA', 'KHALIFE', 'rana.khalife.13@ucl.ac.uk', 'db0566e961621d4fc142156d2f7435d2', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1ba0-ac78-11e7-b423-2c600c86e54c', 'ZAENAB', 'OSHINBOLU', 'zaenab.oshinbolu.10@ucl.ac.uk', '8d6075c17009e9dac507d8430412fcb9', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1be8-ac78-11e7-b423-2c600c86e54c', 'ANSHUL', 'SHARMA', 'anshul.sharma.14@ucl.ac.uk', 'ce43706adc21d65b1154993050063b4b', 'MD5', 'LINKEDIN', 'q7Dqk2U9fa',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1cfe-ac78-11e7-b423-2c600c86e54c', 'ANAND NARAYANAN', 'PALLIPURATH RADHAKRISHNAN', 'anand.radhakrishnan@ucl.ac.uk', 'd6b05dd65ae132f48c69dd3b8fbfec7f', 'MD5', NULL, NULL,
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1d89-ac78-11e7-b423-2c600c86e54c', 'CHRISTOPHER', 'PERRY', 'christopher.perry.14@ucl.ac.uk', '3e41ab7155dc7fa968eab9eedd57e110', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b1ee1-ac78-11e7-b423-2c600c86e54c', 'MICHAEL', 'MARTINEZ', 'michael.martinez.15@ucl.ac.uk', 'a1b9eeba51253d0c3e9b1c0804b30b8c', 'MD5', 'LINKEDIN', 'N6WJrZI3So',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('c92b1f36-ac78-11e7-b423-2c600c86e54c', 'Daniel', 'Wait', 'daniel.wait.13@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'K11HrxHB-u', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2058-ac78-11e7-b423-2c600c86e54c', 'AARRON', 'ERBAS', 'aarron.erbas.11@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', '-h2ga0D2rY', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2101-ac78-11e7-b423-2c600c86e54c', 'BAOLONG', 'WANG', 'baolong.wang.14@ucl.ac.uk', 'bdef386ec07b414547736a800e1751fb', 'MD5', 'LINKEDIN', '1kXj0c0p98',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b216d-ac78-11e7-b423-2c600c86e54c', 'MATHEW', 'ROBINSON', 'mathew.robinson.10@ucl.ac.uk', 'ee7696df5408e10079c379c8ccce16cc', 'MD5', 'LINKEDIN', 'kOSNJkDwVD',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b21da-ac78-11e7-b423-2c600c86e54c', 'PIA', 'GRUBER', 'p.gruber@ucl.ac.uk', '4bacfba260c945c2ae381a128da9d468', 'MD5', 'LINKEDIN', '2TIA1FFdxA', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2234-ac78-11e7-b423-2c600c86e54c', 'DARRYL', 'KONG', 'darryl.kong.10@ucl.ac.uk', 'd03b975bf040c8ab72d132fe0d46fb75', 'MD5', 'LINKEDIN', 'DaU7qt_2Z7', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b22a7-ac78-11e7-b423-2c600c86e54c', 'JOSEPH', 'NEWTON', 'joseph.newton.13@ucl.ac.uk', 'fb85db6fd98993ba0d0c8326bad545f9', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b29ba-ac78-11e7-b423-2c600c86e54c', 'Mohd Shawkat', 'Hussain', 'mohd.hussain.10@ucl.ac.uk', '675c894d410e1fa6da0b06cb15e1fb51', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2a94-ac78-11e7-b423-2c600c86e54c', 'ZARAH', 'ALI', 'z.ali@ucl.ac.uk', '810aad8b9c559f1eb7428496706f1f65', 'MD5', NULL, NULL, '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2b61-ac78-11e7-b423-2c600c86e54c', 'DAVID', 'WARD', 'david.ward.13@ucl.ac.uk', 'd2bfaadecb3c44c2c71042383e4a12da', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2bb0-ac78-11e7-b423-2c600c86e54c', 'CHARNETT', 'CHAU', 'charnett.chau.09@ucl.ac.uk', 'ba39cccba5f21ab69d4c630453b595db', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2cd1-ac78-11e7-b423-2c600c86e54c', 'MICHAEL', 'JENKINS', 'michael.jenkins@ucl.ac.uk', '11b4ca0956de5ccb348ab34ebded5ddb', 'MD5', 'LINKEDIN', 'EGJYPmkdWS',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2d26-ac78-11e7-b423-2c600c86e54c', 'VIVIEN', 'FISCHER', 'vivien.fischer.14@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'BNumdhWaOa', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2d74-ac78-11e7-b423-2c600c86e54c', 'Joana', 'dos Reis', 'joana.reis.13@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', '-lAprQVsg7', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2dff-ac78-11e7-b423-2c600c86e54c', 'VICTOR', 'SANCHEZ TARRE', 'victor.sancheztarre.11@ucl.ac.uk', '3bb58011caeb0f55c27a19183ba1f44d', 'MD5', NULL, NULL,
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2e48-ac78-11e7-b423-2c600c86e54c', 'SARAH', 'SLACK', 'sarah.slack.15@ucl.ac.uk', '5cc52feeed10bc86300055e72b6411bb', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2e96-ac78-11e7-b423-2c600c86e54c', 'NICHOLAS', 'FIELD', 'nicholas.field.13@ucl.ac.uk', 'cce36df42c2dc2062a078672e1e8ca6d', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2ee5-ac78-11e7-b423-2c600c86e54c', 'HENRY', 'WILKINSON', 'henry.wilkinson.15@ucl.ac.uk', 'bdf6425b5b797b0da9111c436502036e', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2f51-ac78-11e7-b423-2c600c86e54c', 'BENJAMIN', 'WEIL', 'b.weil@ucl.ac.uk', '283a6b565f86c7e8b41b66282d8b14ef', 'MD5', 'LINKEDIN', 'RqYzsp7dzu', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2fa0-ac78-11e7-b423-2c600c86e54c', 'LARA', 'FERNANDEZ CEREZO', 'lara.cerezo.10@ucl.ac.uk', '508438e26d2866b5d92689c3e9273c86', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b2fee-ac78-11e7-b423-2c600c86e54c', 'ANA', 'VALINHAS', 'ana.valinhas.14@ucl.ac.uk', '714556894fe76716d50782b055e43f11', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('c92b308b-ac78-11e7-b423-2c600c86e54c', 'Simren', 'Maloni', 'ucbemal@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', '3rmvQysvnZ', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b30e0-ac78-11e7-b423-2c600c86e54c', 'David', 'De Silva-Thompson', 'drdst@hotmail.co.uk', NULL, 'MD5', 'LINKEDIN', 'ayvt27P-y_', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3128-ac78-11e7-b423-2c600c86e54c', 'Enrique', 'de Zamacona', 'enrizamacona@gmail.com', '5493e7dd0182bd82e45f8a36e31bbc10', 'MD5', 'LINKEDIN', 'WYALcvm-4D',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3177-ac78-11e7-b423-2c600c86e54c', 'Saranky', 'Sivapathasundram', 'zcbessi@ucl.ac.uk', '4e2b4a69f613f6dd5d98cb87c66d3f43', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b31c5-ac78-11e7-b423-2c600c86e54c', 'Curtis', 'Dight', 'curtis.dight.14@ucl.ac.uk', 'fb9205f715bc78c3517657c301b7401d', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b320e-ac78-11e7-b423-2c600c86e54c', 'KE', 'MENG', 'zczlkme@ucl.ac.uk', 'ec235a1c4bfae23f0e8079285999499c', 'MD5', NULL, NULL, '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b325c-ac78-11e7-b423-2c600c86e54c', 'Zhelyaz', 'Ovcharov', 'zhelyaz.ovcharov@hotmail.com', '5ab3e8b427b07a811ac630b422002337', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b32c9-ac78-11e7-b423-2c600c86e54c', 'Nadzirah', 'Norazman', 'zcbenbn@ucl.ac.uk', '5c6247f11f2eb66505ae1f00f7e59394', 'MD5', 'LINKEDIN', 'hkK914RHX_', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b331d-ac78-11e7-b423-2c600c86e54c', 'Haneen', 'Alosert', 'alosert.1996@hotmail.co.uk', '4c19751c1c7aec2dcca43c4e8186b574', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b336c-ac78-11e7-b423-2c600c86e54c', 'Katrina', 'Galve', 'katrinagalve@gmail.com', '6fdadd6ec1ae1f99962636865fd6da8c', 'MD5', 'LINKEDIN', 'eF7thUlcGD', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b33ba-ac78-11e7-b423-2c600c86e54c', 'Samantha', 'Hannon', 'sam.hannon@googlemail.com', NULL, 'MD5', 'LINKEDIN', 'Zx9AIEZedW', '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3403-ac78-11e7-b423-2c600c86e54c', 'akaash', 'kumar', 'zcbeaku@ucl.ac.uk', '2fd764b5dfa1c66de102884dc69fe367', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b34e2-ac78-11e7-b423-2c600c86e54c', 'Dylan', 'Mendonca', 'dylanmendonca@gmail.com', '74145194b670ad08266f4b44580af7f9', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3531-ac78-11e7-b423-2c600c86e54c', 'yixing', 'li', 'liyixing04@gmail.com', '970445674d1db9fb52ea5e9917c799b4', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b357f-ac78-11e7-b423-2c600c86e54c', 'Tamunotonye', 'Cookey-Gam', 'zcbetco@ucl.ac.uk', '04589db1f9bc58803a4d97ca58575745', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b35ce-ac78-11e7-b423-2c600c86e54c', 'Samuel', 'Winborne', 'Ucbeslw@ucl.ac.uk', 'ab6825baaa531281096c5f7d39a15c76', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3616-ac78-11e7-b423-2c600c86e54c', 'Arthur', 'Radley', 'zcbeh35@ucl.ac.uk', '069c20da7bb8f591817092352d7407fe', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3665-ac78-11e7-b423-2c600c86e54c', 'Haaniah', 'Hamid', 'haaniah.hamid@gmail.com', 'ceab851dc9470dfac3111b02f6ec043c', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b36ad-ac78-11e7-b423-2c600c86e54c', 'So Yeon', 'Kim', 'zcbesyk@ucl.ac.uk', '072082da7ab59a5c5346f43f6a334bda', 'MD5', 'LINKEDIN', 'OVHia38dy5', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b36fb-ac78-11e7-b423-2c600c86e54c', 'Ajwad', 'Jabbar', 'ajwadjabbar@gmail.com', '22856264cec27b55285185e483cb9c5c', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3744-ac78-11e7-b423-2c600c86e54c', 'Jane', 'Peatie', 'janepeatie@yahoo.co.uk', '62b42b55a8ef6f099192f3161e4481ac', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b37b1-ac78-11e7-b423-2c600c86e54c', 'Uzbaig', 'Barlas', 'uzbaigbarlas@hotmail.com', '627dcc7a1e7157c450d93696936cce4a', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b37ff-ac78-11e7-b423-2c600c86e54c', 'damiano', 'migani', 'damianomigani@hotmail.com', 'a47cd80bc769381bcb2cf41edb96c542', 'MD5', 'LINKEDIN', 'mvBPP8Z0El',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3848-ac78-11e7-b423-2c600c86e54c', 'Paul', 'Cachera', 'ucbepca@ucl.ac.uk', '8ffe70b5ed96fd5253232125f6c11ff3', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3896-ac78-11e7-b423-2c600c86e54c', 'Dustin', 'Sands', 'dustin.sands.11@ucl.ac.uk', 'ae88375961d3e78f4efa89a013529f31', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b38de-ac78-11e7-b423-2c600c86e54c', 'Alaa', 'Latif', 'alaa.latif.11@ucl.ac.uk', 'b24373dd0689ea64f66e38b893e7677f', 'MD5', 'LINKEDIN', 'QExZwP1QMa', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b392d-ac78-11e7-b423-2c600c86e54c', 'Barbara', 'Brinquis Nunez', 'barbara.brinquis@gmail.com', '6f25ad1c628b7bc25751960f41e1a6fe', 'MD5', 'LINKEDIN', 'ig8BruCqz7',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b398e-ac78-11e7-b423-2c600c86e54c', 'Sarah', 'BOURDIN', 'sarah.bourdin@hotmail.com', 'f17b2fffd8398c5b60e4a339521e21ec', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b39dc-ac78-11e7-b423-2c600c86e54c', 'Cꤩle', 'Geier', 'cecile.geier.15@ucl.ac.uk', '9c3c112c1b984bb43bb5be21514c05dd', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3ad4-ac78-11e7-b423-2c600c86e54c', 'Kun', 'Ma', 'kun.ma@ucl.ac.uk', '3218f6503fa54a879befe5287cb876d3', 'MD5', NULL, NULL, '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3b5e-ac78-11e7-b423-2c600c86e54c', 'Chun Ho', 'Lee', 'chun.lee.16@ucl.ac.uk', 'a559d9dffebd68deb0183e3e01900a9e', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3bb3-ac78-11e7-b423-2c600c86e54c', 'Marian', 'Machlouzarides', 'marian.machlouzarides.12@ucl.ac.uk', 'f43a9976dbb37ad81b7f11941c62a1bc', 'MD5', 'LINKEDIN', 'yjc4Oas-Hq',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3c2c-ac78-11e7-b423-2c600c86e54c', 'Thomas', 'Cheung', 'thomas.cheung.16@ucl.ac.uk', '2ad7d4e8b6012d8f15580c82b52b67e5', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3cf3-ac78-11e7-b423-2c600c86e54c', 'Ekta', 'Rayani', 'ekta.rayani.16@ucl.ac.uk', '94f1e33ce774d6302808efed8b33f854', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3f61-ac78-11e7-b423-2c600c86e54c', 'Kapil', 'Rananaware', 'mastrokapil@gmail.com', '2dff9e70c9516e5aa0e91480748b4167', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3faf-ac78-11e7-b423-2c600c86e54c', 'manuela', 'Bonilla Espadas', 'manuela.bonila.16@ucl.ac.uk', 'a9561778abd4bb4e0271aac606b58680', 'MD5', NULL, NULL,
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b3ffe-ac78-11e7-b423-2c600c86e54c', 'Manuela', 'Bonilla Espadas', 'manuela.bonilla.16@ucl.ac.uk', 'a9561778abd4bb4e0271aac606b58680', 'MD5', NULL, NULL,
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4046-ac78-11e7-b423-2c600c86e54c', 'Sean', 'Ooi', 'zcbesoo@ucl.ac.uk', 'b5a5eb1bfcd2bdd5d0d43b0372dc920f', 'MD5', NULL, NULL, '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b40b9-ac78-11e7-b423-2c600c86e54c', 'Enoch', 'Ko', 'enochkojob@gmail.com', '26d82396a624eecfe71537c5cff7ef27', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4101-ac78-11e7-b423-2c600c86e54c', 'Wasif', 'Navil', 'zcbewna@ucl.ac.uk', 'baf39c734e3d7109c144e2fc645b8b95', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b414a-ac78-11e7-b423-2c600c86e54c', 'Eleni', 'Davidson', 'eleni.davidson@gmail.com', '41fad0e4e37f5553d14a608c22d55165', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4198-ac78-11e7-b423-2c600c86e54c', 'Brandon', 'Tuck', 'zcbebtu@ucl.ac.uk', '49efaff803ae69f25994024a343aed26', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b41e1-ac78-11e7-b423-2c600c86e54c', 'Enya', 'Gomes Clynch', 'enya.clynch.14@ucl.ac.uk', '6eebfa4bd544380567cd6bc8b262bc43', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4229-ac78-11e7-b423-2c600c86e54c', 'Catherine', 'Pang', 'c.pang@live.co.uk', '2681bd2e710e15fc9ccd08225e159407', 'MD5', 'LINKEDIN', 'gn4f0WwxW7', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4278-ac78-11e7-b423-2c600c86e54c', 'Viktoria', 'Diamantidou', 'vicdiam98@hotmail.com', '1e69cc895109ce9ba6bcfd747849f1b2', 'MD5', 'LINKEDIN', 'JbsG5CE0hn',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b42c6-ac78-11e7-b423-2c600c86e54c', 'Sarah (Lok Man)', 'Wong', 'sarahwongsw129@gmail.com', 'a8c92c36e0e38928765b0e2b550fa05e', 'MD5', 'LINKEDIN', 'dNgqdeC3vT',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4315-ac78-11e7-b423-2c600c86e54c', 'Syed Mahfuzur', 'Reza', 'zccamre@ucl.ac.uk', '3bb19b8b0b34c79e7e3a3affb070f92d', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4381-ac78-11e7-b423-2c600c86e54c', 'Justyna', 'Bomba', 'justyna.bomba.15@ucl.ac.uk', '2bcbbb54ed3fd87836a3a3d8905aa829', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b43d0-ac78-11e7-b423-2c600c86e54c', 'Allia', 'Ruggiero', 'allia.ruggiero.15@ucl.ac.uk', '09fb90f6c013955975601111f50831b3', 'MD5', 'LINKEDIN', 'WwF5EpCWOq',
   '2017-10-08 23:32:38', '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4418-ac78-11e7-b423-2c600c86e54c', 'Rodrick', 'Avakian', 'zcberav@ucl.ac.uk', 'af02d132711e1a1e1bdae464bed8500a', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4461-ac78-11e7-b423-2c600c86e54c', 'Chun Hong', 'Ip', 'micmic200345@hotmail.com', 'a41c816de041eac869073f836ef4207a', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b44af-ac78-11e7-b423-2c600c86e54c', 'Davina', 'Rehal', 'davina.rehal.15@ucl.ac.uk', '9ae816e108123ec8275c124a955185be', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b44fe-ac78-11e7-b423-2c600c86e54c', 'Davina', 'Rehal', 'davinarehal@hotmail.com', '9ae816e108123ec8275c124a955185be', 'MD5', 'LINKEDIN', 'nFObUZuDtK', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4546-ac78-11e7-b423-2c600c86e54c', 'Miracle', 'Abiola', 'miracleabiola4@gmail.com', '6253e1406b64bbe6ba7b00ac0bf81257', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4595-ac78-11e7-b423-2c600c86e54c', 'Christopher', 'Lee', 'zcbecgl@ucl.ac.uk', 'c00f81f1611a6298c3d02a6a5457273d', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b45dd-ac78-11e7-b423-2c600c86e54c', 'Chloe', 'Ogweng', 'chloe.ogweng.15@ucl.ac.uk', '3fd5e9042440e9839c56b12d0b6efe1e', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b464a-ac78-11e7-b423-2c600c86e54c', 'Chloe', 'Ogweng', 'chloeog97@gmail.com', '3fd5e9042440e9839c56b12d0b6efe1e', 'MD5', 'LINKEDIN', 'vtuOj1yu-y', '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4698-ac78-11e7-b423-2c600c86e54c', 'Ho Ming', 'Lam', 'zcbehml@ucl.ac.uk', 'ffe7ac80947273a89cefe1d677225b09', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b46e1-ac78-11e7-b423-2c600c86e54c', 'Mohammed', 'Fazel', 'zcbemfa@ucl.ac.uk', '80769c9255e75f4bec05926b81fb6159', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4729-ac78-11e7-b423-2c600c86e54c', 'Mujtaba', 'Patel', 'mujtaba.patel@hotmail.co.uk', 'c82d6f5c309befdbb59f94d1e093ea57', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b4778-ac78-11e7-b423-2c600c86e54c', 'Shamal', 'Withanage', 'shamal.withanage.14@ucl.ac.uk', '374851be025f89935150bfa7de2e37aa', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('c92b47c0-ac78-11e7-b423-2c600c86e54c', 'Anselm', 'Lee', 'anselm.kenis@gmail.com', '501df9eae0f9f0408efacf7310b27144', 'MD5', NULL, NULL, '2017-10-08 23:32:38',
   '2017-10-08 23:32:38');

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp, updated_timestamp)
  SELECT
    @biochemicalId,
    user.id,
    'MEMBER',
    'ACCEPTED',
    user.created_timestamp,
    user.updated_timestamp
  FROM user
    LEFT JOIN user_role
      ON user.id = user_role.user_id
  WHERE user_role.id IS NULL;

SET @electricalId = (
  SELECT id
  FROM resource
  WHERE name = 'Electronic & Electrical Engineering');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('0c703a8e-ac78-11e7-b423-2c600c86e54c', 'Tim', 'Bodley-Scott', 't.bodley-scott@ucl.ac.uk', '52e960a38734bf1f75c3c70ce4b16394', 'MD5', 'LINKEDIN', 'U4IWdJe7Jw',
        '2017-10-08 23:27:21', '2017-10-08 23:27:21');

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp, updated_timestamp)
VALUES (@electricalId, @alastairAdminId, 'ADMINISTRATOR', 'ACCEPTED', NOW(), NOW()),
  (@electricalId, last_insert_id(), 'ADMINISTRATOR', 'ACCEPTED', NOW(), NOW());

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('3c8907c0-ac7a-11e7-b423-2c600c86e54c', 'Viet Cuong', 'Vu', 'mr_vcv@hotmail.co.uk', '878d51c4047c8acd844fdb8c942794d1', 'MD5', NULL, NULL, '2017-10-08 23:43:01',
   '2017-10-08 23:43:01');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('3c890b62-ac7a-11e7-b423-2c600c86e54c', 'Nijat', 'Bakhshaliyev', 'nijat.bakhshaliyev.13@ucl.ac.uk', 'd90959018b762a827d51d072222eba45', 'MD5', 'LINKEDIN', 'e_6p1DdYx3',
   '2017-10-08 23:43:01', '2017-10-08 23:43:01');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('3c890d57-ac7a-11e7-b423-2c600c86e54c', 'Franky', 'Saxena', 'franky.saxena.14@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'YnZ4s3Fz6m', '2017-10-08 23:43:01', '2017-10-08 23:43:01');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('3c890ebb-ac7a-11e7-b423-2c600c86e54c', 'Filip', 'Stefanec', 'uceetef@ucl.ac.uk', 'd0907a319d5c1bbb4c25283c5d602266', 'MD5', NULL, NULL, '2017-10-08 23:43:01',
   '2017-10-08 23:43:01');

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp, updated_timestamp)
  SELECT
    @electricalId,
    user.id,
    'MEMBER',
    'ACCEPTED',
    user.created_timestamp,
    user.updated_timestamp
  FROM user
    LEFT JOIN user_role
      ON user.id = user_role.user_id
  WHERE user_role.id IS NULL;

SET @computerId = (
  SELECT id
  FROM resource
  WHERE name = 'Computer Science');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('51f4bf36-ac78-11e7-b423-2c600c86e54c', 'Stephen', 'Marchant', 'stephen.marchant@ucl.ac.uk', 'e0e34c5ad05aac3eef6ab31eacbf7a5c', 'MD5', NULL, NULL, '2017-10-08 23:29:18',
        '2017-10-08 23:29:18');

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp, updated_timestamp)
VALUES (@computerId, @alastairAdminId, 'ADMINISTRATOR', 'ACCEPTED', NOW(), NOW()),
  (@computerId, last_insert_id(), 'ADMINISTRATOR', 'ACCEPTED', NOW(), NOW());

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4769-ac7b-11e7-b423-2c600c86e54c', 'LEWIS', 'MOFFAT', 'lewis.moffat.13@ucl.ac.uk', 'ddfa997db831b195f54e1179463e24fd', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b480c-ac7b-11e7-b423-2c600c86e54c', 'SAMUEL', 'HEMPHILL', 'samuel.hemphill.14@ucl.ac.uk', '55aa313840dd95bbcbfe2b1fe1eda8b0', 'MD5', 'LINKEDIN', 'igjmsObvwu',
   '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4879-ac7b-11e7-b423-2c600c86e54c', 'yuruo', 'zhang', 'zhangyuruo0610@gmail.com', 'c80de8ab06afa5dec05858f2ac648909', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b48cd-ac7b-11e7-b423-2c600c86e54c', 'Raja', 'Upadhyay', 'upadhyayaraja@gmail.com', 'e27c19c2093f8fbea247bc6d33b2a983', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('774b4964-ac7b-11e7-b423-2c600c86e54c', 'Mengyang', 'Wu', 'wumengyang.ok@gmail.com', NULL, 'MD5', 'LINKEDIN', 'z_s8vPOdyD', '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b49c5-ac7b-11e7-b423-2c600c86e54c', 'Mohammad Hossein', 'Afsharmoqaddam', 'moafshaar@gmail.com', 'ccb4c0bffe1b15567a1711d6c9b96cf6', 'MD5', NULL, NULL,
   '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4a19-ac7b-11e7-b423-2c600c86e54c', 'Vlad', 'Popa', 'vladpopabc@gmail.com', '1022e9dc81992506586d93fb1d623538', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('774b4a6e-ac7b-11e7-b423-2c600c86e54c', 'Galen', 'Han', 'galen.han.14@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'UHtlhF5QAY', '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4abc-ac7b-11e7-b423-2c600c86e54c', 'Jason', 'Gwartz', 'jason.gwartz.15@ucl.ac.uk', 'e3ca733924e6df9bbd4d272aa31c073d', 'MD5', 'LINKEDIN', '7pK6cBc6_h',
   '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('774b4b11-ac7b-11e7-b423-2c600c86e54c', 'Xinyi', 'He', 'xinyi.he-cn@hotmail.com', NULL, 'MD5', 'LINKEDIN', 'XkO_f1mtoZ', '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4b5f-ac7b-11e7-b423-2c600c86e54c', 'Romana', 'Nagyov', 'r.nagyova22@gmail.com', '708031b8a51c134b03cb8584da62096e', 'MD5', 'LINKEDIN', '64WZi_CqVR', '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4bb4-ac7b-11e7-b423-2c600c86e54c', 'Mozhdeh', 'Afshar', 'afshar.m.95@gmail.com', 'c3ba4e8b513914625c936186ad2b3edb', 'MD5', 'LINKEDIN', '9Xb-J-8Izd', '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4c26-ac7b-11e7-b423-2c600c86e54c', 'Desislava', 'Koleva', 'desislava.koleva.15@ucl.ac.uk', '4d104c35f2a2d0efe675f3350dc7f2a9', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp)
VALUES ('774b4c75-ac7b-11e7-b423-2c600c86e54c', 'Dhruv', 'Ghulati', 'ghulatid@gmail.com', NULL, 'MD5', 'LINKEDIN', 'znyOLf1ZXv', '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4cc3-ac7b-11e7-b423-2c600c86e54c', 'Gulliver', 'Johnson', 'gulliver.anslow-johnson.13@ucl.ac.uk', '668079c8b7f7203741c70b783b6bf1d4', 'MD5', NULL, NULL,
   '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4d18-ac7b-11e7-b423-2c600c86e54c', 'Niccolo', 'Terreri', 'niccolo.terreri@gmail.com', '4ea8e81a897fe9bce3221f3fba4eaa8f', 'MD5', 'LINKEDIN', 'cDkEJr_n3d',
   '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4d60-ac7b-11e7-b423-2c600c86e54c', 'Jedrzej', 'Stuczynski', 'jedrzej.stuczynski@gmail.com', 'c500fb9f404065674b8c5612f3ab7d54', 'MD5', 'LINKEDIN', 'P5dYTw99Qj',
   '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4db5-ac7b-11e7-b423-2c600c86e54c', 'Lucrezia', 'Morvilli', 'lucrezia.morvilli.15@ucl.ac.uk', NULL, 'MD5', 'LINKEDIN', 'bggILoLMTr', '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4e03-ac7b-11e7-b423-2c600c86e54c', 'Ying', 'Wen', 'ying.wen@cs.ucl.ac.uk', '409c03eb72b59ae2bbdcdd6d0ab1a6c9', 'MD5', 'LINKEDIN', 'KvBzj7KsPt', '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4e4c-ac7b-11e7-b423-2c600c86e54c', 'Andrea', 'Cantamessa', 'andrea.cantamessa@tiscali.it', '560110ebf0398e837ad7451da12d80df', 'MD5', 'LINKEDIN', 'aWOo6lvzSd',
   '2017-10-08 23:51:49', '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4ea6-ac7b-11e7-b423-2c600c86e54c', 'rim', 'ahsaini', 'rim.ahsaini.15@ucl.ac.uk', '82ca228e6d0bb0f14d073136015b006e', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4f07-ac7b-11e7-b423-2c600c86e54c', 'Hanlin', 'Yue', 'hanlin.yue.16@ucl.ac.uk', 'fcc9a75cc09e85f335edbd08b6b125f2', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4f55-ac7b-11e7-b423-2c600c86e54c', 'William', 'Fletcher', 'william.fletcher.16@ucl.ac.uk', '5c11b0388a9e1511d7631a14ae25de84', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user (uuid, given_name, surname, email, password, password_hash, oauth_provider, oauth_account_id, created_timestamp, updated_timestamp) VALUES
  ('774b4faa-ac7b-11e7-b423-2c600c86e54c', 'Kiran', 'Gopinathan', 'zcabkgo@ucl.ac.uk', '1df57e5b35d850161d52d17842b7d6f9', 'MD5', NULL, NULL, '2017-10-08 23:51:49',
   '2017-10-08 23:51:49');

INSERT INTO user_role (resource_id, user_id, role, state, created_timestamp, updated_timestamp)
  SELECT
    @computerId,
    user.id,
    'MEMBER',
    'ACCEPTED',
    user.created_timestamp,
    user.updated_timestamp
  FROM user
    LEFT JOIN user_role
      ON user.id = user_role.user_id
  WHERE user_role.id IS NULL;
