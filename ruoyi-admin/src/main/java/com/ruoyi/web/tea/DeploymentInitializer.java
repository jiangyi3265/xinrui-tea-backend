package com.ruoyi.web.tea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.constant.UserConstants;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Explicit first-install command, never run as a Spring startup hook. */
public final class DeploymentInitializer {
    private DeploymentInitializer() {}

    public static void main(String[] args) {
        try {
            initialize();
        } catch (Exception error) {
            // SQL exceptions can contain values; never log credentials or rows.
            System.err.println("Deployment initialization refused/failed (" + error.getClass().getSimpleName()
                + "). Requires an empty database or a completed deployment marker; keep data and inspect configuration.");
            System.exit(1);
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) throw new IllegalArgumentException(name);
        return value;
    }

    private static long scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            rows.next(); return rows.getLong(1);
        }
    }

    private static void initialize() throws Exception {
        String password = required("TEA_ADMIN_PASSWORD");
        if (password.length() < 16 || password.length() > UserConstants.PASSWORD_MAX_LENGTH) throw new IllegalArgumentException("admin password length");
        Path directory = Paths.get(required("TEA_BOOTSTRAP_DIR"));
        ObjectMapper json = new ObjectMapper();
        JsonNode seed = json.readTree(Files.readAllBytes(directory.resolve("tea_seed.public.json")));
        for (String key : new String[]{"users", "auctions", "auctionBids"}) {
            if (!seed.path(key).isArray() || seed.path(key).size() != 0) throw new IllegalArgumentException("private seed data");
        }
        if (!seed.path("sessions").isObject() || seed.path("sessions").size() != 0
                || !seed.path("content").path("store").path("pay").isObject()
                || seed.path("content").path("store").path("pay").size() != 0) {
            throw new IllegalArgumentException("private seed state");
        }
        String mode = System.getenv().getOrDefault("TEA_SEED_MODE", "sample");
        if (!mode.equals("sample") && !mode.equals("empty")) throw new IllegalArgumentException("seed mode");
        if (mode.equals("empty")) ((com.fasterxml.jackson.databind.node.ObjectNode) seed).putArray("catalog");
        try (Connection connection = DriverManager.getConnection(required("DB_URL"), required("DB_USERNAME"), required("DB_PASSWORD"))) {
            // MySQL advisory lock is scoped to this database and this connection.
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT GET_LOCK(CONCAT('tea-bootstrap:',DATABASE()),30)")) {
                if (!rows.next() || rows.getInt(1) != 1) throw new IllegalStateException("initialization busy");
            }
            long marker = scalar(connection, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='tea_deployment_bootstrap'");
            if (marker > 0) {
                if (scalar(connection, "SELECT COUNT(*) FROM tea_deployment_bootstrap WHERE id=1 AND version=1 AND status='ready'") != 1)
                    throw new IllegalStateException("incomplete initialization; no destructive retry");
                if (scalar(connection, "SELECT COUNT(*) FROM tea_business_state WHERE id=1") != 1
                        || scalar(connection, "SELECT COUNT(*) FROM sys_user WHERE user_id=1") != 1)
                    throw new IllegalStateException("inconsistent deployment");
                System.out.println("Deployment already initialized; accounts, data and passwords were not reset.");
                return;
            }
            if (scalar(connection, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()") != 0)
                throw new IllegalStateException("refusing existing database");
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE tea_deployment_bootstrap(id INT PRIMARY KEY,version INT NOT NULL,status VARCHAR(32) NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");
                statement.execute("INSERT INTO tea_deployment_bootstrap VALUES(1,1,'initializing',CURRENT_TIMESTAMP)");
            }
            // These scripts contain DDL and can only execute behind the empty-DB guard.
            ScriptUtils.executeSqlScript(connection, new FileSystemResource(directory.resolve("ry_20250522.sql").toFile()));
            ScriptUtils.executeSqlScript(connection, new FileSystemResource(directory.resolve("tea_business.sql").toFile()));
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE sys_user SET password=?,email='',phonenumber='',login_ip='',login_date=NULL WHERE user_id=1")) {
                    statement.setString(1, new BCryptPasswordEncoder().encode(password));
                    if (statement.executeUpdate() != 1) throw new IllegalStateException("missing administrator");
                }
                byte[] random = new byte[15]; new SecureRandom().nextBytes(random);
                try (PreparedStatement statement = connection.prepareStatement("UPDATE sys_config SET config_value=? WHERE config_key='sys.user.initPassword'")) {
                    statement.setString(1, Base64.getUrlEncoder().withoutPadding().encodeToString(random)); statement.executeUpdate();
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("UPDATE sys_dept SET phone='',email=''");
                    // Captcha and account registration remain as configured in the safe base schema.
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE tea_business_state SET state_json=?,revision=0 WHERE id=1")) {
                    statement.setString(1, json.writeValueAsString(seed));
                    if (statement.executeUpdate() != 1) throw new IllegalStateException("missing business state");
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("UPDATE tea_deployment_bootstrap SET status='ready' WHERE id=1");
                }
                connection.commit();
            } catch (Exception error) { connection.rollback(); throw error; }
            System.out.println("Fresh deployment initialized with public " + mode + " seed; no member or transaction data imported.");
        }
    }
}
