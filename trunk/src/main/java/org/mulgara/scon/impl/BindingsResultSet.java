/*
 * Copyright 2010 Paul Gearon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.scon.impl;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.net.URI;
import java.net.URL;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.mulgara.scon.ResultSet;
import org.mulgara.scon.ResultSetHeader;
import org.mulgara.scon.ResultSetMetaData;
import org.mulgara.scon.Statement;
import org.mulgara.scon.SparqlException;

import org.mulgara.mrg.Node;
import org.mulgara.mrg.Uri;
import org.mulgara.mrg.Literal;
import org.mulgara.mrg.vocab.uri.XSD;

/**
 * Contains the bindings results of a query operation.
 */
@SuppressWarnings("deprecation")
public class BindingsResultSet implements ResultSet {

  /** An offset to indicate the before-first condition */
  private static final int BEFORE_FIRST = -1;

  /** The parsed header data for this result set */
  private final ResultSetHeader header;

  /** The bindings for this result set. */
  private final List<Node[]> values;

  /** The statement used to create this result set. */
  private final Statement statement;

  /** The offset for the after last position. */
  private final int afterLast;

  /** The cursor pointer. */
  private int cursor = BEFORE_FIRST;

  /**
   * Creates this result set with a header and boolean value.
   */
  public BindingsResultSet(ResultSetHeader header, List<Node[]> values, Statement statement) {
    this.header = header;
    this.values = values;
    this.statement = statement;
    afterLast = values.size();
  }

  /**
   * Returns the type of this result set as being with bindings.
   */
  public Type getSparqlType() {
    return Type.BINDINGS;
  }

  /**
   * Gets the URI for the metadata link in the header.
   */
  public List<URI> getLinks() {
    return header.getLinks();
  }


  public void close() throws SparqlException {
    // no op
  }


  public Statement getStatement() throws SparqlException {
    return statement;
  }


  public boolean isBeforeFirst() throws SparqlException {
    return cursor == BEFORE_FIRST;
  }


  public boolean isAfterLast() throws SparqlException {
    return cursor == afterLast;
  }


  public boolean isFirst() throws SparqlException {
    return cursor == 0;
  }


  public boolean isLast() throws SparqlException {
    return cursor == afterLast - 1;
  }


  public void beforeFirst() throws SparqlException {
    cursor = BEFORE_FIRST;
  }


  public void afterLast() throws SparqlException {
    cursor = afterLast;
  }


  public boolean first() throws SparqlException {
    cursor = 0;
    return !values.isEmpty();
  }


  public boolean last() throws SparqlException {
    cursor = afterLast - 1;
    return !values.isEmpty();
  }



  public int findColumn(String name) throws SparqlException {
    return header.getColumnIndex(name) + 1;
  }


  public int getRow() throws SparqlException {
    return cursor >= afterLast ? 0 : cursor;
  }


  public boolean absolute(int row) throws SparqlException {
    if (row < 0) {
      cursor = afterLast + row;
    } else {
      cursor = row - 1;
    }
    if (cursor < BEFORE_FIRST) cursor = BEFORE_FIRST;
    if (cursor > afterLast) cursor = afterLast;
    return cursor > BEFORE_FIRST && cursor < afterLast;
  }


  public boolean relative(int rows) throws SparqlException {
    cursor += rows;
    if (cursor < BEFORE_FIRST) cursor = BEFORE_FIRST;
    if (cursor > afterLast) cursor = afterLast;
    return cursor > BEFORE_FIRST && cursor < afterLast;
  }


  public boolean next() throws SparqlException {
    if (++cursor > afterLast) throw new SparqlException("next() moved beyond the end of the results");
    return cursor < afterLast;
  }


  public boolean previous() throws SparqlException {
    if (--cursor < BEFORE_FIRST) throw new SparqlException("previous() moved before the start of the results");
    return cursor > BEFORE_FIRST;
  }


  public Object getObject(int column) throws SparqlException {
    return values.get(cursor)[column - 1];
  }


  public Object getObject(String name) throws SparqlException {
    return values.get(cursor)[header.getColumnIndex(name)];
  }


  /**
   * Returns a URI from the object. This will try to convert either a Uri or
   * a Literal with datatype of anyURI.
   */
  public URI getUri(int column) throws SparqlException {
    Node v = values.get(cursor)[column - 1];
    if (v instanceof Uri) return ((Uri)v).getURI();
    if (v instanceof Literal) {
      Literal l = (Literal)v;
      try {
        if (l.getType() == XSD.ANY_URI) return (URI)l.getValue();
      } catch (ClassCastException e) {
        throw new SparqlException("Data is marked as a URI but is not: " + l.getValue());
      }
    }
    throw new SparqlException("Data is not a URI");
  }


  public URI getUri(String name) throws SparqlException {
    return getUri(header.getColumnIndex(name));
  }


