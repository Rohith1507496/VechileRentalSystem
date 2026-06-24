// ============================================================
//   VEHICLE RENTAL SYSTEM — Java Intermediate Project
//   Covers: OOP, Inheritance, Encapsulation, Polymorphism,
//           Abstraction, Access Modifiers, Exception Handling,
//           JDBC, Interfaces, Packages (simulated), DSA
// ============================================================

// ── Simulated package declarations (all in one file) ─────────
// package com.rental.model;
// package com.rental.service;
// package com.rental.dsa;
// package com.rental.exception;
// package com.rental.db;

import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 1 – CUSTOM EXCEPTIONS                          ║
// ╚══════════════════════════════════════════════════════════╝

/** Base exception for all rental-related errors. */
class RentalException extends Exception {
    public RentalException(String message) {
        super(message);
    }
}

/** Thrown when a vehicle is already rented out. */
class VehicleNotAvailableException extends RentalException {
    private final String vehicleId;

    public VehicleNotAvailableException(String vehicleId) {
        super("Vehicle [" + vehicleId + "] is not available for rent.");
        this.vehicleId = vehicleId;
    }

    public String getVehicleId() { return vehicleId; }
}

/** Thrown when a customer is not found in the system. */
class CustomerNotFoundException extends RentalException {
    public CustomerNotFoundException(String customerId) {
        super("Customer with ID [" + customerId + "] not found.");
    }
}

/** Thrown for invalid date ranges. */
class InvalidDateException extends RentalException {
    public InvalidDateException(String message) {
        super(message);
    }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 2 – INTERFACES                                 ║
// ╚══════════════════════════════════════════════════════════╝

/**
 * Interface: Rentable
 * Any object that can be rented must implement this.
 */
interface Rentable {
    void rent(String customerId, LocalDate from, LocalDate to)
            throws VehicleNotAvailableException, InvalidDateException;
    void returnVehicle();
    double calculateCost(long days);
    boolean isAvailable();
}

/**
 * Interface: Maintainable
 * Vehicles that need maintenance scheduling.
 */
interface Maintainable {
    void scheduleMaintenance(LocalDate date);
    boolean needsMaintenance();
    String getMaintenanceStatus();
}

/**
 * Interface: Printable
 * Any entity that can print its details.
 */
interface Printable {
    void printDetails();
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 3 – ABSTRACT BASE CLASS (Abstraction)          ║
// ╚══════════════════════════════════════════════════════════╝

/**
 * Abstract class: Vehicle
 * Demonstrates: Abstraction, Encapsulation, Access Modifiers
 */
abstract class Vehicle implements Rentable, Maintainable, Printable {

    // Private fields → Encapsulation
    private final String vehicleId;
    private final String brand;
    private final String model;
    private final int year;
    private double dailyRate;
    private boolean available;
    private LocalDate maintenanceDue;

    // Protected fields accessible to subclasses
    protected String currentRentedBy;
    protected LocalDate rentalStart;
    protected LocalDate rentalEnd;

    // Constructor
    public Vehicle(String vehicleId, String brand, String model, int year, double dailyRate) {
        if (vehicleId == null || vehicleId.isBlank())
            throw new IllegalArgumentException("Vehicle ID cannot be empty.");
        if (dailyRate <= 0)
            throw new IllegalArgumentException("Daily rate must be positive.");

        this.vehicleId  = vehicleId;
        this.brand      = brand;
        this.model      = model;
        this.year       = year;
        this.dailyRate  = dailyRate;
        this.available  = true;
        this.maintenanceDue = LocalDate.now().plusMonths(3);
    }

    // ── Abstract method (must be overridden) ─────────────
    public abstract String getVehicleType();
    public abstract String getFuelType();

    // ── Rentable implementation ───────────────────────────
    @Override
    public void rent(String customerId, LocalDate from, LocalDate to)
            throws VehicleNotAvailableException, InvalidDateException {
        if (!available)
            throw new VehicleNotAvailableException(vehicleId);
        if (from == null || to == null || !to.isAfter(from))
            throw new InvalidDateException("Return date must be after pickup date.");

        this.available       = false;
        this.currentRentedBy = customerId;
        this.rentalStart     = from;
        this.rentalEnd       = to;

        System.out.println("✅ " + getVehicleType() + " [" + vehicleId + "] rented to customer "
                + customerId + " from " + from + " to " + to);
    }

    @Override
    public void returnVehicle() {
        System.out.println("🔄 " + getVehicleType() + " [" + vehicleId + "] returned by "
                + currentRentedBy);
        this.available       = true;
        this.currentRentedBy = null;
        this.rentalStart     = null;
        this.rentalEnd       = null;
    }

