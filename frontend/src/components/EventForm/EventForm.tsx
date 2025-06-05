import React, { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { type EvenimentFormData } from "../../types/eveniment";
import styles from "./EventForm.module.scss";

interface EventFormProps {
	onEventCreated: () => void;
	onError: (error: string | null) => void;
	onCancel: () => void;
}

const EventForm: React.FC<EventFormProps> = ({ 
	onEventCreated, 
	onError, 
	onCancel 
}) => {
	const [formData, setFormData] = useState<EvenimentFormData>({
		titlu: "",
		descriere: "",
		dataInceput: new Date().toISOString().slice(0, 16), // Default to current date and time
		dataSfarsit: "",
		locatie: "",
		categorie: "",
		vizibilitate: "PRIVAT",
	});
	const [formError, setFormError] = useState<string | null>(null);
	const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

	const navigate = useNavigate();

	const handleInputChange = (
		e: ChangeEvent<
			HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
		>
	) => {
		const { name, value } = e.target;
		setFormData((prev) => ({ ...prev, [name]: value }));
	};

	const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
		e.preventDefault();
		setFormError(null);
		setIsSubmitting(true);

		const token = localStorage.getItem("userToken");
		if (!token) {
			navigate("/login");
			return;
		}

		if (!formData.titlu || !formData.dataInceput) {
			setFormError("Titlul și data de început sunt obligatorii.");
			setIsSubmitting(false);
			return;
		}

		const payload = {
			...formData,
			dataSfarsit: formData.dataSfarsit || undefined, // Send undefined if empty, backend should handle null
		};

		try {
			await axios.post(
				"http://localhost:8081/api/evenimente",
				payload,
				{
					headers: {
						Authorization: `Bearer ${token}`,
						"Content-Type": "application/json",
					},
				}
			);

			// Reset form
			setFormData({
				titlu: "",
				descriere: "",
				dataInceput: new Date().toISOString().slice(0, 16),
				dataSfarsit: "",
				locatie: "",
				categorie: "",
				vizibilitate: "PRIVAT",
			});

			onEventCreated();
		} catch (err) {
			let errorMessage = "Eroare la crearea evenimentului.";
			
			if (axios.isAxiosError(err)) {
				const responseData = err.response?.data;
				errorMessage = 
					responseData?.message ||
					responseData?.error ||
					(Array.isArray(responseData?.errors)
						? responseData.errors
								.map((error: any) => error.defaultMessage)
								.join(", ")
						: `Eroare ${err.response?.status}`);
			}

			setFormError(errorMessage);
			onError(errorMessage);
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<form onSubmit={handleSubmit} className={styles.eventForm}>
			{formError && (
				<div className={styles.errorText}>{formError}</div>
			)}
			
			<div className={styles.formGroup}>
				<label htmlFor="titlu">Titlu:</label>
				<input
					type="text"
					id="titlu"
					name="titlu"
					value={formData.titlu}
					onChange={handleInputChange}
					required
					disabled={isSubmitting}
				/>
			</div>

			<div className={styles.formGroup}>
				<label htmlFor="descriere">Descriere:</label>
				<textarea
					id="descriere"
					name="descriere"
					value={formData.descriere}
					onChange={handleInputChange}
					disabled={isSubmitting}
				/>
			</div>

			<div className={styles.formGroup}>
				<label htmlFor="dataInceput">Data și Ora Început:</label>
				<input
					type="datetime-local"
					id="dataInceput"
					name="dataInceput"
					value={formData.dataInceput}
					onChange={handleInputChange}
					required
					disabled={isSubmitting}
				/>
			</div>

			<div className={styles.formGroup}>
				<label htmlFor="dataSfarsit">Data și Ora Sfârșit (opțional):</label>
				<input
					type="datetime-local"
					id="dataSfarsit"
					name="dataSfarsit"
					value={formData.dataSfarsit}
					onChange={handleInputChange}
					disabled={isSubmitting}
				/>
			</div>

			<div className={styles.formGroup}>
				<label htmlFor="locatie">Locație (opțional):</label>
				<input
					type="text"
					id="locatie"
					name="locatie"
					value={formData.locatie}
					onChange={handleInputChange}
					disabled={isSubmitting}
				/>
			</div>

			<div className={styles.formGroup}>
				<label htmlFor="categorie">Categorie (Tip Eveniment, opțional):</label>
				<input
					type="text"
					id="categorie"
					name="categorie"
					value={formData.categorie}
					onChange={handleInputChange}
					disabled={isSubmitting}
				/>
			</div>

			<div className={styles.formGroup}>
				<label htmlFor="vizibilitate">Vizibilitate:</label>
				<select
					id="vizibilitate"
					name="vizibilitate"
					value={formData.vizibilitate}
					onChange={handleInputChange}
					disabled={isSubmitting}
				>
					<option value="PRIVAT">Privat</option>
					<option value="PRIETENI">Prieteni</option>
					<option value="PUBLIC">Public</option>
				</select>
			</div>

			<div className={styles.buttonGroup}>
				<button 
					type="submit" 
					className={styles.submitButton}
					disabled={isSubmitting}
				>
					{isSubmitting ? "Se salvează..." : "Salvează Eveniment"}
				</button>
				<button 
					type="button" 
					className={styles.cancelButton}
					onClick={onCancel}
					disabled={isSubmitting}
				>
					Anulează
				</button>
			</div>
		</form>
	);
};

export default EventForm;