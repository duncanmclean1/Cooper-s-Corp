import { Typography, Box, Container, Paper, makeStyles } from "@material-ui/core";

export default function CreateOrder() {
    return (
        <Paper style={{width: "100%", display: "flex", backgroundColor: "grey"}}>
            <Container component="main" maxWidth="xs" style={{display: "flex", backgroundColor: "grey"}}>
                <Box 
                    sx={{  
                        marginTop: 8,
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                    }}
                >
                    <Typography variant="h2">Create Order</Typography>
                </Box>
            </Container>
        </Paper>
    )
}