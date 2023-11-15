import {Card, Button} from "@material-ui/core";
export default function DashboardPage()
{
    return (
    <Card spacing={6} direction="column">
      <Button variant="outlined" href="/createorder">Create Order</Button>
      <Button variant="outlined" href="/vieworder">
        View Order
      </Button>
      <Button variant="outlined" href="/editemployee">
        Edit Employee
      </Button>
    </Card>
    );
}