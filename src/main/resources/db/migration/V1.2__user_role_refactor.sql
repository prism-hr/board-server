create table resource (
  id                bigint unsigned not null auto_increment,
  type              varchar(20)     not null,
  name              varchar(255)    not null,
  description       text,
  document_logo_id  bigint,
  category_list     text,
  created_timestamp datetime        not null,
  updated_timestamp datetime,
  old_id            bigint,
  primary key (id),
  index name (type, name),
  index (document_logo_id),
  index created_timestamp (type, created_timestamp),
  index updated_timestamp (type, updated_timestamp),
  foreign key (document_logo_id) references document (id)
)
  collate = utf8_general_ci
  engine = innodb;

insert into resource (type, name, document_logo_id, category_list, created_timestamp, updated_timestamp, old_id)
  select
    'DEPARTMENT',
    name,
    document_logo_id,
    member_categories,
    created_timestamp,
    updated_timestamp,
    id
  from department;

insert into resource (type, name, description, document_logo_id, category_list, created_timestamp, updated_timestamp, old_id)
  select
    'BOARD',
    name,
    purpose,
    document_logo_id,
    member_categories,
    created_timestamp,
    updated_timestamp,
    id
  from department;

create table resource_relation (
  id                bigint unsigned not null auto_increment,
  resource_id1      bigint unsigned,
  resource_id2      bigint unsigned,
  created_timestamp datetime        not null,
  updated_timestamp datetime,
  primary key (id),
  unique index (resource_id1, resource_id2),
  index (resource_id2),
  index (created_timestamp),
  index (updated_timestamp),
  foreign key (resource_id1) references resource (id),
  foreign key (resource_id2) references resource (id)
)
  collate = utf8_general_ci
  engine = innodb;

insert into resource_relation (resource_id1, resource_id2, created_timestamp, updated_timestamp)
  select
    id,
    id,
    created_timestamp,
    updated_timestamp
  from resource;

insert into resource_relation (resource_id1, resource_id2, created_timestamp, updated_timestamp)
  select
    resource.id,
    resource2.id,
    board.created_timestamp,
    board.updated_timestamp
  from resource
    inner join department
      on resource.old_id = department.id
         and resource.type = 'DEPARTMENT'
    inner join board
      on department.id = board.department_id
    inner join resource as resource2
      on resource2.old_id = board.id
         and resource2.type = 'BOARD';

create table user_role (
  id                bigint unsigned not null auto_increment,
  resource_id       bigint unsigned not null,
  user_id           bigint unsigned not null,
  role              varchar(20)     not null,
  created_timestamp datetime        not null,
  updated_timestamp datetime,
  primary key (id),
  unique index (resource_id, user_id, role),
  index (user_id),
  index (created_timestamp),
  index (updated_timestamp),
  foreign key (resource_id) references resource (id),
  foreign key (user_id) references user (id)
)
  collate = utf8_general_ci
  engine = innodb;

insert into user_role (resource_id, user_id, role, created_timestamp, updated_timestamp)
  select
    resource.id,
    department.user_id,
    'ADMINISTRATOR',
    resource.created_timestamp,
    resource.updated_timestamp
  from resource
    inner join department
      on resource.old_id = department.id
         and resource.type = 'DEPARTMENT';

insert into user_role (resource_id, user_id, role, created_timestamp, updated_timestamp)
  select
    resource.id,
    department.user_id,
    'ADMINISTRATOR',
    resource.created_timestamp,
    resource.updated_timestamp
  from resource
    inner join board
      on resource.old_id = board.id
         and resource.type = 'BOARD';

alter table resource
  drop column old_id;

drop table board;

drop table department;
