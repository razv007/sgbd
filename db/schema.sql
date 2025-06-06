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

-- =============================================================================
-- USER SIMILARITY FUNCTION
-- =============================================================================
create or replace function fn_calc_user_similarity (
   p_username1 in utilizatori.nume_utilizator%type,
   p_username2 in utilizatori.nume_utilizator%type
) return number is
   v_user_id1                   utilizatori.utilizator_id%type;
   v_user_id2                   utilizatori.utilizator_id%type;

    -- Scores for each criterion
   v_common_events_score        number := 0;
   v_u1_attends_u2_event_score  number := 0;
   v_u2_attends_u1_event_score  number := 0;
   v_shared_creator_score       number := 0;
   v_common_locations_score     number := 0;
   v_age_similarity_score       number := 0;
   v_total_similarity           number := 0;

    -- Weights
   k_common_events_weight       constant number := 10;
   k_u1_attends_u2_event_weight constant number := 7;
   k_u2_attends_u1_event_weight constant number := 7;
   k_shared_creator_weight      constant number := 3;
   k_common_locations_weight    constant number := 5;
   k_age_similarity_weight      constant number := 4;

    -- Age variables
   v_birth_date1                utilizatori.data_nastere%type;
   v_birth_date2                utilizatori.data_nastere%type;
   v_age1                       number;
   v_age2                       number;
   v_age_difference             number;
begin
    -- Convert usernames to IDs
   select utilizator_id
     into v_user_id1
     from utilizatori
    where nume_utilizator = p_username1;
   select utilizator_id
     into v_user_id2
     from utilizatori
    where nume_utilizator = p_username2;

   if v_user_id1 = v_user_id2 then
      return 0;
   end if;

    -- Same logic as before (only variable names changed)
    -- CRITERION 1: Common Participated Events
   begin
      select count(distinct p1.eveniment_id) * k_common_events_weight
        into v_common_events_score
        from participari p1
        join participari p2
      on p1.eveniment_id = p2.eveniment_id
       where p1.utilizator_id = v_user_id1
         and p2.utilizator_id = v_user_id2;
   exception
      when others then
         v_common_events_score := 0;
   end;

    -- CRITERION 2: User1 Participated in Events Created by User2
   begin
      select count(distinct p.eveniment_id) * k_u1_attends_u2_event_weight
        into v_u1_attends_u2_event_score
        from participari p
        join evenimente e
      on p.eveniment_id = e.eveniment_id
       where p.utilizator_id = v_user_id1
         and e.nume_utilizator = p_username2;
   exception
      when others then
         v_u1_attends_u2_event_score := 0;
   end;

    -- CRITERION 3: User2 Participated in Events Created by User1
   begin
      select count(distinct p.eveniment_id) * k_u2_attends_u1_event_weight
        into v_u2_attends_u1_event_score
        from participari p
        join evenimente e
      on p.eveniment_id = e.eveniment_id
       where p.utilizator_id = v_user_id2
         and e.nume_utilizator = p_username1;
   exception
      when others then
         v_u2_attends_u1_event_score := 0;
   end;

    -- CRITERION 4: Shared Third-Party Creators
   begin
      select count(distinct e.nume_utilizator) * k_shared_creator_weight
        into v_shared_creator_score
        from evenimente e
        join participari p1
      on e.eveniment_id = p1.eveniment_id
         and p1.utilizator_id = v_user_id1
        join participari p2
      on e.eveniment_id = p2.eveniment_id
         and p2.utilizator_id = v_user_id2
       where e.nume_utilizator not in ( p_username1,
                                        p_username2 );
   exception
      when others then
         v_shared_creator_score := 0;
   end;

    -- CRITERION 5: Common Event Locations
   begin
      select count(distinct loc1.locatie) * k_common_locations_weight
        into v_common_locations_score
        from (
         select distinct e.locatie
           from evenimente e
           join participari p
         on e.eveniment_id = p.eveniment_id
          where p.utilizator_id = v_user_id1
            and e.locatie is not null
      ) loc1
        join (
         select distinct e.locatie
           from evenimente e
           join participari p
         on e.eveniment_id = p.eveniment_id
          where p.utilizator_id = v_user_id2
            and e.locatie is not null
      ) loc2
      on loc1.locatie = loc2.locatie;
   exception
      when others then
         v_common_locations_score := 0;
   end;

    -- CRITERION 6: Age Similarity
   begin
      select data_nastere
        into v_birth_date1
        from utilizatori
       where utilizator_id = v_user_id1;
      select data_nastere
        into v_birth_date2
        from utilizatori
       where utilizator_id = v_user_id2;

      if
         v_birth_date1 is not null
         and v_birth_date2 is not null
      then
         v_age1 := trunc(months_between(
            sysdate,
            v_birth_date1
         ) / 12);
         v_age2 := trunc(months_between(
            sysdate,
            v_birth_date2
         ) / 12);
         v_age_difference := abs(v_age1 - v_age2);
         if v_age_difference <= 5 then
            v_age_similarity_score := k_age_similarity_weight;
         elsif v_age_difference <= 10 then
            v_age_similarity_score := k_age_similarity_weight * 0.5;
         else
            v_age_similarity_score := k_age_similarity_weight * 0.1;
         end if;
      else
         v_age_similarity_score := 0;
      end if;
   exception
      when others then
         v_age_similarity_score := 0;
   end;

    -- Final score
   v_total_similarity := v_common_events_score + v_u1_attends_u2_event_score + v_u2_attends_u1_event_score + v_shared_creator_score
   + v_common_locations_score + v_age_similarity_score;
   return v_total_similarity;
exception
   when others then
      return 0;
end fn_calc_user_similarity;
/

select fn_calc_user_similarity('Razv007', 'User') from dual;