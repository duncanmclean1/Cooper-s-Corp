import java.util.ArrayList;

public class JsonStructures {

    static class LoginJson {
        int EMPLOYEE_ID;
        String PASSWORD;
    }

    static class CreateOrderJson {
        int EMPLOYEE_ID;
        String PHONE_NUMBER;
        String TIME;
        ArrayList<OrderDetail> ORDER_DETAILS;

        @Override
        public String toString() {
            return "EMPLOYEE_ID: " + EMPLOYEE_ID + "\nPHONE_NUMBER: " + PHONE_NUMBER
                    + "\nTIME: " + TIME + "\n" + ORDER_DETAILS;
        }

        class OrderDetail {
            int PRODUCT_ID;
            double PRICE_PAID;
            int QUANTITY;
            String NOTES;
          
            public OrderDetail(int PRODUCT_ID, double PRICE_PAID, int QUANTITY, String NOTES) {
                this.PRODUCT_ID = PRODUCT_ID;
                this.PRICE_PAID = PRICE_PAID;
                this.QUANTITY = QUANTITY;
                this.NOTES = NOTES;
            }

            @Override
            public String toString() {
                return "\n{\n\tPRODUCT_ID: " + PRODUCT_ID + "\n\tPRICE_PAID: " + PRICE_PAID + "\n\tQUANTITY: " + QUANTITY + "\n\tNOTES: " + NOTES + "\n}\n";
            }
        }
    }
  
    static class AddEmployeeJson {
        int EMPLOYEE_ID;
        String FIRST_NAME;
        String LAST_NAME;
        String PASSWORD;

        @Override
        public String toString(){
            return "EMPLOYEE_ID: " + EMPLOYEE_ID + "\nFIRST_NAME: " + FIRST_NAME + "\nLAST_NAME: " + LAST_NAME + "\nPASSWORD: " + PASSWORD;
        }
    }

    static class UpdateEmployeeJson {
        int EMPLOYEE_ID;
        String FIRST_NAME;
        String LAST_NAME;
        boolean STATUS;

        @Override
        public String toString(){
            return "EMPLOYEE_ID: " + EMPLOYEE_ID + "\nFIRST_NAME: " + FIRST_NAME + "\nLAST_NAME: " + LAST_NAME + "\n" + "STATUS: " + STATUS;
        }
    }

    static class AddCustomerJson {
        String PHONE_NUMBER;
        String ADDRESS;
        String ZIPCODE_KEY;
    }
  
    static class employeeDetails {
        String EMPLOYEE_ID;
        String FIRST_NAME;
        String LAST_NAME;
        String STATUS;

        public employeeDetails(){}

        public employeeDetails(String id, String first, String last, String status){
            this.EMPLOYEE_ID = id;
            this.FIRST_NAME = first;
            this.LAST_NAME = last;
            this.STATUS = status;
        }

        public String getEmployeeID(){
            return this.EMPLOYEE_ID;
        }

        public String getFirstName(){
            return this.FIRST_NAME;
        }

        public String getLastName(){
            return this.LAST_NAME;
        }

        public String getStatus(){
            return this.STATUS;
        }

        public void setEmployeeID(String id){
            this.EMPLOYEE_ID = id;
        }

        public void setFirstName(String first){
            this.FIRST_NAME = first;
        }

        public void setLastName(String last){
            this.LAST_NAME = last;
        }

        public void setStatus(String status){
            this.STATUS = status;
        }

        @Override
        public String toString(){
            return "EMPLOYEE_ID: " + EMPLOYEE_ID + "\nFIRST_NAME: " + FIRST_NAME + "\nLAST_NAME: " + LAST_NAME + "\nSTATUS: " + STATUS;
        }
    }

    static class CheckForCustomerJson {
        String PHONE_NUMBER;
    }

    static class OrderDetailJson {
        int ORDER_NUMBER;
    }

    static class OrdersByZipcodeJson {
        int ZIPCODE_KEY;
        String TIME_BEGIN;
        String TIME_END;
    }

    static class OrdersByEmployeeJson {
        int EMPLOYEE_ID;
        String TIME_BEGIN;
        String TIME_END;
    }

    static class OrderDetail {
        int ORDER_NUMBER;
        int EMPLOYEE_ID;
        String FIRST_NAME;
        String LAST_NAME;
        String TIME;
        String PHONE_NUMBER;
        int ZIPCODE_KEY;

        public OrderDetail(){}

        public OrderDetail (int ORDER_NUMBER, int EMPLOYEE_ID, String FIRST_NAME, String LAST_NAME, String TIME, String PHONE_NUMBER, int ZIPCODE_KEY) {
            this.ORDER_NUMBER = ORDER_NUMBER;
            this.EMPLOYEE_ID = EMPLOYEE_ID;
            this.FIRST_NAME = FIRST_NAME;
            this.LAST_NAME = LAST_NAME;
            this.TIME = TIME;
            this.PHONE_NUMBER = PHONE_NUMBER;
            this.ZIPCODE_KEY = ZIPCODE_KEY;
        }
    }
}