-- =============================================================================
-- SCHEMĂ BAZĂ DE DATE PENTRU ARHIVA DIGITALĂ PERSONALĂ INTELIGENTĂ
-- =============================================================================

BEGIN
  FOR cur_rec IN (SELECT object_name, object_type 
                  FROM user_objects 
                  WHERE object_type IN ('TABLE', 'SEQUENCE', 'VIEW', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'FUNCTION', 'TRIGGER'))
  LOOP
    BEGIN
      IF cur_rec.object_type = 'TABLE' THEN
        EXECUTE IMMEDIATE 'DROP ' || cur_rec.object_type || ' "' || cur_rec.object_name || '" CASCADE CONSTRAINTS';
      ELSE
        EXECUTE IMMEDIATE 'DROP ' || cur_rec.object_type || ' "' || cur_rec.object_name || '"';
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.put_line('Eroare la ștergerea ' || cur_rec.object_type || ' "' || cur_rec.object_name || '": ' || SQLERRM);
    END;
  END LOOP;
END;
/
-- =============================================================================
-- SECVENȚE
-- =============================================================================
CREATE SEQUENCE seq_utilizator_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_eveniment_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_document_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- =============================================================================
-- TABELE
-- =============================================================================

CREATE TABLE Utilizatori (
    utilizator_id NUMBER(10) NOT NULL,
    nume_utilizator VARCHAR2(50 CHAR) NOT NULL,
    parola_hash VARCHAR2(255 CHAR) NOT NULL, 
    email VARCHAR2(100 CHAR) NOT NULL,
    nume_complet VARCHAR2(100 CHAR),
    data_inregistrare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    DATA_NASTERE DATE,
    CONSTRAINT pk_utilizatori PRIMARY KEY (utilizator_id),
    CONSTRAINT uk_utilizatori_nume_utilizator UNIQUE (nume_utilizator),
    CONSTRAINT uk_utilizatori_email UNIQUE (email)
);

CREATE TABLE Evenimente (
    eveniment_id NUMBER(10) NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    titlu VARCHAR2(200 CHAR) NOT NULL,
    descriere CLOB,
    data_inceput TIMESTAMP NOT NULL,
    data_sfarsit TIMESTAMP,
    locatie VARCHAR2(255 CHAR),
    tip_eveniment VARCHAR2(50 CHAR),
    vizibilitate VARCHAR2(20 CHAR) DEFAULT 'PRIVAT' NOT NULL, -- PRIVAT, PUBLIC
    data_creare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    data_ultima_modificare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evenimente PRIMARY KEY (eveniment_id),
    CONSTRAINT fk_evenimente_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT ck_evenimente_vizibilitate CHECK (vizibilitate IN ('PRIVAT', 'PUBLIC')),
    CONSTRAINT ck_evenimente_date CHECK (data_sfarsit IS NULL OR data_sfarsit >= data_inceput)
);

CREATE TABLE Participari (
    eveniment_id NUMBER(10) NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    CONSTRAINT pk_participari PRIMARY KEY (eveniment_id,utilizator_id),
    CONSTRAINT fk_participari_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT fk_participari_evenimente FOREIGN KEY (eveniment_id) REFERENCES Evenimente(eveniment_id) ON DELETE CASCADE
);

CREATE TABLE Documente (
    document_id NUMBER(10) NOT NULL,
    eveniment_id NUMBER(10) NOT NULL,
    nume_fisier VARCHAR2(255 CHAR) NOT NULL,
    url VARCHAR2(1000 CHAR) NOT NULL UNIQUE,
    data_incarcare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_documente PRIMARY KEY (document_id),
    CONSTRAINT fk_documente_evenimente FOREIGN KEY (eveniment_id)
        REFERENCES Evenimente(eveniment_id) ON DELETE CASCADE
);


-- =============================================================================
-- INDECȘI (opțional, pentru performanță pe coloane frecvent căutate)
-- =============================================================================
CREATE INDEX idx_documente_eveniment_id ON Documente(eveniment_id);
CREATE INDEX idx_evenimente_data_inceput ON Evenimente(data_inceput DESC);
CREATE INDEX idx_evenimente_utilizator_id ON Evenimente(utilizator_id);

BEGIN
  DBMS_OUTPUT.PUT_LINE('Scriptul schema.sql a fost executat cu succes (după corecții).');
END;
/


CREATE OR REPLACE VIEW View_Utilizator_Evenimente AS
SELECT
    u.nume_utilizator,
    e.eveniment_id,
    e.titlu AS titlu_eveniment,
    e.descriere AS descriere_eveniment,
    e.data_inceput,
    e.data_sfarsit,
    e.locatie,
    e.tip_eveniment,
    e.vizibilitate,
    e.data_creare AS data_creare_eveniment
FROM
    Evenimente e
