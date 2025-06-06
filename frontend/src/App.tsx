import {
	BrowserRouter as Router,
	Routes,
	Route,
	Navigate,
} from "react-router-dom";
import LoginPage from "./pages/LoginPage/LoginPage";
import SignupPage from "./pages/LoginPage/SignupPage";
import HomePage from "./pages/HomePage/HomePage";
import TimelinePage from "./pages/TimelinePage/TimelinePage"; // Importă noua pagină // Import HomePage
import "./App.css";

function App() {
	return (
		<Router>
			<Routes>
				<Route path="/login" element={<LoginPage />} />
				<Route path="/signup" element={<SignupPage />} />
				<Route path="/" element={<HomePage />} />{" "}
				{/* Add HomePage route */}
				<Route path="/timeline/:username" element={<TimelinePage />} />
				{/* Adaugă ruta pentru timeline */}
				<Route path="*" element={<Navigate to="/" replace />} />{" "}
				{/* Redirect any other path to HomePage (or login if not auth) */}
			</Routes>
		</Router>
	);
}

export default App;
