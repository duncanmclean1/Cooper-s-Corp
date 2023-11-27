import React, {useState, useEffect} from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Button from '@mui/material/Button';
import Box from '@mui/system/Box';
import Menu from '@mui/material/Menu';
import EditComponent from './EditComponent';
import Select from '@mui/material/Select';
import {MenuButton} from '@mui/base/MenuButton';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import FormControl from '@mui/material/FormControl';
import {FormControlLabel, Checkbox, Grid, Link, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField} from "@material-ui/core";
function createData(name, calories, fat, carbs, protein) {
  return { name, calories, fat, carbs, protein };
}



export default function EditEmployeePage() {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);
  const menuOpen = Boolean(anchorEl);
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState([]);
  const [data, setData] = useState({EMPLOYEE_ID: "", FIRST_NAME: "", LAST_NAME: "", STATUS: ""})
  const [open, setOpen] = useState(false);
  const [firstName, setFirstName] = useState({FIRST_NAME: ""});
  const [lastName, setLastName] = useState({LAST_NAME: ""});
  const [status, setStatus] = useState({STATUS: ""});
  const handleFirstName = firstName => event => {
    setFirstName({...firstName, [firstName]: event.target.value})
  };      
  const handleLastName = lastName => event => {
    setLastName({...lastName, [lastName]: event.target.value})
  };      

  const {employeeId} = useParams();

  const handleSubmit = (event) => {
    event.preventDefault();
    console.log("whatt")
        const employeeUpdate = {
        "EMPLOYEE_ID": data.EMPLOYEE_ID,   
        "FIRST_NAME": firstName.FIRST_NAME === "" ? data.FIRST_NAME : firstName.FIRST_NAME,
        "LAST_NAME": lastName.LAST_NAME === "" ? data.LAST_NAME : lastName.LAST_NAME,
        "STATUS": status.STATUS === "" ? data.STATUS: status.STATUS};
        fetch('/api/updateemployee', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(employeeUpdate),
        })
          .then((response) => response.json())
          .then((newEmployee) => {
            console.log('New employee:', newEmployee);
          })
          .catch((e) => {
            console.error(e);
          });
          getData();
          setOpen(false);
  };
    const getData = async () => {
    setLoading(true);
    try {
    const actualData = await fetch('api/showemployees');
    const result = await actualData.json();
    setRows(result);
    console.log("hi");
    console.log(rows);
    } catch (error) {
      console.log("error");
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {fetch('/api/showemployees', {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json',
    },
    body: JSON.stringify(),
})
.then((response) => response.json())
.then((response) => {
    setRows(response);
})
  }, []);
    const handleClickButton = row => {
      setData(prevEmployeeInfo => ({...prevEmployeeInfo, EMPLOYEE_ID: row.EMPLOYEE_ID, FIRST_NAME: row.FIRST_NAME, LAST_NAME: row.LAST_NAME, STATUS: row.STATUS}));
      console.log("on click", data);
      setOpen(true);
    }
    const handleClose = () => {
      setOpen(false);
    };

    const handleStatusBtn = (event) => {
      setStatus({STATUS: event.target.value})
      console.log(status.STATUS);
    }
  const DisplayData = rows.map((row) => {
    return(<TableRow>
      <TableCell> {row.EMPLOYEE_ID}</TableCell>
      <TableCell> {row.FIRST_NAME}</TableCell>
      <TableCell> {row.LAST_NAME}</TableCell>
      <TableCell> {row.STATUS === "false" ? "Inactive" : "Active"}</TableCell>
      <TableCell align='center'><Button variant='outlined' onClick={() => handleClickButton(row)}>Edit</Button></TableCell>
    </TableRow>)
  })
  return (
    <div>
    <Table>
        <TableHead>
            <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>FIRST NAME</TableCell>
            <TableCell>LAST NAME</TableCell>
            <TableCell>STATUS</TableCell>
            <TableCell align='center'>EDIT</TableCell>
            </TableRow>
        </TableHead>
        <TableBody>
         
            {DisplayData}
            
        </TableBody>
    </Table>
    <Button onClick={()=>navigate(`/addemployee/${employeeId}`)}>Add new Employee</Button>
    <Button onClick={()=>navigate(`/dashboard/${employeeId}`)}>Back</Button>
    <Dialog
        open={open}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">
          {"Edit Employee"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
          Employee ID: {data.EMPLOYEE_ID}
          </DialogContentText>
          <Box component='form' gap={10} display='flex'>
            <TextField name= "FIRST_NAME" label = "First Name" defaultValue = {data.FIRST_NAME} onChange = {handleFirstName("FIRST_NAME")}></TextField>
            <TextField name = "LAST_NAME" label = "Last Name" defaultValue = {data.LAST_NAME} onChange = {handleLastName("LAST_NAME")}></TextField>
            <FormControl required sx={{ m: 1, minWidth: 120 }}>
            <InputLabel id="status_id">Status</InputLabel>
            <Select labelId= "status_id" label= "Status" onChange={handleStatusBtn} defaultValue={data.STATUS} autowidth>
              <MenuItem value={true}>Active</MenuItem>
              <MenuItem value={false}>Inactive</MenuItem>
            </Select>
            </FormControl>
            </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button type = "submit" onClick = {handleSubmit}>
            Submit
          </Button>
        </DialogActions>
      </Dialog></div>
  );
}