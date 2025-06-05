export interface Eveniment {
	id: number | string; // Permite string pentru ID-urile markerelor speciale
	titlu: string;
	descriere?: string;
	dataInceput: string; // String ISO (YYYY-MM-DD sau YYYY-MM-DDTHH:mm:ss)
	dataSfarsit?: string; // String ISO
	locatie?: string;
	categorie?: string;
	vizibilitate?: string;
	type?: "birth" | "today" | "event"; // Tipul item-ului pentru stilizare și logică diferită
}

// Interfață pentru starea formularului
export interface EvenimentFormData {
	titlu: string;
	descriere: string;
	dataInceput: string; // Va fi gestionat ca string pentru input type="datetime-local"
	dataSfarsit: string;
	locatie: string;
	categorie: string;
	vizibilitate: "PRIVAT" | "PRIETENI" | "PUBLIC";
}