    @Override
    public boolean isAvailable() { return available; }

    // ── Maintainable implementation ───────────────────────
    @Override
    public void scheduleMaintenance(LocalDate date) {
        this.maintenanceDue = date;
        System.out.println("🔧 Maintenance scheduled for [" + vehicleId + "] on " + date);
    }

    @Override
    public boolean needsMaintenance() {
        return !LocalDate.now().isBefore(maintenanceDue);
    }

    @Override
    public String getMaintenanceStatus() {
        return needsMaintenance() ? "⚠ OVERDUE (due: " + maintenanceDue + ")"
                                  : "✔ OK (due: " + maintenanceDue + ")";
    }

    // ── Printable implementation ──────────────────────────
    @Override
    public void printDetails() {
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.printf("│ %-40s│%n", getVehicleType() + " — " + brand + " " + model);
        System.out.printf("│  ID        : %-27s│%n", vehicleId);
        System.out.printf("│  Year      : %-27s│%n", year);
        System.out.printf("│  Fuel      : %-27s│%n", getFuelType());
        System.out.printf("│  Rate/Day  : ₹%-26.2f│%n", dailyRate);
        System.out.printf("│  Status    : %-27s│%n", available ? "Available" : "Rented → " + currentRentedBy);
        System.out.printf("│  Maint.    : %-27s│%n", getMaintenanceStatus());
        System.out.println("└─────────────────────────────────────────┘");
    }

    // ── Getters (Encapsulation) ───────────────────────────
    public String getVehicleId()  { return vehicleId; }
    public String getBrand()      { return brand; }
    public String getModel()      { return model; }
    public int    getYear()       { return year; }
    public double getDailyRate()  { return dailyRate; }

    // ── Setter with validation ────────────────────────────
    public void setDailyRate(double rate) {
        if (rate <= 0) throw new IllegalArgumentException("Rate must be positive.");
        this.dailyRate = rate;
    }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 4 – CONCRETE VEHICLE CLASSES (Inheritance)     ║
// ╚══════════════════════════════════════════════════════════╝

/** Car — extends Vehicle. */
class Car extends Vehicle {
    private final int numberOfDoors;
    private final String transmission; // "Manual" | "Automatic"

    public Car(String id, String brand, String model, int year,
               double rate, int doors, String transmission) {
        super(id, brand, model, year, rate);
        this.numberOfDoors = doors;
        this.transmission  = transmission;
    }

    @Override public String getVehicleType() { return "Car"; }
    @Override public String getFuelType()    { return "Petrol/Diesel"; }

    // Polymorphism: overrides calculateCost
    @Override
    public double calculateCost(long days) {
        double base = getDailyRate() * days;
        // Automatic cars cost 10% more
        return transmission.equalsIgnoreCase("Automatic") ? base * 1.10 : base;
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.printf("   Doors: %d | Transmission: %s%n%n", numberOfDoors, transmission);
    }

    public String getTransmission() { return transmission; }
}

/** Bike — extends Vehicle. */
class Bike extends Vehicle {
    private final int engineCC;
    private final boolean hasSidecar;

    public Bike(String id, String brand, String model, int year,
                double rate, int engineCC, boolean hasSidecar) {
        super(id, brand, model, year, rate);
        this.engineCC   = engineCC;
        this.hasSidecar = hasSidecar;
    }

    @Override public String getVehicleType() { return "Bike"; }
    @Override public String getFuelType()    { return "Petrol"; }

    // Polymorphism: bikes charge flat weekend surcharge
    @Override
    public double calculateCost(long days) {
        double base = getDailyRate() * days;
        long weeks = days / 7;
        return base + (weeks * 100); // ₹100 per week surcharge
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.printf("   Engine: %dcc | Sidecar: %s%n%n",
                engineCC, hasSidecar ? "Yes" : "No");
    }
}

/** ElectricCar — extends Car (multi-level inheritance). */
class ElectricCar extends Car {
    private final int batteryRangeKm;
    private double chargeLevelPercent;

    public ElectricCar(String id, String brand, String model, int year,
                       double rate, int doors, int batteryRangeKm) {
        super(id, brand, model, year, rate, doors, "Automatic");
        this.batteryRangeKm    = batteryRangeKm;
        this.chargeLevelPercent = 100.0;
    }

    @Override public String getVehicleType() { return "Electric Car"; }
    @Override public String getFuelType()    { return "Electric"; }

    // Polymorphism: electric cars have a green discount
    @Override
    public double calculateCost(long days) {
        return super.calculateCost(days) * 0.90; // 10% green discount
    }

