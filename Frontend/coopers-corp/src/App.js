import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import DashboardPage from "./Pages/DashboardPage";

export default function App()
{
  return(
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
      </Routes>
    </Router>
  );
}

