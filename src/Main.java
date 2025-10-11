import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        RouteCatalogue routeCatalogue = RouteCatalogue.getInstance();
        ConnectionCatalogue connectionCatalogue = ConnectionCatalogue.getInstance();

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter CSV file path:");
        String path = sc.nextLine().trim();

        // Load routes from CSV (mandatory now)
        routeCatalogue.loadFromCsv(path);

        System.out.println("From city:");
        String from = sc.nextLine().trim();
        System.out.println("To city:");
        String to = sc.nextLine().trim();
        System.out.println("Travel day (e.g., Mon/Tue/...):");
        String day = sc.nextLine().trim();
        System.out.println("Class (first/second):");
        boolean firstClass = sc.nextLine().trim().toLowerCase().startsWith("f");

        ArrayList<Connection> results = connectionCatalogue.computeConnections(
                routeCatalogue, from, to, day);

        if (results.isEmpty()) {
            System.out.println("No connections found.");
            return;
        }

        System.out.println("How would you like to sort the results? (duration/price):");
        String sortKey = sc.nextLine().trim();
        results = connectionCatalogue.sortBy(results, sortKey, firstClass);

        // Display all found connections
        System.out.println("\nConnections found:");
        for (int i = 0; i < results.size(); i++) {
            Connection c = results.get(i);
            long mins = c.totalDurationMinutes(10);
            System.out.printf("[%d] %s | duration=%dh%02d | %s class price=%.2f€\n",
                    i, c.toString(), mins / 60, mins % 60,
                    firstClass ? "first" : "second", c.totalCost(firstClass));
        }

        System.out.println("\nEnter the indexes of the connections you want (comma-separated), or press Enter for all:");
        String pick = sc.nextLine().trim();
        ArrayList<Integer> idxs = new ArrayList<>();
        if (!pick.isEmpty()) {
            for (String s : pick.split(",")) {
                try {
                    idxs.add(Integer.parseInt(s.trim()));
                } catch (Exception ignored) {}
            }
        } else {
            for (int i = 0; i < results.size(); i++) idxs.add(i);
        }

        System.out.println("\nYour selection:");
        for (int i : idxs) {
            if (i < 0 || i >= results.size()) continue;
            Connection c = results.get(i);
            long mins = c.totalDurationMinutes(10);
            System.out.printf("%s | duration=%dh%02d | %s class price=%.2f€\n",
                    c.toString(), mins / 60, mins % 60,
                    firstClass ? "first" : "second", c.totalCost(firstClass));
        }
    }
}
