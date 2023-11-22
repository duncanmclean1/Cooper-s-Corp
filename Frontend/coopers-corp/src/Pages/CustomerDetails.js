import { Typography, Box, Container, TextField, Button } from "@material-ui/core";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert } from "@mui/material";

export default function CustomerDetails() {
    const [phoneNumber, setPhoneNumber] = useState({phoneNumber: ""});
    const [address, setAddress] = useState({address: ""});
    const [zipCode, setZipCode] = useState({zipCode: ""});
    const [data, setData] = useState(DEFAULT_DATA_INFO);

    const handleMessage = () => {
        if (data.alertType === "success") {
          return <Alert severity = {data.alertType}>{data.alertMessage}</Alert>;
        }
        else if (data.alertType === "error") {
          return <Alert severity = {data.alertType}>{data.alertMessage}</Alert>;
        }
      }

    const navigate = useNavigate();

    const handleSubmit = (event) => {
        event.preventDefault();
        const addCustomer = {
            "PHONE_NUMBER": phoneNumber.phoneNumber,
            "ADDRESS": address.address,
            "ZIPCODE_KEY": Number(zipCode.zipCode)
        };
        fetch('/api/addcustomer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(addCustomer),
        })
        .then((response) => response.json())
        .then((addCustomer) => {
            if (addCustomer.isAdded != true)
            {
                setData({
                  alertType: "success",
                  alertMessage: "Successfully logged in.",
                });
                navigate("/addItems");
              }
              else {
                setData({
                  alertType: "error",
                  alertMessage: "Enter correct employee id or password",
                 });
              }
        })
        .catch((error) => {
            setData({
                alertType: "error",
                alertMessage: error,
               })
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


const DEFAULT_DATA_INFO = {
    alertType: "",
    alertMessage: "",
  }