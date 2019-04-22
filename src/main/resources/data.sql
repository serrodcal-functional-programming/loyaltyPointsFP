
create table users (
  id varchar(8) not null primary key,
  loyaltyPoints int not null
);

insert into users (id, loyaltyPoints) values ('1', 10);