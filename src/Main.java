import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static RouteGateway routeGateway;
    private static ConnectionCatalogue connectionCatalogue;
    private static TripGateway tripGateway;
    private static ClientGateway clientGateway;
    private static Scanner sc;

    public static void main(String[] args) {
        initializeSystem();
        
        // loop - allows multiple searches/bookings
        boolean continueProgram = true;
        while (continueProgram) {
            System.out.println("\n=== NEW SEARCH ===");

            // Gather search criteria
            String[] searchParams = gatherSearchCriteria();
            String from = searchParams[0];
            String to = searchParams[1];
            String day = searchParams[2];
            String arrivalTime = searchParams[3];
            String departureTime = searchParams[4];
            String ticketCost = searchParams[5];
            String trainType = searchParams[6];
            boolean firstClass = Boolean.parseBoolean(searchParams[7]);

            // Search and display connections
            ArrayList<Connection> results = searchAndDisplayConnections(from, to, day, arrivalTime, departureTime, ticketCost, trainType, firstClass);
            if (results == null || results.isEmpty()) {
                continueProgram = askToContinue("Would you like to search again? (yes/no):");
                continue;
            }

            results = sortConnections(results, firstClass);

            displayConnections(results, firstClass);

            Connection selectedConnection = selectConnection(results, firstClass);
            if (selectedConnection == null) {
                continueProgram = askToContinue("Would you like to search again? (yes/no):");
                continue;
            }

            book(selectedConnection, firstClass);

            continueProgram = askToContinue("Would you like to search or book another trip? (yes/no):");
        }
        
        System.out.println("\nThank you for using the rail booking system!");
        sc.close();
    }

    private static void initializeSystem() {
        DataBaseTableInitializer.initializeDatabase();
        System.out.println("Setup complete. You can now use the database!");

        routeGateway = RouteGateway.getInstance();
        connectionCatalogue = ConnectionCatalogue.getInstance();
        tripGateway = TripGateway.getInstance();
        clientGateway = ClientGateway.getInstance();
        sc = new Scanner(System.in);

        String csvPath = "eu_rail_network.csv";
        try {
            routeGateway.loadFromCsv(csvPath);
        } catch (Exception e) {
            System.out.println("Failed to load CSV: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String[] gatherSearchCriteria() {
        // From city - required
        String from;
        while (true) {
            System.out.println("From city:");
            from = sc.nextLine().trim();
            if (from == null || from.isEmpty()) {
                System.out.println("Error: From city cannot be empty. Please enter a city name.");
                continue;
            }
            break;
        }

        // To city - required
        String to;
        while (true) {
            System.out.println("To city:");
            to = sc.nextLine().trim();
            if (to == null || to.isEmpty()) {
                System.out.println("Error: To city cannot be empty. Please enter a city name.");
                continue;
            }
            break;
        }

        // Travel day - required
        String day;
        while (true) {
            System.out.println("Travel day (First 3 letters e.g., Mon Tue Wed):");
            day = sc.nextLine().trim();
            if (day == null || day.isEmpty()) {
                System.out.println("Error: Travel day cannot be empty. Please enter first 3 letters.");
                continue;
            }
            break;
        }

        String arrivalTime;
        while (true) {
            System.out.println("Arrival Time (HH:MM format, or leave empty for any time):");
            arrivalTime = sc.nextLine().trim();
            if (arrivalTime == null || arrivalTime.isEmpty()) {
                break; // Empty is allowed
            }
            if (isValidTimeFormat(arrivalTime)) {
                break;
            } else {
                System.out.println("Error: Invalid time format. Please use HH:MM format (e.g., 14:30) or leave empty.");
            }
        }

        String departureTime;
        while (true) {
            System.out.println("Departure Time (HH:MM format, or leave empty for any time):");
            departureTime = sc.nextLine().trim();
            if (departureTime == null || departureTime.isEmpty()) {
                break; // Empty is allowed
            }
            if (isValidTimeFormat(departureTime)) {
                break;
            } else {
                System.out.println("Error: Invalid time format. Please use HH:MM format (e.g., 09:15) or leave empty.");
            }
        }

        String ticketCost;
        while (true) {
            System.out.println("Ticket cost lower than (or leave empty for any price):");
            ticketCost = sc.nextLine().trim();
            if (ticketCost == null || ticketCost.isEmpty()) {
                break; // Empty is allowed
            }
            if (isValidPrice(ticketCost)) {
                break;
            } else {
                System.out.println("Error: Invalid price format. Please enter a positive number (e.g., 150.50) or leave empty.");
            }
        }

        System.out.println("Train Type (or leave empty for any type):");
        String trainType = sc.nextLine().trim();

        boolean firstClass;
        while (true) {
            System.out.println("Class (first/second):");
            String classInput = sc.nextLine().trim().toLowerCase();
            if (classInput.startsWith("f")) {
                firstClass = true;
                break;
            } else if (classInput.startsWith("s") || classInput.isEmpty()) {
                firstClass = false;
                break;
            } else {
                System.out.println("Error: Please enter 'first' or 'second' (or 'f'/'s').");
            }
        }
        
        return new String[]{from, to, day, arrivalTime, departureTime, ticketCost, trainType, String.valueOf(firstClass)};
    }


    private static boolean isValidTimeFormat(String time) {
        if (time == null || time.isEmpty()) {
            return false;
        }
        try {
            LocalTime.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isValidPrice(String price) {
        if (price == null || price.isEmpty()) {
            return false;
        }
        try {
            float priceValue = Float.parseFloat(price);
            return priceValue > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static ArrayList<Connection> searchAndDisplayConnections(String from, String to, String day, 
                                                                      String arrivalTime, String departureTime, 
                                                                      String ticketCost, String trainType, boolean firstClass) {
        ArrayList<Connection> results = connectionCatalogue.searchForConnections(
            routeGateway, 
            from, 
            to, 
            day,
            arrivalTime.isEmpty() ? null : arrivalTime, 
            departureTime.isEmpty() ? null : departureTime, 
            ticketCost.isEmpty() ? null : ticketCost, 
            trainType.isEmpty() ? null : trainType, 
            firstClass
        );
        
        if (results.isEmpty()) {
            System.out.println("\nNo connections found for the given inputs.");
            return null;
        }
        
        return results;
    }

    private static ArrayList<Connection> sortConnections(ArrayList<Connection> results, boolean firstClass) {
        System.out.println("Sort by (duration/price):");
        String sortKey = sc.nextLine().trim();
        return connectionCatalogue.sort(results, sortKey, firstClass);
    }

    private static void displayConnections(ArrayList<Connection> results, boolean firstClass) {
        System.out.println("\nConnections found:");
        for (int i = 0; i < results.size(); i++) {
            Connection c = results.get(i);
            System.out.printf("[%d] %s | duration=%s | %s class price=%.2f%n",
                    i, c.toString(), c.totalDuration(),
                    firstClass ? "first" : "second", c.totalCost(firstClass));
        }
    }

    private static Connection selectConnection(ArrayList<Connection> results, boolean firstClass) {
        System.out.println("\nEnter the index of the connection you want to book:");
        String pick = sc.nextLine().trim();
        
        int selectedIndex = -1;
        try {
            selectedIndex = Integer.parseInt(pick.trim());
        } catch (Exception e) {
            System.out.println("Invalid index. Please try again.");
            return null;
        }
        
        if (selectedIndex < 0 || selectedIndex >= results.size()) {
            System.out.println("Invalid index. Please try again.");
            return null;
        }

        Connection selectedConnection = results.get(selectedIndex);
        
        System.out.println("\nYour selection:");
        System.out.printf("%s | duration=%s | %s class price=%.2f%n",
                selectedConnection.toString(), selectedConnection.totalDuration(),
                firstClass ? "first" : "second", selectedConnection.totalCost(firstClass));
        
        return selectedConnection;
    }

    private static void book(Connection selectedConnection, boolean firstClass) {
        System.out.println("\nWould you like to book this trip? (yes/no):");
        String bookChoice = sc.nextLine().trim().toLowerCase();
    
        if (!bookChoice.startsWith("y")) {
            return;
        }

        String[] clientInfo = gatherClientInformation();
        if (clientInfo == null) {
            return;
        }

        ArrayList<String> travelerNames = new ArrayList<>();
        ArrayList<Integer> ages = new ArrayList<>();
        ArrayList<String> identificationNumbers = new ArrayList<>();
        
        if (!gatherTravelerInformation(travelerNames, ages, identificationNumbers)) {
            return;
        }

        createTicketsForTravelers(selectedConnection, clientInfo[0], clientInfo[1], clientInfo[2], 
                                   travelerNames, ages, identificationNumbers, firstClass);
    }

    private static String[] gatherClientInformation() {
        System.out.println("\n=== BOOKING INFORMATION ===");
        String lastName = "";
        String firstName = "";
        String id = "";

        while (true) {
            System.out.println("Client Last Name:");
            lastName = sc.nextLine().trim();
            if (lastName == null || lastName.isEmpty()) {
                System.out.println("Error: Client Last Name cannot be empty. Please try again.");
                continue;
            }
            break;
        }

        while (true) {
            System.out.println("Client First Name:");
            firstName = sc.nextLine().trim();
            if (firstName == null || firstName.isEmpty()) {
                System.out.println("Error: Client First Name cannot be empty. Please try again.");
                continue;
            }
            break;
        }

        while (true) {
            System.out.println("Client ID:");
            id = sc.nextLine().trim();
            if (id == null || id.isEmpty()) {
                System.out.println("Error: Client ID cannot be empty. Please try again.");
                continue;
            }
            
            // Check if client ID already exists with different name
            if (clientGateway.hasDifferentName(id, firstName, lastName)) {
                System.out.println("Error: Client ID already exists with different name. ");
                continue;
            }
            break;
        }
        
        return new String[]{lastName, firstName, id};
    }

    private static boolean gatherTravelerInformation(ArrayList<String> travelerNames, 
                                                      ArrayList<Integer> ages, 
                                                      ArrayList<String> identificationNumbers) {
        System.out.println("\nNumber of travelers:");
        int numTravelers;
        try {
            numTravelers = Integer.parseInt(sc.nextLine().trim());
            if (numTravelers <= 0) {
                System.out.println("Error: Number of travelers must be positive.");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number of travelers.");
            return false;
        }
        
        for (int i = 0; i < numTravelers; i++) {
            System.out.println("\nTraveler " + (i + 1) + " information:");

            String travelerName = "";
            while (true) {
                System.out.println("Name:");
                travelerName = sc.nextLine().trim();
                if (travelerName == null || travelerName.isEmpty()) {
                    System.out.println("Error: Traveler name cannot be empty. Please try again.");
                    continue;
                }
                travelerNames.add(travelerName);
                break;
            }

            int age = -1;
            while (true) {
                System.out.println("Age:");
                try {
                    String ageInput = sc.nextLine().trim();
                    if (ageInput == null || ageInput.isEmpty()) {
                        System.out.println("Error: Age cannot be empty. Please try again.");
                        continue;
                    }
                    age = Integer.parseInt(ageInput);
                    if (age <= 0) {
                        System.out.println("Error: Age must be a positive number. Please try again.");
                        continue;
                    }
                    ages.add(age);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Error: Age must be a valid number. Please try again.");
                }
            }

            System.out.println("ID Number:");
            identificationNumbers.add(sc.nextLine().trim());
        }
        
        return true;
    }

    private static void createTicketsForTravelers(Connection selectedConnection, String clientLastName, 
                                                   String clientFirstName, String clientId,
                                                   ArrayList<String> travelerNames, 
                                                   ArrayList<Integer> ages, 
                                                   ArrayList<String> identificationNumbers, 
                                                   boolean firstClass) {

        Client client = clientGateway.findClient(clientId);
        if (client == null) {
            client = new Client(clientLastName, clientFirstName, clientId);
            clientGateway.insert(client);
        }

        ArrayList<Connection> selectedConnections = new ArrayList<>();
        selectedConnections.add(selectedConnection);

        Trip trip = tripGateway.createTrip(
            clientLastName, 
            clientId, 
            selectedConnections,
            travelerNames, 
            ages, 
            identificationNumbers, 
            firstClass
        );

        client.addTrip(trip);
        
        System.out.println("\n=== BOOKING CONFIRMED ===");
        System.out.println(trip.toString());

        viewTrips(clientLastName, clientId);
    }

    private static void viewTrips(String clientLastName, String clientId) {
        System.out.println("\nWould you like to view your trip history? (yes/no):");
        String viewHistory = sc.nextLine().trim().toLowerCase();
        
        if (viewHistory.startsWith("y")) {
            System.out.println("\n=== TRIP HISTORY ===");
            ArrayList<Trip> clientTrips = tripGateway.getTripsByClient(clientLastName, clientId);
            if (clientTrips.isEmpty()) {
                System.out.println("No trips found for this client.");
            } else {
                for (Trip t : clientTrips) {
                    System.out.println(t.toString());
                    System.out.println("---");
                }
            }
        }
    }

    private static boolean askToContinue(String message) {
        System.out.println(message);
        String response = sc.nextLine().trim().toLowerCase();
        return response.startsWith("y");
    }
}
