if exists (select 1
           from  sysobjects
           where id = object_id('custom')
                 and   type = 'U')
  drop table custom
go

/*==============================================================*/
/* Table: t_rule_prepaid_card                                   */
/*==============================================================*/
create table custom (
  first_name             varchar(100)                   null,
  last_name             varchar(100)                   null,
)
go


INSERT INTO custom (first_name, last_name) VALUES ('1', '1') go
INSERT INTO custom (first_name, last_name) VALUES ('2', '2') go
INSERT INTO custom (first_name, last_name) VALUES ('3', '3') go
INSERT INTO custom (first_name, last_name) VALUES ('4', '4') go
INSERT INTO custom (first_name, last_name) VALUES ('5', '5') go
INSERT INTO custom (first_name, last_name) VALUES ('6', '6') go
INSERT INTO custom (first_name, last_name) VALUES ('7', '7') go
INSERT INTO custom (first_name, last_name) VALUES ('8', '8') go
INSERT INTO custom (first_name, last_name) VALUES ('9', '8') go
INSERT INTO custom (first_name, last_name) VALUES ('10', '10') go
INSERT INTO custom (first_name, last_name) VALUES ('11', '11') go
INSERT INTO custom (first_name, last_name) VALUES ('12', '12') go
INSERT INTO custom (first_name, last_name) VALUES ('13', '13') go
