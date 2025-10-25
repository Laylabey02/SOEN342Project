import java.util.ArrayList;
import java.util.HashMap;

public class ClientCatalogue {
    private static ClientCatalogue instance;
    private HashMap<String, Client> clients;
    
    private ClientCatalogue() {
        this.clients = new HashMap<>();
    }
    
    public static ClientCatalogue getInstance() {
        if (instance == null) {
            instance = new ClientCatalogue();
        }
        return instance;
    }
    
    public Client create(String name, int age, String clientID) {
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts.length > 1 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : nameParts[0];
        
        Client client = new Client(lastName, firstName, clientID);
        clients.put(clientID, client);
        return client;
    }
    
    public Client findClient(String clientID) {
        return clients.get(clientID);
    }
    
    public ArrayList<Client> getAllClients() {
        return new ArrayList<>(clients.values());
    }
}
