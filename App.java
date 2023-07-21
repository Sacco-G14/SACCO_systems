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
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class App {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/sacco_system";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "should be your password";

    public static void main(String[] args) {
        try {
            // Register the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(3006);
            System.out.println("Server started. Listening on port 3006...");

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
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String processRequest(String request) {
        // Extract command and parameters from the request
        String[] tokens = request.split(" ");
        if (tokens.length < 1) {
            return "Invalid request";
        }
    
        String command = tokens[0];
    
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
                int amount = Integer.parseInt(tokens[1]);
                int receiptNumber = Integer.parseInt(tokens[2]);
                int memberId = Integer.parseInt(tokens[3]);
                return performDeposit(amount, receiptNumber,memberId);
            case "requestLoan":
                // Perform loan request action
                if (tokens.length < 4) {
                    return "Invalid loan request";
                }
                int requestedAmount = Integer.parseInt(tokens[1]);
                int paymentPeriod = Integer.parseInt(tokens[2]);
                String memberNumber = tokens[3];
                return performLoanRequest(requestedAmount, paymentPeriod, memberNumber);
             case "checkLoanStatus":
                // Perform loan request action
                if (tokens.length < 2) {
                    return "Invalid loan request";
                }
                int applicationNumber = Integer.parseInt(tokens[1]);
                
                return performCheckLoanStatus(applicationNumber);
            
            default:
                return "Unknown command";
        }
    }

    private static String performLogin(String username, String password) {
        try {
            // Establish a connection to the database
            
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Prepare the SQL statement
            String sql = "SELECT * FROM users WHERE name = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Login successful
                resultSet.close();
                statement.close();
                connection.close();
                 System.out.println("Correct credentials");
                return "Login successful";
               
            } else {
                // Invalid username or password
                resultSet.close();
                statement.close();
                connection.close();
                return "Invalid username or password";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    private static String performDeposit(int amount, int receiptNumber, int memberNumber) {
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Check if the member has an unpaid loan or balance
            String sql = "SELECT * FROM loans WHERE memberNumber = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, memberNumber);
    
            // Execute the query
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                // Update the balance and amountPaid in the loans table
                int loanId = resultSet.getInt("loanId");
                int balance = resultSet.getInt("balance");
                int amountPaid = resultSet.getInt("amountPaid");
    
                int newBalance = balance - amount;
    
                if (newBalance < 0) {
                    // Calculate the remaining amount after clearing the balance
                    int remainingAmount = Math.abs(newBalance);
    
                    // Update the balance to zero and update the amountPaid
                    sql = "UPDATE loans SET balance = 0, amountPaid = amountPaid + ? WHERE loanId = ?";
                    statement = connection.prepareStatement(sql);
                    statement.setInt(1, amountPaid + balance);
                    statement.setInt(2, loanId);
    
                    // Execute the update
                    int rowsAffected = statement.executeUpdate();
    
                    if (rowsAffected > 0) {
                        // Get the current date and time
                        LocalDateTime now = LocalDateTime.now();
                        Timestamp timestamp = Timestamp.valueOf(now);
    
                        // Insert the remaining amount and current date into the contributions table
                        sql = "INSERT INTO contributions (memberNumber, amount, date) VALUES (?, ?, ?)";
                        statement = connection.prepareStatement(sql);
                        statement.setInt(1, memberNumber);
                        statement.setInt(2, remainingAmount);
                        statement.setTimestamp(3, timestamp);
    
                        // Execute the insert
                        rowsAffected = statement.executeUpdate();
    
                        if (rowsAffected > 0) {
                            // Insert the deposit details into the deposit table
                            sql = "INSERT INTO deposit (memberNumber, depositAmount, receiptNumber,depositDate) VALUES (?, ?, ?, ?)";
                            statement = connection.prepareStatement(sql);
                            statement.setInt(1, memberNumber);
                            statement.setInt(2, amount);
                            statement.setInt(3, receiptNumber);
                            statement.setTimestamp(4, timestamp);
    
                            // Execute the insert
                            rowsAffected = statement.executeUpdate();
    
                            if (rowsAffected > 0) {
                                System.out.println("Balance cleared, remaining amount added to contributions, deposit recorded");
                                return "Balance cleared, remaining amount added to contributions, deposit recorded";
                            } else {
                                return "Failed to insert deposit details";
                            }
                        } else {
                            return "Failed to insert remaining amount";
                        }
                    } else {
                        return "Failed to update balance";
                    }
                } else {
                    // Update the balance and amountPaid in the loans table
                    sql = "UPDATE loans SET balance = ?, amountPaid = amountPaid + ? WHERE loanId = ?";
                    statement = connection.prepareStatement(sql);
                    statement.setInt(1, newBalance);
                    statement.setInt(2, amount);
                    statement.setInt(3, loanId);
    
                    // Execute the update
                    int rowsAffected = statement.executeUpdate();
    
                    if (rowsAffected > 0) {
                        // Get the current date and time
                        LocalDateTime now = LocalDateTime.now();
                        Timestamp timestamp = Timestamp.valueOf(now);
    
                        // Insert the full amount and current date into the contributions table
                        sql = "INSERT INTO contributions (memberNumber, amount, date) VALUES (?, ?, ?)";
                        statement = connection.prepareStatement(sql);
                        statement.setInt(1, memberNumber);
                        statement.setInt(2, amount);
                        statement.setTimestamp(3, timestamp);
    
                        // Execute the insert
                        rowsAffected = statement.executeUpdate();
    
                        if (rowsAffected > 0) {
                            
                            // Insert the deposit details into the deposit table
                            sql = "INSERT INTO deposit (memberNumber, depositAmount, receiptNumber,depositDate) VALUES (?, ?, ?, ?)";
                            statement = connection.prepareStatement(sql);
                            statement.setInt(1, memberNumber);
                            statement.setInt(2, amount);
                            statement.setInt(3, receiptNumber);
                            statement.setTimestamp(4, timestamp);
    
                            // Execute the insert
                            rowsAffected = statement.executeUpdate();
    
                            if (rowsAffected > 0) {
                                System.out.println("Balance updated and amount added to contributions, deposit recorded");
                                return "Balance updated and amount added to contributions, deposit recorded";
                            } else {
                                return "Failed to insert deposit details";
                            }
                        } else {
                            return "Failed to insert amount";
                        }
                    } else {
                        return "Failed to update balance";
                    }
                }
            } else {
                // No unpaid loan or balance found
                return "No unpaid loan or balance found for the member";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
    
    
    
    
   private static String performLoanRequest(int amount, int paymentPeriod, String memberId) {
    try {
        // Establish a connection to the database
        Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

        // Check the count of existing requests
        String countQuery = "SELECT COUNT(requestId) AS requestCount FROM loanRequests WHERE status = ?";
        PreparedStatement countStatement = connection.prepareStatement(countQuery);
        countStatement.setString(1, "pending"); // Set the parameter value before executing the query
        ResultSet countResultSet = countStatement.executeQuery();
        countResultSet.next();
        int requestCount = countResultSet.getInt("requestCount");

        if (requestCount < 10) {
            // Generate a unique application number
            String applicationNumber = generateApplicationNumber();

            // Add a new request to the database
            String insertQuery = "INSERT INTO loanRequests (applicationNumber, memberId, amount, paymentPeriod) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setString(1, applicationNumber);
            insertStatement.setString(2, memberId);
            insertStatement.setInt(3, amount);
            insertStatement.setInt(4, paymentPeriod);

            int rowsAffected = insertStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Request added successfully. Application Number: " + applicationNumber);
                return "Request added successfully. Application Number: " + applicationNumber;
            } else {
                return "Failed to add request";
            }
        } else {
            return "Request limit reached";
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

    
    

    /*  
    private static boolean checkFundsAvailability() {
        // Establish a connection to the database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // Retrieve the balance from the Sacco's account
            String sql = "SELECT balance FROM SaccoAccount";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double balance = resultSet.getDouble("balance");
                    return balance >= 2000000; // Return true if balance is sufficient
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if there's an error or insufficient balance
    }

    */
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
                resultSet.close();
                statement.close();
                connection.close();
                
                return "Your loan application is : " + status;
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
    
    
    
}
