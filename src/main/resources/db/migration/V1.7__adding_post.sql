CREATE TABLE location (
  id                BIGINT UNSIGNED NOT NULL     AUTO_INCREMENT,
  name              VARCHAR(255)    NOT NULL,
  domicile          VARCHAR(3)      NOT NULL,
  google_id         VARCHAR(255)    NOT NULL,
  latitude          DECIMAL(24, 18) NOT NULL,
  longitude         DECIMAL(24, 18) NOT NULL,
  created_timestamp DATETIME        NOT NULL,
  updated_timestamp DATETIME,
  PRIMARY KEY (id),
  UNIQUE INDEX google_id (google_id),
  INDEX domicile (domicile),
  INDEX latitude (latitude, longitude)
)
  COLLATE = utf8_general_ci
  ENGINE = InnoDB;

ALTER TABLE resource
  DROP FOREIGN KEY resource_ibfk_1;

ALTER TABLE document
  MODIFY COLUMN id BIGINT UNSIGNED NOT NULL     AUTO_INCREMENT;

ALTER TABLE resource
  MODIFY COLUMN document_logo_id BIGINT UNSIGNED;

ALTER TABLE resource
  ADD FOREIGN KEY (document_logo_id) REFERENCES document (id);

ALTER TABLE resource
  ADD COLUMN organization_name VARCHAR(255)
  AFTER default_post_visibility,
  ADD COLUMN location_id BIGINT UNSIGNED
  AFTER organization_name,
  ADD COLUMN existing_relation BIT
  AFTER location_id,
  ADD COLUMN apply_website VARCHAR(255)
  AFTER existing_relation,
  ADD COLUMN apply_document_id BIGINT UNSIGNED
  AFTER apply_website,
  ADD COLUMN apply_email VARCHAR(254)
  AFTER apply_document_id,
  ADD FOREIGN KEY (location_id) REFERENCES location (id),
  ADD FOREIGN KEY (apply_document_id) REFERENCES document (id);

