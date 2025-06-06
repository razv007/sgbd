import React, { useState, useEffect } from 'react';
import axios from 'axios';
import styles from './UserProfileForm.module.css';

interface UserProfileData {
  email: string;
  numeComplet: string;
  dataNastere: string; // YYYY-MM-DD format
}

const UserProfileForm: React.FC = () => {
  const [formData, setFormData] = useState<UserProfileData>({
    email: '',
    numeComplet: '',
    dataNastere: '',
  });
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const apiBaseUrl = 'http://localhost:8081'; // Conform memoriei

  useEffect(() => {
    const fetchUserData = async () => {
      const token = localStorage.getItem('userToken');
      if (!token) {
        setError('Nu sunteți autentificat.');
        return;
      }
      try {
        const response = await axios.get(`${apiBaseUrl}/api/users/me`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        const { email, numeComplet, dataNastere } = response.data;
        setFormData({
          email: email || '',
          numeComplet: numeComplet || '',
          dataNastere: dataNastere || '', // Assuming backend returns YYYY-MM-DD or null
        });
      } catch (err) {
        console.error('Error fetching user data:', err);
        setError('A apărut o eroare la preluarea datelor utilizatorului.');
      }
    };

    fetchUserData();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setMessage(null);
    setError(null);

    const token = localStorage.getItem('userToken');
    if (!token) {
      setError('Nu sunteți autentificat. Vă rugăm să vă autentificați din nou.');
      return;
    }

    // Validare simplă pentru data nașterii (format YYYY-MM-DD)
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (formData.dataNastere && !dateRegex.test(formData.dataNastere)) {
        setError('Formatul datei de naștere este invalid. Folosiți YYYY-MM-DD.');
        return;
    }

    try {
      const response = await axios.put(`${apiBaseUrl}/api/users/me`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });
      setMessage('Profilul a fost actualizat cu succes!');
      // Opțional: actualizează datele din formData cu răspunsul, dacă backend-ul returnează date actualizate
      const { email, numeComplet, dataNastere } = response.data;
      setFormData({
        email: email || '',
        numeComplet: numeComplet || '',
        dataNastere: dataNastere || '',
      });

    } catch (err: any) {
      console.error('Error updating profile:', err);
      if (err.response && err.response.data) {
        if (typeof err.response.data === 'string') {
            setError(err.response.data);
        } else if (err.response.data.message) {
            setError(err.response.data.message);
        } else if (err.response.data.errors) { // Pentru erori de validare de la Spring Boot
            const validationErrors = err.response.data.errors.map((error: any) => `${error.field}: ${error.defaultMessage}`).join(', ');
            setError(`Eroare de validare: ${validationErrors}`);
        } else {
            setError('A apărut o eroare la actualizarea profilului.');
        }
      } else {
        setError('A apărut o eroare la actualizarea profilului.');
      }
    }
  };

  return (
    <div className={styles.formContainer}>
      {/* Titlul este acum gestionat de componenta Modal, deci îl putem elimina de aici dacă dorim */}
      {/* <h2>Actualizează Profilul</h2> */}
      {message && <p className={styles.successMessage}>{message}</p>}
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
          <label htmlFor="dataNastere">Data Nașterii (YYYY-MM-DD):</label>
          <input
            type="text"
            id="dataNastere"
            name="dataNastere"
            value={formData.dataNastere}
            onChange={handleChange}
            placeholder="YYYY-MM-DD"
          />
        </div>
        <button type="submit" className={styles.submitButton}>Salvează Modificările</button>
      </form>
    </div>
  );
};

export default UserProfileForm;
