import java.time.LocalTime;
import java.util.ArrayList;

public class Connection {

    private ArrayList<Route> connection;
    public Connection(ArrayList<Route> routes) {
        connection = new ArrayList<Route>();
    }
    public LocalTime totalDuration(){
        for(Route route : connection){
//logic to loop and sum up time
        }
    }
    public float totalCost(){

    }
}
