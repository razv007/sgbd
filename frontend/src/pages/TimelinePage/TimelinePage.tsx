import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import EventForm from "../../components/EventForm/EventForm";
import TimelineContainer from "../../components/TimelineContainer/TimelineContainer";
import { type Eveniment } from "../../types/eveniment";
import styles from "./TimelinePage.module.scss";

const TimelinePage: React.FC = () => {
	const { username } = useParams<{ username: string }>();

	const [evenimente, setEvenimente] = useState<Eveniment[]>([]);
	const [loading, setLoading] = useState<boolean>(true);
	const [error, setError] = useState<string | null>(null);
	const [showForm, setShowForm] = useState<boolean>(false);
	const [birthDate, setBirthDate] = useState<string | null>(null);
	const [currentDateISO, setCurrentDateISO] = useState<string>(
		new Date().toISOString()
	);

	const navigate = useNavigate();

	const fetchEvenimente = async () => {
		const token = localStorage.getItem("userToken");
		if (!token) {
			navigate("/login");
			return;
		}
		try {
			setLoading(true);
			setError(null);
			const response = await axios.get(
				`http://localhost:8081/api/evenimente/public/${username}`,
				{
					headers: {
						Authorization: `Bearer ${token}`,
						"Content-Type": "application/json",
					},
				}
			);

			const data: Eveniment[] = response.data;
			setEvenimente(data.map((ev) => ({ ...ev, type: "event" })));
		} catch (err) {
			if (axios.isAxiosError(err)) {
				const status = err.response?.status;
				if (status === 401 || status === 403) {
					localStorage.removeItem("userToken");
					navigate("/login");
					setError("Sesiune expirată sau neautorizată.");
					return;
				}
				const errorMessage =
					err.response?.data?.message ||
					err.response?.data?.error ||
					`Eroare ${status}`;
				setError(errorMessage);
			} else {
				setError("Eroare la preluarea evenimentelor.");
			}
		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		const storedBirthDate = localStorage.getItem("userDataNastere");
		if (storedBirthDate) {
			setBirthDate(storedBirthDate.split("T")[0]);
		}
		setCurrentDateISO(new Date().toISOString());
		fetchEvenimente();
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [navigate]);

	useEffect(() => {
		if (showForm) {
			document.body.style.overflow = "hidden";
		} else {
			document.body.style.overflow = "auto";
		}
	}, [showForm]);

	const handleEventCreated = async () => {
		setShowForm(false);
		await fetchEvenimente();
	};

	const handleFormError = (error: string | null) => {
		// Handle form errors if needed
		console.error("Form error:", error);
	};

	if (loading && evenimente.length === 0 && !showForm) {
		return (
			<div className={styles.container}>
				<p>Se încarcă evenimentele...</p>
			</div>
		);
	}

	if (error && !showForm) {
		return (
			<div className={styles.timelinePageContainer}>
				<p className={styles.errorText}>{error}</p>
			</div>
		);
	}

	return (
		<div className={styles.timelinePageContainer}>
			<div className={styles.topBar}>
				<h1 className={styles.title}>Timeline Personal</h1>
				<button
					onClick={() => navigate("/")}
					className={styles.actionButton}
				>
					Înapoi la Acasă
				</button>
			</div>

			<button
				onClick={() => setShowForm(!showForm)}
				className={styles.toggleFormButton}
			>
				{showForm ? "Anulează Adăugarea" : "Adaugă Eveniment Nou"}
			</button>

			{showForm && (
				<>
					<div
						className={styles.overlay}
						onClick={() => setShowForm(false)}
					/>
					<div className={styles.formModal}>
						<EventForm
							onEventCreated={handleEventCreated}
							onError={handleFormError}
							onCancel={() => setShowForm(false)}
						/>
					</div>
				</>
			)}

			<TimelineContainer
				evenimente={evenimente}
				birthDate={birthDate}
				currentDateISO={currentDateISO}
				loading={loading}
			/>

			<p className={styles.infoText}>
				Evenimentele private sunt afișate doar dacă ai participat la
				ele.
			</p>
		</div>
	);
};

export default TimelinePage;
