import React, { useEffect, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './TimelinePage.module.css'; // Vom crea acest fișier

interface Eveniment {
    id: number;
    titlu: string;
    descriere?: string;
    dataInceput: string; // Acum va fi un string ISO LocalDateTime
    dataSfarsit?: string; // Acum va fi un string ISO LocalDateTime
    locatie?: string;
    categorie?: string;
    vizibilitate: string; // Adăugat din backend
}

// Interfață pentru starea formularului
interface EvenimentFormData {
    titlu: string;
    descriere: string;
    dataInceput: string; // Va fi gestionat ca string pentru input type="datetime-local"
    dataSfarsit: string;
    locatie: string;
    categorie: string;
    vizibilitate: 'PRIVAT' | 'PRIETENI' | 'PUBLIC';
}

const TimelinePage: React.FC = () => {
    const [evenimente, setEvenimente] = useState<Eveniment[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [showForm, setShowForm] = useState<boolean>(false); // Stare pentru vizibilitatea formularului
    const [formData, setFormData] = useState<EvenimentFormData>({ // Stare pentru datele formularului
        titlu: '',
        descriere: '',
        dataInceput: '',
        dataSfarsit: '',
        locatie: '',
        categorie: '',
        vizibilitate: 'PRIVAT',
    });
    const [formError, setFormError] = useState<string | null>(null); // Stare pentru erorile formularului
    const navigate = useNavigate();

    // Helper function to fetch events
    const fetchEvenimente = async () => {
        const token = localStorage.getItem('userToken');
        console.log('TimelinePage: fetchEvenimente trying to use token:', token); // ADDED

        if (!token) {
            console.log('TimelinePage: No token found in localStorage, navigating to login.'); // ADDED
            navigate('/login');
            return;
        }
        try {
            setLoading(true);
            setError(null);
            const response = await fetch('http://localhost:8081/api/evenimente', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log('TimelinePage: Response status from /api/evenimente:', response.status); // ADDED

            if (!response.ok) {
                let errorDataText = ''; // For storing text response if JSON fails
                try {
                    // Try to parse as JSON first, as original code did
                    const errorDataJson = await response.json(); 
                    console.error('TimelinePage: Error response data (JSON) from /api/evenimente:', errorDataJson); // ADDED
                    if (errorDataJson && errorDataJson.message) {
                        errorDataText = errorDataJson.message;
                    } else if (errorDataJson && errorDataJson.error) {
                        errorDataText = errorDataJson.error;
                    }
                } catch (jsonError) {
                    // If JSON parsing fails, try to get as text
                    try {
                        errorDataText = await response.text();
                        console.error('TimelinePage: Error response data (text) from /api/evenimente:', errorDataText); // ADDED
                    } catch (textError) {
                        console.error('TimelinePage: Could not read error response as JSON or text.');
                    }
                }

                if (response.status === 401 || response.status === 403) {
                    localStorage.removeItem('userToken');
                    navigate('/login');
                    throw new Error('Sesiune expirată sau neautorizată. Vă rugăm să vă reautentificați.');
                }
                
                const errorMessage = errorDataText || `Eroare la preluarea evenimentelor: ${response.statusText}`;
                throw new Error(errorMessage);
            }
            const data: Eveniment[] = await response.json();
            setEvenimente(data);
        } catch (err) {
            console.error('Error fetching events:',err);
            setError(err instanceof Error ? err.message : 'A apărut o eroare necunoscută la preluarea evenimentelor.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchEvenimente();
    }, [navigate]); // navigate is a dependency, if it changes, re-fetch (though typically stable)

    const handleInputChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setFormError(null);
        const token = localStorage.getItem('userToken');
        if (!token) {
            navigate('/login');
            return;
        }

        if (!formData.titlu || !formData.dataInceput) {
            setFormError("Titlul și data de început sunt obligatorii.");
            return;
        }
        
        const payload = {
            ...formData,
            dataSfarsit: formData.dataSfarsit || null, // Ensure null if empty
        };

        try {
            const response = await fetch('http://localhost:8081/api/evenimente', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                let errorMessage = `Eroare la crearea evenimentului: ${response.statusText}`;
                try {
                    const errorData = await response.json();
                    if (errorData && errorData.errors && Array.isArray(errorData.errors)) {
                        errorMessage = errorData.errors.map((err: any) => err.defaultMessage || 'Eroare de validare').join(', ');
                    } else if (errorData && errorData.message) {
                        errorMessage = errorData.message;
                    } else if (errorData && errorData.error) {
                        errorMessage = errorData.error;
                    }
                } catch (e) {
                    // Stick with statusText
                }
                throw new Error(errorMessage);
            }

            setFormData({ titlu: '', descriere: '', dataInceput: '', dataSfarsit: '', locatie: '', categorie: '', vizibilitate: 'PRIVAT' });
            setShowForm(false);
            await fetchEvenimente(); // Re-fetch events to show the new one

        } catch (err) {
            console.error('Error creating event:', err);
            setFormError(err instanceof Error ? err.message : 'A apărut o eroare la crearea evenimentului.');
        }
    };

    const formatDateTime = (dateTimeString: string) => {
        if (!dateTimeString) return '';
        try {
            return new Date(dateTimeString).toLocaleString('ro-RO', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit'
            });
        } catch (e) {
            console.error('Error formatting date:', dateTimeString, e);
            return 'Dată invalidă';
        }
    };

    // Conditional rendering for loading and error states
    if (loading && evenimente.length === 0 && !showForm) {
        return <div className={styles.container}><p>Se încarcă evenimentele...</p></div>;
    }

    if (error && !showForm) { // Only show global error if form is not open, as the form has its own error display
        return <div className={styles.container}><p className={styles.errorText}>{error}</p></div>;
    }


    return (
        <div className={styles.container}>
            <h1>Timeline Personal</h1>

            <button onClick={() => setShowForm(!showForm)} className={styles.toggleFormButton}>
                {showForm ? 'Anulează Adăugare Eveniment' : 'Adaugă Eveniment Nou'}
            </button>

            {showForm && (
                <form onSubmit={handleSubmit} className={styles.eventForm}>
                    <h2>Adaugă un Eveniment Nou</h2>
                    {formError && <p className={styles.errorText}>{formError}</p>}
                    
                    <div>
                        <label htmlFor="titlu">Titlu:</label>
                        <input type="text" id="titlu" name="titlu" value={formData.titlu} onChange={handleInputChange} required />
                    </div>
                    <div>
                        <label htmlFor="descriere">Descriere:</label>
                        <textarea id="descriere" name="descriere" value={formData.descriere} onChange={handleInputChange} />
                    </div>
                    <div>
                        <label htmlFor="dataInceput">Data și Ora Început:</label>
                        <input type="datetime-local" id="dataInceput" name="dataInceput" value={formData.dataInceput} onChange={handleInputChange} required />
                    </div>
                    <div>
                        <label htmlFor="dataSfarsit">Data și Ora Sfârșit (opțional):</label>
                        <input type="datetime-local" id="dataSfarsit" name="dataSfarsit" value={formData.dataSfarsit} onChange={handleInputChange} />
                    </div>
                    <div>
                        <label htmlFor="locatie">Locație (opțional):</label>
                        <input type="text" id="locatie" name="locatie" value={formData.locatie} onChange={handleInputChange} />
                    </div>
                    <div>
                        <label htmlFor="categorie">Categorie (Tip Eveniment, opțional):</label>
                        <input type="text" id="categorie" name="categorie" value={formData.categorie} onChange={handleInputChange} />
                    </div>
                    <div>
                        <label htmlFor="vizibilitate">Vizibilitate:</label>
                        <select id="vizibilitate" name="vizibilitate" value={formData.vizibilitate} onChange={handleInputChange}>
                            <option value="PRIVAT">Privat</option>
                            <option value="PRIETENI">Prieteni</option>
                            <option value="PUBLIC">Public</option>
                        </select>
                    </div>
                    <button type="submit" className={styles.submitButton}>Salvează Eveniment</button>
                </form>
            )}

            {evenimente.length === 0 && !loading && !showForm ? (
                <p>Nu aveți niciun eveniment înregistrat.</p>
            ) : (
                <ul className={styles.eventList}>
                    {evenimente.map((event) => (
                        <li key={event.id} className={styles.eventItem}>
                            <h2>{event.titlu}</h2>
                            <p><strong>Data început:</strong> {formatDateTime(event.dataInceput)}</p>
                            {event.dataSfarsit && <p><strong>Data sfârșit:</strong> {formatDateTime(event.dataSfarsit)}</p>}
                            {event.descriere && <p>{event.descriere}</p>}
                            {event.locatie && <p><em>Locație: {event.locatie}</em></p>}
                            {event.categorie && <p><em>Categorie: {event.categorie}</em></p>}
                             <p><em>Vizibilitate: {event.vizibilitate}</em></p> {/* Afișare vizibilitate */}
                        </li>
                    ))}
                </ul>
            )}
            
        </div>
    );
};

export default TimelinePage;

// Cleanup: Removed the external function definitions for handleInputChange, handleSubmit, formatDateTime
// as they are now correctly placed inside the TimelinePage component.
