import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DataBaseTableInitializer.initializeDatabase();
        System.out.println("Setup complete. You can now use the database!");

        RouteCatalogue routeCatalogue = RouteCatalogue.getInstance();
        ConnectionCatalogue connectionCatalogue = ConnectionCatalogue.getInstance();
        TripCatalogue tripCatalogue = TripCatalogue.getInstance();
        ClientCatalogue clientCatalogue = ClientCatalogue.getInstance();
        Scanner sc = new Scanner(System.in);

        //load CSV
        String csvPath = "eu_rail_network.csv";
        try {
            routeCatalogue.loadFromCsv(csvPath);
        } catch (Exception e) {
            System.out.println("Failed to load CSV: " + e.getMessage());
            return;
        }

        // loop - allows multiple searches/bookings
        boolean continueProgram = true;
        while (continueProgram) {
            //Gather user inputs
            System.out.println("\n=== NEW SEARCH ===");
            System.out.println("From city:");
            String from = sc.nextLine().trim();

            System.out.println("To city:");
            String to = sc.nextLine().trim();

            System.out.println("Travel day (e.g., Mon/Tue/Wed):");
            String day = sc.nextLine().trim();

            System.out.println("Class (first/second):");
            boolean firstClass = sc.nextLine().trim().toLowerCase().startsWith("f");

            //show possible connections
            ArrayList<Connection> results = connectionCatalogue.compute(routeCatalogue, from, to, day);
            if (results.isEmpty()) {
                System.out.println("\nNo connections found for the given inputs.");
                System.out.println("Would you like to search again? (yes/no):");
                String retry = sc.nextLine().trim().toLowerCase();
                if (!retry.startsWith("y")) {
                    continueProgram = false;
                }
                continue;
            }

            //Sort preference
            System.out.println("Sort by (duration/price):");
            String sortKey = sc.nextLine().trim();
            results = connectionCatalogue.sort(results, sortKey, firstClass);

            //results
            System.out.println("\nConnections found:");
            for (int i = 0; i < results.size(); i++) {
                Connection c = results.get(i);
                System.out.printf("[%d] %s | duration=%s | %s class price=%.2f%n",
                        i, c.toString(), c.totalDuration(),
                        firstClass ? "first" : "second", c.totalCost(firstClass));
            }

            //user choose connection
            System.out.println("\nEnter the index of the connection you want to book:");
            String pick = sc.nextLine().trim();
            
            int selectedIndex = -1;
            try {
                selectedIndex = Integer.parseInt(pick.trim());
            } catch (Exception e) {
                System.out.println("Invalid index. Please try again.");
                System.out.println("Would you like to search again? (yes/no):");
                String retry = sc.nextLine().trim().toLowerCase();
                if (!retry.startsWith("y")) {
                    continueProgram = false;
                }
                continue;
            }
            
            if (selectedIndex < 0 || selectedIndex >= results.size()) {
                System.out.println("Invalid index. Please try again.");
                System.out.println("Would you like to search again? (yes/no):");
                String retry = sc.nextLine().trim().toLowerCase();
                if (!retry.startsWith("y")) {
                    continueProgram = false;
                }
                continue;
            }

            //show selection
            Connection selectedConnection = results.get(selectedIndex);
            ArrayList<Connection> selectedConnections = new ArrayList<>();
            selectedConnections.add(selectedConnection);
            
            System.out.println("\nYour selection:");
            System.out.printf("%s | duration=%s | %s class price=%.2f%n",
                    selectedConnection.toString(), selectedConnection.totalDuration(),
                    firstClass ? "first" : "second", selectedConnection.totalCost(firstClass));
            
            //ask if user wants to book
            System.out.println("\nWould you like to book this trip? (yes/no):");
            String bookChoice = sc.nextLine().trim().toLowerCase();
        
        if (bookChoice.startsWith("y")) {
            //get client information
            System.out.println("\n=== BOOKING INFORMATION ===");
            System.out.println("Client Last Name:");
            String clientLastName = sc.nextLine().trim();
            
            System.out.println("Client First Name:");
            String clientFirstName = sc.nextLine().trim();
            
            System.out.println("Client ID:");
            String clientId = sc.nextLine().trim();
            
            //get traveler information
            System.out.println("\nNumber of travelers:");
            int numTravelers = Integer.parseInt(sc.nextLine().trim());
            
            ArrayList<String> travelerNames = new ArrayList<>();
            ArrayList<Integer> ages = new ArrayList<>();
            ArrayList<String> identificationNumbers = new ArrayList<>();
            
            for (int i = 0; i < numTravelers; i++) {
                System.out.println("\nTraveler " + (i + 1) + " information:");
                System.out.println("Name:");
                travelerNames.add(sc.nextLine().trim());
                
                System.out.println("Age:");
                ages.add(Integer.parseInt(sc.nextLine().trim()));
                
                System.out.println("ID Number:");
                identificationNumbers.add(sc.nextLine().trim());
            }
            
            // create client
            String fullName = clientFirstName + " " + clientLastName;
            Client client = clientCatalogue.findClient(clientId);
            if (client == null) {
                client = clientCatalogue.create(fullName, 0, clientId);
                client = new Client(clientLastName, clientFirstName, clientId);
            }
            
            // create trip
            Trip trip = tripCatalogue.createTrip(clientLastName, clientId, selectedConnections,
                                                travelerNames, ages, identificationNumbers, firstClass);
            
            //add trip to client's history
            client.addTrip(trip);
            
            System.out.println("\n=== BOOKING CONFIRMED ===");
            System.out.println(trip.toString());
            
            // ask if user wants to view trip history
            System.out.println("\nWould you like to view your trip history? (yes/no):");
            String viewHistory = sc.nextLine().trim().toLowerCase();
            
            if (viewHistory.startsWith("y")) {
                System.out.println("\n=== TRIP HISTORY ===");
                ArrayList<Trip> clientTrips = tripCatalogue.getTripsByClient(clientLastName, clientId);
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
        
        // ask if user wants to search/book again
        System.out.println("\nWould you like to search or book another trip? (yes/no):");
        String continueChoice = sc.nextLine().trim().toLowerCase();
        if (!continueChoice.startsWith("y")) {
            continueProgram = false;
        }
        }
        
        System.out.println("\nThank you for using the rail booking system!");
    }
}
