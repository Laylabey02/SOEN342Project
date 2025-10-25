import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public final class RouteCatalogue {
    private static RouteCatalogue instance;
    private final ArrayList<Route> routes;

    private RouteCatalogue() {
        this.routes = new ArrayList<>();
    }

    public static RouteCatalogue getInstance() {
        if (instance == null) instance = new RouteCatalogue();
        return instance;
    }

    public ArrayList<Route> getRoutes() { return routes; }

    public Route create(String routeID,
                        LocalTime departureTime,
                        LocalTime arrivalTime,
                        String departureCity,
                        String arrivalCity,
                        String trainType,
                        float firstClassTicket,
                        float secondClassTicket,
                        ArrayList<String> daysOfOperation) {
        Route r = new Route(routeID, departureTime, arrivalTime, departureCity, arrivalCity,
                trainType, firstClassTicket, secondClassTicket, daysOfOperation);
        routes.add(r);
        return r;
    }


    public void loadFromCsv(String path) throws Exception {
        if (!Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("CSV file not found: " + path);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String first = br.readLine(); //read first line to skip
            if (first == null) return; //if it was empty return

            //Detect delimiter by counting separators in the first line
            char delim = detectDelimiter(first);

            //Check if the first line looks like a header
            String[] firstParts = splitCsv(first, delim);
            boolean hasHeader = looksLikeHeader(firstParts);

            HashMap<String,Integer> idx = new HashMap<>();
            if (hasHeader) {
                for (int i = 0; i < firstParts.length; i++) {
                    String key = firstParts[i].trim().toLowerCase(Locale.ROOT);
                    idx.put(key, i);
                }
            } else {
                idx.put("routeid", 0);
                idx.put("departurecity", 1);
                idx.put("arrivalcity", 2);
                idx.put("departuretime", 3);
                idx.put("arrivaltime", 4);
                idx.put("traintype", 5);
                idx.put("days", 6);
                idx.put("firstclass", 7);
                idx.put("secondclass", 8);
                // process the already-read first line below as a data row
            }

            //If first line is a header, proceed to next lines; else process it as data
            if (hasHeader) {
                // continue
            } else {
                processRow(firstParts, idx);
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = splitCsv(line, delim);
                processRow(parts, idx);
            }
        }
    }


    private char detectDelimiter(String line) {
        int commas = count(line, ',');
        int semis  = count(line, ';');
        return (semis > commas) ? ';' : ',';
    }

    private int count(String s, char c) {
        int n=0; for (int i=0;i<s.length();i++) if (s.charAt(i)==c) n++; return n;
    }

    private boolean looksLikeHeader(String[] cells) {
        if (cells.length < 7) return false;
        String joined = String.join(" ", cells).toLowerCase(Locale.ROOT);
        return joined.contains("route") || joined.contains("depart") || joined.contains("arrival")
                || joined.contains("time") || joined.contains("train") || joined.contains("days");
    }


    private String[] splitCsv(String line, char delim) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i=0;i<line.length();i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == delim && !inQuotes) {
                out.add(cur.toString().trim().replaceAll("^\"|\"$", ""));
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString().trim().replaceAll("^\"|\"$", ""));
        return out.toArray(new String[0]);
    }

    private String get(String[] parts, HashMap<String,Integer> idx, String... keys) {
        for (String k : keys) {
            Integer i = idx.get(k);
            if (i != null && i >= 0 && i < parts.length) return parts[i].trim();
        }

        for (String k : keys) {
            for (String key : idx.keySet()) {
                if (key.contains(k)) {
                    Integer i = idx.get(key);
                    if (i != null && i < parts.length) return parts[i].trim();
                }
            }
        }
        return null;
    }

    private void processRow(String[] parts, HashMap<String,Integer> idx) {
        //Pull fields with tolerant keys
        String routeId = get(parts, idx, "routeid", "route id", "id");
        String depCity = get(parts, idx, "departurecity", "departure city", "from", "origin");
        String arrCity = get(parts, idx, "arrivalcity", "arrival city", "to", "destination");
        String depTime = get(parts, idx, "departuretime", "departure time", "depart", "dep time");
        String arrTime = get(parts, idx, "arrivaltime", "arrival time", "arrive", "arr time");
        String train   = get(parts, idx, "traintype", "train type", "type");
        String days    = get(parts, idx, "daysofoperation", "days of operation", "days", "operationdays");
        String first   = get(parts, idx, "firstclassticketrate", "first class ticket rate", "firstclass", "firstclassticket", "firstclassprice");
        String second  = get(parts, idx, "secondclassticketrate", "second class ticket rate", "secondclass", "secondclassticket", "secondclassprice");

        if (routeId == null || depCity == null || arrCity == null ||
                depTime == null || arrTime == null || train == null ||
                days == null || first == null || second == null) {
            // Skip malformed row silently
            return;
        }

        //Handle overnight arrivals
        boolean isOvernight = arrTime.contains("(+1d)");
        String cleanArrTime = arrTime.replaceAll("\\s*\\(\\+1d\\)", "");
        
        LocalTime dep = LocalTime.parse(depTime);
        LocalTime arr = LocalTime.parse(cleanArrTime);

        float firstPrice  = Float.parseFloat(first.replaceAll("[^0-9.]", ""));
        float secondPrice = Float.parseFloat(second.replaceAll("[^0-9.]", ""));

        ArrayList<String> dayList = parseDays(days);

        Route route = create(routeId, dep, arr, depCity, arrCity, train, firstPrice, secondPrice, dayList);
        if (isOvernight) {
            route.setOvernight(true);
        }
    }

    private ArrayList<String> parseDays(String days) {
        ArrayList<String> dayList = new ArrayList<>();
        
        if (days.equalsIgnoreCase("Daily")) {
            // Add all days for daily service
            dayList.add("Mon"); dayList.add("Tue"); dayList.add("Wed"); 
            dayList.add("Thu"); dayList.add("Fri"); dayList.add("Sat"); dayList.add("Sun");
        } else {
            //Handle range formats like "Fri-Sun", "Mon-Fri"
            if (days.contains("-")) {
                String[] parts = days.split("-");
                if (parts.length == 2) {
                    String startDay = parts[0].trim();
                    String endDay = parts[1].trim();
                    dayList.addAll(expandDayRange(startDay, endDay));
                }
            } else {
                //Handle comma-separated or space-separated individual days
                String[] tokens = days.split("[/\\s,]+");
                for (String t : tokens) {
                    if (!t.isBlank()) {
                        dayList.add(t.trim());
                    }
                }
            }
        }
        
        return dayList;
    }

    private ArrayList<String> expandDayRange(String startDay, String endDay) {
        ArrayList<String> days = new ArrayList<>();
        String[] dayOrder = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        
        int startIndex = -1, endIndex = -1;
        for (int i = 0; i < dayOrder.length; i++) {
            if (dayOrder[i].equalsIgnoreCase(startDay)) startIndex = i;
            if (dayOrder[i].equalsIgnoreCase(endDay)) endIndex = i;
        }
        
        if (startIndex != -1 && endIndex != -1) {
            if (startIndex <= endIndex) {
                //Normal range (e.g., Mon-Fri)
                for (int i = startIndex; i <= endIndex; i++) {
                    days.add(dayOrder[i]);
                }
            } else {
                //Wraparound range (e.g., Fri-Mon)
                for (int i = startIndex; i < dayOrder.length; i++) {
                    days.add(dayOrder[i]);
                }
                for (int i = 0; i <= endIndex; i++) {
                    days.add(dayOrder[i]);
                }
            }
        }
        
        return days;
    }
}
