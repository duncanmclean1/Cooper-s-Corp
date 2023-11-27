import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import LoginPage from "./Pages/LoginPage";
import DashboardPage from "./Pages/DashboardPage";
import AddEmployeePage from "./Pages/AddEmployeePage";
import EditEmployeePage from "./Pages/EditEmployeePage";
import CustomerDetails from "./Pages/CustomerDetails";
<<<<<<< HEAD
import AddItems from "./Pages/AddItems";
=======
import EditComponent from "./Pages/EditComponent";
>>>>>>> 91c445809938991af7a99fffa7a1463b39da150c
export default function App()
{
  return(
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/dashboard/:employeeId" element={<DashboardPage />} />
        <Route path= "/addEmployee" element={<AddEmployeePage />} />
        <Route path= "/editEmployee" element={<EditEmployeePage />}/>
<<<<<<< HEAD
        <Route path="/customerDetails/:employeeId" element={<CustomerDetails />} />
        <Route path="/additems/:orderNumber" element={<AddItems />} />
=======
        <Route path="/customerDetails" element={<CustomerDetails />} />
        <Route path="/editEmployee/editComponent" element={<EditComponent />} />
>>>>>>> 91c445809938991af7a99fffa7a1463b39da150c
      </Routes>
    </Router>
  );
}