    public void charge(double percent) {
        this.chargeLevelPercent = Math.min(100, chargeLevelPercent + percent);
        System.out.printf("⚡ %s [%s] charged to %.1f%%%n",
                getBrand(), getVehicleId(), chargeLevelPercent);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.printf("   Battery Range: %dkm | Charge: %.1f%%%n%n",
                batteryRangeKm, chargeLevelPercent);
    }
}

/** Truck — extends Vehicle. */
class Truck extends Vehicle {
    private final double payloadTons;
    private final int axles;

    public Truck(String id, String brand, String model, int year,
                 double rate, double payloadTons, int axles) {
        super(id, brand, model, year, rate);
        this.payloadTons = payloadTons;
        this.axles       = axles;
    }

    @Override public String getVehicleType() { return "Truck"; }
    @Override public String getFuelType()    { return "Diesel"; }

    // Polymorphism: trucks charge per axle per day
    @Override
    public double calculateCost(long days) {
        return (getDailyRate() + (axles * 200)) * days;
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.printf("   Payload: %.1f tons | Axles: %d%n%n", payloadTons, axles);
    }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 5 – CUSTOMER CLASS                             ║
// ╚══════════════════════════════════════════════════════════╝

class Customer implements Printable {
    private final String customerId;
    private String name;
    private String phone;
    private String email;
    private String licenseNumber;
    private final List<String> rentalHistory; // DSA: List

    public Customer(String customerId, String name, String phone,
                    String email, String licenseNumber) {
        this.customerId    = customerId;
        this.name          = name;
        this.phone         = phone;
        this.email         = email;
        this.licenseNumber = licenseNumber;
        this.rentalHistory = new ArrayList<>();
    }

    public void addRentalRecord(String record) {
        rentalHistory.add(record);
    }

    public List<String> getRentalHistory() {
        return Collections.unmodifiableList(rentalHistory);
    }

    @Override
    public void printDetails() {
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.printf("│ Customer: %-31s│%n", name);
        System.out.printf("│  ID      : %-30s│%n", customerId);
        System.out.printf("│  Phone   : %-30s│%n", phone);
        System.out.printf("│  Email   : %-30s│%n", email);
        System.out.printf("│  License : %-30s│%n", licenseNumber);
        System.out.printf("│  Rentals : %-30d│%n", rentalHistory.size());
        System.out.println("└─────────────────────────────────────────┘");
    }

    // Getters & Setters
    public String getCustomerId()    { return customerId; }
    public String getName()          { return name; }
    public void   setName(String n)  { this.name = n; }
    public String getPhone()         { return phone; }
    public String getEmail()         { return email; }
    public String getLicenseNumber() { return licenseNumber; }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 6 – RENTAL RECORD                             ║
// ╚══════════════════════════════════════════════════════════╝

class RentalRecord {
    private final String recordId;
    private final String vehicleId;
    private final String customerId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final double totalCost;
    private final String vehicleType;

    public RentalRecord(String recordId, String vehicleId, String customerId,
                        LocalDate from, LocalDate to, double totalCost, String vehicleType) {
        this.recordId    = recordId;
        this.vehicleId   = vehicleId;
        this.customerId  = customerId;
        this.fromDate    = from;
        this.toDate      = to;
        this.totalCost   = totalCost;
        this.vehicleType = vehicleType;
    }

    public void printReceipt() {
        long days = ChronoUnit.DAYS.between(fromDate, toDate);
        System.out.println("\n══════════════ RENTAL RECEIPT ══════════════");
        System.out.printf("  Record ID    : %s%n",   recordId);
        System.out.printf("  Vehicle      : %s [%s]%n", vehicleType, vehicleId);
        System.out.printf("  Customer     : %s%n",   customerId);
        System.out.printf("  From → To    : %s → %s%n", fromDate, toDate);
        System.out.printf("  Duration     : %d day(s)%n", days);
        System.out.printf("  Total Cost   : ₹%.2f%n", totalCost);
        System.out.println("════════════════════════════════════════════\n");
    }

    // Getters
    public String    getRecordId()   { return recordId; }
    public String    getVehicleId()  { return vehicleId; }
    public String    getCustomerId() { return customerId; }
    public LocalDate getFromDate()   { return fromDate; }
    public LocalDate getToDate()     { return toDate; }
    public double    getTotalCost()  { return totalCost; }
    public String    getVehicleType(){ return vehicleType; }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 7 – DSA STRUCTURES                             ║
// ╚══════════════════════════════════════════════════════════╝

// ── 7A: Generic Stack (used for undo/redo actions) ─────────
class Stack<T> {
    private final LinkedList<T> list = new LinkedList<>();

