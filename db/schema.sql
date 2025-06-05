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
