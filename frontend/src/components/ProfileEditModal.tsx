import React, { useState, useEffect } from 'react';
import styles from './ProfileEditModal.module.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

interface UserProfile {
  id: string;
  numeUtilizator: string; // Păstrat pentru props.userData, dar nu este editabil în acest modal
  email: string;
  numeComplet: string;
  dataNasterii: string | null; // Adăugat pentru compatibilitate
  // parola nu ar trebui editată direct aici, ci printr-un flux separat "Change Password"
}

interface ProfileEditModalProps {
  isOpen: boolean;
  onClose: () => void;
  userData: UserProfile | null; // Poate fi null inițial, până la încărcare
  onSave: (updatedData: UserProfile) => Promise<void>; // Funcție asincronă pentru salvare
  isLoading?: boolean;
  error?: string | null;
}

const ProfileEditModal: React.FC<ProfileEditModalProps> = ({ isOpen, onClose, userData, onSave, isLoading, error }) => {
  const [formData, setFormData] = useState({
    email: '',
    numeComplet: '',
    dataNasterii: '', // Va fi un string în format YYYY-MM-DD
  });

  useEffect(() => {
    if (isOpen) {
      // Când modalul se deschide, inițializează câmpurile editabile ca goale.
      // ID-ul și alte date non-editabile din props.userData (dacă există)
      // vor fi încă folosite la submit prin spread-ul `...userData` în funcția handleSubmit.
      setFormData({
        email: '',
        numeComplet: '',
        dataNasterii: '', 
      });
    }
    // Nu este nevoie să adăugăm userData aici dacă nu îl folosim pentru pre-populare directă la deschidere.
  }, [isOpen]); // Se execută când starea isOpen se schimbă.

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userData) {
      // Eroarea ar trebui gestionată de componenta părinte sau afișată prin prop-ul 'error'
      console.error('User data is not available for saving.');
      return;
    }
    // Apelul onSave este o promisiune; gestionarea stării de încărcare și eroare se face în HomePage
    await onSave({
      ...userData, 
      ...formData,
    });
    // Decizia de a închide modalul se va lua în HomePage, după succesul salvării
  };

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        {error && <p className={styles.errorMessage}>{error}</p>} {/* Afișează eroarea primită ca prop */}
        <button onClick={onClose} className={styles.closeButton}>
          <FontAwesomeIcon icon={faTimes} />
        </button>
        <h2>Editează Profilul</h2>
        {error && <p className={styles.errorMessage}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label htmlFor="numeComplet">Nume Complet:</label>
            <input
              type="text"
              id="numeComplet"
              name="numeComplet"
              value={formData.numeComplet}
              onChange={handleChange}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label htmlFor="email">Email:</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label htmlFor="dataNasterii">Data Nașterii:</label>
            <input
              type="date"
              id="dataNasterii"
              name="dataNasterii"
              value={formData.dataNasterii}
              onChange={handleChange}
            />
          </div>
          <div className={styles.formActions}>
            <button type="submit" className={styles.saveButton} disabled={isLoading}>
              {isLoading ? 'Se salvează...' : 'Salvează Modificările'}
            </button>
            <button type="button" onClick={onClose} className={styles.cancelButton} disabled={isLoading}>
              Anulează
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProfileEditModal;
