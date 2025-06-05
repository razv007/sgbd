CREATE OR REPLACE PACKAGE gestionare_arhiva_pkg AS

  -- Custom Exceptions
  e_utilizator_inexistent EXCEPTION;
  PRAGMA EXCEPTION_INIT(e_utilizator_inexistent, -20001);
  e_fisier_duplicat EXCEPTION;
  PRAGMA EXCEPTION_INIT(e_fisier_duplicat, -20002);

  PROCEDURE adauga_amintire (
    p_nume_utilizator     IN VARCHAR2,
    p_nume_fisier         IN VARCHAR2,
    p_tip_media           IN Amintiri_Digitale.tip_media%TYPE,
    p_cale_stocare        IN Amintiri_Digitale.cale_stocare%TYPE,
    p_metadata            IN Amintiri_Digitale.metadata%TYPE DEFAULT NULL,
    p_descriere_amintire  IN Amintiri_Digitale.descriere_amintire%TYPE DEFAULT NULL
  );

  FUNCTION numar_amintiri_utilizator (
    p_nume_utilizator IN VARCHAR2
  ) RETURN NUMBER;

  PROCEDURE afiseaza_amintiri_utilizator (
    p_nume_utilizator IN VARCHAR2
  );

END gestionare_arhiva_pkg;
/

CREATE OR REPLACE PACKAGE BODY gestionare_arhiva_pkg AS

  PROCEDURE adauga_amintire (
    p_nume_utilizator     IN VARCHAR2,
    p_nume_fisier         IN VARCHAR2,
    p_tip_media           IN Amintiri_Digitale.tip_media%TYPE,
    p_cale_stocare        IN Amintiri_Digitale.cale_stocare%TYPE,
    p_metadata            IN Amintiri_Digitale.metadata%TYPE DEFAULT NULL,
    p_descriere_amintire  IN Amintiri_Digitale.descriere_amintire%TYPE DEFAULT NULL
  ) AS
    l_utilizator_id  Utilizatori.utilizator_id%TYPE;
    l_count          NUMBER;
  BEGIN
    -- Get utilizator_id from nume_utilizator
    BEGIN
      SELECT utilizator_id
      INTO l_utilizator_id
      FROM Utilizatori
      WHERE nume_utilizator = p_nume_utilizator;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20001, 'Utilizatorul specificat nu exista: ' || p_nume_utilizator);
    END;

    -- Check for duplicate nume_fisier for this user
    SELECT COUNT(*)
    INTO l_count
    FROM Amintiri_Digitale
    WHERE utilizator_id = l_utilizator_id AND nume_fisier = p_nume_fisier;

    IF l_count > 0 THEN
      RAISE_APPLICATION_ERROR(-20002, 'Fisierul "' || p_nume_fisier || '" exista deja pentru utilizatorul ' || p_nume_utilizator);
    END IF;

    -- Insert the new digital memory
    INSERT INTO Amintiri_Digitale (
      utilizator_id,
      nume_fisier,
      tip_media,
      CALE_STOCARE,
      METADATA,
      DESCRIERE_AMINTIRE,
      DATA_INCARCARE,
      data_ultima_modificare
    ) VALUES (
      l_utilizator_id,
      p_nume_fisier,
      p_tip_media,
      p_cale_stocare,
      p_metadata,
      p_descriere_amintire,
      SYSTIMESTAMP, -- DATA_INCARCARE
      SYSTIMESTAMP  -- data_ultima_modificare
    );
    DBMS_OUTPUT.PUT_LINE('Amintire digitala "' || p_nume_fisier || '" adaugata cu succes pentru utilizatorul ' || p_nume_utilizator || '.');

  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Eroare la adaugarea amintirii: ' || SQLERRM);
      RAISE; -- Re-raise the exception after logging
  END adauga_amintire;

  FUNCTION numar_amintiri_utilizator (
    p_nume_utilizator IN VARCHAR2
  ) RETURN NUMBER AS
    l_utilizator_id  Utilizatori.utilizator_id%TYPE;
    l_count          NUMBER := 0;
  BEGIN
    -- Try to get utilizator_id
    BEGIN
      SELECT utilizator_id
      INTO l_utilizator_id
      FROM Utilizatori
      WHERE nume_utilizator = p_nume_utilizator;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        -- User not found, return 0 memories
        RETURN 0;
    END;

    -- Count memories for the found user
    SELECT COUNT(*)
    INTO l_count
    FROM Amintiri_Digitale
    WHERE utilizator_id = l_utilizator_id;

    RETURN l_count;
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Eroare la numararea amintirilor: ' || SQLERRM);
      RETURN -1; -- Indicate an error
  END numar_amintiri_utilizator;

  PROCEDURE afiseaza_amintiri_utilizator (
    p_nume_utilizator IN VARCHAR2
  ) AS
    CURSOR c_amintiri IS
      SELECT nume_fisier, tip_media, descriere_amintire, data_incarcare
      FROM V_AMINTIRI_UTILIZATORI
      WHERE nume_utilizator = p_nume_utilizator
      ORDER BY data_incarcare DESC;
    
    v_amintire_rec c_amintiri%ROWTYPE;
    v_gasit_ceva BOOLEAN := FALSE;
  BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Amintirile pentru utilizatorul: ' || p_nume_utilizator || ' ---');
    
    OPEN c_amintiri;
    LOOP
      FETCH c_amintiri INTO v_amintire_rec;
      EXIT WHEN c_amintiri%NOTFOUND;
      v_gasit_ceva := TRUE;
      DBMS_OUTPUT.PUT_LINE(
        '  Fisier: ' || v_amintire_rec.nume_fisier || 
        ', Tip: ' || v_amintire_rec.tip_media || 
        ', Data: ' || TO_CHAR(v_amintire_rec.data_incarcare, 'YYYY-MM-DD HH24:MI:SS')
      );
      IF v_amintire_rec.descriere_amintire IS NOT NULL THEN
        DBMS_OUTPUT.PUT_LINE('    Descriere: ' || v_amintire_rec.descriere_amintire);
      END IF;
    END LOOP;
    CLOSE c_amintiri;

    IF NOT v_gasit_ceva THEN
      DBMS_OUTPUT.PUT_LINE('Nicio amintire gasita pentru utilizatorul ' || p_nume_utilizator || '.');
    END IF;
    DBMS_OUTPUT.PUT_LINE('------------------------------------------');
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Eroare in afiseaza_amintiri_utilizator: ' || SQLERRM);
      IF c_amintiri%ISOPEN THEN
        CLOSE c_amintiri;
      END IF;
      RAISE;
  END afiseaza_amintiri_utilizator;

END gestionare_arhiva_pkg;
/
