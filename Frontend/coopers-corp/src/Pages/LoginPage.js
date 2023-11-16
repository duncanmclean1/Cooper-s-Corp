import {Container, Box, Typography, TextField, Button, Grid, Link} from "@material-ui/core";
import { useState } from "react";
export default function Login() {
      const [employeeId, setEmployeeId] = useState("");
      const [password, setPassword] = useState("");

      const validateForm = () =>
      {
        return employeeId > 0 && password.length > 0;
      }
      
      const handleSubmit = (event) => {
        event.preventDefault();
        const example = {
          method: 'POST',
          headers: { 'Content-Type': 'application/json'},
          body: JSON.stringify({title: 1})
        };
        fetch('/api/login', example)
        .then(response => {
            response.text();
        })
        .then(data => {
          console.log(data)
        })
      };
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
                value={employeeId}
                onChange={event => setEmployeeId(event.target.value)}
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
                value={password}
                onChange={event => setPassword(event.target.value)}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                onSubmit={handleSubmit}
                disabled={!validateForm()}
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