import {Typography, FormControlLabel, Checkbox, Grid, Link} from "@material-ui/core";
import Box from '@mui/system/Box';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Container from '@mui/material/Container';

export default function AddEmployee() {
      return ( 
      <Container maxWidth='sm' justifySelf='center'>
      <Box display = 'flex' alignItems='center' flexDirection='column' gap={2} marginTop={5} bgcolor='#CAF0F8' justifySelf='center' padding='20px'>
      <TextField
      required 
      id = "employeeId"
      label="Employee ID"
     />
      <TextField
      required 
      id = "firstName"
      label="First Name"
     />
      <TextField
      required 
      id = "lastName"
      label="Last Name"
     />
      <TextField
      required 
      id = "password"
      label="Password"
     />
    <Button className="custom-button" variant="outlined">
      Submit
     </Button>
   </Box>
   </Container>
      );
    }
