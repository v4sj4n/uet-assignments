package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:register_state.db";

    public DatabaseManager() {
        createTables();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTables() {
        // Table for overall program states
        String sqlState =
            """
                CREATE TABLE IF NOT EXISTS program_state (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """;

        // Table for individual register values tied to a state
        String sqlRegisters =
            """
                CREATE TABLE IF NOT EXISTS register_state (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    state_id INTEGER NOT NULL,
                    register_name TEXT NOT NULL,
                    register_value INTEGER NOT NULL,
                    FOREIGN KEY (state_id) REFERENCES program_state(id) ON DELETE CASCADE
                );
            """;

        try (
            Connection conn = connect();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sqlState);
            stmt.execute(sqlRegisters);
        } catch (SQLException e) {
            System.err.println(
                "Database table creation failed: " + e.getMessage()
            );
        }
    }

    public List<String> getSavedStates() {
        List<String> states = new ArrayList<>();
        String sql =
            "SELECT id, name, timestamp FROM program_state ORDER BY timestamp DESC";

        try (
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                states.add(
                    rs.getInt("id") +
                    ": " +
                    rs.getString("name") +
                    " (" +
                    rs.getString("timestamp") +
                    ")"
                );
            }
        } catch (SQLException e) {
            System.err.println(
                "Error fetching saved states: " + e.getMessage()
            );
        }
        return states;
    }

    public long saveState(String name, RegisterSet registerSet) {
        String sqlInsertState = "INSERT INTO program_state(name) VALUES(?)";
        long stateId = -1;

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start transaction

            // Insert new program state and get its ID
            try (
                PreparedStatement pstmtState = conn.prepareStatement(
                    sqlInsertState,
                    Statement.RETURN_GENERATED_KEYS
                )
            ) {
                pstmtState.setString(1, name);
                pstmtState.executeUpdate();
                ResultSet generatedKeys = pstmtState.getGeneratedKeys();
                if (generatedKeys.next()) {
                    stateId = generatedKeys.getLong(1);
                } else {
                    throw new SQLException(
                        "Creating program state failed, no ID obtained."
                    );
                }
            }

            // Insert each register's state
            String sqlInsertRegister =
                "INSERT INTO register_state(state_id, register_name, register_value) VALUES(?, ?, ?)";
            try (
                PreparedStatement pstmtRegister = conn.prepareStatement(
                    sqlInsertRegister
                )
            ) {
                for (Map.Entry<String, Register> entry : registerSet
                    .getAllRegisters()
                    .entrySet()) {
                    pstmtRegister.setLong(1, stateId);
                    pstmtRegister.setString(2, entry.getKey());
                    pstmtRegister.setInt(3, entry.getValue().read());
                    pstmtRegister.addBatch();
                }
                pstmtRegister.executeBatch();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            System.err.println("Error saving state: " + e.getMessage());
            return -1;
        }
        return stateId;
    }

    public void loadState(int stateId, RegisterSet registerSet) {
        String sql =
            "SELECT register_name, register_value FROM register_state WHERE state_id = ?";
        registerSet.clearAll(); // Clear current state before loading

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, stateId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String regName = rs.getString("register_name");
                int regValue = rs.getInt("register_value");
                if (registerSet.hasRegister(regName)) {
                    registerSet.write(regName, regValue);
                }
            }
            // Reset modification flags after loading
            registerSet.resetModificationFlags();
        } catch (SQLException e) {
            System.err.println("Error loading state: " + e.getMessage());
        }
    }
}
