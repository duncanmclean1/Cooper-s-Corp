import React, {useState, useEffect} from 'react';
import { useNavigate } from 'react-router-dom';
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
  const [data, setData] = useState();
  const [id, setId] = useState("");
  const [start, setStart] = useState();
  const [end, setEnd] = useState();
  const handleId = (event) => {
    event.preventDefault();
    setId(event.target.value);
    console.log("hi");
    console.log(id);
  };
  const multiOrder = (event) => {
    event.preventDefault();
    const formattedStartDate = (dayjs(start).format('YYYY-MM-DDTHH:mm:ss'))
    const formattedEndDate = (dayjs(end).format('YYYY-MM-DDTHH:mm:ss'))
    console.log("start" + formattedStartDate)
    console.log("end" + formattedEndDate)
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
      </Box>
      </Grid>
      <Grid container spacing={2} margin={5}>
      <Grid>    
    <Typography variant="h1" >View Order by:</Typography>
    <Box component='form' gap={10} display='flex' marginTop={5}>
            <FormControl required sx={{ m: 1, minWidth: 120 }}>
            <InputLabel id="status_id">Sort by</InputLabel>
            <Select labelId= "sort_by" label= "view by:" autowidth>
              <MenuItem value={true}>Employee</MenuItem>
              <MenuItem value={false}>Zipcode</MenuItem>
            </Select>
            </FormControl>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
          <DatePicker label="Start" onChange={(val)=>setStart(val)} value={start} />
          </LocalizationProvider>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
          <DatePicker label="End" onChange={(val)=>setEnd(val)} value={end} />
          </LocalizationProvider>
          <Button onClick={multiOrder}>ENTER</Button>
            </Box>
            
    </Grid>
    </Grid>
    </Box>

  );
}