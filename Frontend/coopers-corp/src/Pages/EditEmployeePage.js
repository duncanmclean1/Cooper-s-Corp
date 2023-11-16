
import {FormControlLabel, Checkbox, Grid, Link, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField} from "@material-ui/core";
import Box from '@mui/system/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import React, {useState} from "react";
//after adding a new employee set status to active 
export default function EditEmployee() {
    const [openPopup, setOpenPopup] = useState(false);
    const [errorPopup, setErrorPopup] = useState(false);
    const [employeeId, setEmployeeId] = useState({employeeId: ""});
    const [firstName, setFirstName] = useState({firstName: ""});
    const [lastName, setLastName] = useState({lastName: ""});
    const [password, setPassword] = useState({password: ""});

    const arr = ["1111", "2222", "3333"];
    const handleClose = () => {
        setOpenPopup(false);
      };
      const handleClick = (event) => {
        event.preventDefault();
        if (employeeId.employeeId == "" || firstName.firstName == "" || lastName.lastName == "" || password.password == "") {
          setErrorPopup(true);
        }
        else {
          console.log({"EMPLOYEE_ID": employeeId.employeeId, "FIRST_NAME": firstName.firstName, "LAST_NAME":lastName.lastName, "PASSWORD": password.password })                                                                 
          setOpenPopup(true);
        }
      }
      const handleErrorClose = (event) => {
        event.preventDefault();
        setErrorPopup(false)
      }
      const handleSubmit = (event) => {
        event.preventDefault();
      }
      const handleEmployeeId = employeeId => event => {
        setEmployeeId({...employeeId, [employeeId]: event.target.value})
      }
      const handleFirstName = firstName => event => {
        setFirstName({...firstName, [firstName]: event.target.value})
      }      
      const handleLastName = lastName => event => {
        setLastName({...lastName, [lastName]: event.target.value})
      }      
      const handlePassword = password => event => {
        setPassword({...password, [password]: event.target.value})
      }
      const error = arr.includes(employeeId.employeeId); 
  return ( 
      <Container maxWidth='sm'>
      <Box component = 'form' display = 'flex' alignItems='center' flexDirection='column' gap={2} marginTop={5} padding='20px'>
      <TextField
      required 
      id = "employeeId"
      name = "employeeId"
      label="Employee ID"
      value = {employeeId.employeeId}
      onChange={handleEmployeeId("employeeId")}
      helperText={error ? "Employee ID already exists" : ""}
      error={error}
      autoFocus
     />
      <TextField
      required 
      value = {firstName.firstName}
      onChange={handleFirstName("firstName")}
      id = "firstName"
      name = "firstName"
      label="First Name"
     />
      <TextField
      required
      value = {lastName.lastName} 
      onChange={handleLastName("lastName")}
      id = "lastName"
      name = "lastName"
      label="Last Name"
     />
      <TextField
      required 
      value = {password.password}
      onChange = {handlePassword("password")}
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
          {"Confirmation"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Are you sure you would like to submit?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={handleClose} autoFocus href="/dashboard">
            Submit
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog
        open={errorPopup}
        onClose={handleErrorClose}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">
          {"!"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Please fill in all fields
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleErrorClose}>OK</Button>
        </DialogActions>
      </Dialog>
   </Container>
  );
}
