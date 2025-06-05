import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './HomePage.module.css';
import { Link } from 'react-router-dom'; // Importă Link pentru navigare // We'll create this CSS module next

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const userToken = localStorage.getItem('userToken');

  const handleLogout = () => {
    localStorage.removeItem('userToken');
    // Optionally, inform the backend about logout if needed
    navigate('/login');
  };

  return (
    <div className={styles.homeContainer}>
      <header className={styles.header}>
        <h1>Arhiva Digitală</h1>
        {userToken && (
          <button onClick={handleLogout} className={styles.logoutButton}>
            Logout
          </button>
        )}
      </header>
      <main className={styles.mainContent}>
        <h2>Bun venit!</h2>{
        userToken ? (
          <>
            <p>Sunteți autentificat în aplicație.</p>
            <Link to="/timeline" className={styles.actionButton}>
              Vezi Timeline
            </Link>
          </>
        ) : (
          // Am păstrat structura originală pentru partea de "neautentificat", 
          // dar am înlocuit <a> cu <Link> pentru consistență și o mai bună practică în React Router
          <p>Vă rugăm să vă <Link to="/login">autentificați</Link> sau să vă <Link to="/signup">înregistrați</Link> pentru a accesa conținutul.</p>
        )
      }  {/* Aici se poate adauga mai mult continut specific paginii principale */}
      </main>
      <footer className={styles.footer}>
        <p>&copy; {new Date().getFullYear()} Arhiva Digitală. Toate drepturile rezervate.</p>
      </footer>
    </div>
  );
};

export default HomePage;
