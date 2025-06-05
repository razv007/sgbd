import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css'; // Temporarily reuse login styles

const SignupPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [fullName, setFullName] = useState(''); // Added fullName state
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState(''); // Moved state declaration for dateOfBirth
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (password !== confirmPassword) {
      setError('Passwords do not match!');
      return;
    }
    setError(null);
    setLoading(true);

    const signUpPayload = {
      numeUtilizator: username,
      email: email,
      parola: password, // Backend will hash this
      numeComplet: fullName || null, // Send null if fullName is empty
      dataNastere: dateOfBirth || null, // Send dateOfBirth string (YYYY-MM-DD) or null
    };

    try {
      const response = await fetch('http://localhost:8081/api/auth/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(signUpPayload),
      });

      const responseBodyText = await response.text(); // Read body as text first

      if (response.ok) {
        alert('Registration successful! Redirecting to login.'); // Simple feedback for now
        navigate('/login');
      } else {
        // Backend sends plain text error message for 400 Bad Request
        setError(responseBodyText || 'Registration failed. Please try again.');
      }
    } catch (err) {
      console.error('Signup API error:', err);
      setError('A network error occurred. Please check your connection or if the server is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginForm}> {/* Reusing loginForm style for the card layout */}
      <h1 className={styles.appTitle}>Arhiva Digitală</h1>
      <h2>Create Account</h2>
      <form onSubmit={handleSubmit}>
        <div className={styles.inputGroup}>
          <label htmlFor="username">Username:</label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className={styles.inputGroup}>
          <label htmlFor="fullName">Full Name (Optional):</label>
          <input
            type="text"
            id="fullName"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
          />
        </div>
        <div className={styles.inputGroup}>
          <label htmlFor="dateOfBirth">Date of Birth (Optional):</label>
          <input
            type="date"
            id="dateOfBirth"
            value={dateOfBirth}
            onChange={(e) => setDateOfBirth(e.target.value)}
            max={new Date().toISOString().split('T')[0]} // Set max to today
          />
        </div>
        <div className={styles.inputGroup}>
          <label htmlFor="email">Email:</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className={styles.inputGroup}>
          <label htmlFor="password">Password:</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <div className={styles.inputGroup}>
          <label htmlFor="confirmPassword">Confirm Password:</label>
          <input
            type="password"
            id="confirmPassword"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
        </div>
        {error && <p className={styles.error}>{error}</p>}
        <button type="submit" disabled={loading} className={styles.button}>
          {loading ? 'Creating Account...' : 'Sign Up'}
        </button>
      </form>
      <Link to="/login" className={styles.link}>Autentifică-te</Link>
    </div>
  );
};

export default SignupPage;
