import { Box, Button, Container, Paper, Typography, TableHead, TableCell, Table, TableContainer, TableRow, TableBody } from "@material-ui/core";
import { useState } from "react";
export default function AddItems() {    
    const createMenu = (productId, productName, description, price, quantity) => {
        return {productId, productName, description, price,  quantity}
    }

    const menuItems = [
        createMenu(1, "Soda", "2 Liter bottle", 3.25),
        createMenu(2, "Breadsticks", "8 per pack", 2.50),
        createMenu(3, "Pizza-small Pepperoni", "8 inch", 7.35),
        createMenu(4, "Pizza-medium Pepperoni", "12 inch", 9.35),
        createMenu(5, "Pizza-large Pepperoni", "18 inch", 14.00),
        createMenu(6, "Pizza-small House special", "8 inch", 8.50),
        createMenu(7, "Pizza-medium House special", "12 inch", 10.50),
        createMenu(8, "Pizza-large House special", "18 inch", 16.00),
    ]

    // const [cartItems, setCartItems] = useState(menuItems);

    // const handleAddItem = (product) => {
    //     const ProductExist = cartItems.find(item => item.productId === product.productId)
    //     if (ProductExist) {
    //         setCartItems(
    //             cartItems.map(item => 
    //                 item.productId === product.productId
    //                 ? {...ProductExist, quantity: ProductExist.quantity + 1} : item)
    //         );
    //     } else {
    //         setCartItems([...cartItems, {
    //             ...product,
    //             quantity: 1
    //         }]);
    //     }

    //     setCartItems((product)=> [
    //         ...product,
    //         {quantity: 1, productName: "Soda", price: 3.25}
    //     ])

    //     setCartItems((state) => [
    //         ...state,
    //         {productId: product.productId, productName: product.productName}
    //     ])
    //  };

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
                            <TableCell>Item</TableCell>
                            <TableCell>Name</TableCell>
                            <TableCell>Description</TableCell>
                            <TableCell>Price</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {menuItems.map((item) => (
                            <TableRow key={item.productId}>
                            <TableCell>{item.productId + 0}</TableCell>
                            <TableCell>{item.productName}</TableCell>
                            <TableCell>{item.description}</TableCell>
                            <TableCell>{item.price}</TableCell>
                            <Button
                                id="fade-button"
                            >
                                Add Item
                            </Button> 
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
                <Typography variant="h3">Added Items to Cart</Typography>
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