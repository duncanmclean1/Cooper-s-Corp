import { Grid,Button, Typography } from "@material-ui/core";

  export default function App() {  
    return (
    <Grid container spacing={2}>
        <Grid item container justifyContent="center" alignContent="center">
            <Typography variant="h2">Coopers-Corp</Typography>
        </Grid>
        <Grid item container justifyContent="center" alignContent="center" direction="column" spacing={2}>
            <Grid item spacing={2}>
                <Button variant="outlined" href="/createorder">
                    Create Order
                </Button>
            </Grid>
            <Grid item spacing={2}>
                <Button variant="outlined" href="/vieworder">
                    View Order
                </Button>
            </Grid>
            <Grid item spacing={2}>
                <Button variant="outlined" href="/editemployee">
                    Edit Employee
                </Button>
            </Grid>
        </Grid>
    </Grid>
    );
  }
  