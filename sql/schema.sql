create table tree
(
    id       serial8 primary key,
    name     varchar(20) not null,
    leftKey  int4        not null,
    rightKey int4        not null,
    level    int4        not null
);
