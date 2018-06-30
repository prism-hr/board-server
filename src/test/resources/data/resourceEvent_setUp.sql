INSERT INTO resource (id, scope, name, state, created_timestamp)
VALUES
  (1, 'POST', 'post1', 'ACCEPTED', '2018-05-21 21:04:54.185'),
  (2, 'POST', 'post2', 'ACCEPTED', '2018-05-21 21:04:54.185');

INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)
VALUES
  (1, UUID(), 'alastair', 'alastair', 'alastair@prism.hr', 'alastair@prism.hr', '2018-05-21 21:04:54.185');

INSERT INTO location (id, name, domicile, google_id, latitude, longitude, created_timestamp)
VALUES
  (1, 'london', 'GB', 'googleId', 1.00, 1.00, '2018-05-21 21:04:54.185');

INSERT INTO document (id, cloudinary_id, cloudinary_url, file_name, created_timestamp)
VALUES
  (1, 'cloudinaryId', 'cloudinaryUrl', 'fileName', '2018-05-21 21:04:54.185');

INSERT INTO resource_event (id, resource_id, event, user_id, referral, gender, age_range, location_nationality_id, member_category, member_program, member_year, document_resume_id, website_resume, covering_note, created_timestamp)
VALUES
  (1, 1, 'REFERRAL', 1, 'referral', 'FEMALE', 'NINETEEN_TWENTYFOUR', 1, 'UNDERGRADUATE_STUDENT', 'program', 2018, null, null, null, '2018-05-21 21:04:54.185'),
  (2, 1, 'REFERRAL', 1, null, 'FEMALE', 'NINETEEN_TWENTYFOUR', 1, 'UNDERGRADUATE_STUDENT', 'program', 2018, null, null, null, '2018-05-21 21:04:54.185');
