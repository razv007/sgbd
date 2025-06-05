import React, { useEffect, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react'; // Import types for events
import { useNavigate } from 'react-router-dom';
import styles from './TimelinePage.module.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus } from '@fortawesome/free-solid-svg-icons'; // faUserEdit removed, faPlus remains

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
  // State for add event form
  const [showAddEventForm, setShowAddEventForm] = useState(false);
  const [formData, setFormData] = useState<EvenimentFormData>({
    titlu: '',
    descriere: '',
    dataInceput: new Date().toISOString().slice(0, 16),
    dataSfarsit: '',
    locatie: '',
    categorie: '',
    vizibilitate: 'PRIVAT',
  });
  const [formError, setFormError] = useState<string | null>(null);

  // State for timeline events
  const [evenimente, setEvenimente] = useState<Eveniment[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  // State for special markers
  const [birthDate, setBirthDate] = useState<string | null>(null);
  const [currentDateISO, setCurrentDateISO] = useState<string>(new Date().toISOString());

  const navigate = useNavigate();

  // Fetch timeline events
  const fetchEvenimente = async () => {
    const token = localStorage.getItem('userToken');
    if (!token) {
      navigate('/login');
      return;
    }
    setLoading(true);
    setError(null);
    try {
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
      setEvenimente(data.map(ev => ({ ...ev, type: 'event' })));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Eroare la preluarea evenimentelor.');
    } finally {
      setLoading(false);
    }
  };

  // Effect for initial data load (birth date, current date, events)
  useEffect(() => {
    const storedBirthDate = localStorage.getItem('userDataNastere');
    if (storedBirthDate) {
      setBirthDate(storedBirthDate.split('T')[0]);
    }
    setCurrentDateISO(new Date().toISOString());
    fetchEvenimente();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate]); // navigate is a stable function, so this effectively runs once

  // Handlers for Add Event Form
  const toggleAddEventForm = () => setShowAddEventForm(!showAddEventForm);
  const handleInputChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setFormError(null);
    const token = localStorage.getItem('userToken');
    if (!token) {
      setFormError('Autentificare necesară.');
      navigate('/login'); // Redirect to login if no token
      return;
    }
    const payload = {
      ...formData,
      dataSfarsit: formData.dataSfarsit || undefined,
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
        const errorData = await response.json();
        const errorMessage = errorData.message || `Eroare ${response.status}`;
        throw new Error(errorMessage);
      }
      setFormData({ titlu: '', descriere: '', dataInceput: new Date().toISOString().slice(0, 16), dataSfarsit: '', locatie: '', categorie: '', vizibilitate: 'PRIVAT' });
      setShowAddEventForm(false);
      await fetchEvenimente(); // Refresh events list
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Eroare la crearea evenimentului.');
    }
  };

  // Helper function to format date/time
  const formatDateTime = (dateString: string, type?: Eveniment['type']): string => {
    if (!dateString) return 'Dată necunoscută';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return 'Dată invalidă';

    if (type === 'birth' || type === 'today') {
      return date.toLocaleDateString('ro-RO', { year: 'numeric', month: 'long', day: 'numeric' });
    }
    return date.toLocaleString('ro-RO', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  // Helper function for event styling (can be extended)
  const getEventStyle = (_item: Eveniment): React.CSSProperties => {
    return {};
  };

  // Prepare items for display (events + special markers)
  let allDisplayableItems: Eveniment[] = [...evenimente];
  if (birthDate) {
    allDisplayableItems.push({
      id: 'birth-marker',
      titlu: 'Data Nașterii',
      descriere: 'Aici începe povestea ta!',
      dataInceput: birthDate,
      dataSfarsit: '',
      locatie: '',
      categorie: '',
      vizibilitate: 'PUBLIC',
      type: 'birth',
    });
  }
  allDisplayableItems.push({
    id: 'today-marker',
    titlu: 'Astăzi',
    descriere: 'Ziua curentă.',
    dataInceput: currentDateISO,
    dataSfarsit: '',
    locatie: '',
    categorie: '',
    vizibilitate: 'PUBLIC',
    type: 'today',
  });

  const sortedTimelineItems = allDisplayableItems
    .sort((a, b) => new Date(a.dataInceput).getTime() - new Date(b.dataInceput).getTime())
    .map((item, _index) => {
      let sideClass = '';
      let onAxisClass = '';
      let markerSpecificClass = '';

      if (item.type === 'birth' || item.type === 'today') {
        onAxisClass = styles.eventOnAxis;
      } else {
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
              <h3>{item.titlu}</h3>
              <p>{item.descriere}</p>
              <small>
                De la: {formatDateTime(item.dataInceput, item.type)}
                {item.dataSfarsit && ` până la: ${formatDateTime(item.dataSfarsit, item.type)}`}
                {item.locatie && ` | Locație: ${item.locatie}`}
                {item.categorie && ` | Categorie: ${item.categorie}`}
              </small>
              {item.type === 'event' && (
                <div className={styles.eventArtifacts}>
                  <h4>Artefacte:</h4>
                  <p><i>(Afișare imagini, PDF-uri etc. aici)</i></p>
                </div>
              )}
            </div>
          )}
        </div>
      );
    });

  // Conditional rendering for loading/error states
  if (loading && evenimente.length === 0) { // Show loading only if no events are yet displayed
    return <div className={styles.container}><p>Se încarcă evenimentele...</p></div>;
  }

  if (error && !showAddEventForm) { // Show main error if not in add event form (form has its own error display)
    return <div className={styles.timelinePageContainer}><p className={styles.errorText}>{error}</p></div>;
  }

  return (
    <div className={styles.timelinePageContainer}>
      <header className={styles.timelineHeader}>
        <h1>Timeline Personal</h1>
        <div className={styles.headerActions}>
          <button onClick={toggleAddEventForm} className={`${styles.headerButton} ${styles.addEventButtonMain}`}>
            <FontAwesomeIcon icon={faPlus} /> {showAddEventForm ? 'Anulează Adăugarea' : 'Adaugă Eveniment'}
          </button>

        </div>
      </header>
      
      {/* Formularul de adăugare eveniment */} 
      {showAddEventForm && (
        <form onSubmit={handleSubmit} className={styles.eventForm}>
          <h3>Adaugă Eveniment Nou</h3>
          {formError && <p className={styles.errorMessage}>{formError}</p>}
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

      {/* Timeline items container */}
      <div className={styles.verticalTimelineContainer}>
        {(!loading && !error && sortedTimelineItems.length === 0 && !showAddEventForm) && 
          <p className={styles.noEventsMessage}>Nu există evenimente de afișat.</p>
        }
        {sortedTimelineItems.length > 0 && sortedTimelineItems}
      </div>

    </div>
  );
};

export default TimelinePage;
