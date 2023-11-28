import { Typography, Box, Container, TextField, Button } from "@material-ui/core";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

export default function CustomerDetails() {
    const [phoneNumber, setPhoneNumber] = useState({phoneNumber: ""});
    const [address, setAddress] = useState({address: ""});
    const [zipCode, setZipCode] = useState({zipCode: ""});
    const {employeeId} = useParams();
    const navigate = useNavigate();
    const handleSubmit = (event) => {
        event.preventDefault();
        const addCustomer = {
            "EMPLOYEE_ID": employeeId,
            "PHONE_NUMBER": phoneNumber.phoneNumber,
            "ADDRESS": address.address,
            "ZIPCODE_KEY": Number(zipCode.zipCode)
        };
        fetch('/api/addcustomerandorder', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(addCustomer),
        })
        .then((response) => response.json())
        .then((response) => {
            navigate(`/additems/${employeeId}/${response.ORDER_NUMBER}`)
        })
        .catch((error) => {
            console.log(error);
        })
    }

    const handlePhoneNumber = phoneNumber => event => {
    setPhoneNumber({...phoneNumber, [phoneNumber]: event.target.value})
    }

    const handleAddress = address => event => {
        setAddress({...address, [address]: event.target.value})
    }

    const handleZipCode = zipCode => event => {
        setZipCode({...zipCode, [zipCode]: event.target.value})
    }

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
                        fullWidth={true}
                        name="Employee Id"
                        label="Employee Id"
                        variant="outlined"
                        value={employeeId}
                        disabled={true}
                    />
                    <TextField 
                        margin="normal"
                        required={true}
                        fullWidth={true}
                        name="Customer Phone Number"
                        label="Customer Phone Number"
                        variant="outlined"
                        value={phoneNumber.phoneNumber}
                        onChange={handlePhoneNumber("phoneNumber")}
                    />
                    <TextField
                        margin="normal"
                        required={true}
                        fullWidth={true}
                        name="Customer Address"
                        label="Customer Address"
                        variant="outlined"
                        value={address.address}
                        onChange={handleAddress("address")}
                    />
                    <TextField
                        margin="normal"
                        required={true}
                        fullWidth={true}
                        name="Customer Zipcode"
                        label="Customer Zipcode"
                        variant="outlined"
                        value={zipCode.zipCode}
                        onChange={handleZipCode("zipCode")}
                    />
                    <Button
                        type="submit"
                        fullWidth={true}
                        variant="contained"
                        onClick={handleSubmit}  
                    >
                        Add Customer
                    </Button>
                </Box>
        </Container>
    )
}
