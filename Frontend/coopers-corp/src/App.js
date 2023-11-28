import {BrowserRouter as Router, Routes, Route, Switch} from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import DashboardPage from "./Pages/DashboardPage";
import AddEmployeePage from "./Pages/AddEmployeePage";
import EditEmployeePage from "./Pages/EditEmployeePage";
import CustomerDetails from "./Pages/CustomerDetails";
import ViewOrdersPage from "./Pages/ViewOrdersPage";
import AddItems from "./Pages/AddItems";
export default function App()
{
  return(
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/dashboard/:employeeId" element={<DashboardPage />} />
        <Route path= "/addEmployee/:employeeId" element={<AddEmployeePage />} />
        <Route path= "/editEmployee/:employeeId" element={<EditEmployeePage />}/>
        <Route path="/vieworder/:employeeId" element={<ViewOrdersPage />} />
        <Route path="/customerDetails/:employeeId" element={<CustomerDetails />} />
        <Route path="/additems/:employeeId/:orderNumber" element={<AddItems />} />
      </Routes>
    </Router>
  );
}

