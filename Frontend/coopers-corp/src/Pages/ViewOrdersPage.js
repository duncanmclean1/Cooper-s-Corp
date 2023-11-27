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
import {FormControlLabel, Checkbox, Link, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField} from "@material-ui/core";
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs from 'dayjs';

export default function ViewOrdersPage() {
  const navigate = useNavigate();
  const [sortBy, setSortBy] = useState();
  const [zipcode, setZipcode] = useState();
  const [rows, setRows] = useState([]);
  const [zipcodeRows, setZipcodeRows] = useState([]);
  const [zipcodeCount, setZipcodeCount] = useState("");
  const [employeeRows, setEmployeeRows] = useState([]);
  const [zipcodeData, setZipCodeData] = useState({"ORDER_NUMBER": "", "EMPLOYEE_ID": "", "FIRST_NAME": "", "LAST_NAME": "", "TIME": "", "PHONE_NUMBER": "", "ZIPCODE_KEY": ""});
  const [employeeData, setEmployeeData] = useState({"ORDER_NUMBER": "", "EMPLOYEE_ID": "", "FIRST_NAME": "", "LAST_NAME": "", "TIME": "", "PHONE_NUMBER": "", "ZIPCODE_KEY": ""});
  const [data, setData] = useState({"ORDER_NUMBER": "", "EMPLOYEE_ID": "", "FIRST_NAME": "", "LAST_NAME": "", "TIME": "", "PHONE_NUMBER": "", "ZIPCODE_KEY": ""});
  const [id, setId] = useState("");
  const [start, setStart] = useState();
  const [end, setEnd] = useState();
  const {employeeId} = useParams();
  const handleId = (event) => {
    event.preventDefault();
    setId(event.target.value);
    console.log("hi");
    console.log(id);
  };
  
  const multiOrder = (event) => {
    event.preventDefault();
    const formattedStartDate = (dayjs(start).format('YYYY-MM-DD HH:mm:ss'))
    const formattedEndDate = (dayjs(end).format('YYYY-MM-DD HH:mm:ss'))
    console.log("start" + formattedStartDate)
    console.log("end" + formattedEndDate)

    console.log("zipcode: " + zipcode)
    if (sortBy==="Zipcode") {
    const viewZipcodeOrder = {"ZIPCODE_KEY": zipcode, "TIME_BEGIN": formattedStartDate, "TIME_END": formattedEndDate};
    fetch('/api/viewordersbyzipcode', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(viewZipcodeOrder),
    })
    .then((response) => response.json())
    .then((viewOrder) => {
      console.log("view order" + viewOrder);
      console.log(viewOrder.ORDER_DETAILS_LIST);
      setRows(viewOrder.ORDER_DETAILS_LIST);

    })
  }
  else if(sortBy==="Employee") {
    const viewEmployeeOrder = {
      "EMPLOYEE_ID": employeeId,
      "TIME_BEGIN": formattedStartDate,
      "TIME_END": formattedEndDate
  };
    fetch('/api/viewordersbyemployee', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(viewEmployeeOrder),
    })
    .then((response) => response.json())
    .then((viewOrder) => {
      console.log(viewOrder);
      console.log(viewOrder.ORDER_DETAILS_LIST);
      setRows(viewOrder.ORDER_DETAILS_LIST);

    })
  }

  };
  const DisplayData = rows.map((row) => {
    return(<TableRow>
      <TableCell> {row.ORDER_NUMBER}</TableCell>
      <TableCell> {row.EMPLOYEE_ID}</TableCell>
      <TableCell> {row.FIRST_NAME}</TableCell>
      <TableCell> {row.LAST_NAME}</TableCell>
      <TableCell> {row.TIME}</TableCell>
      <TableCell> {row.PHONE_NUMBER}</TableCell>
      <TableCell> {row.ZIPCODE_KEY}</TableCell>
    </TableRow>)
  })
  const handleInput = (event) => {
    event.preventDefault();
    setZipcode(event.target.value);
    console.log(zipcode);
  }
  const handleSelect = (event) => {
    event.preventDefault();
    setSortBy(event.target.value);
  }  
  const handleSubmit = (event) => {
    event.preventDefault();
    console.log("id1: " + id)
    const viewOrder = {  
    "ORDER_NUMBER": id};
    fetch('/api/viewoneorder', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(viewOrder),
    })
      .then((response) => response.json())
      .then((viewOrder) => {
        if (viewOrder.EMPLOYEE_ID !== 0) {
        setData(viewOrder);
        }
        else {
          setData({"ORDER_NUMBER": "", "EMPLOYEE_ID": "", "FIRST_NAME": "", "LAST_NAME": "", "TIME": "", "PHONE_NUMBER": "", "ZIPCODE_KEY": ""});
        }
        console.log(viewOrder);
      })
      .catch((e) => {
        console.error(e);
      });
    //navigate("/dashboard")
  }
  return (
    <Box>
    <Grid container spacing={2} margin={5}>
      <Box component="section" sx={{p: 2, border:'1px solid grey'}}>
        <Typography variant="h1" >View Single Order:</Typography>
        <Typography variant="body1">Order ID:</Typography>
        <Box display="flex" flex-direction="row">
        <TextField variant="outlined" onChange={handleId}>Order ID: </TextField>
        <Button onClick={handleSubmit}>ENTER</Button>
        </Box>
        <Table>
        <TableHead>
          <TableRow>
            <TableCell>Order Number</TableCell>
            <TableCell>Employee ID</TableCell>
            <TableCell>First Name</TableCell>
            <TableCell>Last Name</TableCell>
            <TableCell>Time</TableCell>
            <TableCell>Phone Number</TableCell>
            <TableCell>Zipcode</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
          <TableCell>{data.ORDER_NUMBER}</TableCell>
          <TableCell>{data.EMPLOYEE_ID}</TableCell>
          <TableCell>{data.FIRST_NAME}</TableCell>
          <TableCell>{data.LAST_NAME}</TableCell>
          <TableCell>{data.TIME}</TableCell>
          <TableCell>{data.PHONE_NUMBER}</TableCell>
          <TableCell>{data.ZIPCODE_KEY}</TableCell>
          </TableRow>
        </TableBody>
        </Table>
      </Box>
      </Grid>
      <Grid container spacing={2} margin={5}>
      <Grid>    
    <Typography variant="h1" >View Order by:</Typography>
    <Box component='form' gap={10} display='flex' marginTop={5}>
            <Box display="flex" flexDirection="column">
            <FormControl required sx={{ m: 1, minWidth: 120 }}>
            <InputLabel id="sort_by">Sort by</InputLabel>
            <Select labelId= "sort_by" label= "view by:" onChange={handleSelect}>
              <MenuItem value="Employee">Employee</MenuItem>
              <MenuItem value="Zipcode">Zipcode</MenuItem>
            </Select>
            </FormControl>
            <TextField label="Input" onChange={handleInput}></TextField>
          </Box>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
          <DatePicker label="Start" onChange={(val)=>setStart(val)} value={start} />
          </LocalizationProvider>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
          <DatePicker label="End" onChange={(val)=>setEnd(val)} value={end} />
          </LocalizationProvider>
          <Button onClick={multiOrder}>ENTER</Button>
            </Box>
            <Table>
        <TableHead>
            <TableRow>
            <TableCell>Order Number</TableCell>
            <TableCell>Employee ID</TableCell>
            <TableCell>First Name</TableCell>
            <TableCell>Last Name</TableCell>
            <TableCell>Time</TableCell>
            <TableCell>Phone Number</TableCell>
            <TableCell>Zipcode</TableCell>
            </TableRow>
        </TableHead>
        <TableBody>
            {DisplayData}
        </TableBody>
    </Table>
            
    </Grid>
    </Grid>
    </Box>

  );
}