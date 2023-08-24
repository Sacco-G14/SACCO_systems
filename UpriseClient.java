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
    
            while (true) {
                
                System.out.println("*********WELCOME TO UPRISE-SACCO COMMAND LINE INTERFACE********");
                System.out.println("      ");
                // Prompt for command, username, password, and additional details separated by spaces:
                System.out.println("Enter the following command to login (or 'exit' to quit):");
                System.out.println("|-----------login Username PassWord----------");
                
                String userInput = consoleReader.readLine().trim();
    
                if (userInput.equalsIgnoreCase("exit")) {
                     System.out.println("\n----------------------------------------");
                    System.out.println("Exiting Uprise Sacco client. Goodbye!");
                     System.out.println("----------------------------------------\n");
                    break;
                }
    
                String[] inputTokens = userInput.split(" ");
                if (inputTokens.length < 3) {
                    System.out.println("---------------------------");
                    System.out.println("Invalid input format. Please try again.");
                    System.out.println("---------------------------\n");
                    continue;
                }
    
                String command = inputTokens[0];
                String username = inputTokens[1];
                String password = inputTokens[2];
    
                switch (command) {
                    case "login":
                        login(input, output, username, password);
                        break;
                    default:
                    System.out.println("\n---------------------------");
                        System.out.println("Invalid command. Please try again.");
                        System.out.println("---------------------------\n");
                        break;
                }
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
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

       
            // Send login request to the server
            output.println("login " + username + " " + password);

            // Get server response
            String serverResponse = input.readLine();

            // Check if login was successful
            if (serverResponse.startsWith("Login successful")) {
                System.out.println("\n=======================");
            System.out.println(serverResponse);
            System.out.println("==============================\n");
                executeCommands(input, output);
            } else {
                 System.out.println("\n-------------------------------------------------------------------");
                System.out.println("Invalid username or password. enter your member number and phone number . \n");
                 System.out.println("-------------------------------------------------------------------\n");
                String [] refInputs = consoleReader.readLine().split(" ");
                requestReference(input, output,refInputs);
            }
        }
    

    private static void executeCommands(BufferedReader input, PrintWriter output) throws IOException {
        boolean continueExecution = true;
    
        while (continueExecution) {
            System.out.println("                      ");
            System.out.println("----------------------------USE THE FOLLOWING COMMANDS TO TRANSACT-------------------------");
            System.out.println("                      ");
            System.out.println("1. deposit |---------------command amount_deposited receiptNumber  dateDeposited(year-month-day)_----|");
            System.out.println("2. checkLoanStatus |-------command applicationNumber-------------------------------------------------|");
            System.out.println("3. checkStatement |--------command dateFrom(year-month-day) dateTo(year-month-day--------------------|");
            System.out.println("4. requestLoan |-----------command amount paymentPeriod memberMumber---------------------------------|");
            System.out.println("5. checkReference |-----------command referenceNumberr---------------------------------|");
            System.out.println("6. exit |------------------Enter the exit command to quit and to log out-----------------------------|");
            System.out.println("        ");
            System.out.println("Enter a command followed by its corresponding details separated by spaces:");
            System.out.println("           ");
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
                 case "checkReference":
                    reference(input, output, inputTokens);
                    break;    
                case "checkLoanStatus":
                    checkLoanStatus(input, output, inputTokens);
                    break;
                case "exit":
                 System.out.println("\n--------------------------------------");
                    System.out.println("You have been logged out successfuly!");
                    System.out.println("-----------------------------------\n");
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
        System.out.println("------------------------SERVER RESPONSE-----------------------");
        System.out.println( serverResponse);
          System.out.println("------------------------------------------------------------------\n");
    }

     private static void reference(BufferedReader input, PrintWriter output, String[] commandTokens) throws IOException {
        // Extract additional details from commandTokens and perform deposit action
        // Send deposit request to the server
        output.println(String.join(" ", commandTokens));
    
        // Get server response
        String serverResponse = input.readLine();
         System.out.println("\n" + "------------------------SERVER RESPONSE-----------------------");
        System.out.println( serverResponse);
          System.out.println("------------------------------------------------------------------\n");
    }

     private static void requestReference(BufferedReader input, PrintWriter output, String[] refInputs) throws IOException {
        // Extract additional details from commandTokens and perform deposit action
        // Send deposit request to the server
        output.println(String.join(" ", refInputs));
    
        // Get server response
        String serverResponse = input.readLine();
         System.out.println("\n" + "------------------------SERVER RESPONSE-----------------------");
        System.out.println(serverResponse);
          System.out.println("------------------------------------------------------------------\n");
    }
    
    private static void requestLoan(BufferedReader input, PrintWriter output, String[] requestTokens) throws IOException {
        // Extract additional details from requestTokens and perform requestLoan action
        // Send requestLoan request to the server
        output.println(String.join(" ", requestTokens));
    
        // Get server response
        String serverResponse = input.readLine();
         System.out.println("------------------------SERVER RESPONSE-----------------------");
        System.out.println( serverResponse);
          System.out.println("------------------------------------------------------------------\n");
    }
    

    private static void checkStatement(BufferedReader input, PrintWriter output, String[] commandTokens) throws IOException {
        // Extract additional details from commandTokens and perform checkStatement action
        // Send checkStatement request to the server
        output.println(String.join(" ", commandTokens));
            
        // Get server response
        String serverResponse = input.readLine();
            
        if (serverResponse.startsWith("No deposits found")) {
            System.out.println("------------------------SERVER RESPONSE-----------------------");
            System.out.println(serverResponse);
            System.out.println("------------------------------------------------------------------\n");
        } else {
            // Split the response into individual deposit records using newline ("\n") as delimiter
            String[] depositRecords = serverResponse.split("\t");
            
            
           System.out.println("------------------------SERVER RESPONSE-----------------------");
            for (String record : depositRecords) {
                System.out.println(record);
            }
            System.out.println("------------------------------------------------------------------\n");
        }
    }
    
    
    
    
    
    
    

    private static void checkLoanStatus(BufferedReader input, PrintWriter output, String[] loanStatusTokens) throws IOException {
        // Extract additional details from commandTokens and perform checkLoanStatus action
        // Send checkLoanStatus request to the server
      
        output.println(String.join(" ", loanStatusTokens));
    
        // Get server response
        String serverResponse = input.readLine();
    
        // Check if the server response is "approved" and ask the user to enter a command accordingly
        if (serverResponse.equals("approved")) {
            acceptOrRejectLoan(input, output);
        } else {
            System.out.println("\n--------------------------------------------------");
            System.out.println( serverResponse);
            System.out.println("--------------------------------------------------\n");
            // Handle other scenarios based on the server response if needed.
        }
    }

    private static void acceptOrRejectLoan( BufferedReader input, PrintWriter output) throws IOException{
         BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
         System.out.println("\n--------------------------------------------------");
        System.out.println("Your loan request has been approved. \n Please enter  accept to accept or reject to reject the loan:");
        System.out.println("---------------------------------------------------\n");
        String command = consoleReader.readLine();
        output.println(command);
        
       String serverResponse = input.readLine();
        System.out.println("Server: " + serverResponse);
    }
}
