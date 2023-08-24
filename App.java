import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

// LoanRequest class to store details of each loan request

public class App {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/gui_system";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";
    private static String memberNumber;
    private static String phoneNumber;
    
    public static void main(String[] args) throws SQLException {
        try {
            // Register the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(3006);
            System.out.println("Server started. Listening on port 3006...");
            calculateAverageContributionPerformanceForAllMembers();
            calculateAverageLoanPerformanceForAllMembers();

            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create input and output streams for communication with the client
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Handle client requests
                String clientRequest;
                while ((clientRequest = input.readLine()) != null) {
                    // Process the client request and send a response
                        
                        String serverResponse = processRequest(clientRequest);
                    
                    output.println(serverResponse);
                    
                    
                }

                // Close the streams and the client socket
                input.close();
                output.close();
                clientSocket.close();
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String processRequest(String request) throws IOException, SQLException {
        // Extract command and parameters from the request
        String[] tokens = request.split(" ");
        String command = tokens[0];
        if (tokens.length == 1 && (tokens[0] instanceof String)) {
            return handleLoanAcceptance(tokens);
        }
        if (tokens.length < 1 ) {
            return "Invalid command";
            
        }
        if (tokens.length == 2 &&  Character.isDigit(tokens[0].charAt(1))) {
            return provideReference(tokens);
        }
        
        
        
        
        try {
          switch (command) {
            case "login":
                // Perform login action
                if (tokens.length < 3) {
                    return "Invalid login request";
                }
                String username = tokens[1];
                String password = tokens[2];
                return performLogin(username, password);
            case "deposit":
                // Perform deposit action
                if (tokens.length < 4) {
                    return "Invalid deposit request";
                }
                int amountDeposited = Integer.parseInt(tokens[1]);
                String receiptNumber = (tokens[2]);
                String  dateDeposited = tokens[3];
                return performDeposit(amountDeposited, receiptNumber,dateDeposited);
            case "requestLoan":
                // Perform loan request action
                if (tokens.length < 3) {
                    return "Invalid loan request";
                }else{
                     int requestedAmount = Integer.parseInt(tokens[1]);
                int paymentPeriod = Integer.parseInt(tokens[2]);
                 return performLoanRequest(requestedAmount, paymentPeriod);
                }
             case "checkReference":
                // Perform loan request action
                if (tokens.length < 2) {
                    return "Invalid reference request";
                }else{
                     int referenceNumber = Integer.parseInt(tokens[1]);
                
                 return checkReference(referenceNumber);
                }    
             case "checkLoanStatus":
                // Perform loan request action
                if (tokens.length < 2) {
                    return "Invalid loan request";
                }
                int applicationNumber = Integer.parseInt(tokens[1]);
                return performCheckLoanStatus(applicationNumber);
                case "checkStatement":
                // Perform checkStatement action
                if (tokens.length < 3) {
                    return "Invalid checkStatement request";
                }
                String dateFrom = tokens[1];
                String dateTo = tokens[2];
                String response = performCheckStatement(dateFrom, dateTo);
                return response;
            default:
                return "Unknown command";
        }
        } catch (NumberFormatException e) {
        return "Unknown command";
    }
    }
    

    /* */
    private static String provideReference(String[] tokens) {
        // Assuming the first element is the memberNumber and the second element is the phoneNumber
        if (tokens.length < 2) {
            return "Invalid integer-based request.\n Both memberNumber and phoneNumber are required.";
        }
    
        String memberNumber = tokens[0];
        String phoneNumber = tokens[1];
    
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Check if there's an existing referenceNumber in the references table
            String sql = "SELECT * FROM members WHERE member_number = ? AND phone_number = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, memberNumber);
            statement.setString(2, phoneNumber);
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                // The member number and the phone number are in the database
                String passwordSql = "SELECT password FROM members WHERE member_number = ? AND phone_number = ?";
                PreparedStatement passwordStatement = connection.prepareStatement(passwordSql);
                passwordStatement.setString(1, memberNumber);
                passwordStatement.setString(2, phoneNumber);
                ResultSet passwordResultSet = passwordStatement.executeQuery();
    
                if (passwordResultSet.next()) {
                    // Password found
                    String password = passwordResultSet.getString("password");
                    passwordResultSet.close();
                    passwordStatement.close();
                    resultSet.close();
                    statement.close();
                    connection.close();
                    return "Your password is: " + password;
                } else {
                    // Password not found
                    passwordResultSet.close();
                    passwordStatement.close();
                    resultSet.close();
                    statement.close();
    
                    // Generate a random reference number
                    String referenceNumber = generateRandomReferenceNumber();
                    insertIntoReferences(connection, memberNumber, phoneNumber, referenceNumber);
    
                    connection.close();
    
                    return "Password not found. " + referenceNumber;
                }
            } else {
                // Invalid member number or phone number
                resultSet.close();
                statement.close();
                 String referenceNumber = generateRandomReferenceNumber();
                 insertIntoReferences(connection, memberNumber, phoneNumber, referenceNumber);
                connection.close();
                return "Invalid member number or phone number.\n Your reference number is " + referenceNumber +" \n Come back with it for follow up";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
    
    private static String generateRandomReferenceNumber() {
        // Generate a random 9-digit reference number
        Random random = new Random();
        int randomNumber = random.nextInt(900_000_000) + 100_000_000;
        return String.valueOf(randomNumber);
    }
    
    private static void insertIntoReferences(Connection connection, String memberNumber, String phoneNumber, String referenceNumber) throws SQLException {
        // Prepare the SQL statement to insert into the references table
         Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(currentDate);
        String sql = "INSERT INTO reference (memberNumber, phoneNumber, referenceNumber, reason,date) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, memberNumber);
        statement.setString(2, phoneNumber);
        statement.setString(3, referenceNumber);
        statement.setString(4, "Failed to login");
        statement.setString(5, formattedDate.toString());
    
        // Execute the insert
        statement.executeUpdate();
        statement.close();
    }
    
   
    private static String performLogin(String username, String password) {
        try {
            // Establish a connection to the database
            
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            
            // Prepare the SQL statement
            String sql = "SELECT * FROM members WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();
            double progress = calculateContributionPerformance(memberNumber);

            if (resultSet.next()) {
                // Login successful
                memberNumber = resultSet.getString("member_number");
                phoneNumber = resultSet.getString("phone_number");
                
                resultSet.close();
                statement.close();
                connection.close();
                 System.out.println("Correct credentials");
                 if (progress <50){
                    return "Login successful" + " Warning!!!!!!!" + " Your contribution progress is less than 50";
                 }else{
                      return "Login successful";
                 }
              
               
            } else {
                // Invalid username or password
                resultSet.close();
                statement.close();
                connection.close();
                System.out.println("Wrong credentials");
                return "Invalid username or password";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

   
    private static String performDeposit( int amountDeposited, String receiptNumber, String dateDeposited) {
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Check if the member has an unpaid loan or balance
            String sql = "SELECT * FROM available_deposits WHERE receipt_number = ? AND deposit_date = ? AND amount_deposited >= ? AND status = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, receiptNumber);
            statement.setString(2, dateDeposited);
            statement.setInt(3,amountDeposited);
            statement.setString(4, "pending");
    
            // Execute the query
            ResultSet resultSet = statement.executeQuery();
           if (resultSet.next()) {

            String updateSql = "UPDATE available_deposits SET status = ? WHERE receipt_number = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                   updateStatement.setString(1, "verified");
                   updateStatement.setString(2, receiptNumber);
                    updateStatement.executeUpdate();
                    updateStatement.close();
                 resultSet.close();
                statement.close();
                connection.close();
                return allocateFunds( amountDeposited);
           }else{
                resultSet.close();
                statement.close();
                String referenceNumber = generateRandomReferenceNumber();
                 insertDepositIntoReferences(connection, receiptNumber, referenceNumber);
                connection.close();

                return "deposit not found. Reference number:"+ referenceNumber;
           }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }


         private static String checkReference(  int referenceNumber) {
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Check if the member has an unpaid loan or balance
            String sql = "SELECT * FROM reference WHERE referenceNumber = ? AND memberNumber = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, referenceNumber);
            statement.setString(2, memberNumber);
            
    
            // Execute the query
            ResultSet resultSet = statement.executeQuery();
           if (resultSet.next()) {

               String response = resultSet.getString("response");
              resultSet.close();
                statement.close();
                connection.close();
                return response;
           }else{
                resultSet.close();
                statement.close();
                connection.close();
           }

                return "This reference number has expired";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
    

    private static String allocateFunds( int amountDeposited) {
        String response = ""; // Initialize the response
    
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(currentDate);
    
            String sql = "SELECT * FROM loanpayment WHERE memberNumber = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, memberNumber);
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                int amount = resultSet.getInt("amount");
                int amountPaid = resultSet.getInt("amountPaid");
                int amountLeft = amount - amountPaid;
                
    
                if (amount == amountPaid) {
                    String insertSql = "INSERT INTO contributions (memberNumber, amount, date) VALUES (?, ?, ?)";
                    PreparedStatement statement2 = connection.prepareStatement(insertSql);
                    statement2.setString(1, memberNumber);
                    statement2.setInt(2, amountDeposited);
                    statement2.setString(3, formattedDate);
                    statement2.executeUpdate();
                    statement2.close();
    
                    response = "Your deposit was received and the money is transfered to contributions";
    
                } else if (amountDeposited > amountLeft) {
                    int amountRemaining = amountDeposited - amountLeft;
    
                    String insertSql = "INSERT INTO contributions (memberNumber, amount, date) VALUES (?, ?, ?)";
                    PreparedStatement statement2 = connection.prepareStatement(insertSql);
                    statement2.setString(1, memberNumber);
                    statement2.setInt(2, amountRemaining);
                    statement2.setString(3, formattedDate);
                    statement2.executeUpdate();
                    statement2.close();
    
                    String updateSql = "UPDATE loanpayment SET amountPaid = ? WHERE memberNumber = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                    updateStatement.setInt(1, amount);
                   updateStatement.setString(2, memberNumber);
                    updateStatement.executeUpdate();
                    updateStatement.close();
    
                    response = "Loan cleared and remaining amount is transfered to contributions";
    
                } else {
                    String updateSql = "UPDATE loanpayment SET amountPaid = ? WHERE memberNumber = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                    updateStatement.setInt(1, amountPaid + amountDeposited);
                    updateStatement.setString(2, memberNumber);
                    updateStatement.executeUpdate();
                    updateStatement.close();
    
                    response = "Your deposit was received and the money is used to clear your loan";
                }
    
            } else {
                String insertSql = "INSERT INTO contributions (memberNumber, amount, date) VALUES (?, ?, ?)";
                PreparedStatement statement2 = connection.prepareStatement(insertSql);
                statement2.setString(1, memberNumber);
                statement2.setInt(2, amountDeposited);
                statement2.setString(3, formattedDate);
                statement2.executeUpdate();
                statement2.close();
    
                response = "Your deposit was received and the money is transfered to contributions";
            }
    
            statement.close();
            connection.close();
    
        } catch (SQLException e) {
            e.printStackTrace();
            // You might want to handle the exception more gracefully here
        }
    
        return response;
    }
    
    
      private static void insertDepositIntoReferences(Connection connection, String receipNumber,  String referenceNumber) throws SQLException {
        // Prepare the SQL statement to insert into the references table
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(currentDate);
        String sql = "INSERT INTO reference (receiptNumber, referenceNumber, reason, date, memberNumber, phoneNumber) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, receipNumber);
        statement.setString(2, referenceNumber);
        statement.setString(3, "Failed to find deposit");
        statement.setString(4, formattedDate.toString());
        statement.setString(5, memberNumber);
        statement.setString(6, phoneNumber);
    
        // Execute the insert
        statement.executeUpdate();
        statement.close();
    }
    
    
    
    
    private static String performLoanRequest(int loanAmount, int paymentPeriod) {
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            ArrayList<Integer> applicationNumbers = new ArrayList<>();
            // Check the number of existing loan requests in the database
            String countSql = "SELECT applicationNumber, COUNT(*) AS requestCount, SUM(loanAmount) AS totalLoan FROM loanrequests WHERE status = ? GROUP BY applicationNumber";

            PreparedStatement countStatement = connection.prepareStatement(countSql);
            countStatement.setString(1, "pending");
            ResultSet countResultSet = countStatement.executeQuery();
            
            int totalLoan = 0;
           
            int loanRequestCount = 1;
            while (countResultSet.next()) {
                loanRequestCount++;
                totalLoan = countResultSet.getInt("totalLoan");
                int applicationNo = countResultSet.getInt("applicationNumber");
                applicationNumbers.add(applicationNo);
            }
            
            
            // Check the totalmoney in the deposit table
            String totalMoneySql = "SELECT totalmoney FROM saccoaccount";
            PreparedStatement totalMoneyStatement = connection.prepareStatement(totalMoneySql);
            ResultSet totalMoneyResultSet = totalMoneyStatement.executeQuery();
            int totalMoney = 0;
    
            while (totalMoneyResultSet.next()) {
                totalMoney = totalMoneyResultSet.getInt("totalmoney");
            }
            
           
            loanRequestCount = applicationNumbers.size();
            if (loanRequestCount == 10 ) {
                // Distribute the totalmoney equally among the applicants
                // Assuming 10 applicants for demonstration purposes
                if (totalMoney<=2000000){

                    return "Cannot give out loans due to lack of many in the sacco account";

                }else{

                     // Select loan requests and insert them into recommended_loans table
                String selectSql = "SELECT loanAmount, paymentPeriod, memberNumber, applicationNumber FROM loanrequests";
                PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                ResultSet loanResultSet = selectStatement.executeQuery();
                
                while (loanResultSet.next()) {
                       
                    int recommendedAmount = loanResultSet.getInt("loanAmount");
                    int recomPaymentPeriod = loanResultSet.getInt("paymentPeriod");
                    String recomMemberNum = loanResultSet.getString("memberNumber");
                    int appNumber = loanResultSet.getInt("applicationNumber");
                    
                    // Loop through the application numbers and insert the distributed amounts into the recommended_loans table

                    double performance = calculateLoanPerformance(recomMemberNum);
                    if (performance<50) {

                        String updateSql = "UPDATE loanrequests SET status = ? WHERE memberNumber = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, "Your loan performance is loan ");
                updateStatement.setString(2, recomMemberNum);
                updateStatement.executeUpdate();
                updateStatement.close();
                        
                    }else{
               String updateSql = "UPDATE loanrequests SET status = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, "proccessing");
                updateStatement.executeUpdate();
                updateStatement.close();

                        String insertSql = "INSERT INTO recommended_loans (amount, paymentPeriod, memberNumber, applicationNumber) VALUES (?, ?, ?, ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                    insertStatement.setInt(1, recommendedAmount);
                    insertStatement.setInt(2, recomPaymentPeriod);
                    insertStatement.setString(3, recomMemberNum);
                    insertStatement.setInt(4, appNumber);
                    insertStatement.executeUpdate();
                    insertStatement.close();

                    }

                    
            
                }
                
                loanResultSet.close();
                selectStatement.close();
                countResultSet.close();
                countStatement.close();
                totalMoneyResultSet.close();
                totalMoneyStatement.close();
                connection.close();
    
                return "Your loan is being processed";

                }
               
            } else {
               
                double amountLevel = calculateTotalContribution(memberNumber);

                if (amountLevel < loanAmount) {
                    loanAmount =(int)amountLevel;
                    if (amountLevel == 0) {
                        return "We can not give you a loan because you have not contributed";
                        
                    }else {


                    String applicationNumber = generateApplicationNumber();
                String sql = "INSERT INTO loanrequests (loanAmount, paymentPeriod, memberNumber, applicationNumber) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, loanAmount);
                statement.setInt(2, paymentPeriod);
                statement.setString(3, memberNumber);
                statement.setString(4, applicationNumber);
                // Execute the insert
                statement.executeUpdate();
                statement.close();
    
                countResultSet.close();
                countStatement.close();
                totalMoneyResultSet.close();
                totalMoneyStatement.close();
                connection.close();
    
                // Increment the loan request count
                loanRequestCount++;
                totalLoan = totalLoan + loanAmount;
    
                // Return the loan application number to the user
                return "The amount requested is greater than 3/4 of your  total contribution. You can only receive " + loanAmount + " Your application number " + applicationNumber;

                    }


                    
                }else {
                     // Insert the loan request into the database
                String applicationNumber = generateApplicationNumber();
                String sql = "INSERT INTO loanrequests (loanAmount, paymentPeriod, memberNumber, applicationNumber) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, loanAmount);
                statement.setInt(2, paymentPeriod);
                statement.setString(3, memberNumber);
                statement.setString(4, applicationNumber);
                // Execute the insert
                statement.executeUpdate();
                statement.close();
    
                countResultSet.close();
                countStatement.close();
                totalMoneyResultSet.close();
                totalMoneyStatement.close();
                connection.close();
    
                // Increment the loan request count
                loanRequestCount++;
                totalLoan = totalLoan + loanAmount;
    
                // Return the loan application number to the user
                return "Loan request processed. Loan application number: " + applicationNumber;
                }
               
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
    
    
    
    

private static String generateApplicationNumber() {
    // Generate a unique application number based on your requirements
    // This can be a combination of letters, numbers, or any desired format
    // You can use libraries or algorithms to generate unique identifiers
    
    // Example: Generating a random 6-digit application number
    Random random = new Random();
    int applicationNumber = 100000 + random.nextInt(900000); // Range: 100000 to 999999
    return String.valueOf(applicationNumber);
}

    
    private static String performCheckLoanStatus(int applicationNumber) {
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Prepare the SQL statement
            String sql = "SELECT status FROM loanRequests WHERE applicationNumber = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, applicationNumber);
    
            // Execute the query
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                // Loan request found
                String status = resultSet.getString("status");
                if (status == "pending") {
                    resultSet.close();
                statement.close();
                connection.close();
                
                return "Your loan status is : " + status;
                    
                }else if (status == "proccessing"){
                      resultSet.close();
                statement.close();
                connection.close();
                
             return "Your loan status is : " + status;

                }else{
         
                    resultSet.close();
                  statement.close();
                   connection.close();
                    return "Your loan status is : " + status;
                }
                
            } else {
                // Loan request not found
                resultSet.close();
                statement.close();
                connection.close();
                return "Loan request not found";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
    
    
    private static String handleLoanAcceptance(String [] request) throws IOException, SQLException {
         
    
        String command = request[0];
         try {
          switch (command) {
            case "accept":
                // Perform login action
                return acceptLoan();
            case "reject":
                 return rejectLoan();
                 default:
                 return "Enter accept or reject";
        }
        } catch (NumberFormatException e) {
        return "Unknown command";
    }
        
    }

      private static String acceptLoan() throws SQLException {
         Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        // Prepare the SQL statement to insert into the references table
        try {
             String selectSql  = "SELECT * FROM loanrequests WHERE memberNumber = ? ";
        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        selectStatement.setString(1, memberNumber);
        ResultSet resultSet = selectStatement.executeQuery();
        if (resultSet.next()) {
            int recommendedAmount = resultSet.getInt("loanAmount");
            int recomPaymentPeriod = resultSet.getInt("paymentPeriod");
            String recomMemberNum = resultSet.getString("memberNumber");
            int appNumber = resultSet.getInt("applicationNumber");

            // Get the current date
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(currentDate);

            // Calculate the end date using the payment period as the number of months
            java.time.LocalDate startDate = java.time.LocalDate.now();
            java.time.LocalDate endDate = startDate.plusMonths(recomPaymentPeriod);

            // Convert LocalDate to String in the desired format
            String endDateString = endDate.toString();

            String sql = "INSERT INTO loandetails (applicationNumber, amount, startDate, endDate, memberNumber) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, appNumber);
            statement.setInt(2, recommendedAmount);
            statement.setString(3, formattedDate); // Assuming "startDate" is the current date
            statement.setString(4, endDateString);
            statement.setString(5, recomMemberNum);

            statement.executeUpdate();
            statement.close();
             String insertSql = "INSERT INTO loanpayment (applicationNumber, amount, startDate, memberNumber ) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setInt(1, appNumber);
            insertStatement.setInt(2, recommendedAmount);
            insertStatement.setString(3, formattedDate); // Assuming "startDate" is the current date
            insertStatement.setString(4, recomMemberNum);
            

            insertStatement.executeUpdate();
            insertStatement.close();

           String updateSql = "UPDATE loanrequests SET status = ? WHERE memberNumber = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, "accepted");
                updateStatement.setString(2, recomMemberNum);
                updateStatement.executeUpdate();
                updateStatement.close();

                String updateTotalMoneySql = "UPDATE saccoaccount SET totalmoney = totalmoney - ?";

                try (PreparedStatement updateTotalStatement = connection.prepareStatement(updateTotalMoneySql)) {
                    updateTotalStatement.setInt(1, recommendedAmount); // Set the deposit amount here
                
                    updateTotalStatement.executeUpdate();
                } catch (SQLException e) {
                    // Handle the exception
                    e.printStackTrace();
                }
                    
                return "Your loan payment period starts at :" + formattedDate;
        }else{
            
            return "Loan not found";
        }
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return "Database error";
        }
       
        
    }


    private static String rejectLoan() throws SQLException {
         Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        // Prepare the SQL statement to insert into the references table
        try {
           String updateSql = "UPDATE loanrequests SET status = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                updateStatement.setString(1, "rejected");
                updateStatement.executeUpdate();
                updateStatement.close();
                return "You've rejected the loan. Thank you for your collaboration :" ;
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return "Database error";
        }
       
        
    }
     
    private static String performCheckStatement(String dateFrom, String dateTo) {
        List<String> responses = new ArrayList<>();
        
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Prepare the SQL statements
            String contributionSql = "SELECT * FROM contributions WHERE memberNumber = ? AND date BETWEEN ? AND ? ORDER BY date";
            PreparedStatement contributionStatement = connection.prepareStatement(contributionSql);
            contributionStatement.setString(1, memberNumber);
            contributionStatement.setString(2, dateFrom);
            contributionStatement.setString(3, dateTo);
    
            String loanSql = "SELECT * FROM loandetails WHERE memberNumber = ? AND startDate BETWEEN ? AND ? ORDER BY startDate";
            PreparedStatement loanStatement = connection.prepareStatement(loanSql);
            loanStatement.setString(1, memberNumber);
            loanStatement.setString(2, dateFrom);
            loanStatement.setString(3, dateTo);
    
            // Execute the queries
            ResultSet contributionResult = contributionStatement.executeQuery();
            ResultSet loanResult = loanStatement.executeQuery();
            
            double myPerformance = calculateLoanPerformance(memberNumber);
            double contributionPerformance = calculateContributionPerformance(memberNumber);
            double SaccoPerformance = calculateSaccoPerformance();
    
            responses.add("            Contributions      ");
            while (contributionResult.next()) {
                // Extract relevant data from the ResultSet
                String depositDate = contributionResult.getString("date");
                int amountDeposited = contributionResult.getInt("amount");
                
                // Build the response for each record
                StringBuilder response = new StringBuilder();
                response.append("Date: ").append(depositDate).append(", ");
                response.append("Amount : ").append(amountDeposited);
                responses.add(response.toString());
            }
           responses.add("           -------------------------");
            responses.add("                 Loans      ");
            while (loanResult.next()) {
                String loanDate = loanResult.getString("startDate");
                int amount = loanResult.getInt("amount");
                String status = loanResult.getString("status");
                
                // Build the response for each record
                StringBuilder response = new StringBuilder();
               
                response.append("Date: ").append(loanDate).append(", ");
                response.append("Amount : ").append(amount).append(", ");
                response.append("Status : ").append(status);
                responses.add(response.toString());
            }
            responses.add("           -------------------------");
            responses.add("             Performance      ");
            responses.add("Percentage loan progress: " + myPerformance + " %");
            responses.add("Percentage contribution progress: " + contributionPerformance + " %");
            responses.add("Sacco performance: " + SaccoPerformance + " %");
            contributionResult.close();
            contributionStatement.close();
            loanResult.close();
            loanStatement.close();
            connection.close();
    
        } catch (SQLException e) {
            e.printStackTrace();
            responses.add("Database error");
        }
    
        String responseString = String.join("\t", responses);
        
        return responseString;
    }
    
    

    private static double calculateLoanPerformance(String memberNum) {
        double loanPerformance = 0.0; // Initialize the loan performance

        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        
            String sql = "SELECT * FROM loanpayment WHERE memberNumber = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, memberNum);
            ResultSet resultSet = statement.executeQuery();
        
            if (resultSet.next()) {
                int months = resultSet.getInt("loantracker");
                int amount = resultSet.getInt("amount");
                int amountPaid = resultSet.getInt("amountPaid");
        
                String selectSql = "SELECT * FROM loanrequests WHERE memberNumber = ? AND status = ?";
                PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                selectStatement.setString(1, memberNum);
                selectStatement.setString(2, "accepted");
                ResultSet result = selectStatement.executeQuery();
        
                if (result.next()) {
                    int paymentPeriod = result.getInt("paymentPeriod");
                    // Calculate loan performance with double precision
                    if (amountPaid ==0) {
                        loanPerformance =0.00;
                        
                    } else {
                         loanPerformance = (((double) amountPaid /((double) amount / paymentPeriod) ) / months) * 100;
                    }
                   
                }
        
                selectStatement.close();
            } else {
                loanPerformance = 100;
            }
        
            statement.close();
            connection.close();
        
        } catch (SQLException e) {
            e.printStackTrace();
            // You might want to handle the exception more gracefully here
        }
        
        return loanPerformance;
        
    }
    
   private static double calculateContributionPerformance(String memberNumber) throws SQLException {
    double contributionPerformance = 0.0;
    double targetAmount = 20000000;
    int contributionPeriod = 12;
    double monthlyContribution = targetAmount / contributionPeriod;
    String startDateStr = "2023-06-09";
    LocalDate startDate = LocalDate.parse(startDateStr); // Parse the start date
    LocalDate endDate = startDate.plusMonths(12); // Add 12 months to the start date
    LocalDate currentDate = LocalDate.now();
    Period period = Period.between(startDate, currentDate);
    int monthsTracker = period.getYears() * 12 + period.getMonths();

    try {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        String csql = "SELECT SUM(amount) AS totalContribution FROM contributions WHERE memberNumber = ? AND date BETWEEN ? AND ?";
        PreparedStatement statement = connection.prepareStatement(csql);
        statement.setString(1, memberNumber);
        statement.setString(2, startDateStr);
        statement.setString(3, endDate.toString()); // Set the end date here
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int amountContributed = resultSet.getInt("totalContribution");
            double monthsContributedFor = amountContributed / monthlyContribution;
            contributionPerformance = (monthsContributedFor / (double) monthsTracker) * 100;
            
            statement.close();
            connection.close();
        } else {
            contributionPerformance = 0.00;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return contributionPerformance;
}


 private static double calculateTotalContribution(String memberNumber) throws SQLException {
    double amountAllowed = 0.0;
    

    try {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        String csql = "SELECT SUM(amount) AS totalContribution FROM contributions WHERE memberNumber = ? ";
        PreparedStatement statement = connection.prepareStatement(csql);
        statement.setString(1, memberNumber);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int totalContrib = resultSet.getInt("totalContribution");
            amountAllowed = (3*totalContrib)/4;
            
            statement.close();
            connection.close();
        } else {
            amountAllowed = 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return amountAllowed;
}


 private static double calculateAverageContributionPerformanceForAllMembers() throws SQLException {
    double totalContributionPerformance = 0.0;
    int totalMembers = 0;
     Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

    try {
       
        String sql = "SELECT member_number FROM members";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            String memberNo = resultSet.getString("member_number");
            double contributionPerformance = calculateContributionPerformance(memberNo);
            totalContributionPerformance += contributionPerformance;
            totalMembers++;
        }

        statement.close();
        
    } catch (SQLException e) {
        e.printStackTrace();
    }

    if (totalMembers > 0) {
        String sql = "UPDATE saccoaccount SET contributionPerformance = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setDouble(1, (totalContributionPerformance/totalMembers));
        statement.executeUpdate();
        connection.close();
        return totalContributionPerformance / totalMembers;
    } else {
        return 0.0;
    }
}

 private static double calculateAverageLoanPerformanceForAllMembers() throws SQLException {
    double totalLoanPerformance = 0.0;
    int totalMembers = 0;
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

    try {

        String sql = "SELECT memberNumber FROM loanpayment";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            String memberNo = resultSet.getString("memberNumber");
            double loanPerformance = calculateLoanPerformance(memberNo);
            totalLoanPerformance += loanPerformance;
            totalMembers++;
        }

        statement.close();
        
    } catch (SQLException e) {
        e.printStackTrace();
    }

    if (totalMembers > 0) {
        String sql = "UPDATE saccoaccount SET loanPerformance = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        double averagePerformnce = totalLoanPerformance/totalMembers;
        statement.setDouble(1, (averagePerformnce));
        statement.executeUpdate();
        connection.close();
        return averagePerformnce;
        
    } else {
        return 0.0;
    }
}
private static double calculateSaccoPerformance(){
      double SaccoPerformance = 0.00;
    try {
         Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        System.out.println(calculateAverageContributionPerformanceForAllMembers());
        System.out.println(calculateAverageLoanPerformanceForAllMembers());
        SaccoPerformance = calculateAverageContributionPerformanceForAllMembers() + calculateAverageLoanPerformanceForAllMembers();
        
    } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
    }
    return SaccoPerformance;
}
}
