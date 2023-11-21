import { Box, Button, Container, Menu, Typography, TextField, TableHead, TableCell, Table, TableContainer, TableRow, TableBody } from "@material-ui/core";
import { useState } from "react";

export default function AddItems() {
    const [anchorEl, setAnchorEl] = useState(null);
    const open = Boolean(anchorEl);
    
    const handleClick = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    function createMenu(productId, name, notes, price) {
        return {productId, name, notes, price}
    }

    const rows = [
        createMenu(1, "Soda", "2 Liter bottle", 3.25),
        createMenu(2, "Breadsticks", "8 per pack", 2.50),
        createMenu(3, "Pizza-small Pepperoni", "8 inch", 7.35),
        createMenu(2, "Pizza-medium Pepperoni", "12 inch", 9.35),
        createMenu(2, "Pizza-large Pepperoni", "18 inch", 14.00),
        createMenu(2, "Pizza-small House special", "8 inch", 8.50),
        createMenu(2, "Pizza-medium House special", "12 inch", 10.50),
        createMenu(2, "Pizza-large House special", "18 inch", 16.00),
    ]
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
            <Button
                id="fade-button"
                aria-controls={open ? 'fade-menu' : undefined}
                aria-haspopup="true"
                aria-expanded={open ? 'true' : undefined}
                fullWidth={true}
                onClick={handleClick}
            >
                Add Items
            </Button>
            <Menu
                id="fade-menu"
                MenuListProps={{
                'aria-labelledby': 'fade-button',
                }}
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
            >
            <TableContainer>
                <Table sx={{minWidth: 650}} aria-label="Menu">
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell align="right">Notes</TableCell>
                            <TableCell align="right">Price</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {rows.map((item) => (
                            <TableRow key={item.productId}>
                            <TableCell>{item.name}</TableCell>
                            <TableCell>{item.notes}</TableCell>
                            <TableCell>{item.price}</TableCell>
                        </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
          </Menu>
          <TextField
                margin="normal"
                required={true}
                name="Quantity"
                label="Quantity"
                variant="outlined"
                defaultValue={"1"}
                />
          </Box>
        </Container>
    )
}