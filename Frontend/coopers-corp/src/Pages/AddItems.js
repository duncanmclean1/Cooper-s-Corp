import { Box, Button, Container, Paper, Typography, TableHead, TableCell, Table, TableContainer, TableRow, TableBody, TextField, Grid } from "@material-ui/core";
import { useEffect, useState } from "react";
export default function AddItems() {    
    const [menu, setMenu] = useState();
    const [productId, setProductId] = useState({productId:""});
    const [pricePaid, setPricePaid] = useState({pricePaid:""});
    const [quantity, setQuantity] = useState({quantity:""});
    const [notes, setNotes] = useState({notes: ""});


        useEffect(() => {
            fetch('/api/showmenu', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(),
            })
            .then((response) => response.json())
            .then((response) => {
                setMenu(response);
            })
        }, []) 

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
            <Typography variant="h4">Menu</Typography>
            <TableContainer component={Paper}>
                <Table sx={{minWidth: 650}} aria-label="Menu">
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Description</TableCell>
                            <TableCell>Price</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {menu?.map((item) => (
                            <TableRow key={item.productId}>
                            <TableCell>{item.PRODUCT_NAME}</TableCell>
                            <TableCell>{item.SIZE_NAME}</TableCell>
                            <TableCell>{item.PRICE}</TableCell>
                            {/* <Button
                                id="fade-button"
                            >
                                Add Item
                            </Button>  */}
                        </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Box 
                sx={{  
                    marginTop: 8,
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                  }}
            > 
            <Grid item xs={4}>
                <Typography variant="h4">Add Item</Typography>
            </Grid>
            <Grid container rowSpacing={2}>
                <Grid item xs={6}>
                <TextField 
                    margin="normal"
                    required={true}
                    name="Quantity"
                    label="Quantity"
                    value={quantity.quantity}
                    variant="outlined"
                />
                </Grid>
                <TextField 
                    margin="normal"
                    required={true}
                    name="Notes"
                    label="Notes"
                    value={notes.notes}
                    variant="outlined"
                />
            </Grid>
                <Button fullWidth={true}>Add Item</Button>
                <TableBody>
                    <Typography>Added Items will be displayed here</Typography>
                    <Button>Delete item</Button>
                </TableBody>
            </Box>
            <Button fullWidth={true}>Create Order</Button>
            </Box>
        </Container>
    )
}