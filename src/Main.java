import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RouteCatalogue routeCatalogue = RouteCatalogue.getInstance();
        ConnectionCatalogue connectionCatalogue = ConnectionCatalogue.getInstance();
        Scanner sc = new Scanner(System.in);

        // 1) Load CSV

        String csvPath = "eu_rail_network.csv";
        try {
            routeCatalogue.loadFromCsv(csvPath);
        } catch (Exception e) {
            System.out.println("Failed to load CSV: " + e.getMessage());
            return;
        }


        //Gather query inputs
        System.out.println("From city:");
        String from = sc.nextLine().trim();

        System.out.println("To city:");
        String to = sc.nextLine().trim();

        System.out.println("Travel day (e.g., Mon/Tue/Wed):");
        String day = sc.nextLine().trim();

        System.out.println("Class (first/second):");
        boolean firstClass = sc.nextLine().trim().toLowerCase().startsWith("f");

        //Compute possible connections (0–2 stops)
        ArrayList<Connection> results = connectionCatalogue.compute(routeCatalogue, from, to, day);
        if (results.isEmpty()) {
            System.out.println("No connections found for the given inputs.");
            return;
        }

        //Sort preference
        System.out.println("Sort by (duration/price):");
        String sortKey = sc.nextLine().trim();
        results = connectionCatalogue.sort(results, sortKey, firstClass);

        //Display results
        System.out.println("\nConnections found:");
        for (int i = 0; i < results.size(); i++) {
            Connection c = results.get(i);
            System.out.printf("[%d] %s | duration=%s | %s class price=%.2f%n",
                    i, c.toString(), c.totalDuration(),
                    firstClass ? "first" : "second", c.totalCost(firstClass));
        }

        //Let user choose connections
        System.out.println("\nEnter indexes to select (comma-separated), or press Enter for all:");
        String pick = sc.nextLine().trim();

        ArrayList<Integer> chosen = new ArrayList<>();
        if (pick.isEmpty()) {
            for (int i = 0; i < results.size(); i++) chosen.add(i);
        } else {
            for (String s : pick.split(",")) {
                try { chosen.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
            }
        }

        //Show selection
        System.out.println("\nYour selection:");
        for (int i : chosen) {
            if (i < 0 || i >= results.size()) continue;
            Connection c = results.get(i);
            System.out.printf("%s | duration=%s | %s class price=%.2f%n",
                    c.toString(), c.totalDuration(),
                    firstClass ? "first" : "second", c.totalCost(firstClass));
        }
    }
}
