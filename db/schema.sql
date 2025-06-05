-- ======================================================================
-- Script pentru crearea schemei bazei de date Digital Archive
-- ======================================================================

-- Decomentează următoarele linii dacă vrei să ștergi tabelele și secvențele existente înainte de a le recrea
-- ATENȚIE: ACEASTA VA ȘTERGE TOATE DATELE EXISTENTE!
/*
DROP TABLE IstoricActivitati CASCADE CONSTRAINTS;
DROP TABLE SetariUtilizator CASCADE CONSTRAINTS;
DROP TABLE Notificari CASCADE CONSTRAINTS;
DROP TABLE Partajari CASCADE CONSTRAINTS;
DROP TABLE Comentarii CASCADE CONSTRAINTS;
DROP TABLE AmintiriEtichete CASCADE CONSTRAINTS;
DROP TABLE Etichete CASCADE CONSTRAINTS;
DROP TABLE Prietenii CASCADE CONSTRAINTS;
DROP TABLE Evenimente CASCADE CONSTRAINTS;
DROP TABLE Amintiri_Digitale CASCADE CONSTRAINTS;
DROP TABLE UtilizatorRoluri CASCADE CONSTRAINTS;
DROP TABLE Roluri CASCADE CONSTRAINTS;
DROP TABLE Utilizatori CASCADE CONSTRAINTS;

DROP SEQUENCE seq_utilizator_id;
DROP SEQUENCE seq_rol_id;
DROP SEQUENCE seq_amintire_id;
DROP SEQUENCE seq_eveniment_id;
DROP SEQUENCE seq_prietenie_id;
DROP SEQUENCE seq_eticheta_id;
DROP SEQUENCE seq_comentariu_id;
DROP SEQUENCE seq_partajare_id;
DROP SEQUENCE seq_notificare_id;
DROP SEQUENCE seq_setare_id;
DROP SEQUENCE seq_istoric_id;
*/

-- Secvențe pentru generarea ID-urilor
CREATE SEQUENCE seq_utilizator_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_rol_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_amintire_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_eveniment_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_prietenie_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_eticheta_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_comentariu_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_partajare_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_notificare_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_setare_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_istoric_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- Tabela Utilizatori
CREATE TABLE Utilizatori (
    utilizator_id NUMBER(10) DEFAULT seq_utilizator_id.NEXTVAL NOT NULL,
    nume_utilizator VARCHAR2(50 CHAR) NOT NULL,
    parola VARCHAR2(255 CHAR) NOT NULL, -- Stochează hash-ul parolei
    email VARCHAR2(100 CHAR) NOT NULL,
    nume VARCHAR2(50 CHAR),
    prenume VARCHAR2(50 CHAR),
    data_inregistrare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    data_nasterii DATE,
    numar_telefon VARCHAR2(20 CHAR),
    adresa VARCHAR2(255 CHAR),
    oras VARCHAR2(100 CHAR),
    tara VARCHAR2(100 CHAR),
    cod_postal VARCHAR2(20 CHAR),
    profil_public CHAR(1) DEFAULT 'N' NOT NULL, -- 'Y' pentru public, 'N' pentru privat
    activ CHAR(1) DEFAULT 'Y' NOT NULL, -- 'Y' pentru activ, 'N' pentru inactiv/blocat
    poza_profil_cale VARCHAR2(1000 CHAR),
    data_ultima_logare TIMESTAMP,
    CONSTRAINT pk_utilizatori PRIMARY KEY (utilizator_id),
    CONSTRAINT uk_utilizatori_nume_utilizator UNIQUE (nume_utilizator),
    CONSTRAINT uk_utilizatori_email UNIQUE (email),
    CONSTRAINT ck_utilizatori_profil_public CHECK (profil_public IN ('Y', 'N')),
    CONSTRAINT ck_utilizatori_activ CHECK (activ IN ('Y', 'N'))
);

