import java.sql.*;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InvoiceManager {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void manageInvoices() {
        System.out.println("\n=== Invoice Management ===");
        System.out.println("1. Create New Invoice");
        System.out.println("2. View All Invoices");
        System.out.println("3. View Invoice by Client");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                createInvoice();
                break;
            case 2:
                viewAllInvoices();
                break;
            case 3:
                viewInvoiceByClient();
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void createInvoice() {
        System.out.println("\nCreating new invoice:");
        System.out.println("Select client:");

        int clientId = -1;
        String clientName = "";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM clients")) {

            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.print("\nEnter client number: ");
        clientId = scanner.nextInt();
        scanner.nextLine();

        System.out.println("\nAvailable services for client:");
        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM services")) {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getInt("id") + ". " + rs.getString("name") + " ($" + rs.getDouble("rate") + "/hr)");
        }
        } catch (SQLException e) {
        e.printStackTrace();
        }


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM clients WHERE id = ?")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                clientName = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int invoiceId = -1;
        double totalAmount = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO invoices (client_id, total) VALUES (?, 0)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, clientId);
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                invoiceId = generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boolean addingServices = true;
        StringBuilder invoiceDetails = new StringBuilder();
        while (addingServices) {
            System.out.println("\nAdd services to invoice:");
            System.out.println("1. Add service");
            System.out.println("2. Finish invoice");
            System.out.print("Enter choice: ");
            int serviceChoice = scanner.nextInt();
            scanner.nextLine();

            
            if (serviceChoice == 1) {
                System.out.print("\nSelect service number: ");
                int serviceId = scanner.nextInt();
                System.out.print("Enter hours: ");
                int hours = scanner.nextInt();
                scanner.nextLine();

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT name, rate FROM services WHERE id = ?")) {
                    stmt.setInt(1, serviceId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String serviceName = rs.getString("name");
                        double rate = rs.getDouble("rate");
                        double subtotal = rate * hours;
                        totalAmount += subtotal;

                        // Insert service into invoice_services table
                        try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO invoice_details (invoice_id, service_id, hours, subtotal) VALUES (?, ?, ?, ?)")) {
                            insertStmt.setInt(1, invoiceId);
                            insertStmt.setInt(2, serviceId);
                            insertStmt.setInt(3, hours);
                            insertStmt.setDouble(4, subtotal);
                            insertStmt.executeUpdate();
                        }

                        invoiceDetails.append("- ").append(serviceName)
                                .append(": ").append(hours).append("hrs × $")
                                .append(df.format(rate)).append("/hr = $")
                                .append(df.format(subtotal)).append("\n");

                        System.out.println("\nService added to invoice.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                addingServices = false;
            }
        }

        double tax = totalAmount * 0.10;
        double finalTotal = totalAmount + tax;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE invoices SET total = ? WHERE id = ?")) {
            stmt.setDouble(1, finalTotal);
            stmt.setInt(2, invoiceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String invoiceNumber = clientName.substring(0, 2).toUpperCase() + "-" + String.format("%03d", invoiceId);
        String currentMonthYear = new SimpleDateFormat("MMMM yyyy").format(new Date());

        System.out.println("\n...Creating Invoice...");
        System.out.println("Invoice - " + invoiceNumber + " (" + currentMonthYear + ")");
        System.out.println("Client: " + clientName);
        System.out.print(invoiceDetails);
        System.out.println("Subtotal: $" + df.format(totalAmount));
        System.out.println("Tax (10%): $" + df.format(tax));
        System.out.println("Total: $" + df.format(finalTotal));
        System.out.println("Invoice created successfully!");
    }

    private static void viewAllInvoices() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT i.id, c.name AS client_name, i.date_created, i.total FROM invoices i JOIN clients c ON i.client_id = c.id")) {

            System.out.println("=== All Invoices ===");
            while (rs.next()) {
                System.out.println("Invoice ID: " + rs.getInt("id") + ", Client: " + rs.getString("client_name") + ", Date: " + rs.getDate("date_created") + ", Total: $" + df.format(rs.getDouble("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewInvoiceByClient() {
        System.out.print("Enter client ID: ");
        int clientId = scanner.nextInt();
        scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT i.id, i.date_created, i.total FROM invoices i WHERE i.client_id = ?")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("=== Invoices for Client ID " + clientId + " ===");
            while (rs.next()) {
                System.out.println("Invoice ID: " + rs.getInt("id") + ", Date: " + rs.getDate("date_created") + ", Total: $" + df.format(rs.getDouble("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