  // java.io does not provide any tools to do this properly, so use the deprecated StringBufferInputStream
  public InputStream getAsciiStream(int column) throws SparqlException {
    try {
      return new StringBufferInputStream(((Literal)values.get(cursor)[column - 1]).getText());
    } catch (ClassCastException e) {
      throw new SparqlException("Data cannot be serialized to ASCII");
    }
  }


  public InputStream getAsciiStream(String name) throws SparqlException {
    return getAsciiStream(header.getColumnIndex(name));
  }


  public BigDecimal getBigDecimal(int column) throws SparqlException {
    try {
      return (BigDecimal)((Literal)values.get(cursor)[column - 1]).getValue();
    } catch (ClassCastException e) {
      throw new SparqlException("Data is not a BigDecimal");
    }
  }


  public BigDecimal getBigDecimal(String name) throws SparqlException {
    return getBigDecimal(header.getColumnIndex(name));
  }


  public ResultSetMetaData getMetaData() {
    return new MetaData(header);
  }


  @Override
  public boolean wasNull() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getString(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean getBoolean(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public byte getByte(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public short getShort(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getInt(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getLong(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float getFloat(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getDouble(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex, int scale)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public byte[] getBytes(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getDate(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Time getTime(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timestamp getTimestamp(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getUnicodeStream(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getBinaryStream(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getString(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean getBoolean(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public byte getByte(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public short getShort(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getInt(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getLong(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float getFloat(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getDouble(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel, int scale)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public byte[] getBytes(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getDate(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Time getTime(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timestamp getTimestamp(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getUnicodeStream(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getBinaryStream(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clearWarnings() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getCursorName() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Reader getCharacterStream(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Reader getCharacterStream(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int getFetchDirection() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int getFetchSize() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getType() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getConcurrency() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean rowUpdated() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean rowInserted() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean rowDeleted() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateNull(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateByte(int columnIndex, byte x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateShort(int columnIndex, short x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateInt(int columnIndex, int x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateLong(int columnIndex, long x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateFloat(int columnIndex, float x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateDouble(int columnIndex, double x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBigDecimal(int columnIndex, BigDecimal x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateString(int columnIndex, String x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateDate(int columnIndex, Date x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateTime(int columnIndex, Time x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, int length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, int length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, int length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateObject(int columnIndex, Object x, int scaleOrLength)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateObject(int columnIndex, Object x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNull(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBoolean(String columnLabel, boolean x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateByte(String columnLabel, byte x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateShort(String columnLabel, short x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateInt(String columnLabel, int x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateLong(String columnLabel, long x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateFloat(String columnLabel, float x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateDouble(String columnLabel, double x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBigDecimal(String columnLabel, BigDecimal x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateString(String columnLabel, String x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBytes(String columnLabel, byte[] x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateDate(String columnLabel, Date x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateTime(String columnLabel, Time x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateTimestamp(String columnLabel, Timestamp x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, int length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, int length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader,
      int length) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateObject(String columnLabel, Object x, int scaleOrLength)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateObject(String columnLabel, Object x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void insertRow() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateRow() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteRow() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void refreshRow() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void cancelRowUpdates() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void moveToInsertRow() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void moveToCurrentRow() throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Object getObject(int columnIndex, Map<String, Class<?>> map)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Ref getRef(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Blob getBlob(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Clob getClob(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Array getArray(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getObject(String columnLabel, Map<String, Class<?>> map)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Ref getRef(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Blob getBlob(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Clob getClob(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Array getArray(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date getDate(String columnLabel, Calendar cal) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Time getTime(String columnLabel, Calendar cal) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timestamp getTimestamp(int columnIndex, Calendar cal)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timestamp getTimestamp(String columnLabel, Calendar cal)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getURL(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getURL(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateRef(int columnIndex, Ref x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateRef(String columnLabel, Ref x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBlob(int columnIndex, Blob x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBlob(String columnLabel, Blob x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateClob(int columnIndex, Clob x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateClob(String columnLabel, Clob x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateArray(int columnIndex, Array x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateArray(String columnLabel, Array x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public RowId getRowId(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RowId getRowId(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateRowId(int columnIndex, RowId x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateRowId(String columnLabel, RowId x) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int getHoldability() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isClosed() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateNString(int columnIndex, String nString)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNString(String columnLabel, String nString)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public NClob getNClob(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NClob getNClob(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SQLXML getSQLXML(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SQLXML getSQLXML(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateSQLXML(int columnIndex, SQLXML xmlObject)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateSQLXML(String columnLabel, SQLXML xmlObject)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getNString(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getNString(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Reader getNCharacterStream(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Reader getNCharacterStream(String columnLabel) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader,
      long length) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader,
      long length) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream,
      long length) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateClob(int columnIndex, Reader reader, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateClob(String columnLabel, Reader reader, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader, long length)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateClob(int columnIndex, Reader reader) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateClob(String columnLabel, Reader reader) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader)
      throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

}
