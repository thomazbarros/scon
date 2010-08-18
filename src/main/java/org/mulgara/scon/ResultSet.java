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

package org.mulgara.scon;

import java.net.URI;
import java.util.List;

/**
 * Contains the results of a query operation.
 */
public interface ResultSet extends java.sql.ResultSet {

  /** The types of result set that can be returned by SPARQL. */
  public enum Type { BOOLEAN, BINDINGS, GRAPH };

  /**
   * Returns the type of this result set.
   * @return The type of this result.
   */
  public Type getSparqlType();

  /**
   * Gets the URI for the metadata link in the header.
   * @return All the metadata links described in the result header.
   */
  public List<URI> getLinks();

  /**
   * Frees up all resources.
   */
  public void close() throws SparqlException;

  /**
   * Gets the statement that cursor is currently on.
   */
  public Statement getStatement() throws SparqlException;

  /**
   * @return <code>true</code> if the cursor is before the first position.
   */
  public boolean isBeforeFirst() throws SparqlException;

  /**
   * @return <code>true</code> if the cursor is after the last position.
   */
  public boolean isAfterLast() throws SparqlException;

  /**
   * @return <code>true</code> if the cursor is on the first position.
   */
  public boolean isFirst() throws SparqlException;

  /**
   * @return <code>true</code> if the cursor is on the last position.
   */
  public boolean isLast() throws SparqlException;

  /**
   * Sets the cursor position to before the first row.
   */
  public void beforeFirst() throws SparqlException;

  /**
   * Sets the cursor position to after the first row.
   */
  public void afterLast() throws SparqlException;

  /**
   * Moves the cursor to the first row in this ResultSet object.
   * @return <code>true</code> if the cursor is on a valid row;
   *         <code>false</code> if there are no rows in the result set.
   */
  public boolean first() throws SparqlException;

  /**
   * Moves the cursor to the last row in this ResultSet object.
   * @return <code>true</code> if the cursor is on a valid row;
   *         <code>false</code> if there are no rows in the result set.
   */
  public boolean last() throws SparqlException;

  /**
   * Gets the column index of the named variable.
   * @param name The variable to get the index for.
   * @return The 1-based offset of the column with that variable.
   * @throws SparqlException if the column does not exist in the result set.
   */
  public int findColumn(String name) throws SparqlException;

  /**
   * Gets the offset of the current position of the cursor.
   * @return the cursor position.
   */
  public int getRow() throws SparqlException;

  /**
   * Move the cursor to a given row number.
   * @param row The offset of the row to set the cursor to.
   */
  public boolean absolute(int row) throws SparqlException;

  /**
   * Move the cursor to a row number relative to the current position.
   * @param row The number of rows to move the cursor from its current position.
   */
  public boolean relative(int rows) throws SparqlException;

  /**
   * Move the cursor to the next row.
   * @return <code>true</code> if there new cursor position is on a valid row;
   *         <code>false</code> if the cursor has moved past the final row.
   */
  public boolean next() throws SparqlException;

  /**
   * Move the cursor to the previous row.
   * @return <code>true</code> if there new cursor position is on a valid row;
   *         <code>false</code> if the cursor has moved before the first row.
   */
  public boolean previous() throws SparqlException;

  /**
   * Gets the data in the given column.
   * @param The 1-based column offset of the data to retrieve.
   * @return The Node found in that column.
   */
  public Object getObject(int column) throws SparqlException;

  /**
   * Gets the data in the given column.
   * @param The name of the column with the data to retrieve.
   * @return The Node found in that column.
   */
  public Object getObject(String name) throws SparqlException;

  /**
   * Gets the URI in the given column.
   * @param The 1-based column offset of the data to retrieve.
   * @return The URI found in that column.
   */
  public URI getUri(int column) throws SparqlException;

  /**
   * Gets the URI in the given column.
   * @param The name of the column with the data to retrieve.
   * @return The URI found in that column.
   */
  public URI getUri(String name) throws SparqlException;

  /**
   * Retrieve the metadata for this result set.
   * @return The Metadata of this result set.
   */
  public ResultSetMetaData getMetaData();
}
