import React, { useEffect, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './TimelinePage.module.css'; // Vom crea acest fișier

interface Eveniment {
    id: number | string; // Permite string pentru ID-urile markerelor speciale
    titlu: string;
    descriere?: string;
    dataInceput: string; // String ISO (YYYY-MM-DD sau YYYY-MM-DDTHH:mm:ss)
    dataSfarsit?: string; // String ISO
    locatie?: string;
    categorie?: string;
    vizibilitate?: string;
    type?: 'birth' | 'today' | 'event'; // Tipul item-ului pentru stilizare și logică diferită
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
    const [showForm, setShowForm] = useState<boolean>(false);
    const [formData, setFormData] = useState<EvenimentFormData>({
        titlu: '',
        descriere: '',
        dataInceput: new Date().toISOString().slice(0, 16), // Default to current date and time
        dataSfarsit: '',
        locatie: '',
        categorie: '',
        vizibilitate: 'PRIVAT',
    });
    const [formError, setFormError] = useState<string | null>(null);
    const [birthDate, setBirthDate] = useState<string | null>(null); // Storing as YYYY-MM-DD string
    const [currentDateISO, setCurrentDateISO] = useState<string>(new Date().toISOString());

    const navigate = useNavigate();

    const fetchEvenimente = async () => {
        const token = localStorage.getItem('userToken');
        if (!token) {
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
            if (!response.ok) {
                let errorDataText = `Eroare ${response.status}`;
                try {
                    const errorDataJson = await response.json();
                    errorDataText = errorDataJson.message || errorDataJson.error || errorDataText;
                } catch (e) { /* Ignore if not JSON */ }
                if (response.status === 401 || response.status === 403) {
                    localStorage.removeItem('userToken');
                    navigate('/login');
                    throw new Error('Sesiune expirată sau neautorizată.');
                }
                throw new Error(errorDataText);
            }
            const data: Eveniment[] = await response.json();
            setEvenimente(data.map(ev => ({ ...ev, type: 'event' }))); // Mark real events
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Eroare la preluarea evenimentelor.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const storedBirthDate = localStorage.getItem('userDataNastere'); // Assuming 'userDataNastere' is the correct key
        if (storedBirthDate) {
            // Extract only the date part if it includes time
            setBirthDate(storedBirthDate.split('T')[0]); 
        }
        setCurrentDateISO(new Date().toISOString());
        fetchEvenimente();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [navigate]);

    const handleInputChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setFormError(null);
        const token = localStorage.getItem('userToken');
        if (!token) { navigate('/login'); return; }
        if (!formData.titlu || !formData.dataInceput) {
            setFormError("Titlul și data de început sunt obligatorii.");
            return;
        }
        const payload = {
            ...formData,
            dataSfarsit: formData.dataSfarsit || undefined, // Send undefined if empty, backend should handle null
        };
        try {
            const response = await fetch('http://localhost:8081/api/evenimente', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });
            if (!response.ok) {
                let errorMessage = `Eroare ${response.status}`;
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorData.error || (Array.isArray(errorData.errors) ? errorData.errors.map((err: any) => err.defaultMessage).join(', ') : errorMessage);
                } catch (e) { /* Ignore */ }
                throw new Error(errorMessage);
            }
            setFormData({ titlu: '', descriere: '', dataInceput: new Date().toISOString().slice(0, 16), dataSfarsit: '', locatie: '', categorie: '', vizibilitate: 'PRIVAT' });
            setShowForm(false);
            await fetchEvenimente();
        } catch (err) {
            setFormError(err instanceof Error ? err.message : 'Eroare la crearea evenimentului.');
        }
    };

    const formatDateTime = (dateString: string, type?: Eveniment['type']): string => {
        if (!dateString) return 'Data invalidă';
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return 'Data invalidă';

        if (type === 'birth' || type === 'today') {
            return date.toLocaleDateString('ro-RO', { year: 'numeric', month: 'long', day: 'numeric' });
        }
        // Default for 'event' or unspecified type
        return date.toLocaleString('ro-RO', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    };

    const getEventStyle = (_item: Eveniment): React.CSSProperties => {
        // For a simple list display, no dynamic styles are needed for positioning.
        // Specific item types (like 'birth' or 'today') might have distinct visual treatments
        // handled by CSS classes rather than dynamic style objects here.
        // This function can be extended if other dynamic styles are needed in the future.
        return {};
    };

    const allDisplayableItems: Eveniment[] = [...evenimente];

    if (birthDate) {
        allDisplayableItems.push({
            id: 'birth-marker',
            titlu: 'Data Nașterii',
            descriere: 'Aici începe povestea ta!', // Custom description
            dataInceput: birthDate, // birthDate is YYYY-MM-DD, ensure it's treated as start of day if needed
            dataSfarsit: '',
            locatie: '',
            categorie: '',
            vizibilitate: 'PUBLIC', // Or as appropriate
            type: 'birth',
        });
    }

    allDisplayableItems.push({
        id: 'today-marker',
        titlu: 'Astăzi',
        descriere: 'Ziua curentă.', // Custom description
        dataInceput: currentDateISO,
        dataSfarsit: '',
        locatie: '',
        categorie: '',
        vizibilitate: 'PUBLIC', // Or as appropriate
        type: 'today',
    });

    const sortedTimelineItems = allDisplayableItems
        .sort((a, b) => new Date(a.dataInceput).getTime() - new Date(b.dataInceput).getTime())
        .map((item) => {
            let sideClass = '';
            let onAxisClass = '';
            let markerSpecificClass = '';

            if (item.type === 'birth' || item.type === 'today') {
                onAxisClass = styles.eventOnAxis;
                // For on-axis events, we don't use eventLeft or eventRight for main positioning
            } else {
                // Determine if the event should be on the left or right
                // Using index of the sorted array for alternation for regular events
                // We need to be careful with 'index' if birth/today are not counted for alternation
                // Let's filter non-axis events to get a true alternating index
                const regularEvents = allDisplayableItems.filter(e => e.type !== 'birth' && e.type !== 'today');
                const regularEventIndex = regularEvents.findIndex(e => e.id === item.id);
                sideClass = regularEventIndex % 2 === 0 ? styles.eventLeft : styles.eventRight;
            }

            if (item.type === 'birth') markerSpecificClass = styles.birthMarker;
            if (item.type === 'today') markerSpecificClass = styles.todayMarker;

            let contentSpecificClass = '';
            if (item.type === 'birth') contentSpecificClass = styles.birthEventContent;
            if (item.type === 'today') contentSpecificClass = styles.todayEventContent;

            return (
                <div key={item.id} className={`${styles.verticalTimelineEvent} ${sideClass} ${onAxisClass}`} style={getEventStyle(item)}>
                    <div className={`${styles.eventMarker} ${markerSpecificClass}`}></div>
                    <div className={styles.eventDateOnAxis}>{formatDateTime(item.dataInceput, item.type)}</div>
                    {(item.type !== 'birth' && item.type !== 'today') && (
                        <div className={`${styles.verticalTimelineEventContent} ${contentSpecificClass}`}>
                            <strong>{item.titlu}</strong>
                            <p>{item.descriere || (item.type === 'event' ? 'Nicio descriere adăugată.' : '')}</p>
                            {item.type === 'event' && (
                                <>
                                    <p className={styles.eventDate}><strong>Început:</strong> {formatDateTime(item.dataInceput, item.type)}</p>
                                    {item.dataSfarsit && <p className={styles.eventDate}><strong>Sfârșit:</strong> {formatDateTime(item.dataSfarsit, item.type)}</p>}
                                    {item.locatie && <p className={styles.eventLocation}><strong>Locație:</strong> {item.locatie}</p>}
                                    {item.categorie && <p className={styles.eventCategory}><strong>Categorie:</strong> {item.categorie}</p>}
                                </>
                            )}
                            {/* Conținutul specific pentru birth/today a fost eliminat deoarece cardul nu mai e randat pentru ele */}
                        </div>
                    )}
                </div>
            );
        });

    if (loading && evenimente.length === 0 && !showForm) {
        return <div className={styles.container}><p>Se încarcă evenimentele...</p></div>;
    }

    if (error && !(showForm && formError)) { 
        return <div className={styles.timelinePageContainer}><p className={styles.errorText}>{error}</p></div>;
    }

    return (
        <div className={styles.timelinePageContainer}>
            <h1 className={styles.title}>Timeline Personal</h1>

            <button onClick={() => setShowForm(!showForm)} className={styles.toggleFormButton}>
                {showForm ? 'Anulează Adăugarea' : 'Adaugă Eveniment Nou'}
            </button>

            {showForm && (
                <form onSubmit={handleSubmit} className={styles.eventForm}>
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

            <div className={styles.verticalTimelineContainer}>
                {sortedTimelineItems.length > 0 ? sortedTimelineItems : 
                    (!loading && <p className={styles.noEventsMessage}>Nu există evenimente de afișat.</p>)
                }
            </div>
        </div>
    );
};

export default TimelinePage;

// Cleanup: Removed the external function definitions for handleInputChange, handleSubmit, formatDateTime
// as they are now correctly placed inside the TimelinePage component.
