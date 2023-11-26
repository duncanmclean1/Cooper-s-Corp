import { Box, Button, Container, Paper, Typography, TableHead, TableCell, Table, TableContainer, TableRow, TableBody, TextField, Grid} from "@material-ui/core";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

export default function AddItems() {    
    const [menu, setMenu] = useState();
    const [quantity, setQuantity] = useState({quantity:""});
    const [notes, setNotes] = useState({notes:""});
    const [cartItems, setCartItems] = useState();
    const {orderNumber} = useParams();
    const [productName, setProductName] = useState({productName:""});

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
                console.log(response)
            })
        }, []) 

        const handleSubmit = (event) => {
            event.preventDefault();
            const addOrderDetail = {
                "ORDER_NUMBER": orderNumber,
                "PRODUCT_NAME": productName.productName,
                "PRICE_PAID": orderNumber*quantity.quantity,
                "QUANTITY": quantity.quantity,
                "NOTES": notes.notes
            }
            fetch('/api/addorderdetail', {
                method: 'POST',
                headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(addOrderDetail),
                })
                .then((response) => response.json())
                .then((response) => {
                    setCartItems(response.CART)
                })
                .catch((error) => console.log(error))
        }
        
        const handleCancelOrder = (event) => {
            event.preventDefault();
            const cancelOrder = {
                "ORDER_NUMBER": orderNumber
            }
            fetch('/api/cancelorder', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(cancelOrder),
            })
            .then((response) => response.json())
            .catch((error) => console.log(error))
        }
        const handleProductName = productName => event => {
            setProductName({...productName, [productName]: event.target.value})
        }
        const handleQuantity = quantity => event => {
            setQuantity({...quantity, [quantity]: event.target.value})
        }

        const handleNotes = notes => event => {
            setNotes({...notes, [notes]: event.target.value})
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
            <Typography variant="h6">Menu</Typography>
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
                        {menu?.map((item, index) => (
                            <TableRow key={index}>
                            <TableCell>{item.PRODUCT_NAME}</TableCell>
                            <TableCell>{item.SIZE_NAME}</TableCell>
                            <TableCell>{item.PRICE}</TableCell>
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
                <Typography variant="h6">Add Item</Typography>
            </Grid>
            <Box sx={{ minWidth: 120 }}>
    </Box>
            <Grid container rowSpacing={2}>
                <Grid item xs={6}>
                <TextField 
                margin="normal"
                id="Product Name"
                label="Product Name"
                name="Product Name"
                variant="outlined"
                value={productName.productName}
                onChange={handleProductName("productName")}
                />
                <TextField
                margin="normal"
                required={true}
                id="Quantity"
                label="Quantity"
                name="Quantity"
                autoComplete="Quantity"
                variant="outlined"
                value={quantity.quantity}
                onChange={handleQuantity("quantity")}
              />
                </Grid>
                <TextField 
                    margin="normal"
                    required={true}
                    id="Notes"
                    label="Notes"
                    name="Notes"
                    autoComplete="Notes"
                    variant="outlined"
                    value={notes.notes}
                    onChange={handleNotes("notes")}
                />
            </Grid>
                <Button fullWidth={true} onClick={handleSubmit}>Add Item</Button>
    <Box sx={{ flexGrow: 1 }}>
        <Typography variant="h6">Cart</Typography>
        <TableContainer component={Paper}>
                <Table sx={{minWidth: 650}} aria-label="Menu">
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Notes</TableCell>
                            <TableCell>Quantity</TableCell>
                        </TableRow>
                    </TableHead>        
        {cartItems?.map((cart, index) => (
            <TableBody key={index}>
                <TableCell>{cart.PRODUCT_NAME}</TableCell>
                <TableCell>{cart.NOTES}</TableCell>
                <TableCell>{cart.QUANTITY}</TableCell>
                <Button>Delete Item</Button>
            </TableBody>
        ))}
                </Table>
            </TableContainer>
        </Box>
    </Box>
    <Button fullWidth={true} onClick={handleCancelOrder}>Cancel Order</Button>
    </Box>
</Container>
    )
}