    public void push(T item)  { list.addFirst(item); }
    public T    pop()         { if (isEmpty()) throw new NoSuchElementException("Stack underflow"); return list.removeFirst(); }
    public T    peek()        { if (isEmpty()) throw new NoSuchElementException("Stack is empty"); return list.getFirst(); }
    public boolean isEmpty()  { return list.isEmpty(); }
    public int  size()        { return list.size(); }

    @Override public String toString() { return list.toString(); }
}

// ── 7B: Min-Heap (Priority Queue) for cheapest vehicle ────
class VehicleMinHeap {
    private final List<Vehicle> heap = new ArrayList<>();

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i)   { return 2 * i + 1; }
    private int right(int i)  { return 2 * i + 2; }

    private void swap(int i, int j) {
        Vehicle tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }

    public void insert(Vehicle v) {
        heap.add(v);
        int idx = heap.size() - 1;
        while (idx > 0 && heap.get(parent(idx)).getDailyRate() > heap.get(idx).getDailyRate()) {
            swap(idx, parent(idx));
            idx = parent(idx);
        }
    }

    public Vehicle extractMin() {
        if (heap.isEmpty()) throw new NoSuchElementException("Heap is empty");
        Vehicle min = heap.get(0);
        Vehicle last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        return min;
    }

    private void heapifyDown(int idx) {
        int size = heap.size(), smallest = idx;
        int l = left(idx), r = right(idx);
        if (l < size && heap.get(l).getDailyRate() < heap.get(smallest).getDailyRate()) smallest = l;
        if (r < size && heap.get(r).getDailyRate() < heap.get(smallest).getDailyRate()) smallest = r;
        if (smallest != idx) { swap(idx, smallest); heapifyDown(smallest); }
    }

    public boolean isEmpty() { return heap.isEmpty(); }
    public int size()        { return heap.size(); }
}

// ── 7C: Binary Search on sorted vehicle list ──────────────
class VehicleSearch {
    /**
     * Binary Search by dailyRate on a sorted list.
     * Returns index of exact match, or -1.
     */
    public static int binarySearchByRate(List<Vehicle> sorted, double targetRate) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            double midRate = sorted.get(mid).getDailyRate();
            if (midRate == targetRate) return mid;
            else if (midRate < targetRate) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    /** Quick sort vehicles by daily rate (ascending). */
    public static void quickSort(List<Vehicle> list, int lo, int hi) {
        if (lo < hi) {
            int p = partition(list, lo, hi);
            quickSort(list, lo, p - 1);
            quickSort(list, p + 1, hi);
        }
    }

    private static int partition(List<Vehicle> list, int lo, int hi) {
        double pivot = list.get(hi).getDailyRate();
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (list.get(j).getDailyRate() <= pivot) {
                i++;
                Vehicle tmp = list.get(i); list.set(i, list.get(j)); list.set(j, tmp);
            }
        }
        Vehicle tmp = list.get(i + 1); list.set(i + 1, list.get(hi)); list.set(hi, tmp);
        return i + 1;
    }
}

// ── 7D: Graph — city connection for vehicle dispatch ───────
class CityGraph {
    private final Map<String, List<String>> adjacency = new HashMap<>();

    public void addCity(String city) {
        adjacency.putIfAbsent(city, new ArrayList<>());
    }

    public void addRoute(String from, String to) {
        adjacency.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        adjacency.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
    }

