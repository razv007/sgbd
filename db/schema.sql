-- =============================================================================
-- SCHEMĂ BAZĂ DE DATE PENTRU ARHIVA DIGITALĂ PERSONALĂ INTELIGENTĂ
-- =============================================================================

-- Curățarea obiectelor existente (opțional, pentru dezvoltare)

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
CREATE SEQUENCE seq_amintire_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_timeline_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_prietenie_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_notificare_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_preferinta_timeline_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- =============================================================================
-- TABELE
-- =============================================================================

-- Tabela Utilizatori
CREATE TABLE Utilizatori (
    utilizator_id NUMBER(10) NOT NULL,
    nume_utilizator VARCHAR2(50 CHAR) NOT NULL,
    parola_hash VARCHAR2(255 CHAR) NOT NULL, -- Se va stoca hash-ul parolei
    email VARCHAR2(100 CHAR) NOT NULL,
    nume_complet VARCHAR2(100 CHAR),
    data_inregistrare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    data_ultima_logare TIMESTAMP,
    este_activ NUMBER(1) DEFAULT 1 NOT NULL, -- 1 pentru activ, 0 pentru inactiv
    rol VARCHAR2(20 CHAR) DEFAULT 'USER' NOT NULL, -- ex: USER, ADMIN
    preferinte_notificari CLOB, -- JSON sau XML pentru setări detaliate
    DATA_NASTERE DATE,
    CONSTRAINT pk_utilizatori PRIMARY KEY (utilizator_id),
    CONSTRAINT uk_utilizatori_nume_utilizator UNIQUE (nume_utilizator),
    CONSTRAINT uk_utilizatori_email UNIQUE (email),
    CONSTRAINT ck_utilizatori_este_activ CHECK (este_activ IN (0, 1)),
    CONSTRAINT ck_utilizatori_rol CHECK (rol IN ('USER', 'ADMIN'))
);

-- Tabela Evenimente
CREATE TABLE Evenimente (
    eveniment_id NUMBER(10) NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    titlu VARCHAR2(200 CHAR) NOT NULL,
    descriere CLOB,
    data_inceput TIMESTAMP NOT NULL,
    data_sfarsit TIMESTAMP,
    locatie VARCHAR2(255 CHAR),
    tip_eveniment VARCHAR2(50 CHAR), -- ex: personal, profesional, educational
    vizibilitate VARCHAR2(20 CHAR) DEFAULT 'PRIVAT' NOT NULL, -- PRIVAT, PRIETENI, PUBLIC
    data_creare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    data_ultima_modificare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evenimente PRIMARY KEY (eveniment_id),
    CONSTRAINT fk_evenimente_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT ck_evenimente_vizibilitate CHECK (vizibilitate IN ('PRIVAT', 'PRIETENI', 'PUBLIC')),
    CONSTRAINT ck_evenimente_date CHECK (data_sfarsit IS NULL OR data_sfarsit >= data_inceput)
);

-- Tabela Amintiri_Digitale
CREATE TABLE Amintiri_Digitale (
    amintire_id NUMBER(10) NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    nume_fisier VARCHAR2(255 CHAR) NOT NULL,
    tip_media VARCHAR2(50 CHAR), -- ex: imagine, video, document, audio
    cale_stocare VARCHAR2(1000 CHAR) NOT NULL, -- Poate fi o cale în sistemul de fișiere sau un URL
    dimensiune_fisier NUMBER, -- în bytes
    metadata CLOB, -- JSON sau XML pentru metadate specifice (ex: EXIF pentru imagini)
    data_incarcare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    descriere_amintire VARCHAR2(500 CHAR),
    data_ultima_modificare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_amintiri PRIMARY KEY (amintire_id),
    CONSTRAINT fk_amintiri_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE
);

-- Tabela de legătură Evenimente - Amintiri_Digitale (relație many-to-many)
CREATE TABLE Legaturi_Eveniment_Amintire (
    eveniment_id NUMBER(10) NOT NULL,
    amintire_id NUMBER(10) NOT NULL,
    CONSTRAINT pk_leg_even_amint PRIMARY KEY (eveniment_id, amintire_id),
    CONSTRAINT fk_leg_even_art_evenimente FOREIGN KEY (eveniment_id) REFERENCES Evenimente(eveniment_id) ON DELETE CASCADE,
    CONSTRAINT fk_leg_even_amint_amintiri FOREIGN KEY (amintire_id) REFERENCES Amintiri_Digitale(amintire_id) ON DELETE CASCADE
);

-- Tabela Timeline
CREATE TABLE Timeline (
    timeline_id NUMBER(10) NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    nume_timeline VARCHAR2(100 CHAR) NOT NULL,
    descriere_timeline CLOB,
    data_creare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_timeline PRIMARY KEY (timeline_id),
    CONSTRAINT fk_timeline_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT uk_timeline_util_nume UNIQUE (utilizator_id, nume_timeline) -- Asigură unicitatea numelui timeline-ului per utilizator
);

