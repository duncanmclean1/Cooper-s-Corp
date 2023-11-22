import {Container, Box, Typography, TextField, Button} from "@material-ui/core";
import { Alert } from "@mui/material";
import {useState } from "react";
import {useNavigate} from "react-router-dom";
export default function Login() {
      const [employeeId, setEmployeeId] = useState({employeeId:""});
      const [password, setPassword] = useState({password: ""});
      const [data, setData] = useState(DEFAULT_DATA_INFO);
      const navigate = useNavigate();
      const handleMessage = () => {
        if (data.alertType === "success") {
          return <Alert severity = {data.alertType}>{data.alertMessage}</Alert>;
        }
        else if (data.alertType === "error") {
          return <Alert severity = {data.alertType}>{data.alertMessage}</Alert>;
        }
      }

      const handleSubmit = (event) => {
        event.preventDefault();
        const login = {
          "EMPLOYEE_ID": Number(employeeId.employeeId),
          "PASSWORD": password.password
        };
        fetch('/api/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(login),
        })
        .then((response) => response.json())
        .then((login) => {
          if (login.isAuthorized === true)
          {
            setData({
              alertType: "success",
              alertMessage: "Successfully logged in.",
            });
            navigate("/dashboard");
          }
          else {
            setData({
              alertType: "error",
              alertMessage: "Enter correct employee id or password",
             });
          }
        })
        .catch((error) => {
           setData({
            alertType: "error",
            alertMessage: error,
           })
        });
      };
      
      const handleEmployeeId = employeeId => event => {
        setEmployeeId({...employeeId, [employeeId]: event.target.value})
      }
      
      const handlePassword = password => event => {
        setPassword({...password, [password]: event.target.value})
      }

      return (
        <Container component="main" maxWidth="sm">
          <Box
            sx={{  
              marginTop: 8,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
            }}
          >
            <Typography component="h1" variant="h5">
              Login To Coopers-Corp
            </Typography>
            <Box component="form" noValidate sx={{ mt: 1 }}>
              <TextField
                margin="normal"
                required={true}
                fullWidth={true}
                id="employee id"
                label="Employee-Id"
                name="employee id"
                autoComplete="employee id"
                variant="outlined"
                value={employeeId.employeeId}
                onChange={handleEmployeeId("employeeId")}
              />
              <TextField
                margin="normal"
                required={true}
                fullWidth={true}
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="current-password"
                variant="outlined"
                value={password.password}
                onChange={handlePassword("password")}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
               onClick={handleSubmit}
                href="/dashboard"
              >
                Sign In
              </Button>
            </Box>
            {handleMessage()}
          </Box>
        </Container>
      );
    }

const DEFAULT_DATA_INFO = {
  alertType: "",
  alertMessage: "",
}