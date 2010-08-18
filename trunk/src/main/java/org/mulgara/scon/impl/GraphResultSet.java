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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mulgara.scon.ResultSet;
import org.mulgara.scon.ResultSetHeader;
import org.mulgara.scon.ResultSetMetaData;
import org.mulgara.scon.Statement;
import org.mulgara.scon.SparqlException;

import org.mulgara.mrg.Node;
import org.mulgara.mrg.SubjectNode;
import org.mulgara.mrg.PredicateNode;
import org.mulgara.mrg.ObjectNode;
import org.mulgara.mrg.AbstractGraphExt;
import org.mulgara.mrg.Graph;
import org.mulgara.mrg.Triple;
import org.mulgara.mrg.Uri;
import org.mulgara.mrg.PropertyValue;


/**
 * Contains the results of a construct operation.
 */
public class GraphResultSet extends AbstractGraphExt implements ResultSet, Graph {

  /** An offset to indicate the before-first condition */
  private static final int BEFORE_FIRST = -1;

  /** The name of the subject column. */
  private static final String SUBJECT = "subject";

  /** The name of the predicate column. */
  private static final String PREDICATE = "predicate";

  /** The name of the object column. */
  private static final String OBJECT = "object";

  /** The columns in the graph. */
  private static final String[] COLUMNS = { SUBJECT, PREDICATE, OBJECT };

  /** The graph constructed from the query. */
  private Graph graph;

  /** A list of all the triples in the graph. Only populated if requested. */
  private List<Triple> triples = null;

  /** The statement used to create this result set. */
  private final Statement statement;

  /** The offset for the after last position. */
  private final int afterLast;

  /** The cursor pointer. */
  private int cursor = BEFORE_FIRST;

  /**
   * Creates this result set with a header and boolean value.
   */
  public GraphResultSet(Graph graph, Statement statement) {
    this.graph = graph;
    this.statement = statement;
    afterLast = (int)graph.size();
  }

  /**
   * Returns the type of this result set as being with bindings.
   */
  public Type getSparqlType() {
    return Type.GRAPH;
  }

  /**
   * Gets the URI for the metadata link in the header.
   */
  public List<URI> getLinks() {
    return Collections.emptyList();
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
    return !graph.isEmpty();
  }


  public boolean last() throws SparqlException {
    cursor = afterLast - 1;
    return !graph.isEmpty();
  }



  public int findColumn(String name) throws SparqlException {
    for (int i = 0; i < COLUMNS.length; i++) if (COLUMNS[i].equals(name)) return i + 1;
    throw new SparqlException("Column not found: " + name);
  }


  public int getRow() throws SparqlException {
    return cursor >= afterLast ? 0 : (int)cursor;
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
    if (triples == null) triples = graph.getTriples();
    return columnValue(triples.get(cursor), column - 1);
  }


  public Object getObject(String name) throws SparqlException {
    if (triples == null) triples = graph.getTriples();
    return columnValue(triples.get(cursor), findColumn(name) - 1);
  }


  public URI getUri(int column) throws SparqlException {
    try {
      return ((Uri)getObject(column)).getURI();
    } catch (ClassCastException e) {
      throw new SparqlException("Data is not a URI", e);
    }
  }


  public URI getUri(String name) throws SparqlException {
    try {
      return ((Uri)getObject(name)).getURI();
    } catch (ClassCastException e) {
      throw new SparqlException("Data is not a URI", e);
    }
  }


  public ResultSetMetaData getMetaData() {
    return new GraphResultMetaData();
  }


  /**
   * Gets all the properties for a given subject.
   * @param s The subject.
   * @return A list of property/value pairs.
   */
  public List<PropertyValue> getProperties(SubjectNode s) {
    return graph.getProperties(s);
  }


  /**
   * Gets all the values for a given property on a subject.
   * @param s The subject to get the properties for.
   * @param p The property of interest.
   * @return The list of values for the property on that subject.
   */
  public List<ObjectNode> getValues(SubjectNode s, PredicateNode p) {
    return graph.getValues(s, p);
  }

  /**
   * Gets a single value for a given property on a subject.
   * @param s The subject to get the properties for.
   * @param p The property of interest.
   * @return The first values for the property on that subject.
   */
  public ObjectNode getValue(SubjectNode s, PredicateNode p) {
    return graph.getValue(s, p);
  }

 /**
   * Gets an rdf:List property from an object. If more than one
   * value exists for this property, then returns the first and assumes it's a list.
   * @param s The subject to get the property for.
   * @param p The property of interest.
   * @return The list associates with the property on that subject.
   */
  public List<ObjectNode> getRdfList(SubjectNode s, PredicateNode p) {
    return graph.getRdfList(s, p);
  }