-- Tabela Prietenii
CREATE TABLE Prietenii (
    prietenie_id NUMBER(10) NOT NULL,
    utilizator1_id NUMBER(10) NOT NULL,
    utilizator2_id NUMBER(10) NOT NULL,
    stare_prietenie VARCHAR2(20 CHAR) DEFAULT 'IN_ASTEPTARE' NOT NULL, -- IN_ASTEPTARE, ACCEPTATA, RESPINGA, BLOCATA
    data_solicitare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    data_raspuns TIMESTAMP,
    CONSTRAINT pk_prietenii PRIMARY KEY (prietenie_id),
    CONSTRAINT fk_prietenii_user1 FOREIGN KEY (utilizator1_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT fk_prietenii_user2 FOREIGN KEY (utilizator2_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT uk_prietenie_unica CHECK (utilizator1_id < utilizator2_id), -- Asigură unicitatea perechii (user1, user2) indiferent de ordine
    CONSTRAINT ck_prietenii_stare CHECK (stare_prietenie IN ('IN_ASTEPTARE', 'ACCEPTATA', 'RESPINSA', 'BLOCATA'))
);

-- Tabela Notificari
CREATE TABLE Notificari (
    notificare_id NUMBER(10) NOT NULL,
    utilizator_id NUMBER(10) NOT NULL, -- Utilizatorul care primește notificarea
    tip_notificare VARCHAR2(50 CHAR) NOT NULL, -- ex: PRIETENIE_NOUA, EVENIMENT_PARTAJAT, COMENTARIU_NOU
    mesaj VARCHAR2(500 CHAR) NOT NULL,
    referinta_id NUMBER(10), -- ID-ul obiectului la care se referă notificarea (ex: eveniment_id, prietenie_id)
    referinta_tip VARCHAR2(50 CHAR), -- ex: EVENIMENT, PRIETENIE, TIMELINE
    este_citita NUMBER(1) DEFAULT 0 NOT NULL, -- 0 pentru necitită, 1 pentru citită
    data_creare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_notificari PRIMARY KEY (notificare_id),
    CONSTRAINT fk_notificari_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT ck_notificari_este_citita CHECK (este_citita IN (0, 1))
);

-- Tabela Preferinte_Timeline (pentru setările de vizibilitate ale timeline-ului utilizatorului)
CREATE TABLE Preferinte_Timeline (
    preferinta_id NUMBER(10) NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    tip_vizibilitate VARCHAR2(20 CHAR) DEFAULT 'PRIVAT' NOT NULL,
    data_modificare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_preferinte_timeline PRIMARY KEY (preferinta_id),
    CONSTRAINT fk_pref_timeline_util FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT uk_preferinte_timeline_util_id UNIQUE (utilizator_id), -- Asigură o singură înregistrare de preferințe per utilizator
    CONSTRAINT ck_preferinte_tip_vizibilitate CHECK (tip_vizibilitate IN ('PRIVAT', 'PRIETENI', 'PUBLIC_AUTENTIFICAT'))
);

-- =============================================================================
-- INDECȘI (opțional, pentru performanță pe coloane frecvent căutate)
-- =============================================================================
CREATE INDEX idx_evenimente_data_inceput ON Evenimente(data_inceput DESC);
CREATE INDEX idx_evenimente_utilizator_id ON Evenimente(utilizator_id);
CREATE INDEX idx_amintiri_utilizator_id ON Amintiri_Digitale(utilizator_id);
CREATE INDEX idx_notificari_utilizator_id ON Notificari(utilizator_id);

BEGIN
  DBMS_OUTPUT.PUT_LINE('Scriptul schema.sql a fost executat cu succes (după corecții).');
END;
/

CREATE OR REPLACE VIEW V_AMINTIRI_UTILIZATORI AS
SELECT
    a.amintire_id,
    a.nume_fisier,
    a.tip_media,
    a.cale_stocare,
    a.metadata,
    a.descriere_amintire,
    a.data_incarcare,
    a.data_ultima_modificare AS data_modif_amintire, -- Alias to avoid confusion
    u.utilizator_id,
    u.nume_utilizator,
    u.email AS email_utilizator,
    u.nume_complet AS nume_complet_utilizator
FROM
    Amintiri_Digitale a
JOIN
    Utilizatori u ON a.utilizator_id = u.utilizator_id;

-- View pentru a afișa evenimentele împreună cu detaliile utilizatorului creator
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

CREATE OR REPLACE VIEW View_Detalii_Prietenii AS
SELECT
    p.prietenie_id,
    u1.nume_utilizator AS nume_utilizator1,
    u2.nume_utilizator AS nume_utilizator2,
    p.stare_prietenie,
    p.data_solicitare,
    p.data_raspuns
FROM
    Prietenii p
JOIN
    Utilizatori u1 ON p.utilizator1_id = u1.utilizator_id
JOIN
    Utilizatori u2 ON p.utilizator2_id = u2.utilizator_id;
    
SELECT * FROM View_Detalii_Prietenii;