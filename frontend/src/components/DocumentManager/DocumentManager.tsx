import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import styles from "./DocumentManager.module.scss";

interface Props {
	eventId: number | string;
}

interface DocumentEntry {
	id: string;
	name: string;
	url: string;
}

const DocumentManager: React.FC<Props> = ({ eventId }) => {
	const [documents, setDocuments] = useState<DocumentEntry[]>([]);
	const fileInputRef = useRef<HTMLInputElement | null>(null);

	const uploadDocument = async (file: File) => {
		try {
			const formData = new FormData();
			formData.append("file", file);
			formData.append("eventId", String(eventId));

			const token = localStorage.getItem("userToken");

			const response = await axios.post<DocumentEntry>(
				"http://localhost:8081/api/documents/upload",
				formData,
				{
					headers: {
						Authorization: `Bearer ${token || ""}`,
						"Content-Type": "multipart/form-data",
					},
				}
			);

			setDocuments((prev) => [...prev, response.data]);
		} catch (error: any) {
			console.error("Upload failed:", error);
			alert("Eroare la încărcarea documentului.");
		}
	};

	const deleteDocument = async (docId: string) => {
		try {
			const token = localStorage.getItem("userToken");

			await axios.delete(
				`http://localhost:8081/api/documents/${docId}`,
				{
					headers: {
						Authorization: `Bearer ${token || ""}`,
					},
				}
			);

			setDocuments((prev) => prev.filter((d) => d.id !== docId));
		} catch (error: any) {
			console.error("Delete failed:", error);
			alert("Eroare la ștergerea documentului.");
		}
	};

	const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		if (e.target.files && e.target.files.length > 0) {
			uploadDocument(e.target.files[0]);
			e.target.value = ""; // Clear input
		}
	};

	const handleAddClick = () => {
		fileInputRef.current?.click();
	};

	const handleDeleteClick = (docId: string) => {
		if (window.confirm("Are you sure you want to delete this document?")) {
			deleteDocument(docId);
		}
	};

	useEffect(() => {
		const fetchDocuments = async () => {
			try {
				const token = localStorage.getItem("userToken");

				const response = await axios.get<DocumentEntry[]>(
					`http://localhost:8081/api/documents`,
					{
						params: { eventId },
						headers: {
							Authorization: `Bearer ${token || ""}`,
						},
					}
				);

				setDocuments(response.data);
			} catch (error: any) {
				console.error("Fetch error:", error);
				alert("Eroare la încărcarea documentelor.");
			}
		};

		fetchDocuments();
	}, [eventId]);

	return (
		<div className={styles.documentManager}>
			<h4>Artefacte:</h4>
			<button onClick={handleAddClick} className={styles.addBtn}>
				Adaugă document
			</button>
			<input
				type="file"
				accept="*/*"
				ref={fileInputRef}
				style={{ display: "none" }}
				onChange={handleFileChange}
			/>
			<div className={styles.documentList}>
				{documents.map((doc) => (
					<div key={doc.id} className={styles.documentItem}>
						<a
							href={doc.url}
							target="_blank"
							rel="noopener noreferrer"
						>
							{doc.name}
						</a>
						<button onClick={() => handleDeleteClick(doc.id)}>
							X
						</button>
					</div>
				))}
			</div>
		</div>
	);
};

export default DocumentManager;
