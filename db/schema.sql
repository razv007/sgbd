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

    
CREATE OR REPLACE TRIGGER trg_update_evenimente
AFTER INSERT ON Documente
FOR EACH ROW
BEGIN
    UPDATE Evenimente
    SET data_ultima_modificare = SYSTIMESTAMP
    WHERE eveniment_id = :NEW.eveniment_id;
END;
/

-- =============================================================================
-- USER SIMILARITY FUNCTION
-- =============================================================================
CREATE OR REPLACE FUNCTION FN_CALC_USER_SIMILARITY (
    p_username1 IN UTILIZATORI.NUME_UTILIZATOR%TYPE,
    p_username2 IN UTILIZATORI.NUME_UTILIZATOR%TYPE
) RETURN NUMBER
IS
    v_user_id1 UTILIZATORI.UTILIZATOR_ID%TYPE;
    v_user_id2 UTILIZATORI.UTILIZATOR_ID%TYPE;

    -- Scores for each criterion
    v_common_events_score NUMBER := 0;
    v_u1_attends_u2_event_score NUMBER := 0;
    v_u2_attends_u1_event_score NUMBER := 0;
    v_shared_creator_score NUMBER := 0;
    v_common_locations_score NUMBER := 0;
    v_age_similarity_score NUMBER := 0;

    v_total_similarity NUMBER := 0;

    -- Weights
    K_COMMON_EVENTS_WEIGHT CONSTANT NUMBER := 10;
    K_U1_ATTENDS_U2_EVENT_WEIGHT CONSTANT NUMBER := 7;
    K_U2_ATTENDS_U1_EVENT_WEIGHT CONSTANT NUMBER := 7;
    K_SHARED_CREATOR_WEIGHT CONSTANT NUMBER := 3;
    K_COMMON_LOCATIONS_WEIGHT CONSTANT NUMBER := 5;
    K_AGE_SIMILARITY_WEIGHT CONSTANT NUMBER := 4;

    -- Age variables
    v_birth_date1 UTILIZATORI.DATA_NASTERE%TYPE;
    v_birth_date2 UTILIZATORI.DATA_NASTERE%TYPE;
    v_age1 NUMBER;
    v_age2 NUMBER;
    v_age_difference NUMBER;

BEGIN
    -- Convert usernames to IDs
    SELECT utilizator_id INTO v_user_id1 FROM utilizatori WHERE nume_utilizator = p_username1;
    SELECT utilizator_id INTO v_user_id2 FROM utilizatori WHERE nume_utilizator = p_username2;

    IF v_user_id1 = v_user_id2 THEN
        RETURN 0;
    END IF;

    -- Same logic as before (only variable names changed)
    -- CRITERION 1: Common Participated Events
    BEGIN
        SELECT COUNT(DISTINCT p1.eveniment_id) * K_COMMON_EVENTS_WEIGHT
        INTO v_common_events_score
        FROM Participari p1
        JOIN Participari p2 ON p1.eveniment_id = p2.eveniment_id
        WHERE p1.utilizator_id = v_user_id1
          AND p2.utilizator_id = v_user_id2;
    EXCEPTION WHEN OTHERS THEN v_common_events_score := 0;
    END;

    -- CRITERION 2: User1 Participated in Events Created by User2
    BEGIN
        SELECT COUNT(DISTINCT p.eveniment_id) * K_U1_ATTENDS_U2_EVENT_WEIGHT
        INTO v_u1_attends_u2_event_score
        FROM Participari p
        JOIN Evenimente e ON p.eveniment_id = e.eveniment_id
        WHERE p.utilizator_id = v_user_id1
          AND e.nume_utilizator = p_username2;
    EXCEPTION WHEN OTHERS THEN v_u1_attends_u2_event_score := 0;
    END;

    -- CRITERION 3: User2 Participated in Events Created by User1
    BEGIN
        SELECT COUNT(DISTINCT p.eveniment_id) * K_U2_ATTENDS_U1_EVENT_WEIGHT
        INTO v_u2_attends_u1_event_score
        FROM Participari p
        JOIN Evenimente e ON p.eveniment_id = e.eveniment_id
        WHERE p.utilizator_id = v_user_id2
          AND e.nume_utilizator = p_username1;
    EXCEPTION WHEN OTHERS THEN v_u2_attends_u1_event_score := 0;
    END;

    -- CRITERION 4: Shared Third-Party Creators
    BEGIN
        SELECT COUNT(DISTINCT e.nume_utilizator) * K_SHARED_CREATOR_WEIGHT
        INTO v_shared_creator_score
        FROM Evenimente e
        JOIN Participari p1 ON e.eveniment_id = p1.eveniment_id AND p1.utilizator_id = v_user_id1
        JOIN Participari p2 ON e.eveniment_id = p2.eveniment_id AND p2.utilizator_id = v_user_id2
        WHERE e.nume_utilizator NOT IN (p_username1, p_username2);
    EXCEPTION WHEN OTHERS THEN v_shared_creator_score := 0;
    END;

    -- CRITERION 5: Common Event Locations
    BEGIN
        SELECT COUNT(DISTINCT loc1.locatie) * K_COMMON_LOCATIONS_WEIGHT
        INTO v_common_locations_score
        FROM
            (SELECT DISTINCT e.locatie
             FROM Evenimente e
             JOIN Participari p ON e.eveniment_id = p.eveniment_id
             WHERE p.utilizator_id = v_user_id1 AND e.locatie IS NOT NULL) loc1
        JOIN
            (SELECT DISTINCT e.locatie
             FROM Evenimente e
             JOIN Participari p ON e.eveniment_id = p.eveniment_id
             WHERE p.utilizator_id = v_user_id2 AND e.locatie IS NOT NULL) loc2
        ON loc1.locatie = loc2.locatie;
    EXCEPTION WHEN OTHERS THEN v_common_locations_score := 0;
    END;

    -- CRITERION 6: Age Similarity
    BEGIN
        SELECT data_nastere INTO v_birth_date1 FROM utilizatori WHERE utilizator_id = v_user_id1;
        SELECT data_nastere INTO v_birth_date2 FROM utilizatori WHERE utilizator_id = v_user_id2;

        IF v_birth_date1 IS NOT NULL AND v_birth_date2 IS NOT NULL THEN
            v_age1 := TRUNC(MONTHS_BETWEEN(SYSDATE, v_birth_date1) / 12);
            v_age2 := TRUNC(MONTHS_BETWEEN(SYSDATE, v_birth_date2) / 12);
            v_age_difference := ABS(v_age1 - v_age2);

            IF v_age_difference <= 5 THEN
                v_age_similarity_score := K_AGE_SIMILARITY_WEIGHT;
            ELSIF v_age_difference <= 10 THEN
                v_age_similarity_score := K_AGE_SIMILARITY_WEIGHT * 0.5;
            ELSE
                v_age_similarity_score := K_AGE_SIMILARITY_WEIGHT * 0.1;
            END IF;
        ELSE
            v_age_similarity_score := 0;
        END IF;
    EXCEPTION WHEN OTHERS THEN v_age_similarity_score := 0;
    END;

    -- Final score
    v_total_similarity := v_common_events_score +
                          v_u1_attends_u2_event_score +
                          v_u2_attends_u1_event_score +
                          v_shared_creator_score +
                          v_common_locations_score +
                          v_age_similarity_score;

    RETURN v_total_similarity;

EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_CALC_USER_SIMILARITY;
/
