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

---

## How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/Laylabey02/SOEN342Project.git
   cd soen342-train-system
