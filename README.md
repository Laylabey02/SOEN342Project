# SOEN342Project  
**SOEN 342 – Software Requirements and Deployment (Fall 2025)**  
**Concordia University**

#### Group Members
- Layla beylouneh - 40264291
- Maximilian Grabowski 40251210
- Mohamed Ali Bahi - 40282763



## Project Overview
This Java application models a **railway network among European cities**.  
It loads route data from a CSV file, allows clients to **search for direct or indirect connections**, **book trips**, and **view current or past trips**.  
Data persistence is implemented using an **SQLite database**.

---

## Main Features
- **Load Records** from CSV into memory  
- **Search Connections** by parameters (city, time, price, etc.)  
- **Show Direct and Indirect (1–2 stop) Routes**  
- **Sort Results** by trip duration or ticket price  
- **Book Trips** with layover time policies  
- **View Trips** (current and history) by client ID and name  
- **SQLite Persistence** for routes, trips, and clients  


---

## Technologies
- **Language:** Java  
- **Database:** SQLite  
- **Libraries:** JDBC, java.sql, java.time  
- **IDE:** IntelliJ IDEA / VS Code  
- **Version Control:** GitHub  

#### Extra Important Note for grading:
- We pushed all our artifacts for each iteration by the due date in the form of issues. If you check our "closed issues" section, you will be able to see that we pushed all diagrams iteration by iteration. Since Christopher gave us a feedback that no artifacts were done, we decided to group the diagrams in folders and we pushed it onto github. If you see that the date is 11-14. It is not. The diagrams were done and pushed in issues by the due dates! We simply grouped them in folders and commited that onto github. 

---

## How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/Laylabey02/SOEN342Project.git
   cd soen342-train-system
