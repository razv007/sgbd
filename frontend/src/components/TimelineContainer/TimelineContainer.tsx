import React, { useMemo } from "react";
import { type Eveniment } from "../../types/eveniment";
import TimelineEvent from "../TimelineEvent/TimelineEvent";
import styles from "./TimelineContainer.module.scss";

interface TimelineContainerProps {
	evenimente: Eveniment[];
	birthDate: string | null;
	currentDateISO: string;
	loading: boolean;
}

const TimelineContainer: React.FC<TimelineContainerProps> = ({
	evenimente,
	birthDate,
	currentDateISO,
	loading,
}) => {
	const allDisplayableItems = useMemo(() => {
		const items: Eveniment[] = [...evenimente];

		if (birthDate) {
			items.push({
				id: "birth-marker",
				titlu: "Data Nașterii",
				descriere: "Aici începe povestea ta!",
				dataInceput: birthDate,
				dataSfarsit: "",
				locatie: "",
				categorie: "",
				vizibilitate: "PUBLIC",
				type: "birth",
			});
		}

		items.push({
			id: "today-marker",
			titlu: "Astăzi",
			descriere: "Ziua curentă.",
			dataInceput: currentDateISO,
			dataSfarsit: "",
			locatie: "",
			categorie: "",
			vizibilitate: "PUBLIC",
			type: "today",
		});

		return items;
	}, [evenimente, birthDate, currentDateISO]);

	const sortedTimelineItems = useMemo(() => {
		return allDisplayableItems
			.sort(
				(a, b) =>
					new Date(a.dataInceput).getTime() -
					new Date(b.dataInceput).getTime()
			)
			.map((item, _) => {
				let sideClass = "";
				let isOnAxis = false;

				if (item.type === "birth" || item.type === "today") {
					isOnAxis = true;
				} else {
					const regularEvents = allDisplayableItems.filter(
						(e) => e.type !== "birth" && e.type !== "today"
					);
					const regularEventIndex = regularEvents.findIndex(
						(e) => e.id === item.id
					);
					sideClass = regularEventIndex % 2 === 0 ? "left" : "right";
				}

				return (
					<TimelineEvent
						key={item.id}
						event={item}
						sideClass={sideClass as "" | "left" | "right"}
						isOnAxis={isOnAxis}
					/>
				);
			});
	}, [allDisplayableItems]);

	if (loading && sortedTimelineItems.length === 0) {
		return (
			<div className={styles.verticalTimelineContainer}>
				<p className={styles.loadingMessage}>Se încarcă evenimentele...</p>
			</div>
		);
	}

	return (
		<div className={styles.verticalTimelineContainer}>
			{sortedTimelineItems.length > 0 ? (
				sortedTimelineItems
			) : (
				<p className={styles.noEventsMessage}>
					Nu există evenimente de afișat.
				</p>
			)}
		</div>
	);
};

export default TimelineContainer;