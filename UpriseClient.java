import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UpriseClient {

    public static void main(String[] args) {
        try {
            // Connect to the server
            Socket socket = new Socket("localhost", 3006);

            // Get the input and output streams for communication
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Create a reader to read user input from the console
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // Prompt for command, username, and password
            System.out.println("Enter command, username, password, and additional details separated by spaces:");
            String[] inputTokens = consoleReader.readLine().split(" ");
            
            if (inputTokens.length < 3) {
                System.out.println("Invalid input format. Please try again.");
            }
            
            String command = inputTokens[0];
            String username = inputTokens[1];
            String password = inputTokens[2];

            switch (command) {
                case "login":
                    login(input, output, username, password);
                    break;
                case "exit":
                    System.out.println("Exiting Uprise Sacco client. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
                    break;
            }

            // Close the streams and socket
            consoleReader.close();
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private static void login(BufferedReader input, PrintWriter output, String username, String password) throws IOException {
        System.out.println("Welcome to Uprise Sacco Command Line Login page!");
        int loginAttempts = 0;

        while (loginAttempts < 3) {
            // Send login request to the server
            output.println("login " + username + " " + password);

            // Get server response
            String serverResponse = input.readLine();
            System.out.println("Server: " + serverResponse);

            // Check if login was successful
            if (serverResponse.equals("Login successful")) {
                System.out.println("Login successful");
                executeCommands(input, output);
                break;
            } else {
                System.out.println("Invalid username or password. Please try again.");
                loginAttempts++;
            }
        }
    }

    private static void executeCommands(BufferedReader input, PrintWriter output) throws IOException {
        boolean continueExecution = true;
    
        while (continueExecution) {
            System.out.println("1. deposit");
            System.out.println("2. checkLoanStatus");
            System.out.println("3. checkStatement");
            System.out.println("4. requestLoan");
    
            System.out.println("Enter a command followed by its details:");
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String[] inputTokens = consoleReader.readLine().split(" ");
            String commandName = inputTokens[0];
    
            switch (commandName) {
                case "deposit":
                    deposit(input, output, inputTokens);
                    break;
                case "requestLoan":
                    requestLoan(input, output, inputTokens);
                    break;
                case "checkStatement":
                    checkStatement(input, output, inputTokens);
                    break;
                case "checkLoanStatus":
                    checkLoanStatus(input, output, inputTokens);
                    break;
                case "exit":
                    System.out.println("Exiting Uprise Sacco client. Goodbye!");
                    continueExecution = false;
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
                    break;
            }
        }
    }
    
    

    private static void deposit(BufferedReader input, PrintWriter output, String[] commandTokens) throws IOException {
        // Extract additional details from commandTokens and perform deposit action
        // Send deposit request to the server
        output.println(String.join(" ", commandTokens));
    
        // Get server response
        String serverResponse = input.readLine();
        System.out.println("Server: " + serverResponse);
    }
    
    private static void requestLoan(BufferedReader input, PrintWriter output, String[] commandTokens) throws IOException {
        // Extract additional details from commandTokens and perform requestLoan action
        // Send requestLoan request to the server
        output.println(String.join(" ", commandTokens));

        // Get server response
        String serverResponse = input.readLine();
        System.out.println("Server: " + serverResponse);
    }

    private static void checkStatement(BufferedReader input, PrintWriter output, String[] commandTokens) throws IOException {
        // Extract additional details from commandTokens and perform checkStatement action
        // Send checkStatement request to the server
        output.println(String.join(" ", commandTokens));

        // Get server response
        String serverResponse = input.readLine();
        System.out.println("Server: " + serverResponse);
    }

    private static void checkLoanStatus(BufferedReader input, PrintWriter output, String[] commandTokens) throws IOException {
        // Extract additional details from commandTokens and perform checkLoanStatus action
        // Send checkLoanStatus request to the server
        output.println(String.join(" ", commandTokens));

        // Get server response
        String serverResponse = input.readLine();
        System.out.println("Server: " + serverResponse);
    }
}