-- Tabela Roluri
CREATE TABLE Roluri (
    rol_id NUMBER(10) DEFAULT seq_rol_id.NEXTVAL NOT NULL,
    nume_rol VARCHAR2(50 CHAR) NOT NULL, -- ex: ROLE_USER, ROLE_ADMIN
    descriere_rol VARCHAR2(255 CHAR),
    CONSTRAINT pk_roluri PRIMARY KEY (rol_id),
    CONSTRAINT uk_roluri_nume_rol UNIQUE (nume_rol)
);

-- Tabela de legătură UtilizatorRoluri
CREATE TABLE UtilizatorRoluri (
    utilizator_id NUMBER(10) NOT NULL,
    rol_id NUMBER(10) NOT NULL,
    CONSTRAINT pk_utilizator_roluri PRIMARY KEY (utilizator_id, rol_id),
    CONSTRAINT fk_utilizatorroluri_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT fk_utilizatorroluri_roluri FOREIGN KEY (rol_id) REFERENCES Roluri(rol_id) ON DELETE CASCADE
);

-- Tabela Amintiri_Digitale
CREATE TABLE Amintiri_Digitale (
    amintire_id NUMBER(10) DEFAULT seq_amintire_id.NEXTVAL NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    titlu VARCHAR2(255 CHAR),
    descriere CLOB,
    nume_fisier VARCHAR2(255 CHAR) NOT NULL,
    tip_media VARCHAR2(50 CHAR), -- ex: imagine, video, document, audio
    cale_stocare VARCHAR2(1000 CHAR) NOT NULL, -- Poate fi o cale în sistemul de fișiere sau un URL
    dimensiune_fisier NUMBER, -- în bytes
    metadata CLOB, -- JSON sau XML pentru metadate specifice (ex: EXIF pentru imagini)
    data_creare_amintire DATE, -- Data la care a fost creata amintirea (ex: data unei poze)
    data_incarcare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    vizibilitate VARCHAR2(20 CHAR) DEFAULT 'PRIVAT' NOT NULL, -- PRIVAT, PRIETENI, PUBLIC
    locatie_geografica VARCHAR2(255 CHAR), -- ex: Lat, Long sau nume locatie
    CONSTRAINT pk_amintiri PRIMARY KEY (amintire_id),
    CONSTRAINT fk_amintiri_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT ck_amintiri_vizibilitate CHECK (vizibilitate IN ('PRIVAT', 'PRIETENI', 'PUBLIC'))
);

