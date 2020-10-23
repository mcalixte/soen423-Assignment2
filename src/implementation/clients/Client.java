package implementation.clients;


import StoreApp.Store;
import StoreApp.StoreHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static HashMap<String, HashMap<String, String>> completeCommandInterface = new HashMap<>();
    private static String userType;
    private static String provinceID;
    private static double budget = 1000.00;


    ////////////////////////////////////
    ///    User Related Fields       ///
    ////////////////////////////////////
    private static String quebecProvincePrefix = "QC";
    private static String ontarioProvincePrefix = "ON";
    private static String britishColumbiaProvincePrefix = "BC";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            Store quebecStore = StoreHelper.narrow(ncRef.resolve_str("quebecStore"));
            Store britishColumbiaStore = StoreHelper.narrow(ncRef.resolve_str("britishColumbiaStore"));
            Store ontarioStore = StoreHelper.narrow(ncRef.resolve_str("ontarioStore"));

            Scanner scanner = new Scanner(System.in);

            prepareCommandConsoleInterfaces(completeCommandInterface);

            while (true) {
                System.out.println("\t\t >>>>>>>>>> Welcome to the DSMS <<<<<<<<<<<<< \n\n");
                System.out.print("Please Specify what type of user you are. Type 'C' for Customer or 'M' for Manager.\n");
                System.out.print("User-Type: ");

                handleUserTypeInput(scanner);
                handleProvinceCodeInput(scanner);
                handleUserIDCreation(scanner);

                switch (provinceID.toLowerCase()) {
                    case "qc":
                        handleUserAction(scanner, quebecStore);
                        break;
                    case "on":
                        handleUserAction(scanner, ontarioStore);
                        break;
                    case "bc":
                        handleUserAction(scanner, britishColumbiaStore);
                        break;

                }
            }
        } catch (Exception e) {
            System.out.println("Hello Client exception: " + e);
        }
    }

    private static void handleProvinceCodeInput(Scanner scanner) {
        System.out.print("Enter your province Identifier: ");
        Client.provinceID = scanner.nextLine();
        if (verifyProvinceID(provinceID)) {
            Client.provinceID = provinceID;
        } else {
            System.out.print("Alert: Invalid province ID  \n\n");
            while (!verifyProvinceID(provinceID)) {
                System.out.print("Enter your province Identifier: ");
                Client.provinceID = scanner.nextLine();
            }
        }
    }

    private static boolean verifyProvinceID(String provinceID) {
        return provinceID.equalsIgnoreCase(quebecProvincePrefix) || provinceID.equalsIgnoreCase(ontarioProvincePrefix) || provinceID.equalsIgnoreCase(britishColumbiaProvincePrefix);
    }

    private static void prepareCommandConsoleInterfaces(HashMap<String, HashMap<String, String>> commandInterface) {
        HashMap<String, String> managerCommandInterfaces = new HashMap<>();
        HashMap<String, String> customerCommandInterfaces = new HashMap<>();

        String addCommandInterface = "\t>>>>>>>>>>> Add <<<<<<<<<<<\n"
                + "\trequired parameters:  Manager ID  , Item ID, Item name, Quantity, Price \n\n";

        String removeCommandInterface = "\t>>>>>>>>>>> Remove <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Manager ID  ,  Item ID, Quantity )\n\n";
        String listCommandInterface = "\t>>>>>>>>>>> List <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Manager ID )\n\n";


        String purchaseCommandInterface = "\t>>>>>>>>>>> Purchase <<<<<<<<<<<\n"
                + "\trequired parameters:  Customer ID  , Item ID \n\n";
        String findCommandInterface = "\t>>>>>>>>>>> Find <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Customer ID  ,  Item Name )\n\n";
        String returnCommandInterface = "\t>>>>>>>>>>> Return <<<<<<<<<<<\n"
                + "\trequired parameters:  ( Customer ID  ,  Item ID )\n\n";

        managerCommandInterfaces.put("add", addCommandInterface);
        managerCommandInterfaces.put("remove", removeCommandInterface);
        managerCommandInterfaces.put("list", listCommandInterface);
        completeCommandInterface.put("M", managerCommandInterfaces);

        customerCommandInterfaces.put("purchase", purchaseCommandInterface);
        customerCommandInterfaces.put("find", findCommandInterface);
        customerCommandInterfaces.put("return", returnCommandInterface);
        completeCommandInterface.put("C", customerCommandInterfaces);
    }

    private static void handleUserAction(Scanner scanner, Store store) {
        if (userType.toLowerCase().equalsIgnoreCase("M")) {
            System.out.println("Enter a command: ");

            String command = scanner.nextLine();
            while (!validateCommand(command, completeCommandInterface)) {
                System.out.println("Enter a command: ");
                command = scanner.nextLine();
            }
            displayCommandInterface(command);
            handleManagerInputParameters(command, scanner, store);
        } else if (userType.toLowerCase().equalsIgnoreCase("C")) {
            System.out.println("Enter a command: ");

            String command = scanner.nextLine();
            while (!validateCommand(command, completeCommandInterface)) {
                System.out.println("Enter a command: ");
                command = scanner.nextLine();
            }
            displayCommandInterface(command);
            handleCustomerInputParameters(command, scanner, store);
        }

    }

    private static void handleCustomerInputParameters(String command, Scanner scanner, Store store) {
        System.out.println("Alert: Be sure to enter the parameters in order \n");
        String customerID = "";
        String itemID = "";
        String itemName = "";
        String dateString;
        Date date = null;
        switch (command.toLowerCase()) {
            case "purchase":
                System.out.println("Enter the required Customer ID : ");
                customerID = scanner.nextLine();
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy\' : ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy\' : ");
                    dateString = scanner.nextLine();
                }
                purchaseItem(store, customerID, itemID, dateString);
                break;
            case "find":
                System.out.println("Enter the required Customer ID : ");
                customerID = scanner.nextLine();
                System.out.println("Enter the required item name : ");
                itemName = scanner.nextLine();
                findItem(store, customerID, itemName);
                break;
            case "return":
                System.out.println("Enter the required Customer ID : ");
                customerID = scanner.nextLine();
                System.out.println("Enter the required item ID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required date string, format \'dd/mm/yyyy\' : ");
                dateString = scanner.nextLine();
                while (!isDateFormatValid(dateString)) {
                    System.out.println("Date string invalid, try again...");
                    System.out.println("Enter the required date string, format \'dd/mm/yyyy\' : ");
                    dateString = scanner.nextLine();
                }
                returnItem(store, customerID, itemID, dateString);
                break;
        }

    }

    private static boolean isDateFormatValid(String dateString) { // mm/dd/yyyy HH:mm
        String strDateRegEx = "(0[1-9]|[12][0-9]|[3][01])\\/(0[1-9]|1[012])\\/\\d{4} (0[1-9]|[1][0-9]|2[0123]):([0-5][0-9])";
        return dateString.matches(strDateRegEx);
    }

    private static void handleManagerInputParameters(String command, Scanner scanner, Store store) {
        System.out.println("Alert: Be sure to enter the parameters in order \n");
        String managerID = null;
        String itemID = null;
        String itemName = null;
        int quantity = 0;
        double price = 0.00;
        switch (command.toLowerCase()) {
            case "add":
                System.out.println("Enter the required managerID : ");
                managerID = scanner.nextLine();
                System.out.println("Enter the required itemID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required itemName : ");
                itemName = scanner.nextLine();
                System.out.println("Enter the required quantity : ");
                quantity = scanner.nextInt();
                System.out.println("Enter the required price : ");
                price = scanner.nextDouble();
                addItem(store, managerID, itemID, itemName, quantity, price);
                break;
            case "remove":
                System.out.println("Enter the required managerID : ");
                managerID = scanner.nextLine();
                System.out.println("Enter the required itemID : ");
                itemID = scanner.nextLine();
                System.out.println("Enter the required quantity : ");
                quantity = scanner.nextInt();
                removeItem(store, managerID, itemID, quantity);
                break;
            case "list":
                System.out.println("Enter the required managerID : ");
                managerID = scanner.nextLine();
                listItemAvailability(store, managerID);
                break;
        }

    }

    private static void displayCommandInterface(String command) {
        for (Map.Entry<String, HashMap<String, String>> entry : completeCommandInterface.entrySet()) {
            for (Map.Entry<String, String> commandEntry : entry.getValue().entrySet())
                if (commandEntry.getKey().equalsIgnoreCase(command)) {
                    System.out.println("Here is the interface for your chosen method: \n");
                    System.out.println(commandEntry.getValue());
                }
        }
    }

    private static boolean validateCommand(String command, HashMap<String, HashMap<String, String>> completeInterface) {
        boolean isValidCommand = false;

        for (Map.Entry<String, HashMap<String, String>> entry : completeInterface.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(userType))
                for (Map.Entry<String, String> commandEntry : entry.getValue().entrySet())
                    if (commandEntry.getKey().equalsIgnoreCase(command))
                        isValidCommand = true;
        }

        return isValidCommand;
    }


    private static void handleUserIDCreation(Scanner scanner) {
        System.out.print("Time to create a user ID. ID structure  : [Province Prefix][M or C][4 digit number] \n\n");
        System.out.print("Enter your user ID: ");
        String userID = scanner.nextLine();

        while (!isUserIDValid(userID)) {
            System.out.print("Enter your user ID: ");
            userID = scanner.nextLine();
        }
    }

    private static boolean isUserIDValid(String userID) {
        String provinceID = userID.substring(0, 2);
        String userType = userID.substring(2, 3);
        String userNumber = userID.substring(3, userID.length() - 1);

        return verifyProvinceID(provinceID) || validateUserType(userType) || userNumber.length() == 4;
    }

    private static void handleUserTypeInput(Scanner scanner) {
        userType = scanner.nextLine();

        boolean invalidEntry = true;

        if (validateUserType(userType) && userType.equalsIgnoreCase("C")) {
            System.out.print("A Customer can execute the following actions in their own store: \n\n");
            for (Map.Entry<String, HashMap<String, String>> entry : completeCommandInterface.entrySet())
                if (entry.getKey().equalsIgnoreCase("C"))
                    for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                        System.out.println(command.getValue());
                    }

        } else if (validateUserType(userType) && userType.equalsIgnoreCase("M")) {
            System.out.print("A Manager can execute the following actions in their own store: \n\n");
            for (Map.Entry<String, HashMap<String, String>> entry : completeCommandInterface.entrySet())
                if (entry.getKey().equalsIgnoreCase("M"))
                    for (Map.Entry<String, String> command : entry.getValue().entrySet()) {
                        System.out.println(command.getValue());
                    }

        } else {
            System.out.println("Alert: Invalid user type entry. Please try again ...");
            while (invalidEntry) {
                System.out.print("User-Type: ");
                userType = scanner.nextLine();
                invalidEntry = validateUserType(userType);
            }
        }

    }

    private static boolean validateUserType(String userType) {
        return userType.toLowerCase().replace(" ", "").equalsIgnoreCase("C") || userType.toLowerCase().replace(" ", "").equalsIgnoreCase("M");
    }

    ////////////////////////////////////
    ///     CORBA remote Methods     ///
    ////////////////////////////////////

    public static void addItem(Store store, String managerID, String itemID, String itemName, int quantity, double price) {
        store.addItem(managerID, itemID, itemName, quantity, price);
    }

    public static void removeItem(Store store, String managerID, String itemID, int quantity) {
        store.removeItem(managerID, itemID, quantity);
    }

    public static void listItemAvailability(Store store, String managerID) {
        store.listItemAvailability(managerID);
    }

    public static void purchaseItem(Store store, String customerID, String itemID, String dateOfPurchase) {
        store.purchaseItem(customerID, itemID, dateOfPurchase);
    }

    public static void findItem(Store store, String customerID, String itemID) {
        store.findItem(customerID, itemID);
    }

    public static void returnItem(Store store, String customerID, String itemID, String dateOfReturn) {
        store.returnItem(customerID, itemID, dateOfReturn);
    }
}
