import { Grid,Button, Typography } from "@material-ui/core";
import { useParams } from "react-router-dom";

  export default function DashboardPage() {  
    const {employeeId} = useParams();
    return (
    <Grid container spacing={2}>
        <Grid item container justifyContent="center" alignContent="center">
            <Typography variant="h2">Premo Pizza</Typography>
        </Grid>
        <Grid item container justifyContent="center" alignContent="center" direction="column" spacing={2}>
            <Grid item spacing={2}>
                <Button variant="outlined" href={`/customerdetails/${employeeId}`}>
                    Create Order
                </Button>
            </Grid>
            <Grid item spacing={2}>
                <Button variant="outlined" href="/vieworder">
                    View Order
                </Button>
            </Grid>
            <Grid item spacing={2}>
            <Button variant="outlined" href={`/editemployee/${employeeId}`}>
                    Edit Employee
                </Button>
            </Grid>
        </Grid>
    </Grid>
    );
  }
  