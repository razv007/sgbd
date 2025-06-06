import React from "react";
import { type Eveniment } from "../../types/eveniment";
import styles from "./TimelineEvent.module.scss";
import DocumentManager from "../DocumentManager/DocumentManager";

interface TimelineEventProps {
	event: Eveniment;
	sideClass: "left" | "right" | "";
	isOnAxis: boolean;
	onDelete?: (eventId: number) => void;
}

const TimelineEvent: React.FC<TimelineEventProps> = ({
	event,
	sideClass,
	isOnAxis,
	onDelete,
}) => {
	const formatDateTime = (
		dateString: string,
		type?: Eveniment["type"]
	): string => {
		if (!dateString) return "Data invalidă";
		const date = new Date(dateString);
		if (isNaN(date.getTime())) return "Data invalidă";

		if (type === "birth" || type === "today") {
			return date.toLocaleDateString("ro-RO", {
				year: "numeric",
				month: "long",
				day: "numeric",
			});
		}
		// Default for 'event' or unspecified type
		return date.toLocaleString("ro-RO", {
			year: "numeric",
			month: "long",
			day: "numeric",
			hour: "2-digit",
			minute: "2-digit",
		});
	};

	// Build CSS classes
	const containerClasses = [styles.verticalTimelineEvent];
	if (sideClass === "left") containerClasses.push(styles.eventLeft);
	if (sideClass === "right") containerClasses.push(styles.eventRight);
	if (isOnAxis) containerClasses.push(styles.eventOnAxis);

	// Marker classes
	const markerClasses = [styles.eventMarker];
	if (event.type === "birth") markerClasses.push(styles.birthMarker);
	if (event.type === "today") markerClasses.push(styles.todayMarker);

	// Content classes
	const contentClasses = [styles.verticalTimelineEventContent];
	if (event.type === "birth") contentClasses.push(styles.birthEventContent);
	if (event.type === "today") contentClasses.push(styles.todayEventContent);

	const username = localStorage.getItem("nume");

	return (
		<div className={containerClasses.join(" ")}>
			<div className={markerClasses.join(" ")}></div>
			<div className={styles.eventDateOnAxis}>
				{formatDateTime(event.dataInceput, event.type)}
			</div>
			{event.type !== "birth" && event.type !== "today" && (
				<div className={contentClasses.join(" ")}>
					<strong>{event.titlu}</strong>
					{event.numeUtilizator && (
						<p className={styles.eventOwner}>
							<em>by {event.numeUtilizator === username ? 'you' : event.numeUtilizator }</em>
						</p>
					)}

					<p>
						{event.descriere ||
							(event.type === "event"
								? "Nicio descriere adăugată."
								: "")}
					</p>
					{event.type === "event" && onDelete && (
						<button
							className={styles.deleteButton}
							onClick={() => onDelete(Number(event.id))}
							title="Șterge eveniment"
						>
							🗑️
						</button>
					)}

					{event.type === "event" && (
						<>
							<p className={styles.eventDate}>
								<strong>Început:</strong>{" "}
								{formatDateTime(event.dataInceput, event.type)}
							</p>
							{event.dataSfarsit && (
								<p className={styles.eventDate}>
									<strong>Sfârșit:</strong>{" "}
									{formatDateTime(
										event.dataSfarsit,
										event.type
									)}
								</p>
							)}
							{event.locatie && (
								<p className={styles.eventLocation}>
									<strong>Locație:</strong> {event.locatie}
								</p>
							)}
							{event.categorie && (
								<p className={styles.eventCategory}>
									<strong>Categorie:</strong>{" "}
									{event.categorie}
								</p>
							)}
						</>
					)}
					{event.type === "event" && (
						<div className={styles.eventArtifacts}>
							<DocumentManager eventId={event.id} />
						</div>
					)}
				</div>
			)}
		</div>
	);
};

export default TimelineEvent;
