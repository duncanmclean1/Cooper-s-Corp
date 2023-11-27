
import {FormControlLabel, Checkbox, Grid, Link, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField, Typography} from "@material-ui/core";
import Box from '@mui/system/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import React, {useState} from "react";
import { useNavigate } from "react-router-dom";
//after adding a new employee set status to active 
export default function AddEmployee() {
    const [employeeId, setEmployeeId] = useState();
    const [success, setSuccess] = useState(false);
    const [openPopup, setOpenPopup] = useState(false);
    const [errorPopup, setErrorPopup] = useState(false);
    const [firstName, setFirstName] = useState({firstName: ""});
    const [lastName, setLastName] = useState({lastName: ""});
    const [password, setPassword] = useState({password: ""});
    const navigate = useNavigate();
    const completed = () => {
      setSuccess(true);
    }
    const completedClose = () => {
      setSuccess(false);
    }
    const handleClose = () => {
        setOpenPopup(false);
      };
      const handleClick = (event) => {
        event.preventDefault();
        if (firstName.firstName == "" || lastName.lastName == "" || password.password == "") {
          setErrorPopup(true);
        }
        else {
          console.log({"FIRST_NAME": firstName.firstName, "LAST_NAME":lastName.lastName, "PASSWORD": password.password})                                                                 
          setOpenPopup(true);
        }
      }
      const handleErrorClose = (event) => {
        event.preventDefault();
        setErrorPopup(false)
      }
      const handleConfirmSubmit = (event) => {
        event.preventDefault();
        const newEmployee = {  
        "FIRST_NAME": firstName.firstName,
        "LAST_NAME": lastName.lastName,
        "PASSWORD": password.password};
        fetch('/api/addemployee', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(newEmployee),
        })
          .then((response) => response.json())
          .then((newEmployee) => {
            console.log('New employee:', newEmployee);
            console.log("the" + newEmployee.EMPLOYEE_ID)
            setEmployeeId(newEmployee.EMPLOYEE_ID)
          })
          .catch((e) => {
            console.error(e);
          });
        setSuccess(true)
        //navigate("/dashboard")
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
     
  return ( 
      <Container maxWidth='sm'>
      <Box component = 'form' display = 'flex' alignItems='center' flexDirection='column' gap={2} marginTop={5} padding='20px'>
  
      <TextField
      required 
      value = {firstName.firstName}
      onChange={handleFirstName("firstName")}
      id = "firstName"
      name = "firstName"
      label="First Name"
      autoFocus
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
          <Button onClick={handleConfirmSubmit} autoFocus>
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
      <Dialog open={success} onClose={completedClose}> 
      <DialogContent> 
        <Typography variant="subtitle1">Your unique Employee ID: </Typography>
        </DialogContent>   
        <DialogActions>
          <Button onClick={completedClose}>OK</Button>
        </DialogActions>
    </Dialog>
   </Container>
  );
}