JOIN
    Utilizatori u ON e.utilizator_id = u.utilizator_id;
    
CREATE OR REPLACE TRIGGER trg_update_evenimente
AFTER INSERT ON Documente
FOR EACH ROW
BEGIN
    UPDATE Evenimente
    SET data_ultima_modificare = SYSTIMESTAMP
    WHERE eveniment_id = :NEW.eveniment_id;
END;
/

<<<<<<< HEAD
-- =============================================================================
-- USER SIMILARITY FUNCTION
-- =============================================================================

CREATE OR REPLACE FUNCTION FN_CALC_USER_SIMILARITY (
    p_user_id1 IN UTILIZATORI.UTILIZATOR_ID%TYPE,
    p_user_id2 IN UTILIZATORI.UTILIZATOR_ID%TYPE
) RETURN NUMBER
IS
    -- Scores for each criterion
    v_common_events_score NUMBER := 0;
    v_u1_attends_u2_event_score NUMBER := 0;
    v_u2_attends_u1_event_score NUMBER := 0;
    v_shared_creator_score NUMBER := 0;
    v_common_locations_score NUMBER := 0;
    v_age_similarity_score NUMBER := 0; -- New score variable

    v_total_similarity NUMBER := 0;

    -- Weights
    K_COMMON_EVENTS_WEIGHT CONSTANT NUMBER := 10;
    K_U1_ATTENDS_U2_EVENT_WEIGHT CONSTANT NUMBER := 7;
    K_U2_ATTENDS_U1_EVENT_WEIGHT CONSTANT NUMBER := 7;
    K_SHARED_CREATOR_WEIGHT CONSTANT NUMBER := 3;
    K_COMMON_LOCATIONS_WEIGHT CONSTANT NUMBER := 5;
    K_AGE_SIMILARITY_WEIGHT CONSTANT NUMBER := 4; -- New weight for age similarity

    -- Variables for age calculation
    v_birth_date1 UTILIZATORI.DATA_NASTERE%TYPE;
    v_birth_date2 UTILIZATORI.DATA_NASTERE%TYPE;
    v_age1 NUMBER;
    v_age2 NUMBER;
    v_age_difference NUMBER;

