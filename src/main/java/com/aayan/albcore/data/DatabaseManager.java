package com.aayan.albcore.data;

import com.aayan.albcore.ALBCore;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public final class DatabaseManager {

    private final ALBCore plugin;
    private Connection connection;
    private static final Logger LOG = Logger.getLogger("ALBCore");

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "ALBCore-DB");
                t.setDaemon(true);
                return t;
            });

    public DatabaseManager(ALBCore plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    // ── Connect ───────────────────────────────────────────

    private void connect() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "albcore.db");

            if (!dbFile.getParentFile().exists())
                dbFile.getParentFile().mkdirs();

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + dbFile.getAbsolutePath());

            connection.setAutoCommit(true);

            // ── SQLite stability fixes ──
            try (Statement stmt = connection.createStatement()) {
                // WAL mode — allows reads while writes are happening
                stmt.execute("PRAGMA journal_mode=WAL");
                // wait up to 5s if DB is locked instead of failing instantly
                stmt.execute("PRAGMA busy_timeout=5000");
                // slightly faster writes, still safe on crash
                stmt.execute("PRAGMA synchronous=NORMAL");
            }

            LOG.info("[ALBCore] Database connected. (WAL mode enabled)");

        } catch (ClassNotFoundException e) {
            LOG.severe("[ALBCore] SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            LOG.severe("[ALBCore] Failed to connect to database: " + e.getMessage());
        } catch (Exception e) {
            LOG.severe("[ALBCore] Unexpected error during DB connect: " + e.getMessage());
        }
    }

    // ── Tables ────────────────────────────────────────────

    private void createTables() {
        execute("""
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid TEXT NOT NULL,
                    key TEXT NOT NULL,
                    value TEXT NOT NULL,
                    PRIMARY KEY (uuid, key)
                );
                """);
        execute("""
                CREATE TABLE IF NOT EXISTS cooldowns (
                    uuid TEXT NOT NULL,
                    key TEXT NOT NULL,
                    expiry BIGINT NOT NULL,
                    PRIMARY KEY (uuid, key)
                );
                """);
    }

    // ── Persistent Cooldowns ──────────────────────────────

    public void saveCooldown(UUID uuid, String key, long expiry) {
        if (!isConnected("saveCooldown")) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO cooldowns (uuid, key, expiry) VALUES (?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, key);
            ps.setLong(3, expiry);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] saveCooldown failed: " + e.getMessage());
        }
    }

    public void removeCooldown(UUID uuid, String key) {
        if (!isConnected("removeCooldown")) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM cooldowns WHERE uuid = ? AND key = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] removeCooldown failed: " + e.getMessage());
        }
    }

    /**
     * Loads all active cooldowns into the provided map.
     */
    public void loadCooldowns(java.util.Map<UUID, java.util.Map<String, Long>> target) {
        if (!isConnected("loadCooldowns")) return;
        pruneCooldowns(); // clean up old ones first
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cooldowns")) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String key = rs.getString("key");
                long expiry = rs.getLong("expiry");
                target.computeIfAbsent(uuid, k -> new java.util.concurrent.ConcurrentHashMap<>())
                        .put(key, expiry);
            }
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] loadCooldowns failed: " + e.getMessage());
        }
    }

    public void pruneCooldowns() {
        if (!isConnected("pruneCooldowns")) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM cooldowns WHERE expiry < ?")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] pruneCooldowns failed: " + e.getMessage());
        }
    }

    // ── Player Data Key/Value ─────────────────────────────
    // Blocking — waits for the table to be created before returning
    // This prevents multiple plugins firing registerTable at the same
    // time and conflicting on the shared connection

    public void registerTable(String createSQL) {
        if (connection == null) {
            LOG.severe("[ALBCore] registerTable called but database connection is null! " +
                    "Make sure ALBCore is listed under 'depend' in your plugin.yml");
            return;
        }
        try {
            // submit to DB thread and WAIT for it to finish
            Future<?> future = executor.submit(() -> execute(createSQL));
            future.get(); // blocks until done — safe on main thread at startup
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warning("[ALBCore] registerTable interrupted: " + e.getMessage());
        } catch (Exception e) {
            LOG.warning("[ALBCore] registerTable failed: " + e.getMessage());
        }
    }

    // ── Generic Execute ───────────────────────────────────

    public void execute(String sql) {
        if (connection == null) {
            LOG.severe("[ALBCore | DB] execute() called but connection is null! SQL: "
                    + sql.substring(0, Math.min(sql.length(), 60)).trim() + "...");
            return;
        }
        try {
            // check if connection is still alive
            if (connection.isClosed()) {
                LOG.severe("[ALBCore | DB] execute() called but connection is CLOSED! " +
                        "Another plugin may have called db().close() — only ALBCore should call close().");
                return;
            }
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] Execute failed: " + e.getMessage() +
                    " | SQL: " + sql.substring(0, Math.min(sql.length(), 60)).trim() + "...");
        }
    }

    // ── Player Data Key/Value ─────────────────────────────

    public void setData(UUID uuid, String key, String value) {
        if (!isConnected("setData")) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_data (uuid, key, value) VALUES (?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, key);
            ps.setString(3, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] setData failed: " + e.getMessage());
        }
    }

    public String getData(UUID uuid, String key) {
        if (!isConnected("getData")) return null;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT value FROM player_data WHERE uuid = ? AND key = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] getData failed: " + e.getMessage());
        }
        return null;
    }

    public String getData(UUID uuid, String key, String defaultValue) {
        String value = getData(uuid, key);
        return value != null ? value : defaultValue;
    }

    public void removeData(UUID uuid, String key) {
        if (!isConnected("removeData")) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM player_data WHERE uuid = ? AND key = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] removeData failed: " + e.getMessage());
        }
    }

    // ── Async Methods ─────────────────────────────────────

    public void setDataAsync(UUID uuid, String key, String value) {
        executor.submit(() -> setData(uuid, key, value));
    }

    public void setDataAsync(UUID uuid, String key, String value, Runnable callback) {
        executor.submit(() -> {
            setData(uuid, key, value);
            plugin.getServer().getScheduler().runTask(plugin, callback);
        });
    }

    public void executeAsync(Runnable task) {
        if (!isConnected("executeAsync")) return;
        executor.submit(task);
    }

    public void executeAsync(Runnable task, Runnable callback) {
        if (!isConnected("executeAsync")) return;
        executor.submit(() -> {
            task.run();
            plugin.getServer().getScheduler().runTask(plugin, callback);
        });
    }

    // ── Connection Access ─────────────────────────────────

    public Connection getConnection() {
        if (connection == null) {
            LOG.severe("[ALBCore] getConnection() returned null! " +
                    "Database failed to initialize.");
        }
        return connection;
    }

    // ── Internal connection guard ─────────────────────────

    private boolean isConnected(String caller) {
        try {
            if (connection == null || connection.isClosed()) {
                LOG.severe("[ALBCore | DB] " + caller + "() called but connection is " +
                        (connection == null ? "null" : "closed") + "! " +
                        "Do NOT call db().close() from outside ALBCore.");
                return false;
            }
            return true;
        } catch (SQLException e) {
            LOG.severe("[ALBCore | DB] Failed to check connection state: " + e.getMessage());
            return false;
        }
    }

    // ── Shutdown ──────────────────────────────────────────

    public void close() {
        executor.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOG.info("[ALBCore] Database disconnected.");
            }
        } catch (SQLException e) {
            LOG.warning("[ALBCore | DB] Failed to close: " + e.getMessage());
        }
    }
}