import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import DashboardPage from "./Pages/DashboardPage";
import AddEmployeePage from "./Pages/AddEmployeePage";
import EditEmployeePage from "./Pages/EditEmployeePage";
import EditComponent from "./Pages/EditComponent";
export default function App()
{
  return(
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path= "/addEmployee" element={<AddEmployeePage />} />
        <Route path= "/editEmployee" element={<EditEmployeePage />}/>
        <Route path= "/editComponent" element={<EditComponent />}/>
      </Routes>
    </Router>
  );
}

