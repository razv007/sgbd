# Arhiva Digitală Personală Inteligentă cu Timeline Interactiv

## 1. Prezentare Generală a Proiectului

Acest proiect își propune să dezvolte o aplicație web complexă, denumită "Arhiva Digitală Personală Inteligentă cu Timeline Interactiv". Aplicația va permite utilizatorilor să își construiască și să gestioneze o arhivă personală de evenimente și artefacte digitale (documente, imagini, notițe, link-uri, fișiere audio/video) într-un format vizual de timeline interactiv.

Funcționalitățile cheie includ:
*   Crearea și managementul evenimentelor și artefactelor pe un timeline personal.
*   Upload de fișiere și gestionarea metadatelor asociate (folosind XMLTYPE sau JSON în Oracle).
*   Algoritmi PL/SQL pentru sugestii inteligente de conținut relevant (evenimente/artefacte similare sau corelate temporal).
*   Partajarea timeline-ului personal cu alți utilizatori, pe baza unor setări de confidențialitate (privat, public pentru utilizatori autentificați).
*   Un sistem social de prietenii, cu posibilitatea de a trimite/accepta/respinge cereri de prietenie.
*   Recomandări inteligente de prieteni bazate pe interese comune (deduse din categoriile evenimentelor) și prieteni comuni.
*   Interfață grafică web modernă și intuitivă.
*   Utilizarea avansată a capabilităților Oracle DB: PL/SQL, triggere, tipuri de date complexe, gestionarea excepțiilor, audit.

## 2. Tehnologii Utilizate (Technology Stack)

*   **Backend:** Java cu Spring Boot
*   **Frontend:** React cu TypeScript
*   **Bază de Date:** Oracle Database
*   **Dezvoltare Bază de Date & PL/SQL:** Oracle SQL Developer
*   **Management Dependințe Java (sugestie):** Maven sau Gradle
*   **Management Pachete Frontend (sugestie):** npm sau yarn
*   **Sistem de Control al Versiunilor:** Git

## 3. Structura Proiectului (Propunere Inițială)

```
proiect sgbd/ 
├── backend/                  # Codul sursă pentru Spring Boot (Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Pachetele Java (ex: com/example/digitaltimeline)
│   │   │   └── resources/  # Fișiere de configurare (application.properties, etc.)
│   └── pom.xml             # Sau build.gradle
├── frontend/                 # Codul sursă pentru React (TypeScript)
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── App.tsx
│   │   └── index.tsx
│   ├── package.json
│   └── tsconfig.json
├── db/                       # Scripturi SQL și PL/SQL
│   ├── schema.sql          # Definirea tabelelor, secvențelor, constrângerilor
│   ├── plsql_packages.sql  # Pachetele PL/SQL (specificații și corpuri)
│   ├── triggers.sql        # Triggere
│   └── sample_data.sql     # (Opțional) Script pentru inserarea datelor de test
└── README.md                 # Acest fișier
```

## 4. Acoperirea Cerințelor Universitare

Proiectul acoperă o gamă largă de cerințe tipice pentru un proiect universitar avansat de baze de date și dezvoltare software:

1.  **Modelare Complexă a Datelor.**
2.  **Algoritmi PL/SQL Avansați.**
3.  **Triggere și Automatizări.**
4.  **Gestiunea Tranzacțiilor și Excepțiilor.**
5.  **Interfață Grafică (GUI).**
6.  **Arhitectură Multi-Tier.**
7.  **Securitate.**
8.  **Documentație.**

## 5. Pași Următori Imediati (Recomandări)

1.  **Inițializarea Proiectelor (dacă nu s-a făcut deja de Cascade).**
2.  **Configurarea Conexiunii la Baza de Date Oracle în `application.properties`.**
3.  **Popularea scripturilor SQL (`db/`) cu definițiile create anterior.**
4.  **Rularea `db/schema.sql`.**
5.  **Dezvoltarea Incrementală.**
