import java.sql.*;
import java.util.Scanner;

public class ServiceManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void manageServices() {
        System.out.println("\n=== Service Management ===");
        System.out.println("1. Add Service\n2. View Services\n3. Update Service\n4. Delete Service\n5. Back");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1:
                addService();
                break;
            case 2:
                viewServices();
                break;
            case 3:
                updateService();
                break;
            case 4:
                deleteService();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void addService() {
        System.out.print("\nEnter service name: ");
        String name = scanner.nextLine();
        System.out.print("Enter service rate per hour: ");
        double rate = scanner.nextDouble();
        scanner.nextLine();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO services (name, rate) VALUES (?, ?)");) {
            stmt.setString(1, name);
            stmt.setDouble(2, rate);
            stmt.executeUpdate();
            System.out.println("\nService added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewServices() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM services")) {
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("name") + " ($" + rs.getDouble("rate") + "/hr)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateService() {
        System.out.print("\nEnter service ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter new service name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new service rate per hour: ");
        double rate = scanner.nextDouble();
        scanner.nextLine();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE services SET name = ?, rate = ? WHERE id = ?")) {
            stmt.setString(1, name);
            stmt.setDouble(2, rate);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            System.out.println("Service updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteService() {
        System.out.print("\nEnter service ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM services WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("\nService deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}