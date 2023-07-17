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

public class App {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/sacco_system";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "1234";

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
                if (tokens.length < 3) {
                    return "Invalid deposit request";
                }
                int amount = Integer.parseInt(tokens[1]);
                int receiptNumber = Integer.parseInt(tokens[2]);
                return performDeposit(amount, receiptNumber);
                case "loanRequest":
                // Perform deposit action
                if (tokens.length < 3) {
                    return "Invalid Loan  request";
                }
                int requestedamount = Integer.parseInt(tokens[1]);
                int paymentPeriod = Integer.parseInt(tokens[2]);
                return performLoanRequest(requestedamount, paymentPeriod);
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

    private static String performDeposit(int amount, int receiptNumber) {
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Check if the receiptNumber exists in the database
            String sql = "SELECT * FROM Deposit WHERE receiptNumber = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, receiptNumber);
    
            // Execute the query
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                // Amount exists, update the amount in the database
                sql = "UPDATE Deposit SET depositAmount = depositAmount + ? WHERE receiptNumber = ?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, amount);
                statement.setInt(2, receiptNumber);
    
                // Execute the update
                int rowsAffected = statement.executeUpdate();
    
                if (rowsAffected > 0) {
                    System.out.println("Amount updated successfully");
                    return "Amount updated successfully";
                } else {
                    return "Failed to update amount";
                }
            } else {
                // Amount doesn't exist in the database
                return "Receipt number not found";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
    private static String performLoanRequest(int amount, int paymentPeriod) {
        try {
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    
            // Check the count of existing requests
            String countQuery = "SELECT COUNT(requestID) AS requestCount FROM Request";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResultSet = countStatement.executeQuery();
            countResultSet.next();
            int requestCount = countResultSet.getInt("requestCount");
            
            if (requestCount < 10) {
                // Add a new request to the database
                String insertQuery = "INSERT INTO Request (amount, paymentPeriod) VALUES (?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setInt(1, amount);
                insertStatement.setInt(2, paymentPeriod);
                int rowsAffected = insertStatement.executeUpdate();
    
                if (rowsAffected > 0) {
                    System.out.println("Request added successfully");
                    return "Request added successfully";
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
    
}
