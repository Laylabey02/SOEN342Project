import java.util.ArrayList;


public final class RouteCatalogue {
    private static RouteCatalogue instance;
    private ArrayList<Route> routes;

    private RouteCatalogue() {
    }
    public static RouteCatalogue getInstance() {
        if(instance == null) {
            instance = new RouteCatalogue();
        }
        return instance;
    }
    public Route create(){
        return null;
    }
}
