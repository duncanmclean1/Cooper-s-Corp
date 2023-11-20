import { Typography, Box, Container, Paper, TextField, Button } from "@material-ui/core";

export default function CustomerDetails() {
    return (
        <Container component="main" maxWidth="xs">
            <Typography variant="h3">Customer Details</Typography>
                <Box 
                    sx={{  
                        marginTop: 8,
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                    }}
                >
                    <TextField
                        margin="normal"
                        label="Employee Id"
                        fullWidth={true}
                        disabled={true}
                        variant="outlined"
                        defaultValue={"1234"}
                    />
                    <TextField 
                        margin="normal"
                        required={true}
                        fullWidth={true}
                        name="Customer Phone Number"
                        label="Customer Phone Number"
                        variant="outlined"
                    />
                    <TextField
                        margin="normal"
                        required={true}
                        fullWidth={true}
                        name="Customer Address"
                        label="Customer Address"
                        variant="outlined"
                    />
                    <TextField
                        margin="normal"
                        required={true}
                        fullWidth={true}
                        name="Customer Zipcode"
                        label="Customer Zipcode"
                        variant="outlined"
                    />
                    <Button
                        type="submit"
                        fullWidth={true}
                        variant="contained"
                        href="/addItem"
                    >
                        Add Customer
                    </Button>
                </Box>
        </Container>
    )
}