    /** BFS to find shortest path between two cities. */
    public List<String> bfsPath(String start, String end) {
        if (!adjacency.containsKey(start) || !adjacency.containsKey(end)) return Collections.emptyList();
        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) {
                List<String> path = new ArrayList<>();
                for (String node = end; node != null; node = parent.get(node)) path.add(0, node);
                return path;
            }
            for (String neighbor : adjacency.getOrDefault(current, Collections.emptyList())) {
                if (!parent.containsKey(neighbor)) {
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    public void printGraph() {
        System.out.println("\n📍 City Route Graph:");
        adjacency.forEach((city, neighbors) ->
                System.out.println("  " + city + " → " + neighbors));
    }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 8 – DATABASE (JDBC)                            ║
// ╚══════════════════════════════════════════════════════════╝

/**
 * DatabaseManager handles all JDBC operations.
 * Uses SQLite in-memory for portability (no server required).
 *
 * To switch to MySQL, update the URL, driver, user & password.
 */
class DatabaseManager {

    // ── Change these for MySQL/PostgreSQL ─────────────────
    // MySQL  : jdbc:mysql://localhost:3306/rental_db
    // SQLite : jdbc:sqlite:rental.db  (or :memory:)
    private static final String DB_URL =
        "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    private Connection connection;

   public DatabaseManager() throws SQLException {
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");

        connection = DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASS
        );

        System.out.println("🗄 Database connected: " + DB_URL);
        initializeTables();

    } catch (ClassNotFoundException e) {
        System.err.println("MySQL JDBC Driver not found.");
        e.printStackTrace();   // Added
        connection = null;

    } catch (SQLException e) {
        System.err.println("Database Connection Error:");
        e.printStackTrace();   // Added
        connection = null;
    }
}

    private void initializeTables() throws SQLException {
    if (connection == null) return;

    String createVehicles = """
        CREATE TABLE IF NOT EXISTS vehicles (
            vehicle_id VARCHAR(20) PRIMARY KEY,
            brand VARCHAR(50) NOT NULL,
            model VARCHAR(50) NOT NULL,
            year INT,
            daily_rate DOUBLE,
            vehicle_type VARCHAR(30),
            available BOOLEAN DEFAULT TRUE
        )""";

    String createCustomers = """
        CREATE TABLE IF NOT EXISTS customers (
            customer_id VARCHAR(20) PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            phone VARCHAR(20),
            email VARCHAR(100),
            license_no VARCHAR(50)
        )""";

    String createRentals = """
        CREATE TABLE IF NOT EXISTS rental_records (
            record_id VARCHAR(20) PRIMARY KEY,
            vehicle_id VARCHAR(20),
            customer_id VARCHAR(20),
            from_date DATE,
            to_date DATE,
            total_cost DOUBLE,
            vehicle_type VARCHAR(30),
            FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
            FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        )""";

    try (Statement st = connection.createStatement()) {
        st.execute(createVehicles);
        st.execute(createCustomers);
        st.execute(createRentals);
    }

    System.out.println("✅ Database tables initialized.");
}

    // ── INSERT VEHICLE ─────────────────────────────────────
    public void saveVehicle(Vehicle v) {
        if (connection == null) return;
        String sql = "REPLACE INTO vehicles VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, v.getVehicleId());
            ps.setString(2, v.getBrand());
            ps.setString(3, v.getModel());
            ps.setInt   (4, v.getYear());
            ps.setDouble(5, v.getDailyRate());
            ps.setString(6, v.getVehicleType());
            ps.setInt   (7, v.isAvailable() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB Error saving vehicle: " + e.getMessage());
        }
    }

    // ── INSERT CUSTOMER ────────────────────────────────────
    public void saveCustomer(Customer c) {
        if (connection == null) return;
        String sql = "REPLACE INTO customers VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getLicenseNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB Error saving customer: " + e.getMessage());
        }
    }

    // ── INSERT RENTAL RECORD ───────────────────────────────
    public void saveRentalRecord(RentalRecord r) {
        if (connection == null) return;
        String sql = "INSERT INTO rental_records VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getRecordId());
            ps.setString(2, r.getVehicleId());
            ps.setString(3, r.getCustomerId());
            ps.setString(4, r.getFromDate().toString());
            ps.setString(5, r.getToDate().toString());
            ps.setDouble(6, r.getTotalCost());
            ps.setString(7, r.getVehicleType());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB Error saving rental: " + e.getMessage());
        }
    }

    // ── UPDATE VEHICLE AVAILABILITY ────────────────────────
    public void updateVehicleAvailability(String vehicleId, boolean available) {
        if (connection == null) return;
        String sql = "UPDATE vehicles SET available = ? WHERE vehicle_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1, available ? 1 : 0);
            ps.setString(2, vehicleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB Error updating availability: " + e.getMessage());
        }
    }

    // ── SELECT ALL RENTALS ─────────────────────────────────
    public void printAllRentals() {
        if (connection == null) { System.out.println("(DB not connected — skipping)"); return; }
        String sql = "SELECT * FROM rental_records ORDER BY from_date DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n📋 ALL RENTAL RECORDS FROM DATABASE:");
            System.out.println("─".repeat(80));
            System.out.printf("%-12s %-10s %-12s %-12s %-12s %10s%n",
                    "RecordID", "VehicleID", "CustomerID", "From", "To", "Cost(₹)");
            System.out.println("─".repeat(80));
            while (rs.next()) {
                System.out.printf("%-12s %-10s %-12s %-12s %-12s %10.2f%n",
                        rs.getString("record_id"),
                        rs.getString("vehicle_id"),
                        rs.getString("customer_id"),
                        rs.getString("from_date"),
                        rs.getString("to_date"),
                        rs.getDouble("total_cost"));
            }
        } catch (SQLException e) {
            System.err.println("DB Error reading rentals: " + e.getMessage());
        }
    }

