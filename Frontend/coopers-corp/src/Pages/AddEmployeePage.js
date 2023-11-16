
import {FormControlLabel, Checkbox, Grid, Link, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField} from "@material-ui/core";
import Box from '@mui/system/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import React, {useState} from "react";
import Modal from '@mui/material/Modal';
import Typography from '@mui/material/Typography';
export default function AddEmployee() {
    const [openPopup, setOpenPopup] = useState(false);
    const [employeeId, setEmployeeId] = useState({employeeID: ""});
    const arr = ["1111", "2222", "3333"];
    const handleSubmit = (event) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        console.log(data.get("employeeId"))

      };
      const handleClose = () => {
        setOpenPopup(false);
      };
      const handleClick = (event) => {
        event.preventDefault();
        setOpenPopup(true);
      }
      const handleEmployeeId = employeeID => event => {
        setEmployeeId({...employeeId, [employeeID]: event.target.value})
      }
      const error = arr.includes(employeeId.employeeID); 
  return ( 
      <Container maxWidth='sm' justifySelf='center'>
      <Box component = 'form' onClick = {handleSubmit} display = 'flex' alignItems='center' flexDirection='column' gap={2} marginTop={5} justifySelf='center' padding='20px'>
      <TextField
      required 
      id = "employeeId"
      name = "employeeId"
      label="Employee ID"
      value = {employeeId.employeeId}
      onChange={handleEmployeeId("employeeID")}
      helperText={error ? "Employee ID already exists" : ""}
      error={error}
      autoFocus
     />
      <TextField
      required 
      id = "firstName"
      name = "firstName"
      label="First Name"
     />
      <TextField
      required 
      id = "lastName"
      name = "lastName"
      label="Last Name"
     />
      <TextField
      required 
      id = "password"
      name = "password"
      label="Password"
     />
    <Button type = "submit" variant="outlined" onClick = {handleClick}>
      Submit
     </Button>
   </Box>
   <Dialog
        open={openPopup}
        onClose={handleClose}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">
          {"Use Google's location service?"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Let Google help apps determine location. This means sending anonymous
            location data to Google, even when no apps are running.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Disagree</Button>
          <Button onClick={handleClose} autoFocus>
            Agree
          </Button>
        </DialogActions>
      </Dialog>
   </Container>
  );
}
