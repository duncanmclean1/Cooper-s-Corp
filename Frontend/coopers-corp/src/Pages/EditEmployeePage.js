import React, {useState, useEffect} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

function createData(name, calories, fat, carbs, protein) {
  return { name, calories, fat, carbs, protein };
}



export default function EditEmployeePage() {
  const [rows, setRows] = useState([]);
    useEffect(() => { getData();
    }, []);
    const getData = async () => {
      const actualData = await fetch('api/showemployees');
      const result = await actualData.json();
      setRows(result);
      console.log("hi");
      console.log(rows);
    };

  const DisplayData = rows.map((row) => {
    return(<tr>
      <td> {row.EMPLOYEE_ID}</td>
      <td> {row.FIRST_NAME}</td>
      <td> {row.LAST_NAME}</td>
    </tr>)
  })
  return (
    <div>
    <table>
        <thead>
            <tr>
            <th>ID</th>
            <th>FIRST NAME</th>
            <th>LAST NAME</th>
            </tr>
        </thead>
        <tbody>
         
            
            {DisplayData}
            
        </tbody>
    </table>
     
</div>
  );
}