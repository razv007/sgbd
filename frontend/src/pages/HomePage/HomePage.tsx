import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./HomePage.module.css";
import { Link } from "react-router-dom"; // Importă Link pentru navigare // We'll create this CSS module next
import Modal from "../../components/Modal"; // Importă componenta Modal
import UserProfileForm from "../../components/UserProfileForm"; // Importă formularul de profil

interface CurrentUser {
    id: number;
    numeUtilizator: string;
    email: string;
    numeComplet: string;
    roles: string[];
}

const HomePage: React.FC = () => {
	const navigate = useNavigate();
	const userToken = localStorage.getItem("userToken");
    const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [isProfileModalOpen, setIsProfileModalOpen] = useState<boolean>(false);

    useEffect(() => {
        const fetchCurrentUser = async () => {
            if (userToken) {
                try {
                    const response = await fetch("http://localhost:8081/api/users/me", {
                        method: "GET",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${userToken}`,
                        },
                    });
                    if (response.ok) {
                        const data: CurrentUser = await response.json();
                        setCurrentUser(data);
                    } else {
                        console.error("Failed to fetch user data:", response.status);
                        // Handle token expiration or other errors, e.g., redirect to login
                        if (response.status === 401 || response.status === 403) {
                            // localStorage.removeItem("userToken"); // Optional: clear invalid token
                            // navigate("/login");
                        }
                    }
                } catch (error) {
                    console.error("Error fetching user data:", error);
                }
            }
            setLoading(false);
        };

        fetchCurrentUser();
    }, [userToken, navigate]);

	const handleLogout = () => {
		localStorage.removeItem("userToken");
		// Optionally, inform the backend about logout if needed
		navigate("/login");
	};

	return (
		<div className={styles.homeContainer}>
			<header className={styles.header}>
				<h1>Arhiva Digitală</h1>
				<div className={styles.userActions}> {/* Container pentru butoane/link-uri utilizator */}
					{userToken && (
						<>
							<button onClick={() => setIsProfileModalOpen(true)} className={styles.profileButton}>Profil</button>
							<button
								onClick={handleLogout}
								className={styles.logoutButton}
							>
								Logout
							</button>
						</>
					)}
				</div>
			</header>
			<main className={styles.mainContent}>
				<h2>Bun venit{currentUser ? `, ${currentUser.numeComplet}` : ""}!</h2>
                {userToken ? (
                    <>
                        {loading && <p>Se încarcă datele utilizatorului...</p>}
                        {!loading && currentUser && (
                            <p>Sunteți autentificat în aplicație ca {currentUser.numeUtilizator}.</p>
                        )}
                        {!loading && !currentUser && (
                            <p>Sunteți autentificat, dar nu am putut încărca detaliile utilizatorului.</p>
                        )}
                        <Link to="/timeline" className={styles.actionButton}>
                            Vezi Timeline
                        </Link>
                    </>
                ) : (
					// Am păstrat structura originală pentru partea de "neautentificat",
					// dar am înlocuit <a> cu <Link> pentru consistență și o mai bună practică în React Router
					<p>
						Vă rugăm să vă <Link to="/login">autentificați</Link>{" "}
						sau să vă <Link to="/signup">înregistrați</Link> pentru
						a accesa conținutul.
					</p>
				)}{" "}
				{/* Aici se poate adauga mai mult continut specific paginii principale */}
			</main>
			<footer className={styles.footer}>
				<p>
					&copy; {new Date().getFullYear()} Arhiva Digitală. Toate
					drepturile rezervate.
				</p>
			</footer>

			{/* Modal pentru actualizarea profilului */}
			<Modal isOpen={isProfileModalOpen} onClose={() => setIsProfileModalOpen(false)} title="Actualizează Profilul">
				<UserProfileForm />
			</Modal>
		</div>
	);
};

export default HomePage;
