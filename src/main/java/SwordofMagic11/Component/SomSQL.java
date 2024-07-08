package SwordofMagic11.Component;

import SwordofMagic11.DataBase.DataBase;
import com.github.jasync.sql.db.*;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import com.github.jasync.sql.db.pool.ConnectionPool;
import com.google.common.primitives.Doubles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static SwordofMagic11.SomCore.Log;

public class SomSQL {
    private static final String host = "192.168.0.9";
    private static final int port = 3306;
    private static final String database = "SwordofMagic11";
    private static final String user = "somnet";
    private static final String pass = "somnet";

    private static Connection connection;

    public static void connection() {
        long start = System.currentTimeMillis();
        Log("§a[SomSQL]§eConnecting...");
        MySQLConnectionFactory factory = new MySQLConnectionFactory(new Configuration(user, host, port, pass, database));
        ConnectionPoolConfiguration configuration = new ConnectionPoolConfiguration();
        connection = new ConnectionPool<>(factory, configuration);
        try {
            connection.connect().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        long milli = System.currentTimeMillis() - start;
        if (connection.isConnected()) {
            Log("§a[SomSQL]§bConnection - " + milli + "ms");
        } else {
            Log("§a[SomSQL]§cError - " + milli + "ms");
        }
    }

    public static ResultSet query(String query) {
        CompletableFuture<QueryResult> future = connection.sendQuery(query);
        try {
            return future.get().getRows();
        } catch (InterruptedException | ExecutionException e) {
            Log("§c[QueryError]§r" + query + "\n§7" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String[] normalization(String[] primaryKey, Object[] primaryValue) {
        StringBuilder primaryInsert = new StringBuilder(primaryKey[0]);
        StringBuilder keyInsert = new StringBuilder(primaryValue[0] instanceof String ? "'" + primaryValue[0] + "'" : primaryValue[0].toString());
        for (int i = 1; i < primaryKey.length; i++) {
            primaryInsert.append(", ").append(primaryKey[i]);
            keyInsert.append(", ").append(primaryValue[i] instanceof String ? "'" + primaryValue[i] + "'" : primaryValue[i].toString());
        }
        return new String[]{primaryInsert.toString(), keyInsert.toString()};
    }

    public static Function function(String function, Object... objects) {
        StringBuilder builder = new StringBuilder("(").append(checkString(objects[0]));
        for (int i = 1; i < objects.length; i++) {
            Object object = objects[i];
            builder.append(", ");
            builder.append(checkString(object));
        }
        builder.append(")");
        return new Function(query("SELECT " + function + builder + " AS 'Return'"));
    }

    public static class Function {

        private final ResultSet resultSet;

        public Function(ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        public String asString() {
            return resultSet.get(0).getString("Return");
        }

        public double asDouble(double defaultValue) {
            Double value = resultSet.get(0).getDouble("Return");
            return value != null ? value : defaultValue;
        }

        public double asDouble() {
            return resultSet.get(0).getDouble("Return");
        }

        public int asInt(int defaultValue) {
            Integer value = resultSet.get(0).getInt("Return");
            return value != null ? value : defaultValue;
        }

        public int asInt() {
            return resultSet.get(0).getInt("Return");
        }

        public boolean asBool() {
            return Boolean.parseBoolean(resultSet.get(0).getString("Return"));
        }
    }

    public static ResultSet call(String call, Object... objects) {
        StringBuilder builder = new StringBuilder("(").append(checkString(objects[0]));
        for (int i = 1; i < objects.length; i++) {
            Object object = objects[i];
            builder.append(", ");
            builder.append(checkString(object));
        }
        builder.append(")");
        return query("CALL " + call + builder);
    }

    public static Object checkString(Object text) {
        if (text == null) return "NULL";
        return Doubles.tryParse(text.toString()) != null ? text : "'" + text + "'";
    }

    public static boolean exists(DataBase.Table table, String primaryKey, String primaryValue) {
        return exists(table, new String[]{primaryKey}, new String[]{primaryValue}, "*");
    }

    public static boolean exists(DataBase.Table table, String primaryKey, String primaryValue, String column) {
        return exists(table, new String[]{primaryKey}, new String[]{primaryValue}, column);
    }

    public static boolean exists(DataBase.Table table, String[] primaryKey, String[] primaryValue) {
        return exists(table, primaryKey, primaryValue, "*");
    }

    public static boolean exists(DataBase.Table table, String[] primaryKey, String[] primaryValue, String column) {
        String[] primary = normalization(primaryKey, primaryValue);
        Integer count = query("SELECT EXISTS(SELECT " + column + " FROM `" + table + "` WHERE (" + primary[0] + ") = (" + primary[1] + ")) AS `Boolean`").get(0).getInt("Boolean");
        return count > 0;
    }

    public static RowData getSql(DataBase.Table table, String primaryKey, String primaryValue, String colum) {
        return getSql(table, new String[]{primaryKey}, new String[]{primaryValue}, colum);
    }

    public static RowData getSql(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum) {
        String[] primary = normalization(primaryKey, primaryValue);
        return query("select " + colum + " from `" + table + "` where (" + primary[0] + ") = (" + primary[1] + ")").get(0);
    }

    public static RowData getSql(DataBase.Table table, String colum) {
        return query("select " + colum + " from `" + table + "`").get(0);
    }

    public static ResultSet getSqlList(DataBase.Table table, String colum) {
        return getSqlList(table, colum, "");
    }

    public static ResultSet getSqlList(DataBase.Table table, String colum, String extra) {
        return query("select " + colum + " from `" + table + "` " + extra);
    }

    public static ResultSet getSqlList(DataBase.Table table, String primaryKey, String primaryValue, String colum) {
        return getSqlList(table, primaryKey, primaryValue, colum, "");
    }

    public static ResultSet getSqlList(DataBase.Table table, String primaryKey, String primaryValue, String colum, String extra) {
        return getSqlList(table, new String[]{primaryKey}, new String[]{primaryValue}, colum, extra);
    }

    public static ResultSet getSqlList(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum) {
        return getSqlList(table, primaryKey, primaryValue, colum, "");
    }

    public static ResultSet getSqlList(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum, String extra) {
        String[] primary = normalization(primaryKey, primaryValue);
        return query("select " + colum + " from `" + table + "` where (" + primary[0] + ") = (" + primary[1] + ") " + extra);
    }

    public static String getString(DataBase.Table table, String primaryKey, String primaryValue, String colum) {
        return getString(table, new String[]{primaryKey}, new String[]{primaryValue}, colum);
    }

    public static String getString(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum) {
        return getSql(table, primaryKey, primaryValue, colum).getString(colum);
    }

    public static Double getDouble(DataBase.Table table, String primaryKey, String primaryValue, String colum) {
        return getDouble(table, new String[]{primaryKey}, new String[]{primaryValue}, colum);
    }

    public static Double getDouble(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum) {
        return getSql(table, primaryKey, primaryValue, colum).getDouble(colum);
    }

    public static Float getFloat(DataBase.Table table, String primaryKey, String primaryValue, String colum) {
        return getFloat(table, new String[]{primaryKey}, new String[]{primaryValue}, colum);
    }

    public static Float getFloat(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum) {
        return getSql(table, primaryKey, primaryValue, colum).getFloat(colum);
    }

    public static Integer getInt(DataBase.Table table, String primaryKey, String primaryValue, String colum) {
        return getInt(table, new String[]{primaryKey}, new String[]{primaryValue}, colum);
    }

    public static Integer getInt(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum) {
        return getSql(table, primaryKey, primaryValue, colum).getInt(colum);
    }

    public static Boolean getBool(DataBase.Table table, String primaryKey, String primaryValue, String colum) {
        return getBool(table, new String[]{primaryKey}, new String[]{primaryValue}, colum);
    }

    public static Boolean getBool(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum) {
        return getSql(table, primaryKey, primaryValue, colum).getBoolean(colum);
    }

    public static int getDistinct(DataBase.Table table, String colum) {
        return query("SELECT `" + colum + "` count(distinct `" + colum + "`) AS \"Count\" FROM " + table).get(0).getInt("Count");
    }

    public static void setSql(DataBase.Table table, String colum, Object value) {
        setSql(table, "ID", "0", colum, value);
    }

    public static void setSql(DataBase.Table table, String primaryKey, String primaryValue, String colum, Enum<?> value) {
        setSql(table, new String[]{primaryKey}, new String[]{primaryValue}, colum, value.toString());
    }

    public static void setSql(DataBase.Table table, String primaryKey, String primaryValue, String colum, Object value) {
        setSql(table, new String[]{primaryKey}, new String[]{primaryValue}, colum, value);
    }

    public static void setSql(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum, Object value) {
        String[] primary = normalization(primaryKey, primaryValue);
        String data = null;
        if (value instanceof String) {
            data = "'" + value + "'";
        } else if (value != null) {
            data = value.toString();
        }
        query("insert into `" + table + "` (" + primary[0] + ", " + colum + ") values (" + primary[1] + ", " + data + ") on duplicate key update " + colum + " = " + data);
    }

    public static void delete(DataBase.Table table, String primaryKey, String primaryValue) {
        delete(table, new String[]{primaryKey}, new String[]{primaryValue});
    }

    public static void delete(DataBase.Table table, String[] primaryKey, String[] primaryValue) {
        String[] primary = normalization(primaryKey, primaryValue);
        query("delete from `" + table + "` where (" + primary[0] + ") = (" + primary[1] + ")");
    }

    public static void addNumber(DataBase.Table table, String primaryKey, String primaryValue, String colum, int number) {
        addNumber(table, new String[]{primaryKey}, new String[]{primaryValue}, colum, number);
    }

    public static void addNumber(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum, int number) {
        String[] primary = normalization(primaryKey, primaryValue);
        query("update `" + table + "` set " + colum + " = " + colum + " + " + number + " where (" + primary[0] + ") = (" + primary[1] + ")");
    }

    public static void addNumber(DataBase.Table table, String colum, int number) {
        query("update `" + table + "` set " + colum + " = " + colum + " + " + number);
    }

    public static void addNumber(DataBase.Table table, String colum, double number) {
        query("update `" + table + "` set " + colum + " = " + colum + " + " + number);
    }

    public static void removeNumber(DataBase.Table table, String primaryKey, String primaryValue, String colum, int number) {
        removeNumber(table, new String[]{primaryKey}, new String[]{primaryValue}, colum, number);
    }

    public static void removeNumber(DataBase.Table table, String[] primaryKey, String[] primaryValue, String colum, int number) {
        String[] primary = normalization(primaryKey, primaryValue);
        query("update `" + table + "` set " + colum + " = " + colum + " - " + number + " where (" + primary[0] + ") = (" + primary[1] + ")");
    }

    public static void removeNumber(DataBase.Table table, String colum, int number) {
        query("update `" + table + "` set " + colum + " = " + colum + " - " + number);
    }

    public static void removeNumber(DataBase.Table table, String colum, double number) {
        query("update `" + table + "` set " + colum + " = " + colum + " - " + number);
    }
}
