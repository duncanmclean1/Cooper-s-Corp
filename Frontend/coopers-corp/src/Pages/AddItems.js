import { Box, Button, Container, Paper, Typography, TableHead, TableCell, Table, TableContainer, TableRow, TableBody, TextField, Grid} from "@material-ui/core";
import { ToggleButton } from "@mui/material";
import { useEffect, useState } from "react";
import {useNavigate, useParams } from "react-router-dom";
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import FormControl from '@mui/material/FormControl';
export default function AddItems() {    
    const [menu, setMenu] = useState();
    const [quantity, setQuantity] = useState({quantity:""});
    const [notes, setNotes] = useState({notes:""});
    const [cartItems, setCartItems] = useState();
    const {orderNumber} = useParams();
    const [productName, setProductName] = useState({productName:""});
    const {employeeId} = useParams();
    const [cartTotal, setCartTotal] = useState();
    const [orderDetailKey, setOrderDetailKey] = useState({ORDER_DETAIL_KEY:""});
    const [discount, setDiscount] = useState({discount:""});
    const navigate = useNavigate();

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

        const handleDelete = cart => {
            setOrderDetailKey(detailkey => ({...detailkey, ORDER_DETAIL_KEY: cart.ORDER_DETAIL_KEY}));
            const removeOrderDetail = {
                "ORDER_NUMBER": orderNumber,
                "ORDER_DETAIL_KEY": cart.ORDER_DETAIL_KEY
            }
            fetch('/api/removeorderdetail', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(removeOrderDetail),
            })
            .then((response) => response.json())
            .then((response) => {
                setCartItems(response.CART)
                setCartTotal(response.CART_TOTAL)
            })
        }
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
                    setCartTotal(response.CART_TOTAL)
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
            .then((response) => {
                navigate(`/dashboard/${employeeId}`)
            })
            .catch((error) => console.log(error))
        }

        const calculateCartTotal = (event) => {
            event.preventDefault();
            const caclulateTotal = {
                "ORDER_NUMBER": orderNumber,
                "DISCOUNT": discount.discount
            }
            fetch('/api/calculatecarttotal', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(caclulateTotal),
            })
            .then((response) => response.json())
            .then((response) => {
                setCartTotal(response.DISCOUNTED_CART_TOTAL);
            })
        }
        const handleProductName = productName => event => {
            setProductName({...productName, [productName]: event.target.value})
        }
        const handleProduct = (event) => {
            setProductName({productName: event.target.value});
            console.log(productName.productName);
          };
        const handleQuantity = quantity => event => {
            setQuantity({...quantity, [quantity]: event.target.value})
        }

        const handleNotes = notes => event => {
            setNotes({...notes, [notes]: event.target.value})
        }

        const handleDiscount = discount => event => {
            setDiscount({...discount, [discount]: event.target.value})
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
                <Typography variant="h6" align="center">Add Item</Typography>
    <Grid item xs={12}>
        <Grid marginTop={1} container spacing={2}>
            <Grid item xs={4}>
            <Box component='form' display='flex' alignItems="center" flexDirection="row" marginTop={3}>
            <FormControl required sx={{ width: 175, marginTop: 2 }}>
            <InputLabel id="product">Product Name</InputLabel>
            <Select labelId= "product" label= "Product" onChange={handleProduct} autowidth>
              <MenuItem value="SODA">SODA</MenuItem>
              <MenuItem value="BREADSTICKS">BREADSTICKS</MenuItem>
              <MenuItem value="PIZZA - SMALL PEPPERONI">PIZZA - SMALL PEPPERONI</MenuItem>
              <MenuItem value="PIZZA - MEDIUM PEPPERONI">PIZZA - MEDIUM PEPPERONI</MenuItem>
              <MenuItem value="PIZZA - LARGE PEPPERONI">PIZZA - LARGE PEPPERONI</MenuItem>
              <MenuItem value="PIZZA - SMALL HOUSE SPECIAL">PIZZA - SMALL HOUSE SPECIAL</MenuItem>
              <MenuItem value="PIZZA - MEDIUM HOUSE SPECIAL">PIZZA - MEDIUM HOUSE SPECIAL</MenuItem>
              <MenuItem value="PIZZA - LARGE HOUSE SPECIAL">PIZZA - LARGE HOUSE SPECIAL</MenuItem>
            </Select>
            </FormControl>
            </Box>
            </Grid>
                {/* <TextField 
                margin="normal"
                id="Product Name"
                label="Product Name"
                name="Product Name"
                variant="outlined"
                value={productName.productName}
                onChange={handleProductName("productName")}
                /> */}
                <Grid item xs={4}>
                <TextField
                margin="normal"
                required={true}
                type="number"
                InputProps={{
                    inputProps: { min: 0 }
                  }}
                id="Quantity"
                label="Quantity"
                name="Quantity"
                autoComplete="Quantity"
                variant="outlined"
                value={quantity.quantity}
                onChange={handleQuantity("quantity")}
              />
              </Grid>
                <Grid item xs={4}>
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
            </Grid>
            <Button variant='outlined' fullWidth={true} onClick={handleSubmit}>Add Item</Button>
            </Grid>
                <Box display="flex" justifyContent="center" alignItems="center">
                <Grid item xs={12}>
                <TextField 
                    margin="normal"
                    required={true}
                    id="Discount"
                    label="Discount"
                    name="Discount"
                    autoComplete="Discount"
                    variant="outlined"
                    value={discount.discount}
                    onChange={handleDiscount("discount")}
                    align='center'
                />
                </Grid>
                </Box>
                <Button variant='outlined' fullWidth={true} onClick={calculateCartTotal}>Apply Discount</Button>
    <Box sx={{ flexGrow: 1 }}>
        <Typography variant="h6">Cart</Typography>
        <TableContainer component={Paper}>
                <Table sx={{minWidth: 650}} aria-label="Menu">
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Notes</TableCell>
                            <TableCell>Quantity</TableCell>
                            <TableCell>Price</TableCell>
                        </TableRow>
                    </TableHead> 
        {cartItems?.map((cart) => (
            <TableBody key={cart.ORDER_DETAIL_KEY}>
                <TableCell>{cart.PRODUCT_NAME}</TableCell>
                <TableCell>{cart.NOTES}</TableCell>
                <TableCell>{cart.QUANTITY}</TableCell>
                <TableCell>{cart.PRICE_PAID}</TableCell>
                <Button key={cart.ORDER_DETAIL_KEY}  onClick={() => handleDelete(cart)}>Delete Item</Button>
            </TableBody>
        ))}
        <Typography>Sub Total: {cartTotal}</Typography>
                </Table>
            </TableContainer>
        </Box>
    </Box>
    <Grid container spacing={2}>
        <Grid item xs={6}>
        <Button fullWidth={true} href={`/dashboard/${employeeId}`}>Submit Order</Button>
        </Grid>
        <Grid item xs={6}>
        <Button fullWidth={true} onClick={handleCancelOrder}>Cancel Order</Button>
        </Grid>
    </Grid>
    </Box>
</Container>
    )
}