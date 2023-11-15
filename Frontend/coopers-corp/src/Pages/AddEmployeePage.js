
import {FormControlLabel, Checkbox, Grid, Link} from "@material-ui/core";
import Box from '@mui/system/Box';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Container from '@mui/material/Container';
import React, {useState} from "react";
import Modal from '@mui/material/Modal';
import Typography from '@mui/material/Typography';
export default function AddEmployee() {
    const [openPopup, setOpenPopup] = useState(false);
    const arr = [1111, 2222, 3333];
    const handleSubmit = (event) => {
        event.preventDefault();
        setOpenPopup(true);
      };
  return ( 
      <Container maxWidth='sm' justifySelf='center'>
      <Box display = 'flex' alignItems='center' flexDirection='column' gap={2} marginTop={5} bgcolor='#CAF0F8' justifySelf='center' padding='20px'>
      <TextField
      required 
      id = "employeeId"
      name = "employeeId"
      label="Employee ID"
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
    <Button type = "submit" variant="outlined">
      Submit
     </Button>
   </Box>
   <Modal
        open={openPopup}
        aria-labelledby="modal-modal-title"
        aria-describedby="modal-modal-description"
      >
        <Box display = 'flex' justifySelf='center' alignItems='center' bgcolor="blue">
          <Typography id="modal-modal-title" variant="h6" component="h2">
            Text in a modal
          </Typography>
          <Typography id="modal-modal-description" sx={{ mt: 2 }}>
            Duis mollis, est non commodo luctus, nisi erat porttitor ligula.
          </Typography>
        </Box>
      </Modal>
   </Container>
  );
}
