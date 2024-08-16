package login;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Scanner;
public class Login {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.println("1. NEW USER\n2. LOGIN\n3. EXIT");
            int input = s.nextInt();
            s.nextLine(); // Consume newline left-over
            if (input == 1) {
                System.out.println("CREATE AN ACCOUNT\n\n");
                insertdata();
            } else if (input == 2) {
                System.out.println("=======LOGIN=======");
                int attempts = 0;
                int id = 0;
                boolean loggedIn = false;
                while (attempts < 3) {
                    id = checkdata();
                    if (id != 0) {
                        System.out.println("=======LOGIN SUCCESSFUL=======");
                        loggedIn = true;
                        break;
                    } else {
                        System.out.println("=======LOGIN ID/PASSWORD WRONG, PLEASE TRY AGAIN=======");
                        attempts++;
                    }
                }
                if (loggedIn) {
                    while (true) {
                        System.out.println("1. DISPLAY DATA\n2. MODIFY DATA\n3. DELETE DATA\n4. LOGOUT");
                        int choice = s.nextInt();
                        s.nextLine(); // Consume newline left-over
                        if (choice == 1) {
                            displayuserdata(id);
                        } else if (choice == 2) {
                            modifydata(id);
                        } else if (choice == 3) {
                            deleteUser(id);
                            break; // Exit to login menu after deletion
                        } else if (choice == 4) {
                            System.out.println("Logging out...");
                            break;
                        } else {
                            System.out.println("Invalid choice. Please try again.");
                        }
                    }
                } else {
                    System.out.println("Too many failed login attempts. Returning to main menu.");
                }
            } else if (input == 3) {
                System.out.println("Exiting...");
                break;
            } else {
                System.out.println("Invalid input. Please try again.");
            }
            System.out.println("==========================================");
        }
        s.close();
    }
    public static void displayuserdata(int id) {
        String url = "jdbc:mysql://localhost:3306/data";
        String username = "root";
        String password = "1290@143mM";
        String query = "SELECT * FROM user WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                System.out.println("YOUR DATA");
                while (rs.next()) {
                    System.out.println("ID:          " + rs.getInt("id"));
                    System.out.println("NAME:        " + rs.getString("name"));
                    System.out.println("AGE:         " + rs.getInt("age"));
                    System.out.println("RATE:        " + rs.getString("star"));
                    System.out.println("SALARY:      " + rs.getFloat("salary"));
                    System.out.println("JOINDATE:    " + rs.getDate("joindate"));
                    System.out.println();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static int checkdata() {
        String url = "jdbc:mysql://localhost:3306/data";
        String username = "root";
        String password = "1290@143mM";
        String query = "{CALL checkUserCredentials(?, ?, ?)}";
        try (Connection con = DriverManager.getConnection(url, username, password);
             CallableStatement cstmt = con.prepareCall(query)) {

            Scanner s = new Scanner(System.in);
            System.out.println("ENTER YOUR ID:");
            int id = s.nextInt();
            System.out.println("ENTER YOUR PASSWORD:");
            String cpassword = s.next();
            cstmt.setInt(1, id);
            cstmt.setString(2, cpassword);
            cstmt.registerOutParameter(3, Types.BOOLEAN);
            cstmt.execute();
            boolean isValid = cstmt.getBoolean(3);
            if (isValid) {
                return id;
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static void insertdata() {
        String url = "jdbc:mysql://localhost:3306/data";
        String username = "root";
        String password = "1290@143mM";
        String query = "INSERT INTO user(name, password, age, star, salary, joindate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            Scanner s = new Scanner(System.in);
            System.out.println("ENTER YOUR NAME:");
            String name = s.nextLine();
            pst.setString(1, name);

            System.out.println("CREATE A PASSWORD:");
            String upassword = s.next();
            pst.setString(2, upassword);

            System.out.println("ENTER YOUR AGE:");
            int age = s.nextInt();
            pst.setInt(3, age);

            System.out.println("YOUR RATING:");
            char rate = s.next().charAt(0);
            pst.setString(4, String.valueOf(rate));

            System.out.println("ENTER YOUR SALARY:");
            float salary = s.nextFloat();
            pst.setFloat(5, salary);

            LocalDate currentDate = LocalDate.now();
            Date sqlDate = Date.valueOf(currentDate);
            pst.setDate(6, sqlDate);

            int count = pst.executeUpdate();
            if (count == 1) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        System.out.println("ACCOUNT CREATED SUCCESSFULLY. YOUR ID IS = " + newId + "\n\n");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void modifydata(int id) {
        String url = "jdbc:mysql://localhost:3306/data";
        String username = "root";
        String password = "1290@143mM";
        String query = "UPDATE user SET name = ?, age = ?, star = ?, salary = ? WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement pst = con.prepareStatement(query)) {

            Scanner s = new Scanner(System.in);
            System.out.println("ENTER NEW NAME (or press Enter to keep current):");
            String name = s.nextLine();
            if (name.isEmpty()) {
                name = getCurrentFieldValue("name", id);
            }
            pst.setString(1, name);

            System.out.println("ENTER NEW AGE (or press Enter to keep current):");
            String ageInput = s.nextLine();
            int age = ageInput.isEmpty() ? Integer.parseInt(getCurrentFieldValue("age", id)) : Integer.parseInt(ageInput);
            pst.setInt(2, age);

            System.out.println("ENTER NEW RATING (or press Enter to keep current):");
            String rateInput = s.nextLine();
            String rate = rateInput.isEmpty() ? getCurrentFieldValue("star", id) : rateInput;
            pst.setString(3, rate);

            System.out.println("ENTER NEW SALARY (or press Enter to keep current):");
            String salaryInput = s.nextLine();
            float salary = salaryInput.isEmpty() ? Float.parseFloat(getCurrentFieldValue("salary", id)) : Float.parseFloat(salaryInput);
            pst.setFloat(4, salary);

            pst.setInt(5, id);

            int count = pst.executeUpdate();
            if (count > 0) {
                System.out.println("DATA UPDATED SUCCESSFULLY.");
            } else {
                System.out.println("UPDATE FAILED. USER ID MAY NOT EXIST.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(int id) {
        String url = "jdbc:mysql://localhost:3306/data";
        String username = "root";
        String password = "1290@143mM";
        String query = "DELETE FROM user WHERE id = ?";

        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, id);
            int count = pst.executeUpdate();
            if (count > 0) {
                System.out.println("USER DELETED SUCCESSFULLY.");
            } else {
                System.out.println("DELETE FAILED. USER ID MAY NOT EXIST.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentFieldValue(String fieldName, int id) {
        String url = "jdbc:mysql://localhost:3306/data";
        String username = "root";
        String password = "1290@143mM";
        String query = "SELECT " + fieldName + " FROM user WHERE id = ?";
        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(fieldName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
