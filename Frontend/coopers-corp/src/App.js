import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import DashboardPage from "./Pages/DashboardPage";
import AddEmployeePage from "./Pages/AddEmployeePage";
import EditEmployeePage from "./Pages/EditEmployeePage";
import CustomerDetails from "./Pages/CustomerDetails";
import ViewOrdersPage from "./Pages/ViewOrdersPage";
import AddItems from "./Pages/AddItems";
import EditComponent from "./Pages/EditComponent";
export default function App()
{
  return(
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/dashboard/:employeeId" element={<DashboardPage />} />
        <Route path= "/addEmployee" element={<AddEmployeePage />} />
        <Route path= "/editEmployee" element={<EditEmployeePage />}/>
        <Route path="/vieworder" element={<ViewOrdersPage />} />
        <Route path="/customerDetails/:employeeId" element={<CustomerDetails />} />
        <Route path="/additems/:employeeId/:orderNumber" element={<AddItems />} />
        <Route path="/editEmployee/editComponent" element={<EditComponent />} />
      </Routes>
    </Router>
  );
}