    // ── AGGREGATE: Revenue per vehicle type ───────────────
    public void printRevenueByType() {
        if (connection == null) { System.out.println("(DB not connected — skipping)"); return; }
        String sql = "SELECT vehicle_type, SUM(total_cost) AS revenue FROM rental_records GROUP BY vehicle_type";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\n💰 REVENUE BY VEHICLE TYPE:");
            while (rs.next()) {
                System.out.printf("  %-15s : ₹%.2f%n",
                        rs.getString("vehicle_type"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
        }
    }

    public void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { System.err.println("Error closing DB: " + e.getMessage()); }
    }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 9 – RENTAL SERVICE (Core Business Logic)       ║
// ╚══════════════════════════════════════════════════════════╝

class RentalService {
    // DSA: HashMap for O(1) vehicle & customer lookup
    private final Map<String, Vehicle>  vehicles  = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();
    private final List<RentalRecord>    records   = new ArrayList<>();

    // DSA: Stack for undo-last-action
    private final Stack<String> actionLog = new Stack<>();

    // DSA: Min-Heap for cheapest vehicle queries
    private final VehicleMinHeap cheapHeap = new VehicleMinHeap();

    private final DatabaseManager db;
    private int recordCounter = 1000;

    public RentalService(DatabaseManager db) {
        this.db = db;
    }

    // ── Add Vehicle ────────────────────────────────────────
    public void addVehicle(Vehicle v) {
        vehicles.put(v.getVehicleId(), v);
        cheapHeap.insert(v);
        db.saveVehicle(v);
        actionLog.push("ADD_VEHICLE:" + v.getVehicleId());
        System.out.println("🚗 Vehicle added: " + v.getVehicleType() + " [" + v.getVehicleId() + "]");
    }

    // ── Register Customer ──────────────────────────────────
    public void registerCustomer(Customer c) {
        customers.put(c.getCustomerId(), c);
        db.saveCustomer(c);
        actionLog.push("ADD_CUSTOMER:" + c.getCustomerId());
        System.out.println("👤 Customer registered: " + c.getName() + " [" + c.getCustomerId() + "]");
    }

    // ── Rent Vehicle ───────────────────────────────────────
    public RentalRecord rentVehicle(String vehicleId, String customerId,
                                    LocalDate from, LocalDate to)
            throws RentalException {

        Vehicle v = vehicles.get(vehicleId);
        if (v == null) throw new VehicleNotAvailableException(vehicleId);

        Customer c = customers.get(customerId);
        if (c == null) throw new CustomerNotFoundException(customerId);

        // Calls polymorphic rent() on the vehicle
        v.rent(customerId, from, to);

        long days      = ChronoUnit.DAYS.between(from, to);
        double cost    = v.calculateCost(days); // polymorphic cost!
        String recId   = "REC" + (++recordCounter);

        RentalRecord rec = new RentalRecord(recId, vehicleId, customerId,
                from, to, cost, v.getVehicleType());
        records.add(rec);
        c.addRentalRecord(recId + " - " + vehicleId + " (" + from + " to " + to + ")");

        db.saveRentalRecord(rec);
        db.updateVehicleAvailability(vehicleId, false);
        actionLog.push("RENT:" + recId);

        return rec;
    }

    // ── Return Vehicle ─────────────────────────────────────
    public void returnVehicle(String vehicleId) throws RentalException {
        Vehicle v = vehicles.get(vehicleId);
        if (v == null) throw new VehicleNotAvailableException(vehicleId);
        v.returnVehicle();
        db.updateVehicleAvailability(vehicleId, true);
        actionLog.push("RETURN:" + vehicleId);
    }

    // ── Show all available vehicles ────────────────────────
    public void showAvailableVehicles() {
        System.out.println("\n🚘 AVAILABLE VEHICLES:");
        vehicles.values().stream()
                .filter(Vehicle::isAvailable)
                .forEach(Vehicle::printDetails);
    }

    // ── DSA: Sort & Search ─────────────────────────────────
    public void showVehiclesSortedByRate() {
        List<Vehicle> list = new ArrayList<>(vehicles.values());
        VehicleSearch.quickSort(list, 0, list.size() - 1);
        System.out.println("\n📊 VEHICLES SORTED BY DAILY RATE (QuickSort):");
        for (Vehicle v : list)
            System.out.printf("  [%s] %s %s — ₹%.2f/day%n",
                    v.getVehicleId(), v.getBrand(), v.getModel(), v.getDailyRate());
    }

    public void findCheapestAvailableVehicle() {
        // Rebuild heap with only available vehicles
        VehicleMinHeap heap = new VehicleMinHeap();
        vehicles.values().stream().filter(Vehicle::isAvailable).forEach(heap::insert);
        if (heap.isEmpty()) { System.out.println("No available vehicles."); return; }
        Vehicle cheapest = heap.extractMin();
        System.out.println("\n💡 CHEAPEST AVAILABLE (Min-Heap): "
                + cheapest.getBrand() + " " + cheapest.getModel()
                + " — ₹" + cheapest.getDailyRate() + "/day");
    }

    // ── Undo Last Action ───────────────────────────────────
    public void undoLastAction() {
        if (actionLog.isEmpty()) { System.out.println("Nothing to undo."); return; }
        String last = actionLog.pop();
        System.out.println("↩  Undo logged (manual reversal may be needed): " + last);
    }

    // ── Print customer history ─────────────────────────────
    public void printCustomerHistory(String customerId) throws CustomerNotFoundException {
        Customer c = customers.get(customerId);
        if (c == null) throw new CustomerNotFoundException(customerId);
        c.printDetails();
        System.out.println("  Rental History:");
        c.getRentalHistory().forEach(r -> System.out.println("    • " + r));
        System.out.println();
    }

    // Expose DB for main
    public DatabaseManager getDb() { return db; }
}


// ╔══════════════════════════════════════════════════════════╗
// ║  SECTION 10 – MAIN CLASS (Demo)                         ║
// ╚══════════════════════════════════════════════════════════╝

public class VehicleRentalSystem {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║     🚗  VEHICLE RENTAL SYSTEM  🚗            ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        // ── 1. Database Setup (JDBC) ──────────────────────
        DatabaseManager db;
        try {
            db = new DatabaseManager();
        } catch (SQLException e) {
            System.err.println("Critical DB error: " + e.getMessage());
            return;
        }

        RentalService service = new RentalService(db);

        // ── 2. Create Vehicles (Constructors + Inheritance) ─
        System.out.println("\n── Creating Vehicles ──────────────────────────");
        Car car1 = new Car("CAR001", "Toyota", "Camry",    2022, 1500, 4, "Automatic");
        Car car2 = new Car("CAR002", "Hyundai","Creta",    2023, 1200, 4, "Manual");
        Bike bk1 = new Bike("BIK001","Royal Enfield","Bullet",2021, 700, 350, false);
        ElectricCar ec1 = new ElectricCar("ECA001","Tesla","Model 3",2023, 2000, 4, 500);
        Truck tk1 = new Truck("TRK001","Tata","Prima",     2020, 3000, 25.0, 6);

        service.addVehicle(car1);
        service.addVehicle(car2);
        service.addVehicle(bk1);
        service.addVehicle(ec1);
        service.addVehicle(tk1);

        // ── 3. Register Customers ─────────────────────────
        System.out.println("\n── Registering Customers ──────────────────────");
        Customer cust1 = new Customer("CUST001","Arjun Kumar",   "9876543210","arjun@mail.com","TN2024567");
        Customer cust2 = new Customer("CUST002","Priya Sharma",  "9123456789","priya@mail.com","MH2024789");
        Customer cust3 = new Customer("CUST003","Vikram Singh",  "9988776655","viks@mail.com", "DL2024001");

        service.registerCustomer(cust1);
        service.registerCustomer(cust2);
        service.registerCustomer(cust3);

        // ── 4. Rent Vehicles (Polymorphism in action) ─────
        System.out.println("\n── Renting Vehicles ───────────────────────────");
        try {
            RentalRecord r1 = service.rentVehicle("CAR001","CUST001",
                    LocalDate.now(), LocalDate.now().plusDays(5));
            r1.printReceipt();

            RentalRecord r2 = service.rentVehicle("ECA001","CUST002",
                    LocalDate.now(), LocalDate.now().plusDays(3));
            r2.printReceipt();

            RentalRecord r3 = service.rentVehicle("TRK001","CUST003",
                    LocalDate.now(), LocalDate.now().plusDays(2));
            r3.printReceipt();

            RentalRecord r4 = service.rentVehicle("BIK001","CUST001",
                    LocalDate.now().plusDays(10), LocalDate.now().plusDays(17));
            r4.printReceipt();

        } catch (RentalException e) {
            System.err.println("❌ Rental Error: " + e.getMessage());
        }

        // ── 5. Exception Handling Demos ───────────────────
        System.out.println("── Exception Handling Demo ─────────────────────");

        // Try renting an already-rented vehicle
        try {
            service.rentVehicle("CAR001","CUST002",
                    LocalDate.now(), LocalDate.now().plusDays(2));
        } catch (VehicleNotAvailableException e) {
            System.out.println("✔ Caught: " + e.getMessage());
        } catch (RentalException e) {
            System.out.println("✔ Caught: " + e.getMessage());
        }

        // Try renting with invalid dates
        try {
            service.rentVehicle("CAR002","CUST001",
                    LocalDate.now().plusDays(5), LocalDate.now()); // to < from
        } catch (RentalException e) {
            System.out.println("✔ Caught: " + e.getMessage());
        }

        // Try looking up a missing customer
        try {
            service.printCustomerHistory("CUST999");
        } catch (CustomerNotFoundException e) {
            System.out.println("✔ Caught: " + e.getMessage());
        }

        // ── 6. Return a Vehicle ───────────────────────────
        System.out.println("\n── Returning a Vehicle ────────────────────────");
        try {
            service.returnVehicle("CAR001");
        } catch (RentalException e) {
            System.err.println(e.getMessage());
        }

        // ── 7. DSA Demos ──────────────────────────────────
        System.out.println("\n── DSA: QuickSort Vehicles by Rate ────────────");
        service.showVehiclesSortedByRate();

        System.out.println("\n── DSA: Min-Heap → Cheapest Available Vehicle ─");
        service.findCheapestAvailableVehicle();

        System.out.println("\n── DSA: Binary Search by Rate ─────────────────");
        List<Vehicle> sortedList = new ArrayList<>(
                List.of(car1, car2, bk1, ec1, tk1));
        VehicleSearch.quickSort(sortedList, 0, sortedList.size() - 1);
        int idx = VehicleSearch.binarySearchByRate(sortedList, 1500.0);
        System.out.println("  Search ₹1500/day → index: " + idx
                + (idx >= 0 ? " (" + sortedList.get(idx).getModel() + ")" : " (not found)"));

        System.out.println("\n── DSA: Stack (Action Log / Undo) ─────────────");
        System.out.println("  Last 3 actions: ");
        Stack<String> temp = new Stack<>();
        // peek at top 3 without destroying log
        for (int i = 0; i < 3; i++) {
            // We only demo the concept; we don't drain the original
            System.out.println("    • (see actionLog — internal stack)");
            break;
        }
        service.undoLastAction();

        System.out.println("\n── DSA: Graph — City Routes (BFS) ─────────────");
        CityGraph graph = new CityGraph();
        graph.addCity("Chennai");
        graph.addCity("Madurai");
        graph.addCity("Coimbatore");
        graph.addCity("Bangalore");
        graph.addCity("Hyderabad");
        graph.addRoute("Chennai",    "Madurai");
        graph.addRoute("Chennai",    "Bangalore");
        graph.addRoute("Madurai",    "Coimbatore");
        graph.addRoute("Bangalore",  "Hyderabad");
        graph.addRoute("Coimbatore", "Bangalore");
        graph.printGraph();

        List<String> path = graph.bfsPath("Madurai", "Hyderabad");
        System.out.println("  Shortest path Madurai → Hyderabad: " + path);

        // ── 8. Polymorphism showcase ──────────────────────
        System.out.println("\n── Polymorphism: calculateCost() for 7 days ───");
        Vehicle[] fleet = { car1, car2, ec1, bk1, tk1 };
        for (Vehicle v : fleet) {
            System.out.printf("  %-15s %-18s : ₹%.2f%n",
                    v.getVehicleType(), v.getBrand() + " " + v.getModel(),
                    v.calculateCost(7));
        }

        // ── 9. Maintainable interface demo ────────────────
        System.out.println("\n── Maintainable Interface Demo ────────────────");
        car2.scheduleMaintenance(LocalDate.now().minusDays(1)); // force overdue
        System.out.println("  CAR002 maintenance: " + car2.getMaintenanceStatus());
        ec1.scheduleMaintenance(LocalDate.now().plusMonths(2));
        System.out.println("  ECA001 maintenance: " + ec1.getMaintenanceStatus());

        // Electric car charge
        System.out.println("\n── Electric Car Specific Feature ──────────────");
        ec1.charge(45.0);

        // ── 10. Customer History ──────────────────────────
        System.out.println("\n── Customer Rental History ────────────────────");
        try {
            service.printCustomerHistory("CUST001");
            service.printCustomerHistory("CUST002");
        } catch (CustomerNotFoundException e) {
            System.err.println(e.getMessage());
        }

        // ── 11. Available Vehicles ────────────────────────
        service.showAvailableVehicles();

        // ── 12. JDBC: Print Records & Revenue ────────────
        db.printAllRentals();
        db.printRevenueByType();

        // ── Cleanup ────────────────────────────────────────
        db.close();
        System.out.println("\n✅ Vehicle Rental System demo complete.");
    }
}