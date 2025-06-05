-- =============================================================================
-- POPULARE BAZA DE DATE - Arhiva Digitala Personala
-- =============================================================================

-- Pentru a evita problemele cu caractere speciale în SQL Developer
SET DEFINE OFF;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Inceput script populare...');
END;
/

-- =============================================================================
-- CURATARE TABELE (recomandat pentru rulari repetate)
-- =============================================================================
-- Asigurati-va ca ordinea respecta constrangerile de chei straine (copii inaintea parintilor)
DELETE FROM Notificari;
DELETE FROM Legaturi_Eveniment_Amintire;
DELETE FROM Preferinte_Timeline; -- Stergem si din aceasta tabela definita in schema
DELETE FROM Prietenii;
DELETE FROM Amintiri_Digitale;
DELETE FROM Evenimente;
DELETE FROM Timeline; -- Stergem si din aceasta tabela inainte de Utilizatori
DELETE FROM Utilizatori;
COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Tabele curatate (daca existau date).');
END;
/

-- =============================================================================
-- UTILIZATORI
-- parola_hash este un placeholder, într-o aplicație reală ar fi un hash securizat
-- =============================================================================
INSERT INTO Utilizatori (utilizator_id, nume_utilizator, parola_hash, email, nume_complet, data_inregistrare, data_ultima_logare, este_activ, rol)
VALUES (seq_utilizator_id.NEXTVAL, 'anapopescu', 'hash_ana123', 'ana.popescu@email.com', 'Popescu Ana', TO_TIMESTAMP('2023-01-15 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), SYSTIMESTAMP, 1, 'USER');

INSERT INTO Utilizatori (utilizator_id, nume_utilizator, parola_hash, email, nume_complet, data_inregistrare, data_ultima_logare, este_activ, rol)
VALUES (seq_utilizator_id.NEXTVAL, 'bogdanionescu', 'hash_bogdan456', 'bogdan.ionescu@email.com', 'Ionescu Bogdan', TO_TIMESTAMP('2023-02-20 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), SYSTIMESTAMP, 1, 'USER');

INSERT INTO Utilizatori (utilizator_id, nume_utilizator, parola_hash, email, nume_complet, data_inregistrare, data_ultima_logare, este_activ, rol)
VALUES (seq_utilizator_id.NEXTVAL, 'corinav', 'hash_corina789', 'corina.v@email.com', 'Vasilescu Corina', TO_TIMESTAMP('2023-03-10 09:15:00', 'YYYY-MM-DD HH24:MI:SS'), SYSTIMESTAMP, 1, 'USER');

COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Utilizatori inserati.');
END;
/

-- =============================================================================
-- EVENIMENTE
-- =============================================================================
INSERT INTO Evenimente (eveniment_id, utilizator_id, titlu, descriere, data_inceput, data_sfarsit, locatie, tip_eveniment, vizibilitate, data_creare, data_ultima_modificare)
VALUES (seq_eveniment_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'), 
    'Vacanță la Roma', 
    'O săptămână de neuitat explorând capitala Italiei.', 
    TO_TIMESTAMP('2023-06-10 08:00:00', 'YYYY-MM-DD HH24:MI:SS'), 
    TO_TIMESTAMP('2023-06-17 20:00:00', 'YYYY-MM-DD HH24:MI:SS'), 
    'Roma, Italia', 
    'VACANTA', 
    'PRIVAT', 
    SYSTIMESTAMP, 
    SYSTIMESTAMP
);

INSERT INTO Evenimente (eveniment_id, utilizator_id, titlu, descriere, data_inceput, data_sfarsit, locatie, tip_eveniment, vizibilitate, data_creare, data_ultima_modificare)
VALUES (seq_eveniment_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'bogdanionescu'), 
    'Conferință TechZone 2023', 
    'Participare la conferința anuală de tehnologie.', 
    TO_TIMESTAMP('2023-09-05 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), 
    TO_TIMESTAMP('2023-09-07 18:00:00', 'YYYY-MM-DD HH24:MI:SS'), 
    'Centrul de Conferințe, București', 
    'PROFESIONAL', 
    'PUBLIC', 
    SYSTIMESTAMP, 
    SYSTIMESTAMP
);

INSERT INTO Evenimente (eveniment_id, utilizator_id, titlu, descriere, data_inceput, data_sfarsit, locatie, tip_eveniment, vizibilitate, data_creare, data_ultima_modificare)
VALUES (seq_eveniment_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'), 
    'Zi de naștere Maria', 
    'Petrecere surpriză pentru Maria.', 
    TO_TIMESTAMP('2023-07-22 19:00:00', 'YYYY-MM-DD HH24:MI:SS'), 
    TO_TIMESTAMP('2023-07-22 23:59:00', 'YYYY-MM-DD HH24:MI:SS'), 
    'Acasă', 
    'PERSONAL', 
    'PRIETENI', 
    SYSTIMESTAMP, 
    SYSTIMESTAMP
);

INSERT INTO Evenimente (eveniment_id, utilizator_id, titlu, descriere, data_inceput, data_sfarsit, locatie, tip_eveniment, vizibilitate, data_creare, data_ultima_modificare)
VALUES (seq_eveniment_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav'), 
    'Absolvire Master', 
    'Ceremonia de absolvire a masterului în Marketing Digital.', 
    TO_TIMESTAMP('2023-07-15 11:00:00', 'YYYY-MM-DD HH24:MI:SS'), 
    TO_TIMESTAMP('2023-07-15 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), 
    'Aula Magna, Universitatea X', 
    'EDUCATIONAL', 
    'PUBLIC', 
    SYSTIMESTAMP, 
    SYSTIMESTAMP
);

COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Evenimente inserate.');
END;
/

-- =============================================================================
-- AMINTIRI_DIGITALE (ARTEFACTE)
-- =============================================================================
INSERT INTO Amintiri_Digitale (amintire_id, utilizator_id, nume_fisier, tip_media, CALE_STOCARE, METADATA, DESCRIERE_AMINTIRE, DATA_INCARCARE, data_ultima_modificare)
VALUES (seq_amintire_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'),
    'colosseum.jpg',
    'IMAGINE',
    '/path/to/images/anapopescu/colosseum.jpg',
    '{"rezolutie": "4032x3024", "camera": "iPhone 12"}',
    'Fotografie cu Colosseumul din Roma, iunie 2023.',
    TO_TIMESTAMP('2023-06-12 14:30:00', 'YYYY-MM-DD HH24:MI:SS'),
    TO_TIMESTAMP('2023-06-12 14:30:00', 'YYYY-MM-DD HH24:MI:SS')
);

INSERT INTO Amintiri_Digitale (amintire_id, utilizator_id, nume_fisier, tip_media, CALE_STOCARE, METADATA, DESCRIERE_AMINTIRE, DATA_INCARCARE, data_ultima_modificare)
VALUES (seq_amintire_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'bogdanionescu'),
    'prezentare_ai.pdf',
    'DOCUMENT',
    '/path/to/documents/bogdanionescu/prezentare_ai.pdf',
    '{"autor": "Bogdan Ionescu", "pagini": 25}',
    'Slide-urile prezentării despre Inteligența Artificială susținută la TechZone.',
    TO_TIMESTAMP('2023-09-06 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    TO_TIMESTAMP('2023-09-06 10:00:00', 'YYYY-MM-DD HH24:MI:SS')
);

INSERT INTO Amintiri_Digitale (amintire_id, utilizator_id, nume_fisier, tip_media, CALE_STOCARE, METADATA, DESCRIERE_AMINTIRE, DATA_INCARCARE, data_ultima_modificare)
VALUES (seq_amintire_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'),
    'maria_bday.mp4',
    'VIDEO',
    '/path/to/videos/anapopescu/maria_bday.mp4',
    '{"durata": "00:05:30", "format": "MP4"}',
    'Clip video de la ziua Mariei.',
    TO_TIMESTAMP('2023-07-23 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    TO_TIMESTAMP('2023-07-23 10:00:00', 'YYYY-MM-DD HH24:MI:SS')
);

INSERT INTO Amintiri_Digitale (amintire_id, utilizator_id, nume_fisier, tip_media, CALE_STOCARE, METADATA, DESCRIERE_AMINTIRE, DATA_INCARCARE, data_ultima_modificare)
VALUES (seq_amintire_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav'),
    'diploma_master_corina.jpg',
    'IMAGINE',
    '/path/to/images/corinav/diploma_master_corina.jpg',
    '{"tip_document": "Diploma", "emitent": "Universitatea X"}',
    'Fotografie cu diploma de master.',
    TO_TIMESTAMP('2023-07-16 12:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    TO_TIMESTAMP('2023-07-16 12:00:00', 'YYYY-MM-DD HH24:MI:SS')
);

INSERT INTO Amintiri_Digitale (amintire_id, utilizator_id, nume_fisier, tip_media, CALE_STOCARE, METADATA, DESCRIERE_AMINTIRE, DATA_INCARCARE, data_ultima_modificare)
VALUES (seq_amintire_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'),
    'roma2023.docx',
    'DOCUMENT',
    '/path/to/documents/anapopescu/roma2023.docx',
    '{"cuvinte": 1500}',
    'Notițe și impresii din călătoria la Roma.',
    TO_TIMESTAMP('2023-06-18 18:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    TO_TIMESTAMP('2023-06-18 18:00:00', 'YYYY-MM-DD HH24:MI:SS')
);

COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Amintiri Digitale inserate.');
END;
/

-- =============================================================================
-- LEGATURI_EVENIMENT_AMINTIRE
-- =============================================================================
INSERT INTO Legaturi_Eveniment_Amintire (eveniment_id, amintire_id)
VALUES (
    (SELECT eveniment_id FROM Evenimente WHERE titlu = 'Vacanță la Roma' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu')),
    (SELECT amintire_id FROM Amintiri_Digitale WHERE nume_fisier = 'colosseum.jpg' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'))
);

INSERT INTO Legaturi_Eveniment_Amintire (eveniment_id, amintire_id)
VALUES (
    (SELECT eveniment_id FROM Evenimente WHERE titlu = 'Conferință TechZone 2023' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'bogdanionescu')),
    (SELECT amintire_id FROM Amintiri_Digitale WHERE nume_fisier = 'prezentare_ai.pdf' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'bogdanionescu'))
);

INSERT INTO Legaturi_Eveniment_Amintire (eveniment_id, amintire_id)
VALUES (
    (SELECT eveniment_id FROM Evenimente WHERE titlu = 'Zi de naștere Maria' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu')),
    (SELECT amintire_id FROM Amintiri_Digitale WHERE nume_fisier = 'maria_bday.mp4' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'))
);

INSERT INTO Legaturi_Eveniment_Amintire (eveniment_id, amintire_id)
VALUES (
    (SELECT eveniment_id FROM Evenimente WHERE titlu = 'Absolvire Master' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav')),
    (SELECT amintire_id FROM Amintiri_Digitale WHERE nume_fisier = 'diploma_master_corina.jpg' AND utilizator_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav'))
);

COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Legaturi Eveniment-Amintire inserate.');
END;
/

-- =============================================================================
-- PRIETENII
-- =============================================================================
INSERT INTO Prietenii (prietenie_id, utilizator1_id, utilizator2_id, stare_prietenie, data_solicitare, data_raspuns)
VALUES (seq_prietenie_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'),
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'bogdanionescu'),
    'ACCEPTATA',
    TO_TIMESTAMP('2023-03-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
    TO_TIMESTAMP('2023-03-01 12:00:00', 'YYYY-MM-DD HH24:MI:SS')
);

INSERT INTO Prietenii (prietenie_id, utilizator1_id, utilizator2_id, stare_prietenie, data_solicitare, data_raspuns)
VALUES (seq_prietenie_id.NEXTVAL, 
    LEAST((SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav'), (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu')),
    GREATEST((SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav'), (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu')),
    'IN_ASTEPTARE',
    TO_TIMESTAMP('2023-04-05 15:30:00', 'YYYY-MM-DD HH24:MI:SS'),
    NULL
);

COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Prietenii inserate.');
END;
/

-- =============================================================================
-- TIMELINE
-- =============================================================================
INSERT INTO Timeline (timeline_id, utilizator_id, nume_timeline, descriere_timeline)
VALUES (seq_timeline_id.NEXTVAL, (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'), 'Timeline Principal Ana', 'Timeline-ul principal pentru Ana Popescu.');
INSERT INTO Timeline (timeline_id, utilizator_id, nume_timeline, descriere_timeline)
VALUES (seq_timeline_id.NEXTVAL, (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'bogdanionescu'), 'Timeline Principal Bogdan', 'Timeline-ul principal pentru Bogdan Ionescu.');
INSERT INTO Timeline (timeline_id, utilizator_id, nume_timeline, descriere_timeline)
VALUES (seq_timeline_id.NEXTVAL, (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav'), 'Timeline Principal Corina', 'Timeline-ul principal pentru Corina Vasilescu.');
COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Timeline-uri inserate.');
END;
/

-- =============================================================================
-- NOTIFICARI
-- =============================================================================
INSERT INTO Notificari (notificare_id, utilizator_id, tip_notificare, mesaj, data_creare, este_citita, referinta_id, referinta_tip)
VALUES (seq_notificare_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu'),
    'PRIETENIE_NOUA',
    'Bogdan Ionescu ți-a acceptat cererea de prietenie.',
    TO_TIMESTAMP('2023-03-01 12:05:00', 'YYYY-MM-DD HH24:MI:SS'),
    0, -- 0 pentru false (necitită)
    (SELECT prietenie_id FROM Prietenii WHERE utilizator1_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'anapopescu') AND utilizator2_id = (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'bogdanionescu')),
    'PRIETENIE'
);

INSERT INTO Notificari (notificare_id, utilizator_id, tip_notificare, mesaj, data_creare, este_citita, referinta_id, referinta_tip)
VALUES (seq_notificare_id.NEXTVAL, 
    (SELECT utilizator_id FROM Utilizatori WHERE nume_utilizator = 'corinav'),
    'COMENTARIU_NOU',
    'Ana Popescu a comentat la evenimentul tău "Absolvire Master".',
    SYSTIMESTAMP,
    1, -- 1 pentru true (citită)
    (SELECT eveniment_id FROM Evenimente e JOIN Utilizatori u ON e.utilizator_id = u.utilizator_id WHERE e.titlu = 'Absolvire Master' AND u.nume_utilizator = 'corinav'),
    'EVENIMENT'
);

COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Notificari inserate.');
END;
/


COMMIT;

BEGIN
  DBMS_OUTPUT.PUT_LINE('Script populare finalizat cu succes!');
END;
/