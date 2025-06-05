-- Trigger pentru Utilizatori
CREATE OR REPLACE TRIGGER trg_utilizatori_pk
BEFORE INSERT ON Utilizatori
FOR EACH ROW
BEGIN
  IF :NEW.utilizator_id IS NULL THEN
    SELECT seq_utilizator_id.NEXTVAL INTO :NEW.utilizator_id FROM DUAL;
  END IF;
END;
/

-- Trigger pentru Evenimente - PK
CREATE OR REPLACE TRIGGER trg_evenimente_pk
BEFORE INSERT ON Evenimente
FOR EACH ROW
BEGIN
  IF :NEW.eveniment_id IS NULL THEN
    SELECT seq_eveniment_id.NEXTVAL INTO :NEW.eveniment_id FROM DUAL;
  END IF;
END;
/

-- Trigger pentru Evenimente - Actualizare Timestamp
CREATE OR REPLACE TRIGGER TRG_Actualizeaza_Timestamp_Ev
BEFORE UPDATE ON Evenimente
FOR EACH ROW
BEGIN
  :NEW.data_ultima_modificare := SYSTIMESTAMP;
END;
/

-- Trigger pentru Amintiri_Digitale
CREATE OR REPLACE TRIGGER trg_amintiri_digitale_pk
BEFORE INSERT ON Amintiri_Digitale
FOR EACH ROW
BEGIN
  IF :NEW.amintire_id IS NULL THEN
    SELECT seq_amintire_id.NEXTVAL INTO :NEW.amintire_id FROM DUAL;
  END IF;
END;
/

-- Trigger pentru Amintiri_Digitale - Actualizare Timestamp
CREATE OR REPLACE TRIGGER TRG_UPD_TS_AMINTIRI
BEFORE UPDATE ON Amintiri_Digitale
FOR EACH ROW
BEGIN
  :NEW.data_ultima_modificare := SYSTIMESTAMP;
END;
/

-- Trigger pentru Timeline - PK
CREATE OR REPLACE TRIGGER trg_timeline_pk
BEFORE INSERT ON Timeline
FOR EACH ROW
BEGIN
  IF :NEW.timeline_id IS NULL THEN
    SELECT seq_timeline_id.NEXTVAL INTO :NEW.timeline_id FROM DUAL;
  END IF;
END;
/

-- Trigger pentru Prietenii
CREATE OR REPLACE TRIGGER trg_prietenii_pk
BEFORE INSERT ON Prietenii
FOR EACH ROW
BEGIN
  IF :NEW.prietenie_id IS NULL THEN
    SELECT seq_prietenie_id.NEXTVAL INTO :NEW.prietenie_id FROM DUAL;
  END IF;
END;
/

-- Trigger pentru Notificari
CREATE OR REPLACE TRIGGER trg_notificari_pk
BEFORE INSERT ON Notificari
FOR EACH ROW
BEGIN
  IF :NEW.notificare_id IS NULL THEN
    SELECT seq_notificare_id.NEXTVAL INTO :NEW.notificare_id FROM DUAL;
  END IF;
END;
/

-- Trigger pentru Preferinte_Timeline - PK
CREATE OR REPLACE TRIGGER trg_preferinte_timeline_pk
BEFORE INSERT ON Preferinte_Timeline
FOR EACH ROW
BEGIN
  IF :NEW.preferinta_id IS NULL THEN
    SELECT seq_preferinta_timeline_id.NEXTVAL INTO :NEW.preferinta_id FROM DUAL;
  END IF;
END;
/

-- Trigger pentru Utilizatori - Inserare preferințe default la creare utilizator
CREATE OR REPLACE TRIGGER TRG_Utilizatori_After_Insert
AFTER INSERT ON Utilizatori
FOR EACH ROW
BEGIN
  INSERT INTO Preferinte_Timeline (utilizator_id, tip_vizibilitate)
  VALUES (:NEW.utilizator_id, 'PRIVAT');
EXCEPTION
  WHEN OTHERS THEN
    -- Log error or handle, e.g., using DBMS_OUTPUT for now
    DBMS_OUTPUT.PUT_LINE('Eroare in TRG_Utilizatori_After_Insert pentru utilizator_id ' || :NEW.utilizator_id || ': ' || SQLERRM);
END;
/

BEGIN
  DBMS_OUTPUT.PUT_LINE('Scriptul triggers.sql (PK auto-increment) a fost executat cu succes.');
END;
/
