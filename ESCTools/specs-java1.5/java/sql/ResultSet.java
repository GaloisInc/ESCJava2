package java.sql;

import java.math.BigDecimal;
import java.util.Calendar;

public interface ResultSet {
    
    boolean next() throws SQLException;
    
    void close() throws SQLException;
    
    boolean wasNull() throws SQLException;
    
    String getString(int columnIndex) throws SQLException;
    
    boolean getBoolean(int columnIndex) throws SQLException;
    
    byte getByte(int columnIndex) throws SQLException;
    
    short getShort(int columnIndex) throws SQLException;
    
    int getInt(int columnIndex) throws SQLException;
    
    long getLong(int columnIndex) throws SQLException;
    
    float getFloat(int columnIndex) throws SQLException;
    
    double getDouble(int columnIndex) throws SQLException;
    
    
    BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException;
    
    byte[] getBytes(int columnIndex) throws SQLException;
    
    java.sql.Date getDate(int columnIndex) throws SQLException;
    
    java.sql.Time getTime(int columnIndex) throws SQLException;
    
    java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException;
    
    java.io.InputStream getAsciiStream(int columnIndex) throws SQLException;
    
    
    java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException;
    
    java.io.InputStream getBinaryStream(int columnIndex) throws SQLException;
    
    String getString(String columnName) throws SQLException;
    
    boolean getBoolean(String columnName) throws SQLException;
    
    byte getByte(String columnName) throws SQLException;
    
    short getShort(String columnName) throws SQLException;
    
    int getInt(String columnName) throws SQLException;
    
    long getLong(String columnName) throws SQLException;
    
    float getFloat(String columnName) throws SQLException;
    
    double getDouble(String columnName) throws SQLException;
    
    
    BigDecimal getBigDecimal(String columnName, int scale) throws SQLException;
    
    byte[] getBytes(String columnName) throws SQLException;
    
    java.sql.Date getDate(String columnName) throws SQLException;
    
    java.sql.Time getTime(String columnName) throws SQLException;
    
    java.sql.Timestamp getTimestamp(String columnName) throws SQLException;
    
    java.io.InputStream getAsciiStream(String columnName) throws SQLException;
    
    
    java.io.InputStream getUnicodeStream(String columnName) throws SQLException;
    
    java.io.InputStream getBinaryStream(String columnName) throws SQLException;
    
    SQLWarning getWarnings() throws SQLException;
    
    void clearWarnings() throws SQLException;
    
    String getCursorName() throws SQLException;
    
    ResultSetMetaData getMetaData() throws SQLException;
    
    Object getObject(int columnIndex) throws SQLException;
    
    Object getObject(String columnName) throws SQLException;
    
    int findColumn(String columnName) throws SQLException;
    
    java.io.Reader getCharacterStream(int columnIndex) throws SQLException;
    
    java.io.Reader getCharacterStream(String columnName) throws SQLException;
    
    BigDecimal getBigDecimal(int columnIndex) throws SQLException;
    
    BigDecimal getBigDecimal(String columnName) throws SQLException;
    
    boolean isBeforeFirst() throws SQLException;
    
    boolean isAfterLast() throws SQLException;
    
    boolean isFirst() throws SQLException;
    
    boolean isLast() throws SQLException;
    
    void beforeFirst() throws SQLException;
    
    void afterLast() throws SQLException;
    
    boolean first() throws SQLException;
    
    boolean last() throws SQLException;
    
    int getRow() throws SQLException;
    
    boolean absolute(int row) throws SQLException;
    
    boolean relative(int rows) throws SQLException;
    
    boolean previous() throws SQLException;
    int FETCH_FORWARD = 1000;
    int FETCH_REVERSE = 1001;
    int FETCH_UNKNOWN = 1002;
    
    void setFetchDirection(int direction) throws SQLException;
    
    int getFetchDirection() throws SQLException;
    
    void setFetchSize(int rows) throws SQLException;
    
    int getFetchSize() throws SQLException;
    int TYPE_FORWARD_ONLY = 1003;
    int TYPE_SCROLL_INSENSITIVE = 1004;
    int TYPE_SCROLL_SENSITIVE = 1005;
    
    int getType() throws SQLException;
    int CONCUR_READ_ONLY = 1007;
    int CONCUR_UPDATABLE = 1008;
    
    int getConcurrency() throws SQLException;
    
    boolean rowUpdated() throws SQLException;
    
    boolean rowInserted() throws SQLException;
    
    boolean rowDeleted() throws SQLException;
    
