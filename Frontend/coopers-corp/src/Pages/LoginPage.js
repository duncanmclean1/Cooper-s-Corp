import {Container, Box, Typography, TextField, Button, Grid, Link} from "@material-ui/core";
import { useState } from "react";
export default function Login() {
      const [employeeId, setEmployeeId] = useState({employeeId:""});
      const [password, setPassword] = useState({password: ""});
  
      // const validateForm = () =>
      // {
      //   return employeeId.employeeId > 0 && password.password > 0;
      // }
      
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
          console.log("Login:", login);
        })
        .catch((error) => {
          console.error(error)
        });
      };
      
      const handleEmployeeId = employeeId => event => {
        setEmployeeId({...employeeId, [employeeId]: event.target.value})
      }
      
      const handlePassword = password => event => {
        setPassword({...password, [password]: event.target.value})
      }

      return (
        <Container component="main" maxWidth="xs">
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
            <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="employee id"
                label="Employee Id"
                name="employee id"
                autoComplete="employee id"
                autoFocus
                value={employeeId.employeeId}
                onChange={handleEmployeeId("employeeId")}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="current-password"
                value={password.password}
                onChange={handlePassword("password")}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                onClick={handleSubmit}
                // disabled={!validateForm()}
              >
                Sign In
              </Button>
              <Grid container className="center">
                <Grid item>
                  <Link href="/addEmployee" variant="body2">
                    {"Don't have an account? Sign Up"}
                  </Link>
                </Grid>
              </Grid>
            </Box>
          </Box>
        </Container>
      );
    }