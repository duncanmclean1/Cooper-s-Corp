import { Box, Button, Container, Paper, Typography, TableHead, TableCell, Table, TableContainer, TableRow, TableBody, TextField } from "@material-ui/core";
import { useState } from "react";
export default function AddItems() {    
    const createMenu = (productId, productName, notes, price) => {
        return {productId, productName, notes, price}
    }

    const rows = [
        createMenu(1, "Soda", "2 Liter bottle", 3.25),
        createMenu(2, "Breadsticks", "8 per pack", 2.50),
        createMenu(3, "Pizza-small Pepperoni", "8 inch", 7.35),
        createMenu(4, "Pizza-medium Pepperoni", "12 inch", 9.35),
        createMenu(5, "Pizza-large Pepperoni", "18 inch", 14.00),
        createMenu(6, "Pizza-small House special", "8 inch", 8.50),
        createMenu(7, "Pizza-medium House special", "12 inch", 10.50),
        createMenu(8, "Pizza-large House special", "18 inch", 16.00),
    ]

    const [list, setList] = useState(rows);
    const [name, setName] = useState('');
    const handleChange = (event) => {
        setName(event.target.value);
    }

    const handleAdd = () => {

    }
    return (
        <Container component="main" maxWidth="sm">
          <Box
            sx={{  
              marginTop: 8,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
            }}
          >
            <Typography variant="h4">Would like to add some items?</Typography>
            <TableContainer component={Paper}>
                <Table sx={{minWidth: 650}} aria-label="Menu">
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell align="right">Notes</TableCell>
                            <TableCell align="right">Price</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {list.map((item) => (
                            <TableRow key={item.productId}>
                            <TableCell>{item.productName}</TableCell>
                            <TableCell>{item.notes}</TableCell>
                            <TableCell>{item.price}</TableCell>
                        </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <TextField onChange={handleChange}></TextField>
            <Button
                id="fade-button"
                fullWidth={true}
                onClick={handleAdd}
            >
                Add Items
            </Button>          
            </Box>
        </Container>
    )
}