    void updateNull(int columnIndex) throws SQLException;
    
    void updateBoolean(int columnIndex, boolean x) throws SQLException;
    
    void updateByte(int columnIndex, byte x) throws SQLException;
    
    void updateShort(int columnIndex, short x) throws SQLException;
    
    void updateInt(int columnIndex, int x) throws SQLException;
    
    void updateLong(int columnIndex, long x) throws SQLException;
    
    void updateFloat(int columnIndex, float x) throws SQLException;
    
    void updateDouble(int columnIndex, double x) throws SQLException;
    
    void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException;
    
    void updateString(int columnIndex, String x) throws SQLException;
    
    void updateBytes(int columnIndex, byte[] x) throws SQLException;
    
    void updateDate(int columnIndex, java.sql.Date x) throws SQLException;
    
    void updateTime(int columnIndex, java.sql.Time x) throws SQLException;
    
    void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException;
    
    void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException;
    
    void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException;
    
    void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException;
    
    void updateObject(int columnIndex, Object x, int scale) throws SQLException;
    
    void updateObject(int columnIndex, Object x) throws SQLException;
    
    void updateNull(String columnName) throws SQLException;
    
    void updateBoolean(String columnName, boolean x) throws SQLException;
    
    void updateByte(String columnName, byte x) throws SQLException;
    
    void updateShort(String columnName, short x) throws SQLException;
    
    void updateInt(String columnName, int x) throws SQLException;
    
    void updateLong(String columnName, long x) throws SQLException;
    
    void updateFloat(String columnName, float x) throws SQLException;
    
    void updateDouble(String columnName, double x) throws SQLException;
    
    void updateBigDecimal(String columnName, BigDecimal x) throws SQLException;
    
    void updateString(String columnName, String x) throws SQLException;
    
    void updateBytes(String columnName, byte[] x) throws SQLException;
    
    void updateDate(String columnName, java.sql.Date x) throws SQLException;
    
    void updateTime(String columnName, java.sql.Time x) throws SQLException;
    
    void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException;
    
    void updateAsciiStream(String columnName, java.io.InputStream x, int length) throws SQLException;
    
    void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException;
    
    void updateCharacterStream(String columnName, java.io.Reader reader, int length) throws SQLException;
    
    void updateObject(String columnName, Object x, int scale) throws SQLException;
    
    void updateObject(String columnName, Object x) throws SQLException;
    
    void insertRow() throws SQLException;
    
    void updateRow() throws SQLException;
    
    void deleteRow() throws SQLException;
    
    void refreshRow() throws SQLException;
    
    void cancelRowUpdates() throws SQLException;
    
    void moveToInsertRow() throws SQLException;
    
    void moveToCurrentRow() throws SQLException;
    
    Statement getStatement() throws SQLException;
    
    Object getObject(int i, java.util.Map map) throws SQLException;
    
    Ref getRef(int i) throws SQLException;
    
    Blob getBlob(int i) throws SQLException;
    
    Clob getClob(int i) throws SQLException;
    
    Array getArray(int i) throws SQLException;
    
    Object getObject(String colName, java.util.Map map) throws SQLException;
    
    Ref getRef(String colName) throws SQLException;
    
    Blob getBlob(String colName) throws SQLException;
    
    Clob getClob(String colName) throws SQLException;
    
    Array getArray(String colName) throws SQLException;
    
    java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException;
    
    java.sql.Date getDate(String columnName, Calendar cal) throws SQLException;
    
    java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException;
    
    java.sql.Time getTime(String columnName, Calendar cal) throws SQLException;
    
    java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException;
    
    java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException;
    int HOLD_CURSORS_OVER_COMMIT = 1;
    int CLOSE_CURSORS_AT_COMMIT = 2;
    
    java.net.URL getURL(int columnIndex) throws SQLException;
    
    java.net.URL getURL(String columnName) throws SQLException;
    
    void updateRef(int columnIndex, java.sql.Ref x) throws SQLException;
    
    void updateRef(String columnName, java.sql.Ref x) throws SQLException;
    
    void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException;
    
    void updateBlob(String columnName, java.sql.Blob x) throws SQLException;
    
    void updateClob(int columnIndex, java.sql.Clob x) throws SQLException;
    
    void updateClob(String columnName, java.sql.Clob x) throws SQLException;
    
    void updateArray(int columnIndex, java.sql.Array x) throws SQLException;
    
    void updateArray(String columnName, java.sql.Array x) throws SQLException;
}
