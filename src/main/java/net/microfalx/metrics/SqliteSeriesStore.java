package net.microfalx.metrics;

import net.microfalx.lang.annotation.Order;
import net.microfalx.lang.annotation.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import org.sqlite.SQLiteOpenMode;

import java.io.File;
import java.sql.*;
import java.time.temporal.Temporal;
import java.util.OptionalDouble;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FileUtils.validateFileExists;
import static net.microfalx.lang.IOUtils.closeQuietly;
import static net.microfalx.lang.JvmUtils.getVariableDirectory;

/**
 * A store implementation backed by <a href="https://www.sqlite.org/">SQLite</a>
 */
@Provider
@Order(Order.LOW)
class SqliteSeriesStore extends AbstractSeriesStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteSeriesStore.class);

    private static final Metrics METRICS = Metrics.of("Series").withGroup("Store");
    private static final String DEFAULT_NAME = "metrics";
    private static final String FILE_EXTENSION = ".db";

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rlock = lock.readLock();
    private final Lock wlock = lock.writeLock();
    private final Set<String> metricsCreated = new ConcurrentSkipListSet<>();
    private final String name;
    private File db;
    private Driver driver;
    private Properties properties;

    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();

    public SqliteSeriesStore() {
        this(DEFAULT_NAME);
    }

    public SqliteSeriesStore(String name) {
        requireNotEmpty(name);
        this.name = name + FILE_EXTENSION;
    }

    @Override
    public Set<Metric> getMetrics() {
        return Set.of();
    }

    @Override
    public Series get(Metric metric) {
        requireNonNull(metric);
        checkMetricTable(metric);
        return null;
    }

    @Override
    public Series get(Metric metric, Temporal from, Temporal to) {
        checkMetricTable(metric);
        return null;
    }

    @Override
    public OptionalDouble getAverage(Metric metric, Temporal from, Temporal to) {
        return OptionalDouble.of(0);
    }

    @Override
    public void add(Metric metric, Value value) {
        requireNonNull(metric);
        requireNonNull(value);
        checkMetricTable(metric);
    }

    @Override
    public void add(Batch batch) {
        requireNonNull(batch);
    }

    @Override
    public void clear() {

    }

    private void checkMetricTable(Metric metric) {
        String id = metric.getId();
        if (metricsCreated.contains(id)) return;
        wlock.lock();
        try {
            execute(createCreateSQL(metric));
            metricsCreated.add(id);
        } catch (SQLException e) {
            SQLiteErrorCode resultCode = ((SQLiteException) e).getResultCode();
            if (resultCode != SQLiteErrorCode.SQLITE_ERROR) {
                throw new MetricException("Failed to create storage for metric " + metric.getName(), e);
            }
        } finally {
            wlock.unlock();
        }
    }

    private void execute(String sql) throws SQLException {
        doInConnection(connection -> {
            Statement statement = connection.createStatement();
            try (Timer ignored = METRICS.startTimer("Execute")) {
                statement.execute(sql);
            } finally {
                closeQuietly(statement);
            }
            return null;
        });
    }

    private int update(String sql, Object... args) throws SQLException {
        requireNonNull(sql);
        return doInConnection(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            try (Timer ignored = METRICS.startTimer("Update")) {
                int index = 1;
                for (Object value : args) {
                    statement.setObject(index++, value);
                }
                return statement.executeUpdate();
            } finally {
                closeQuietly(statement);
            }
        });
    }

    private <T> T doWithResultSet(String sql, ResultSetCallback<T> callback, Object... args) throws SQLException {
        requireNonNull(sql);
        requireNonNull(callback);
        return doInConnection(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = null;
            try {
                try (Timer ignored = METRICS.startTimer("Query")) {
                    int index = 1;
                    for (Object value : args) {
                        statement.setObject(index++, value);
                    }
                    resultSet = statement.executeQuery();
                }
                return callback.doWithResultSet(resultSet);
            } finally {
                closeQuietly(resultSet);
                closeQuietly(statement);
            }
        });
    }

    private <T> T doInConnection(ConnectionCallback<T> callback) throws SQLException {
        requireNonNull(callback);
        Connection connection = CONNECTION.get();
        if (connection != null) {
            return callback.doInConnection(connection);
        } else {
            try {
                connection = getConnection();
                return callback.doInConnection(connection);
            } finally {
                closeQuietly(connection);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return getDriver().connect(getJdbcUrl(), properties);
    }

    private Driver getDriver() {
        rlock.lock();
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            config.setOpenMode(SQLiteOpenMode.READWRITE);
            properties = config.toProperties();
            String url = getJdbcUrl();
            LOGGER.info("Create database {}", url);
            try {
                driver = DriverManager.getDriver(url);
                LOGGER.info("SQLite database opened, version {}.{}", driver.getMajorVersion(), driver.getMinorVersion());
            } catch (SQLException e) {
                LOGGER.atError().setCause(e).log("Failed to initialize SQLite database {}", url);
            }
            return driver;
        } finally {
            rlock.unlock();
        }
    }

    private String getJdbcUrl() {
        return "jdbc:sqlite:" + getFile().getAbsolutePath();
    }

    private File getFile() {
        wlock.lock();
        try {
            if (db == null) {
                db = validateFileExists(new File(new File(getVariableDirectory(), "storage"), name));
            }
            return db;
        } finally {
            wlock.unlock();
        }
    }

    private static String getTableName(Metric metric) {
        return metric.getId();
    }

    private String createCreateSQL(Metric metric) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").append(getTableName(metric)).append(" (\n")
                .append("  timestamp INTEGER PRIMARY KEY ASC,\n")
                .append("  value REAL\n")
                .append("\n) WITHOUT ROWID");
        return builder.toString();
    }

    interface ConnectionCallback<T> {

        T doInConnection(Connection connection) throws SQLException;
    }

    interface ResultSetCallback<T> {

        T doWithResultSet(ResultSet resultSet) throws SQLException;
    }
}
