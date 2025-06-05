package com.arhiva_digitala.digital_archive_api.model;

public enum StarePrietenie {
    IN_ASTEPTARE, // Solicitare trimisă, așteaptă răspuns
    ACCEPTATA,    // Solicitare acceptată, sunt prieteni
    RESPINSA,     // Solicitare respinsă
    BLOCATA       // Relație blocată de unul dintre utilizatori (poate fi o direcție)
}
