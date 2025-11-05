public class Ticket {
    private String ticketNumber;
    private String travelerName;
    private int age;
    private String identificationNumber;
    private boolean isFirstClass;
    private float price;
    
    public Ticket(String ticketNumber, String travelerName, int age, String identificationNumber, 
                  boolean isFirstClass, float price) {
        this.ticketNumber = ticketNumber;
        this.travelerName = travelerName;
        this.age = age;
        this.identificationNumber = identificationNumber;
        this.isFirstClass = isFirstClass;
        this.price = price;
    }
    

    public String getTicketNumber() { return ticketNumber; }
    public String getTravelerName() { return travelerName; }
    public int getAge() { return age; }
    public String getIdentificationNumber() { return identificationNumber; }
    public boolean isFirstClass() { return isFirstClass; }
    public float getPrice() { return price; }
    
    @Override
    public String toString() {
        return String.format("Ticket %s - %s (Age: %d, ID: %s) - %s Class - $%.2f", 
                           ticketNumber, travelerName, age, identificationNumber,
                           isFirstClass ? "First" : "Second", price);
    }
}



