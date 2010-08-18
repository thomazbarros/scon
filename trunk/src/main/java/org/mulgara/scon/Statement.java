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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


/**
 * A Statement to be executed on an endpoint.
 */
public class Statement implements GraphURILists, java.sql.Statement {

  /** The connection for this statement. */
  private final Connection connection;

  /** The list of default graphs to use by default */
  private List<URI> defaultGraphs = new ArrayList<URI>();

  /** The list of named graphs to use by default */
  private List<URI> namedGraphs = new ArrayList<URI>();

  /** The last query executed on this statement */
  @SuppressWarnings("unused")
  private String lastOperation = null;

  /**
   * Creates a new statement to work on a connection.
   * @param connection The connection that this statements is associated with.
   */
  Statement(Connection connection) {
    this.connection = connection;
  }

  /**
   * Set the default graph for the connection to use.
   * @param u the default graph to use.
   */
  public void setDefaultGraph(String u) throws URISyntaxException {
    setDefaultGraph(new URI(u));
  }

  /**
   * Set the default graph for the connection to use.
   * @param u the default graph to use.
   */
  public void setDefaultGraph(URI u) {
    if (!defaultGraphs.isEmpty()) defaultGraphs.clear();
    defaultGraphs.add(u);
  }

  /**
   * Set the default graphs for the connection to use.
   * @param u the list of default graphs to use.
   */
  public void setDefaultGraphs(List<URI> uList) {
    defaultGraphs.addAll(uList);
  }

  /**
   * Adds a default graph to the existing set of graphs.
   * @param u the default graph to use.
   */
  public void addDefaultGraph(String u) throws URISyntaxException {
    addDefaultGraph(new URI(u));
  }

  /**
   * Adds a default graph to the existing set of graphs.
   * @param u the default graph to use.
   */
  public void addDefaultGraph(URI u) {
    if (!defaultGraphs.contains(u)) defaultGraphs.add(u);
  }

  /**
   * Adds a list of default graphs to the existing set of graphs.
   * @param u the default graph to use.
   */
  public void addDefaultGraphs(List<URI> uList) {
    for (URI u: uList) {
      if (!defaultGraphs.contains(u)) defaultGraphs.add(u);
    }
  }

  /**
   * Clears the default graphs for the connection to use.
   * This will be overridden by statements, if the statements
   * have a default graph set.
   */
  public void clearDefaultGraphs() {
    defaultGraphs.clear();
  }

  /**
   * Return the first default graph.
   * @return The first default graph if any are set, else null.
   */
  public URI getDefaultGraph() {
    return defaultGraphs.isEmpty() ? null : defaultGraphs.get(0);
  }

  /**
   * Get the default graphs.
   * @return The default graphs.
   */
  public List<URI> getDefaultGraphs() {
    return Collections.unmodifiableList(defaultGraphs);
  }

  /**
   * Set the named graph for the connection to use.
   * @param u the named graph to use.
   */
  public void setNamedGraph(String u) throws URISyntaxException {
    setNamedGraph(new URI(u));
  }

  /**
   * Set the named graph for the connection to use.
   * @param u the named graph to use.
   */
  public void setNamedGraph(URI u) {
    if (!namedGraphs.isEmpty()) namedGraphs.clear();
    namedGraphs.add(u);
  }

  /**
   * Set the named graphs for the connection to use.
   * @param u the list of named graphs to use.
   */
  public void setNamedGraphs(List<URI> uList) {
    namedGraphs.addAll(uList);
  }

  /**
   * Adds a named graph to the existing set of graphs.
   * @param u the named graph to use.
   */
  public void addNamedGraph(String u) throws URISyntaxException {
    addNamedGraph(new URI(u));
  }

  /**
   * Adds a named graph to the existing set of graphs.
   * @param u the named graph to use.
   */
  public void addNamedGraph(URI u) {
    if (!namedGraphs.contains(u)) namedGraphs.add(u);
  }

  /**
   * Adds a list of named graphs to the existing set of graphs.
   * @param u the named graph to use.
   */
  public void addNamedGraphs(List<URI> uList) {
    for (URI u: uList) {
      if (!namedGraphs.contains(u)) namedGraphs.add(u);
    }
  }

  /**
   * Clears the named graphs for the connection to use.
   */
  public void clearNamedGraphs() {
    namedGraphs.clear();
  }

  /**
   * Return the first named graph.
   * @return The first named graph if any are set, else null.
   */
  public URI getNamedGraph() {
    return namedGraphs.isEmpty() ? null : namedGraphs.get(0);
  }

  /**
   * Get the named graphs.
   * @return The named graphs.
   */
  public List<URI> getNamedGraphs() {
    return Collections.unmodifiableList(namedGraphs);
  }

  /**
   * Executes a given query on a connection.
   * @param query The query to execute.
   * @return A result set with the results of the query.
   */
  public ResultSet executeQuery(String query) throws SparqlException {
    this.lastOperation = query;
    try {
      return connection.executeQuery(this, query);
    } catch (IOException e) {
      throw new SparqlException("Error connecting to SPARQL endpoint", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) return (T)this;
    throw new SQLException("scon does not implement: " + iface.getName());
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isInstance(this);
  }

  @Override
  public int executeUpdate(String sparql) throws SQLException {
    this.lastOperation = sparql;
    try {
      return connection.executeUpdate(this, sparql);
    } catch (IOException e) {
      throw new SparqlException("Error connecting to SPARQL endpoint", e);
    }
  }

  @Override
  public void close() throws SQLException {
    // no resources held
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    // No limit
    return 0;
  }

  @Override
  public void setMaxFieldSize(int max) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMaxRows() throws SQLException {
    return 0;
  }

  @Override
  public void setMaxRows(int max) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEscapeProcessing(boolean enable) throws SQLException {
    // no op
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    return 0;
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
  }

  @Override
  public void cancel() throws SQLException {
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  @Override
  public void clearWarnings() throws SQLException {
  }

  @Override
  public void setCursorName(String name) throws SQLException {
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    return false;
  }

  @Override
  public java.sql.ResultSet getResultSet() throws SQLException {
    return null;
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return 0;
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    return false;
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return 0;
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
  }

  @Override
  public int getFetchSize() throws SQLException {
    return 0;
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    return 0;
  }

  @Override
  public int getResultSetType() throws SQLException {
    return 0;
  }

  @Override
  public void addBatch(String sql) throws SQLException {
  }

  @Override
  public void clearBatch() throws SQLException {
  }

  @Override
  public int[] executeBatch() throws SQLException {
    return null;
  }

  @Override
  public java.sql.Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public boolean getMoreResults(int current) throws SQLException {
    return false;
  }

  @Override
  public java.sql.ResultSet getGeneratedKeys() throws SQLException {
    return null;
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys)  throws SQLException {
    return 0;
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    return 0;
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    return 0;
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    return false;
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    return false;
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    return false;
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return 0;
  }

  @Override
  public boolean isClosed() throws SQLException {
    return connection.isClosed();
  }

  @Override
  public void setPoolable(boolean poolable) throws SQLException {
  }

  @Override
  public boolean isPoolable() throws SQLException {
    return false;
  }

}
