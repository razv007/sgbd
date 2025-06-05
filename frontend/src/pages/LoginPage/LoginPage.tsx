import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./LoginPage.module.css";

const LoginPage: React.FC = () => {
	const navigate = useNavigate();
	const [username, setUsername] = useState("");
	const [password, setPassword] = useState("");
	const [error, setError] = useState<string | null>(null);
	const [loading, setLoading] = useState(false);

	const handleSubmit = async (event: React.FormEvent) => {
		event.preventDefault();
		setError(null);
		setLoading(true);

		const loginPayload = {
			numeUtilizator: username,
			parola: password,
		};

		try {
			const response = await fetch(
				"http://localhost:8081/api/auth/signin",
				{
					method: "POST",
					headers: {
						"Content-Type": "application/json",
					},
					body: JSON.stringify(loginPayload),
				}
			);

			if (response.ok) {
				const data = await response.json();
				console.log("Login successful:", data);
				// Store JWT token (assuming the token is in data.token or data.accessToken)
				const token = data.token || data.accessToken;
				if (token) {
					localStorage.setItem("userToken", token);
					console.log("Token stored in localStorage");

					// Store user's birth date if available
					if (data.dataNastere) {
						localStorage.setItem(
							"userDataNastere",
							data.dataNastere
						);
						console.log(
							"User birth date stored in localStorage:",
							data.dataNastere
						);
					} else {
						console.warn(
							"No birth date received from server for the user."
						);
					}
				} else {
					console.warn("No token received from server");
				}
				// Navigate to the dashboard page
				navigate("/"); // Navigate to the main application page (root)
			} else {
				// Try to parse error response as JSON, otherwise use text
				let errorResponseMessage =
					"Login failed. Please check your credentials.";
				try {
					const errorData = await response.json();
					errorResponseMessage =
						errorData.message ||
						errorData.error ||
						errorResponseMessage;
				} catch (e) {
					// If parsing JSON fails, try to get text, or use default message
					const textError = await response.text();
					errorResponseMessage = textError || errorResponseMessage;
				}
				setError(errorResponseMessage);
			}
		} catch (err) {
			console.error("Login API error:", err);
			setError(
				"A network error occurred. Please check your connection or if the server is running."
			);
		} finally {
			setLoading(false);
		}
	};

	return (
		<div className={styles.loginForm}>
			<h1 className={styles.appTitle}>Arhiva Digitală</h1>
			<h2>Login</h2>
			<form onSubmit={handleSubmit}>
				<div className={styles.inputGroup}>
					<label htmlFor="username">Username:</label>
					<input
						type="text"
						id="username"
						value={username}
						onChange={(e) => setUsername(e.target.value)}
						required
					/>
				</div>
				<div className={styles.inputGroup}>
					<label htmlFor="password">Password:</label>
					<input
						type="password"
						id="password"
						value={password}
						onChange={(e) => setPassword(e.target.value)}
						required
					/>
				</div>
				{error && <p className={styles.error}>{error}</p>}
				<button
					type="submit"
					disabled={loading}
					className={styles.button}
				>
					{loading ? "Logging in..." : "Login"}
				</button>
			</form>
			<Link to="/signup" className={styles.link}>
				Înregistrează-te
			</Link>
		</div>
	);
};

export default LoginPage;
