import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './HomePage.module.css';
import { Link } from 'react-router-dom'; // Importă Link pentru navigare
import ProfileEditModal from './ProfileEditModal';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUserEdit } from '@fortawesome/free-solid-svg-icons';

// Definiția interfeței UserProfile (asigură-te că este identică cu cea din ProfileEditModal.tsx)
interface UserProfile {
  id: string;
  numeUtilizator: string;
  email: string;
  numeComplet: string;
  dataNasterii: string | null;
}

const HomePage: React.FC = () => {
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
  const [profileModalError, setProfileModalError] = useState<string | null>(null);
  const [isProfileSaving, setIsProfileSaving] = useState(false);
  const [isUserLoading, setIsUserLoading] = useState<boolean>(true); // Inițial true, pentru că vom încerca să încărcăm datele
  const [userFetchError, setUserFetchError] = useState<string | null>(null);
  const navigate = useNavigate(); // Mutat aici pentru a corecta eroarea de lint

  useEffect(() => {
    const fetchUserProfile = async () => {
      const token = localStorage.getItem('userToken');
      if (!token) {
        setIsUserLoading(false);
        // Nu este necesar să navigăm aici, componenta va afișa starea de nelogat
        return;
      }

      setIsUserLoading(true);
      setUserFetchError(null);
      try {
        const response = await fetch('http://localhost:8081/api/profil', {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('userToken');
            localStorage.removeItem('userData');
            localStorage.removeItem('userDataNastere');
            setCurrentUser(null);
            navigate('/login'); // Redirecționează la login dacă tokenul e invalid
            throw new Error('Sesiune invalidă sau expirată. Te rugăm să te re-autentifici.');
          }
          let errorMsg = `Eroare ${response.status} la preluarea profilului.`;
          try {
            const errorData = await response.json();
            errorMsg = errorData.message || errorData.error || errorMsg;
          } catch (e) { /* Ignoră dacă nu e JSON */ }
          throw new Error(errorMsg);
        }

        const userData: UserProfile = await response.json();
        setCurrentUser(userData);
        localStorage.setItem('userData', JSON.stringify(userData));
        if (userData.dataNasterii) {
          localStorage.setItem('userDataNastere', userData.dataNasterii); // Pentru TimelinePage
        }

      } catch (err) {
        console.error('Failed to fetch user profile:', err);
        setUserFetchError(err instanceof Error ? err.message : 'A apărut o eroare la încărcarea profilului.');
        // Considerăm să nu ștergem currentUser aici, pentru a nu pierde datele dacă erau deja din localStorage
        // și doar fetch-ul a eșuat temporar.
      } finally {
        setIsUserLoading(false);
      }
    };

    fetchUserProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate]); // navigate este adăugat ca dependență, deși e stabil

  const toggleProfileModal = () => setIsProfileModalOpen(!isProfileModalOpen);

  const handleSaveProfile = async (updatedData: UserProfile) => {
    console.log('Saving profile data from HomePage:', updatedData);
    const token = localStorage.getItem('userToken');
    if (!token) {
      setProfileModalError('Autentificare necesară. Te rugăm să te re-loghezi.');
      // Poate ar trebui să navighezi către pagina de login aici
      return;
    }

    setIsProfileSaving(true);
    setProfileModalError(null);

    try {
      const userId = currentUser?.id || updatedData.id;
      if (!userId) {
        throw new Error('ID-ul utilizatorului lipsește.');
      }
      const endpointURL = `http://localhost:8081/api/profil`; 

      const response = await fetch(endpointURL, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(updatedData),
      });

      if (!response.ok) {
        let errorMsg = `Eroare ${response.status} la salvarea profilului.`;
        try {
          const errorData = await response.json();
          errorMsg = errorData.message || errorData.error || errorMsg;
        } catch (e) {
          // Nu s-a putut parsa JSON-ul din eroare, folosim mesajul generic
        }
        throw new Error(errorMsg);
      }

      localStorage.setItem('userData', JSON.stringify(updatedData));
      setCurrentUser(updatedData);
      setIsProfileModalOpen(false); 
      alert('Profilul a fost actualizat cu succes!'); 

    } catch (err) {
      console.error('Failed to save profile:', err);
      setProfileModalError(err instanceof Error ? err.message : 'A apărut o eroare necunoscută.');
      // Nu închidem modalul, pentru ca utilizatorul să vadă eroarea și să încerce din nou
    } finally {
      setIsProfileSaving(false);
    }
  };

  const userToken = localStorage.getItem('userToken');

  const handleLogout = () => {
    localStorage.removeItem('userToken');
    // Optionally, inform the backend about logout if needed
    navigate('/login');
  };

  return (
    <>
    <div className={styles.homeContainer}>
      <header className={styles.header}>
        <h1>Arhiva Digitală</h1>
        <div className={styles.headerActions}>
          {userToken && currentUser && (
            <button onClick={toggleProfileModal} className={`${styles.actionButton} ${styles.profileButton}`}>
              <FontAwesomeIcon icon={faUserEdit} /> Editează Profil
            </button>
          )}
          {userToken && (
            <button onClick={handleLogout} className={`${styles.actionButton} ${styles.logoutButton}`}>
              Logout
            </button>
          )}
        </div>
      </header>
      <main className={styles.mainContent}>
        {isUserLoading && <p>Se încarcă datele utilizatorului...</p>}
        {userFetchError && !isUserLoading && (
          <p className={styles.errorMessage}>{userFetchError}</p>
          // O sugestie ar fi să oferi un buton de reîncercare sau link către login
        )}

        {!isUserLoading && !userFetchError && (
          <>
            {userToken && currentUser ? (
              <>
                <h1>Bun Venit pe platforma noastră!</h1>
                {currentUser && currentUser.numeComplet && (
                  <h2 className={styles.welcomeUser}>Salut, {currentUser.numeComplet}!</h2>
                )}
                <Link to="/timeline" className={styles.actionButton}>
                  Vezi Timeline
                </Link>
              </>
            ) : (
              // Afișat dacă nu există token (utilizator nelogat) și nu sunt erori de fetch
              <>
                <h1>Bun Venit pe platforma noastră!</h1>
                <p>Pentru a accesa toate funcționalitățile, te rugăm să te <Link to="/login">autentifici</Link> sau să îți <Link to="/register">creezi un cont</Link>.</p>
              </>
            )}
          </>
        )}
      </main>
      <footer className={styles.footer}>
        <p>&copy; {new Date().getFullYear()} Arhiva Digitală. Toate drepturile rezervate.</p>
      </footer>
    </div>
    {currentUser && (
        <ProfileEditModal 
          isOpen={isProfileModalOpen} 
          onClose={toggleProfileModal} 
          userData={currentUser} 
          onSave={handleSaveProfile} 
          isLoading={isProfileSaving}
          error={profileModalError}
        />
      )}
      {/* Modal pentru schimbarea parolei */}
  </>
  );
};

export default HomePage;
