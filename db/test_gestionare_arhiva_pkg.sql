-- Script pentru testarea pachetului gestionare_arhiva_pkg
SET SERVEROUTPUT ON;

-- =============================================================================
-- Test a: Verificare functie numar_amintiri_utilizator
-- =============================================================================
DECLARE
  v_count NUMBER;
  v_nume_utilizator VARCHAR2(100);
BEGIN
  DBMS_OUTPUT.PUT_LINE('--- Inceput Test a: numar_amintiri_utilizator ---');
  v_nume_utilizator := 'anapopescu'; -- Utilizator existent
  v_count := gestionare_arhiva_pkg.numar_amintiri_utilizator(v_nume_utilizator);
  DBMS_OUTPUT.PUT_LINE('Utilizatorul ' || v_nume_utilizator || ' are ' || v_count || ' amintiri (inainte de testul b).');

  v_nume_utilizator := 'utilizator_inexistent'; -- Utilizator inexistent
  v_count := gestionare_arhiva_pkg.numar_amintiri_utilizator(v_nume_utilizator);
  DBMS_OUTPUT.PUT_LINE('Utilizatorul ' || v_nume_utilizator || ' are ' || v_count || ' amintiri.');
  DBMS_OUTPUT.PUT_LINE('--- Sfarsit Test a ---');
  DBMS_OUTPUT.PUT_LINE('');
END;
/

-- =============================================================================
-- Test b: Verificare procedura adauga_amintire (caz de succes)
-- =============================================================================
BEGIN
  DBMS_OUTPUT.PUT_LINE('--- Inceput Test b: adauga_amintire (succes) ---');
  gestionare_arhiva_pkg.adauga_amintire(
    p_nume_utilizator    => 'anapopescu',
    p_nume_fisier        => 'noua_amintire.jpg',
    p_tip_media          => 'IMAGINE',
    p_cale_stocare       => '/path/to/images/anapopescu/noua_amintire.jpg',
    p_metadata           => '{"rezolutie": "1920x1080", "test_case": "b"}',
    p_descriere_amintire => 'O amintire noua adaugata prin pachet (test b).'
  );
  COMMIT; 
  DBMS_OUTPUT.PUT_LINE('Test b: Amintire adaugata si commit efectuat.');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Eroare la Test b (adauga_amintire succes): ' || SQLERRM);
    ROLLBACK;
END;
/

-- Verificare dupa Test b
DECLARE
  v_count NUMBER;
BEGIN
  v_count := gestionare_arhiva_pkg.numar_amintiri_utilizator('anapopescu');
  DBMS_OUTPUT.PUT_LINE('Dupa Test b: Utilizatorul anapopescu are ' || v_count || ' amintiri.');
  DBMS_OUTPUT.PUT_LINE('--- Sfarsit Test b ---');
  DBMS_OUTPUT.PUT_LINE('');
END;
/

-- =============================================================================
-- Test c: Verificare procedura adauga_amintire (utilizator inexistent)
-- =============================================================================
BEGIN
  DBMS_OUTPUT.PUT_LINE('--- Inceput Test c: adauga_amintire (user inexistent) ---');
  gestionare_arhiva_pkg.adauga_amintire(
    p_nume_utilizator    => 'utilizator_inexistent_test',
    p_nume_fisier        => 'test_fail_user.jpg',
    p_tip_media          => 'IMAGINE',
    p_cale_stocare       => '/path/to/images/fail/test_fail_user.jpg',
    p_metadata           => '{"test_case": "c"}'
  );
  COMMIT; -- Nu ar trebui sa ajunga aici
  DBMS_OUTPUT.PUT_LINE('Test c: Commit neasteptat.');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Eroare asteptata la Test c (user inexistent): ' || SQLERRM);
    ROLLBACK;
END;
/
BEGIN
  DBMS_OUTPUT.PUT_LINE('--- Sfarsit Test c ---');
  DBMS_OUTPUT.PUT_LINE('');
END;
/


-- =============================================================================
-- Test d: Verificare procedura adauga_amintire (fisier duplicat pentru utilizator)
-- =============================================================================
BEGIN
  DBMS_OUTPUT.PUT_LINE('--- Inceput Test d: adauga_amintire (fisier duplicat) ---');
  -- Incercare de a adauga acelasi fisier "noua_amintire.jpg" pentru 'anapopescu'
  -- Acest fisier a fost adaugat in Test b
  gestionare_arhiva_pkg.adauga_amintire(
    p_nume_utilizator    => 'anapopescu',
    p_nume_fisier        => 'noua_amintire.jpg', 
    p_tip_media          => 'IMAGINE',
    p_cale_stocare       => '/path/to/images/anapopescu/noua_amintire_duplicat.jpg',
    p_metadata           => '{"test_case": "d"}',
    p_descriere_amintire => 'Incercare de adaugare duplicat (test d).'
  );
  COMMIT; -- Nu ar trebui sa ajunga aici
  DBMS_OUTPUT.PUT_LINE('Test d: Commit neasteptat.');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Eroare asteptata la Test d (fisier duplicat): ' || SQLERRM);
    ROLLBACK;
END;
/
BEGIN
  DBMS_OUTPUT.PUT_LINE('--- Sfarsit Test d ---');
  DBMS_OUTPUT.PUT_LINE('');
  DBMS_OUTPUT.PUT_LINE('=== Toate testele au fost executate ===');
END;
/
