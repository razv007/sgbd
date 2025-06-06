export interface Eveniment {
	id: number | string;
	titlu: string;
	descriere?: string;
	dataInceput: string;
	dataSfarsit?: string;
	locatie?: string;
	categorie?: string;
	vizibilitate?: string;
	numeUtilizator?: string; // 👈 add this
	type?: "birth" | "today" | "event";
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