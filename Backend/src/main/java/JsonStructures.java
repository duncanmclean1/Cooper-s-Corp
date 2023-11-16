import java.util.ArrayList;

public class JsonStructures {

    static class LoginJson {
        int EMPLOYEE_ID;
        String PASSWORD;
    }

    static class CreateOrderJson {
        int EMPLOYEE_ID;
        String PHONE_NUMBER;
        String ADDRESS;
        String ZIPCODE_KEY;
        ArrayList<OrderDetail> ORDER_DETAILS;

        @Override
        public String toString() {
            return "EMPLOYEE_ID: " + EMPLOYEE_ID + "\nPHONE_NUMBER: "
                    + PHONE_NUMBER + "\nADDRESS: " + ADDRESS + "\nZIPCODE_KEY: " 
                    + ZIPCODE_KEY + "\n" + ORDER_DETAILS;
        }

        class OrderDetail {
            int PRODUCT_ID;
            int QUANTITY;
            String NOTES;

            public OrderDetail(int PRODUCT_ID, int QUANTITY, String NOTES) {
                this.PRODUCT_ID = PRODUCT_ID;
                this.QUANTITY = QUANTITY;
                this.NOTES = NOTES;
            }

            @Override
            public String toString() {
                return "\n{\n\tPRODUCT_ID: " + PRODUCT_ID + "\n\tQUANTITY: " + QUANTITY + "\n\tNOTES: " + NOTES + "\n}";
            }
        }
    }

    static class CheckForCustomerJson {
        String PHONE_NUMBER;
    }
}