BEGIN
    IF p_user_id1 = p_user_id2 THEN
        RETURN 0;
    END IF;

    -- CRITERION 1: Common Participated Events (cod existent)
    BEGIN
        SELECT COUNT(DISTINCT p1.eveniment_id) * K_COMMON_EVENTS_WEIGHT
        INTO v_common_events_score
        FROM Participari p1
        JOIN Participari p2 ON p1.eveniment_id = p2.eveniment_id
        WHERE p1.utilizator_id = p_user_id1
          AND p2.utilizator_id = p_user_id2;
        DBMS_OUTPUT.PUT_LINE('User ' || p_user_id1 || ' & ' || p_user_id2 || ' - C1 (CommonEvents) Score: ' || v_common_events_score);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN v_common_events_score := 0;
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Error C1 (CommonEvents) for ' || p_user_id1 || ',' || p_user_id2 || ': ' || SQLERRM);
            v_common_events_score := 0;
    END;

    -- CRITERION 2: User1 Participated in Events Created by User2 (cod existent)
    BEGIN
        SELECT COUNT(DISTINCT p.eveniment_id) * K_U1_ATTENDS_U2_EVENT_WEIGHT
        INTO v_u1_attends_u2_event_score
        FROM Participari p
        JOIN Evenimente e ON p.eveniment_id = e.eveniment_id
        WHERE p.utilizator_id = p_user_id1
          AND e.utilizator_id = p_user_id2;
        DBMS_OUTPUT.PUT_LINE('User ' || p_user_id1 || ' & ' || p_user_id2 || ' - C2 (U1AttendsU2Event) Score: ' || v_u1_attends_u2_event_score);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN v_u1_attends_u2_event_score := 0;
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Error C2 (U1AttendsU2Event) for ' || p_user_id1 || ',' || p_user_id2 || ': ' || SQLERRM);
            v_u1_attends_u2_event_score := 0;
    END;

    -- CRITERION 3: User2 Participated in Events Created by User1 (cod existent)
    BEGIN
        SELECT COUNT(DISTINCT p.eveniment_id) * K_U2_ATTENDS_U1_EVENT_WEIGHT
        INTO v_u2_attends_u1_event_score
        FROM Participari p
        JOIN Evenimente e ON p.eveniment_id = e.eveniment_id
        WHERE p.utilizator_id = p_user_id2
          AND e.utilizator_id = p_user_id1;
        DBMS_OUTPUT.PUT_LINE('User ' || p_user_id1 || ' & ' || p_user_id2 || ' - C3 (U2AttendsU1Event) Score: ' || v_u2_attends_u1_event_score);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN v_u2_attends_u1_event_score := 0;
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Error C3 (U2AttendsU1Event) for ' || p_user_id1 || ',' || p_user_id2 || ': ' || SQLERRM);
            v_u2_attends_u1_event_score := 0;
    END;

    -- CRITERION 4: Shared Interest via Third-Party Creators (cod existent)
    BEGIN
        SELECT COUNT(DISTINCT e.utilizator_id) * K_SHARED_CREATOR_WEIGHT
        INTO v_shared_creator_score
        FROM Evenimente e
        JOIN Participari p1 ON e.eveniment_id = p1.eveniment_id AND p1.utilizator_id = p_user_id1
        JOIN Participari p2 ON e.eveniment_id = p2.eveniment_id AND p2.utilizator_id = p_user_id2
        WHERE e.utilizator_id NOT IN (p_user_id1, p_user_id2);
        DBMS_OUTPUT.PUT_LINE('User ' || p_user_id1 || ' & ' || p_user_id2 || ' - C4 (SharedCreator) Score: ' || v_shared_creator_score);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN v_shared_creator_score := 0;
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Error C4 (SharedCreator) for ' || p_user_id1 || ',' || p_user_id2 || ': ' || SQLERRM);
            v_shared_creator_score := 0;
    END;

    -- CRITERION 5: Common Event Locations (cod existent)
    BEGIN
        SELECT COUNT(DISTINCT loc1.locatie) * K_COMMON_LOCATIONS_WEIGHT
        INTO v_common_locations_score
        FROM
            (SELECT DISTINCT e.locatie
             FROM Evenimente e
             JOIN Participari p ON e.eveniment_id = p.eveniment_id
             WHERE p.utilizator_id = p_user_id1 AND e.locatie IS NOT NULL) loc1
        JOIN
            (SELECT DISTINCT e.locatie
             FROM Evenimente e
             JOIN Participari p ON e.eveniment_id = p.eveniment_id
             WHERE p.utilizator_id = p_user_id2 AND e.locatie IS NOT NULL) loc2
        ON loc1.locatie = loc2.locatie;
        DBMS_OUTPUT.PUT_LINE('User ' || p_user_id1 || ' & ' || p_user_id2 || ' - C5 (CommonLocations) Score: ' || v_common_locations_score);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN v_common_locations_score := 0;
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Error C5 (CommonLocations) for ' || p_user_id1 || ',' || p_user_id2 || ': ' || SQLERRM);
            v_common_locations_score := 0;
    END;

    -- CRITERION 6: Age Similarity
    BEGIN
        SELECT data_nastere INTO v_birth_date1 FROM Utilizatori WHERE utilizator_id = p_user_id1;
        SELECT data_nastere INTO v_birth_date2 FROM Utilizatori WHERE utilizator_id = p_user_id2;

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
            
            DBMS_OUTPUT.PUT_LINE('User ' || p_user_id1 || ' (Age '||v_age1||') & ' || p_user_id2 || ' (Age '||v_age2||') - C6 (AgeSimilarity) Diff: ' || v_age_difference || ', Score: ' || v_age_similarity_score);
        ELSE
            v_age_similarity_score := 0; 
            DBMS_OUTPUT.PUT_LINE('User ' || p_user_id1 || ' & ' || p_user_id2 || ' - C6 (AgeSimilarity) Score: 0 (missing birth date(s))');
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN 
            v_age_similarity_score := 0;
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Error C6 (AgeSimilarity) for ' || p_user_id1 || ',' || p_user_id2 || ': ' || SQLERRM);
            v_age_similarity_score := 0;
    END;


    v_total_similarity := v_common_events_score +
                          v_u1_attends_u2_event_score +
                          v_u2_attends_u1_event_score +
                          v_shared_creator_score +
                          v_common_locations_score +
                          v_age_similarity_score; -- Add new score

    DBMS_OUTPUT.PUT_LINE('Final Similarity Score between User ' || p_user_id1 || ' and User ' || p_user_id2 || ': ' || v_total_similarity);

    RETURN v_total_similarity;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Unhandled Error in FN_CALC_USER_SIMILARITY for users ' || p_user_id1 || ', ' || p_user_id2 || ': ' || SQLERRM);
        RETURN 0;
END FN_CALC_USER_SIMILARITY;
/

SET SERVEROUTPUT ON;
DECLARE
    v_score NUMBER;
BEGIN
    -- Inlocuieste 1 si 2 cu ID-uri de utilizatori existenti in baza ta de date
    v_score := FN_CALC_USER_SIMILARITY(1, 2); 
    DBMS_OUTPUT.PUT_LINE('Test Score: ' || v_score);
END;
/
=======

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
>>>>>>> 6a0ba1a4e97e022f694a35609d9efe4c72b535e8
