package banking;

import java.sql.*;

public class Connect {

    private final String baseName;

    protected Connect(String baseName) {
        this.baseName = baseName;
    }

    protected Connection connect() {
        String url = "jdbc:sqlite:" + this.baseName;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            Statement statement = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS card(\n" +
                    "id INTEGER AUTO_INCREMENT,\n" +
                    "number TEXT,\n" +
                    "pin TEXT,\n" +
                    "balance INTEGER DEFAULT 0\n" +
                    ");";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    protected boolean createNewAccount(String number, String pin) {
        String sql = "INSERT INTO card(number,pin) VALUES(?,?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            stmt.setString(2, pin);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    protected String findAccountNumber(String number) {
        String sql = "SELECT pin FROM card " +
                "WHERE number = ?;";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            ResultSet rs = stmt.executeQuery();
            String pinNumber = "Error";
            if (rs.next()) pinNumber = rs.getString("pin");
            return pinNumber;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "Error";
        }
    }

    protected int findAccountBalance(String number) {
        String sql = "SELECT balance FROM card " +
                "WHERE number = ?;";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            ResultSet rs = stmt.executeQuery();
            int balance = 0;
            if (rs.next()) balance = rs.getInt("balance");
            return balance;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    protected boolean addIncome(String accountNumber, String transactedAmount) {
        String sql = "UPDATE card SET balance = balance + ? "
                + "WHERE number = ?;";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transactedAmount);
            stmt.setString(2, accountNumber);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    protected boolean performTransfer(String fromAccountNumber, String toAccountNumber, String transactedAmount) {

        String withdrawFromAccount = "UPDATE card SET balance = balance - ? "
                + "WHERE number = ?;";
        String sendToAccount = "UPDATE card SET balance = balance + ? "
                + "WHERE number = ?;";

        // SQL Transaction
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement withdrawAccount = conn.prepareStatement(withdrawFromAccount);
                 PreparedStatement sendAccount = conn.prepareStatement(sendToAccount)) {

                withdrawAccount.setString(1, transactedAmount);
                withdrawAccount.setString(2, fromAccountNumber);
                withdrawAccount.executeUpdate();

                sendAccount.setString(1, transactedAmount);
                sendAccount.setString(2, toAccountNumber);
                sendAccount.executeUpdate();

                conn.commit();

                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    protected boolean checkAccountExists(String number) {
        String sql = "SELECT EXISTS(SELECT 1 FROM card " +
                "WHERE number = ?);";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1) == 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    protected boolean deleteAccount(String number) {
        String sql = "DELETE FROM card "
                + "WHERE number = ?;";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