-- Tabela Evenimente
CREATE TABLE Evenimente (
    eveniment_id NUMBER(10) DEFAULT seq_eveniment_id.NEXTVAL NOT NULL,
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

-- =============================================================================
-- TABELA DOCUMENTE (LEGĂTURĂ 1:N CU EVENIMENTE)
-- =============================================================================

CREATE SEQUENCE seq_document_id START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

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

CREATE INDEX idx_documente_eveniment_id ON Documente(eveniment_id);

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
    prietenie_id NUMBER(10) DEFAULT seq_prietenie_id.NEXTVAL NOT NULL,
    utilizator1_id NUMBER(10) NOT NULL, -- Cel care trimite cererea
    utilizator2_id NUMBER(10) NOT NULL, -- Cel care primește cererea
    stare_prietenie VARCHAR2(20 CHAR) DEFAULT 'IN_ASTEPTARE' NOT NULL, -- IN_ASTEPTARE, ACCEPTATA, RESPINSA, BLOCATA
    data_solicitare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    data_raspuns TIMESTAMP,
    CONSTRAINT pk_prietenii PRIMARY KEY (prietenie_id),
    CONSTRAINT fk_prietenii_user1 FOREIGN KEY (utilizator1_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT fk_prietenii_user2 FOREIGN KEY (utilizator2_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT uk_prietenie_pereche UNIQUE (utilizator1_id, utilizator2_id), -- Asigură că nu există duplicate (A->B) și (A->B)
    CONSTRAINT ck_prietenii_stare CHECK (stare_prietenie IN ('IN_ASTEPTARE', 'ACCEPTATA', 'RESPINSA', 'BLOCATA')),
    CONSTRAINT ck_prietenii_no_self_friendship CHECK (utilizator1_id <> utilizator2_id) -- Previne auto-prietenia
);

-- Tabela Etichete (Tags)
CREATE TABLE Etichete (
    eticheta_id NUMBER(10) DEFAULT seq_eticheta_id.NEXTVAL NOT NULL,
    nume_eticheta VARCHAR2(50 CHAR) NOT NULL,
    utilizator_id NUMBER(10), -- NULL dacă eticheta e globală, sau ID utilizator dacă e specifică utilizatorului
    CONSTRAINT pk_etichete PRIMARY KEY (eticheta_id),
    CONSTRAINT uk_etichete_nume UNIQUE (nume_eticheta, utilizator_id), -- O etichetă e unică per utilizator sau global
    CONSTRAINT fk_etichete_utilizator FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE
);

-- Tabela de legătură AmintiriEtichete
CREATE TABLE AmintiriEtichete (
    amintire_id NUMBER(10) NOT NULL,
    eticheta_id NUMBER(10) NOT NULL,
    CONSTRAINT pk_amintiri_etichete PRIMARY KEY (amintire_id, eticheta_id),
    CONSTRAINT fk_amintirietichete_amintiri FOREIGN KEY (amintire_id) REFERENCES Amintiri_Digitale(amintire_id) ON DELETE CASCADE,
    CONSTRAINT fk_amintirietichete_etichete FOREIGN KEY (eticheta_id) REFERENCES Etichete(eticheta_id) ON DELETE CASCADE
);

-- Tabela Comentarii (pentru Amintiri sau Evenimente)
CREATE TABLE Comentarii (
    comentariu_id NUMBER(10) DEFAULT seq_comentariu_id.NEXTVAL NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    amintire_id NUMBER(10), -- Poate fi NULL dacă comentariul e la un eveniment
    eveniment_id NUMBER(10), -- Poate fi NULL dacă comentariul e la o amintire
    text_comentariu CLOB NOT NULL,
    data_comentariu TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    comentariu_parinte_id NUMBER(10), -- Pentru răspunsuri la comentarii
    CONSTRAINT pk_comentarii PRIMARY KEY (comentariu_id),
    CONSTRAINT fk_comentarii_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT fk_comentarii_amintiri FOREIGN KEY (amintire_id) REFERENCES Amintiri_Digitale(amintire_id) ON DELETE CASCADE,
    CONSTRAINT fk_comentarii_evenimente FOREIGN KEY (eveniment_id) REFERENCES Evenimente(eveniment_id) ON DELETE CASCADE,
    CONSTRAINT fk_comentarii_parinte FOREIGN KEY (comentariu_parinte_id) REFERENCES Comentarii(comentariu_id) ON DELETE CASCADE,
    CONSTRAINT ck_comentariu_target CHECK ((amintire_id IS NOT NULL AND eveniment_id IS NULL) OR (amintire_id IS NULL AND eveniment_id IS NOT NULL))
);

-- Tabela Partajari (pentru Amintiri sau Evenimente)
CREATE TABLE Partajari (
    partajare_id NUMBER(10) DEFAULT seq_partajare_id.NEXTVAL NOT NULL,
    element_id NUMBER(10) NOT NULL, -- ID-ul amintirii sau evenimentului
    tip_element VARCHAR2(20 CHAR) NOT NULL, -- 'AMINTIRE' sau 'EVENIMENT'
    utilizator_partajator_id NUMBER(10) NOT NULL, -- Cine a partajat
    utilizator_destinatar_id NUMBER(10), -- Cu cine s-a partajat (dacă e un singur utilizator)
    -- grup_destinatar_id NUMBER(10), -- (Opțional) Dacă se partajează cu un grup predefinit
    permisiuni VARCHAR2(50 CHAR), -- ex: 'VIZUALIZARE', 'EDITARE'
    data_partajare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_partajari PRIMARY KEY (partajare_id),
    CONSTRAINT fk_partajari_partajator FOREIGN KEY (utilizator_partajator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT fk_partajari_destinatar FOREIGN KEY (utilizator_destinatar_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT ck_partajari_tip_element CHECK (tip_element IN ('AMINTIRE', 'EVENIMENT'))
);

-- Tabela Notificari
CREATE TABLE Notificari (
    notificare_id NUMBER(10) DEFAULT seq_notificare_id.NEXTVAL NOT NULL,
    utilizator_id NUMBER(10) NOT NULL, -- Utilizatorul care primește notificarea
    tip_notificare VARCHAR2(50 CHAR) NOT NULL, -- ex: CERERE_PRIETENIE, COMENTARIU_NOU, EVENIMENT_VIITOR
    mesaj CLOB NOT NULL,
    link_relevant VARCHAR2(1000 CHAR), -- URL către resursa relevantă (ex: profil, amintire)
    citita CHAR(1) DEFAULT 'N' NOT NULL, -- 'Y' sau 'N'
    data_creare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_notificari PRIMARY KEY (notificare_id),
    CONSTRAINT fk_notificari_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT ck_notificari_citita CHECK (citita IN ('Y', 'N'))
);

-- Tabela SetariUtilizator
CREATE TABLE SetariUtilizator (
    setare_id NUMBER(10) DEFAULT seq_setare_id.NEXTVAL NOT NULL,
    utilizator_id NUMBER(10) NOT NULL,
    cheie_setare VARCHAR2(100 CHAR) NOT NULL,
    valoare_setare VARCHAR2(1000 CHAR) NOT NULL,
    data_ultima_modificare TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_setari_utilizator PRIMARY KEY (setare_id),
    CONSTRAINT fk_setari_utilizator_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE CASCADE,
    CONSTRAINT uk_setari_utilizator_cheie UNIQUE (utilizator_id, cheie_setare)
);

-- Tabela IstoricActivitati (Log)
CREATE TABLE IstoricActivitati (
    istoric_id NUMBER(10) DEFAULT seq_istoric_id.NEXTVAL NOT NULL,
    utilizator_id NUMBER(10),
    tip_actiune VARCHAR2(100 CHAR) NOT NULL, -- ex: LOGARE, DECONECTARE, CREARE_AMINTIRE, MODIFICARE_PROFIL
    detalii_actiune CLOB,
    adresa_ip VARCHAR2(45 CHAR),
    user_agent VARCHAR2(500 CHAR),
    data_actiune TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_istoric_activitati PRIMARY KEY (istoric_id),
    CONSTRAINT fk_istoric_utilizatori FOREIGN KEY (utilizator_id) REFERENCES Utilizatori(utilizator_id) ON DELETE SET NULL -- Păstrăm logul chiar dacă userul e șters
);

-- Inserare roluri de bază
INSERT INTO Roluri (rol_id, nume_rol, descriere_rol) VALUES (seq_rol_id.NEXTVAL, 'ROLE_USER', 'Utilizator standard al aplicației');
INSERT INTO Roluri (rol_id, nume_rol, descriere_rol) VALUES (seq_rol_id.NEXTVAL, 'ROLE_ADMIN', 'Administrator al aplicației');

COMMIT;

-- Indexuri pentru performanță (exemple, adaugă mai multe după nevoie)
CREATE INDEX idx_prietenii_user2_stare ON Prietenii(utilizator2_id, stare_prietenie);
CREATE INDEX idx_amintiri_data_creare ON Amintiri_Digitale(data_creare_amintire DESC);
CREATE INDEX idx_evenimente_data_inceput ON Evenimente(data_inceput DESC);
CREATE INDEX idx_notificari_user_citita ON Notificari(utilizator_id, citita, data_creare DESC);


-- Sfârșitul scriptului