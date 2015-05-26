create table APP.PAR_PARTICIPANTS (PRJ_ID bigint not null, PER_ID bigint not null, constraint PK_PAR primary key (PRJ_ID, PER_ID)) in TSAPP4; 

create table APP.PER_PERSON (PER_ID bigint generated by default as identity (start with 1), PER_DT_BIRTH varbinary(255), PER_NM_CELLPHONE varchar(255), PER_ST_FIRST_NAME varchar(255), PER_ST_LAST_NAME varchar(255), constraint PK_PER primary key (PER_ID)) in TSAPP4; 

create table APP.PRJ_PROJECT (PRJ_ID bigint generated by default as identity (start with 1), PRJ_DT_BEGIN varbinary(255), PRJ_DT_END varbinary(255), PRJ_ST_NAME varchar(255), PRJ_ST_STATUS integer, constraint PK_PRJ primary key (PRJ_ID)) in TSAPP4; 

alter table APP.PAR_PARTICIPANTS add constraint IRPRJPAR foreign key (PRJ_ID) references APP.PRJ_PROJECT; 

alter table APP.PAR_PARTICIPANTS add constraint IRPERPAR foreign key (PER_ID) references APP.PER_PERSON; 

grant select on APP.PAR_PARTICIPANTS to GAPPDR; 

grant select on APP.PER_PERSON to GAPPDR; 

grant select on APP.PRJ_PROJECT to GAPPDR; 

grant delete, insert, select, update on APP.PAR_PARTICIPANTS to GAPPDW; 

grant delete, insert, select, update on APP.PER_PERSON to GAPPDW; 

grant delete, insert, select, update on APP.PRJ_PROJECT to GAPPDW; 

create index APP.FKPARPER on APP.PAR_PARTICIPANTS (PER_ID) allow reverse scans; 

create index APP.FKPARPRJ on APP.PAR_PARTICIPANTS (PRJ_ID) allow reverse scans; 

-- Content of this file is appended in generated SQL file 
