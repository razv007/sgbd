-- =============================================================================
-- SCHEMĂ BAZĂ DE DATE PENTRU ARHIVA DIGITALĂ PERSONALĂ INTELIGENTĂ
-- =============================================================================

begin
   for cur_rec in (
      select object_name,
             object_type
        from user_objects
       where object_type in ( 'TABLE',
                              'SEQUENCE',
                              'VIEW',
                              'PACKAGE',
                              'PACKAGE BODY',
                              'PROCEDURE',
                              'FUNCTION',
                              'TRIGGER' )
   ) loop
      begin
         if cur_rec.object_type = 'TABLE' then
            execute immediate 'DROP '
                              || cur_rec.object_type
                              || ' "'
                              || cur_rec.object_name
                              || '" CASCADE CONSTRAINTS';
         else
            execute immediate 'DROP '
                              || cur_rec.object_type
                              || ' "'
                              || cur_rec.object_name
                              || '"';
         end if;
      exception
         when others then
            dbms_output.put_line('Eroare la ștergerea '
                                 || cur_rec.object_type
                                 || ' "'
                                 || cur_rec.object_name
                                 || '": '
                                 || sqlerrm);
      end;
   end loop;
end;
/
-- =============================================================================
-- SECVENȚE
-- =============================================================================
create sequence seq_utilizator_id start with 1 increment by 1 nocache nocycle;
create sequence seq_eveniment_id start with 1 increment by 1 nocache nocycle;
create sequence seq_document_id start with 1 increment by 1 nocache nocycle;

-- =============================================================================
-- TABELE
-- =============================================================================

create table utilizatori (
   utilizator_id     number(10) not null,
   nume_utilizator   varchar2(50 char) not null,
   parola_hash       varchar2(255 char) not null,
   email             varchar2(100 char) not null,
   nume_complet      varchar2(100 char),
   data_inregistrare timestamp default systimestamp not null,
   data_nastere      date,
   constraint pk_utilizatori primary key ( utilizator_id ),
   constraint uk_utilizatori_nume_utilizator unique ( nume_utilizator ),
   constraint uk_utilizatori_email unique ( email )
);

create table evenimente (
   eveniment_id           number(10) not null,
   nume_utilizator        varchar2(50 char) not null,
   titlu                  varchar2(200 char) not null,
   descriere              clob,
   data_inceput           timestamp not null,
   data_sfarsit           timestamp,
   locatie                varchar2(255 char),
   tip_eveniment          varchar2(50 char),
   vizibilitate           varchar2(20 char) default 'PRIVAT' not null, -- PRIVAT, PUBLIC
   data_creare            timestamp default systimestamp not null,
   data_ultima_modificare timestamp default systimestamp not null,
   constraint pk_evenimente primary key ( eveniment_id ),
   constraint fk_evenimente_utilizatori foreign key ( nume_utilizator )
      references utilizatori ( nume_utilizator ),
   constraint ck_evenimente_vizibilitate check ( vizibilitate in ( 'PRIVAT',
                                                                   'PUBLIC' ) ),
   constraint ck_evenimente_date
      check ( data_sfarsit is null
          or data_sfarsit >= data_inceput )
);

create table participari (
   eveniment_id  number(10) not null,
   utilizator_id number(10) not null,
   constraint pk_participari primary key ( eveniment_id,
                                           utilizator_id ),
   constraint fk_participari_utilizatori foreign key ( utilizator_id )
      references utilizatori ( utilizator_id )
         on delete cascade,
   constraint fk_participari_evenimente foreign key ( eveniment_id )
      references evenimente ( eveniment_id )
         on delete cascade
);

create table documente (
   document_id    number(10) not null,
   eveniment_id   number(10) not null,
   nume_fisier    varchar2(255 char) not null,
   url            varchar2(1000 char) not null unique,
   data_incarcare timestamp default systimestamp not null,
   constraint pk_documente primary key ( document_id ),
   constraint fk_documente_evenimente foreign key ( eveniment_id )
      references evenimente ( eveniment_id )
         on delete cascade
);


-- =============================================================================
-- INDECȘI (opțional, pentru performanță pe coloane frecvent căutate)
-- =============================================================================
create index idx_documente_eveniment_id on
   documente (
      eveniment_id
   );
create index idx_evenimente_data_inceput on
   evenimente (
      data_inceput
   desc );
create index idx_evenimente_utilizator on
   evenimente (
      nume_utilizator
   );

begin
   dbms_output.put_line('Scriptul schema.sql a fost executat cu succes (după corecții).');
end;
/


create or replace view view_utilizator_evenimente as
   select e.nume_utilizator,
          e.eveniment_id,
          e.titlu as titlu_eveniment,
          e.descriere as descriere_eveniment,
          e.data_inceput,
          e.data_sfarsit,
          e.locatie,
          e.tip_eveniment,
          e.vizibilitate,
          e.data_creare as data_creare_eveniment
     from evenimente e;
create or replace trigger trg_update_evenimente after
   insert on documente
   for each row
begin
   update evenimente
      set
      data_ultima_modificare = systimestamp
    where eveniment_id = :new.eveniment_id;
end;
/


-- CREATE OR REPLACE TRIGGER trg_delete_event_if_no_participants
-- AFTER DELETE ON Participari
-- FOR EACH ROW
-- DECLARE
--     v_count NUMBER;
-- BEGIN
--     -- Count remaining participants for the event
--     SELECT COUNT(*) INTO v_count
--     FROM Participari
--     WHERE eveniment_id = :OLD.eveniment_id;

--     -- If none are left, delete the event
--     IF v_count = 0 THEN
--         DELETE FROM Evenimente WHERE eveniment_id = :OLD.eveniment_id;
--     END IF;
-- END;
-- /