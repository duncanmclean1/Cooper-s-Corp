import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import DashboardPage from "./Pages/DashboardPage";
import AddEmployeePage from "./Pages/AddEmployeePage";

export default function App()
{
  return(
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path= "/addEmployee" element={<AddEmployeePage />}/>
      </Routes>
    </Router>
  );
}

