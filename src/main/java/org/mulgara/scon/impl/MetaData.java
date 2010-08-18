package org.mulgara.scon.impl;

import java.sql.SQLException;

import org.mulgara.scon.ResultSetHeader;
import org.mulgara.scon.ResultSetMetaData;
import org.mulgara.scon.SparqlException;

/**
 * Inner class to represent the metadata of the result set.
 */
class MetaData implements ResultSetMetaData {

  private ResultSetHeader header;

  public MetaData(ResultSetHeader hdr) {
    this.header = hdr;
  }

  public int getColumnCount() throws SparqlException {
    return header.getVariables().length;
  }

  public String getColumnName(int column) throws SparqlException {
    try {
      return header.getVariables()[column - 1];
    } catch (ArrayIndexOutOfBoundsException ae) {
      throw new SparqlException("Column out of range");
    }
  }

  @Override
  public boolean isAutoIncrement(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isCaseSensitive(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isSearchable(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isCurrency(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int isNullable(int column) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isSigned(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int getColumnDisplaySize(int column) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getColumnLabel(int column) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getSchemaName(int column) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getPrecision(int column) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getScale(int column) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getTableName(int column) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getCatalogName(int column) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getColumnType(int column) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getColumnTypeName(int column) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isReadOnly(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isWritable(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isDefinitelyWritable(int column) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getColumnClassName(int column) throws SQLException {
    // TODO Auto-generated method stub
    return null;
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
