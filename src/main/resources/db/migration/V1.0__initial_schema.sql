create table user (
  id                bigint auto_increment,
  given_name        varchar(30)  not null,
  surname           varchar(40)  not null,
  email             varchar(254) not null,
  stormpath_id      varchar(30)  not null,
  created_timestamp datetime     not null,
  updated_timestamp datetime,
  primary key (id),
  unique index (email),
  unique index (stormpath_id),
  index (created_timestamp),
  index (updated_timestamp)
)
  collate = utf8_general_ci
  engine = innodb;

create table document (
  id                bigint auto_increment,
  cloudinary_id     varchar(30)  not null,
  cloudinary_url    varchar(255) not null,
  file_name         varchar(500) not null,
  created_timestamp datetime     not null,
  updated_timestamp datetime,
  primary key (id),
  index (created_timestamp),
  index (updated_timestamp)
)
  collate = utf8_general_ci
  engine = innodb;

create table department (
  id                bigint auto_increment,
  user_id           bigint       not null,
  document_logo_id  bigint,
  name              varchar(255) not null,
  created_timestamp datetime     not null,
  updated_timestamp datetime,
  primary key (id),
  index (name),
  index (created_timestamp),
  index (updated_timestamp),
  foreign key (user_id) references user (id),
  foreign key (document_logo_id) references document (id)
)
  collate = utf8_general_ci
  engine = innodb;

create table board (
  id                bigint auto_increment,
  user_id           bigint       not null,
  department_id     bigint       not null,
  name              varchar(255) not null,
  purpose           text         not null,
  created_timestamp datetime     not null,
  updated_timestamp datetime,
  primary key (id),
  index (name),
  index (created_timestamp),
  index (updated_timestamp),
  foreign key (user_id) references user (id),
  foreign key (department_id) references department (id)
)
  collate = utf8_general_ci
  engine = innodb;
