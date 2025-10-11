import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        RouteCatalogue routeCatalogue = RouteCatalogue.getInstance();
        ConnectionCatalogue connectionCatalogue = ConnectionCatalogue.getInstance();
        Scanner sc = new Scanner(System.in);

        // 1) Load CSV
        System.out.println("Enter path to eu_rail_network.csv:");
        String csvPath = sc.nextLine().trim();
        routeCatalogue.loadFromCsv(csvPath);

        // 2) Get search inputs
        System.out.println("From city:");
        String from = sc.nextLine().trim();

        System.out.println("To city:");
        String to = sc.nextLine().trim();

        System.out.println("Travel day (e.g., Mon/Tue/...):");
        String day = sc.nextLine().trim();

        System.out.println("Class (first/second):");
        boolean firstClass = sc.nextLine().trim().toLowerCase().startsWith("f");

        // 3) Compute + sort
        ArrayList<Connection> results = connectionCatalogue.compute(routeCatalogue, from, to, day);
        if (results.isEmpty()) {
            System.out.println("No connections found.");
            return;
        }

        System.out.println("Sort by (duration/price):");
        String key = sc.nextLine().trim();
        results = connectionCatalogue.sort(results, key, firstClass);

        // 4) Show and choose
        for (int i=0; i<results.size(); i++) {
            Connection c = results.get(i);
            System.out.printf("[%d] %s | duration=%s | %s class price=%.2f%n",
                    i, c, c.totalDuration(), firstClass ? "first" : "second", c.totalCost(firstClass));
        }

        System.out.println("Enter indexes to select (comma-separated) or press Enter for all:");
        String pick = sc.nextLine().trim();
        ArrayList<Integer> chosen = new ArrayList<>();
        if (pick.isEmpty()) {
            for (int i=0; i<results.size(); i++) chosen.add(i);
        } else {
            for (String s : pick.split(",")) {
                try { chosen.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
            }
        }

        System.out.println("\nYour selection:");
        for (int i : chosen) {
            if (i < 0 || i >= results.size()) continue;
            Connection c = results.get(i);
            System.out.printf("%s | duration=%s | %s class price=%.2f%n",
                    c, c.totalDuration(), firstClass ? "first" : "second", c.totalCost(firstClass));
        }
    }
}