  /**
   * Gets all the subjects that share a given property/value.
   * @param property The property being looked for.
   * @param value The value being looked for.
   * @return The subjects that have the value for the property.
   */
  public List<SubjectNode> getSubjects(PredicateNode property, ObjectNode value) {
    return graph.getSubjects(property, value);
  }

  /**
   * Gets the entire graph as a list of triples.
   * @return All the triples in the graph.
   */
  public List<Triple> getTriples() {
    return graph.getTriples();
  }


  /**
   * Tests if a resource exists anywhere in the graph.
   * @param r The resource to test.
   * @return <code>true</code> only if the resource is used somewhere in the graph.
   */
  public boolean doesResourceExist(Node r) {
    return graph.doesResourceExist(r);
  }

  /**
   * Writes the contents of the graph to an output stream as N3.
   * @param out The stream to write to.
   */
  public void exportN3(OutputStream out) throws IOException {
    graph.exportN3(out);
  }

  /**
   * Writes the contents of the graph to an output stream as N3.
   * @param out The stream to write to.
   * @param base The base to write to.
   */
  public void exportN3(OutputStream out, URI base) throws IOException {
    graph.exportN3(out, base);
  }

  /**
   * Writes the contents of the graph to an output stream as RDF/XML.
   * @param out The stream to write to.
   */
  public void exportXML(OutputStream out) throws IOException {
    graph.exportXML(out);
  }

  /**
   * Writes the contents of the graph to an output stream as RDF/XML.
   * @param out The stream to write to.
   * @param base The base to write to.
   */
  public void exportXML(OutputStream out, URI base) throws IOException {
    graph.exportXML(out, base);
  }

  /**
   * Gets all the objects in the graph.
   * @return All the objects in the graph.
   */
  public Collection<ObjectNode> getObjects() {
    return graph.getObjects();
  }

  /**
   * Gets all the predicates in the graph.
   * @return All the predicatess in the graph.
   */
  public Collection<PredicateNode> getPredicates() {
    return graph.getPredicates();
  }

  /**
   * Gets all the subjects in the graph.
   * @return All the subjects in the graph.
   */
  public Collection<SubjectNode> getSubjects() {
    return graph.getSubjects();
  }

  /**
   * Tests if a triple has been asserted. Be careful of blank nodes, as
   * they will only match if they are exactly alike.
   * @param t The triple to test for.
   * @return <code>true</code> only if the triple exists in the graph.
   */
  public boolean isAsserted(Triple t) {
    return graph.isAsserted(t);
  }

  /**
   * Tests if a triple has been asserted. Be careful of blank nodes, as
   * they will only match if they are exactly alike.
   * @param s The subject of the triple to search for.
   * @param p The predicate of the triple to search for.
   * @param o The object of the triple to search for.
   * @return <code>true</code> only if the triple exists in the graph.
   */
  public boolean isAsserted(SubjectNode s, PredicateNode p, ObjectNode o) {
    return graph.isAsserted(s, p, o);
  }

  /**
   * Gets the number of triples in this graph.
   * @return the number of triples in the graph.
   */
  public long size() {
    return graph.size();
  }


  /**
   * Tests if the graph has any entries.
   * @return <code>true</code> if there are no entries.
   */
  public boolean isEmpty() {
    return graph.isEmpty();
  }

  /**
   * Gets a value from Triple as if it were an array.
   * @param t The triple to get the value from.
   * @param i The offset into the effective array of the triple.
   * @return The value found in the triple at the given offset.
   */
  private static final Node columnValue(Triple t, int i) {
    if (i == 0) return t.getSubject();
    if (i == 1) return t.getPredicate();
    if (i == 2) return t.getObject();
    throw new IndexOutOfBoundsException();
  }

  /**
   * Inner class to represent the metadata of the result set.
   */
  static class GraphResultMetaData extends MetaData {

    @SuppressWarnings("unchecked")
    private static final ResultSetHeader HDR = new ResultSetHeader(Arrays.asList("subject", "predicate", "object"), Collections.EMPTY_LIST);

    GraphResultMetaData() {
      super(HDR);
    }

    public int getColumnCount() throws SparqlException {
      return 3;
    }

    public String getColumnName(int column) throws SparqlException {
      try {
        return COLUMNS[column - 1];
      } catch (ArrayIndexOutOfBoundsException ae) {
        throw new SparqlException("Column out of range");
      }
    }

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
  public InputStream getAsciiStream(int columnIndex) throws SQLException {
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
  public InputStream getAsciiStream(String columnLabel) throws SQLException {
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
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
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
