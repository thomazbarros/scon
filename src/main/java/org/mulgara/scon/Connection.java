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

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// HTTP Client 4.0
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

// HTTP Core 4.1-alpha1
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import static org.apache.http.protocol.HTTP.UTF_8;

import org.mulgara.scon.impl.ResultBuilder;

/**
 * This class represents a virtual connection to a SPARQL endpoint.
 * While each operation will create a separated HTTP request/response,
 * this object keeps the parameters to be used in these interations consistent.
 */
public class Connection implements GraphURILists, java.sql.Connection {

  /** The maximum length of a GET request */
  private static final int QUERY_LIMIT = 1024;

  @SuppressWarnings("unused")
  private static final int INFORMATIONAL_MIN = 100;
  @SuppressWarnings("unused")
  private static final int INFORMATIONAL_MAX = 199;
  private static final int SUCCESS_MIN = 200;
  private static final int SUCCESS_MAX = 299;
  @SuppressWarnings("unused")
  private static final int REDIRECT_MIN = 300;
  @SuppressWarnings("unused")
  private static final int REDIRECT_MAX = 399;
  private static final int CLIENT_ERROR_MIN = 400;
  private static final int CLIENT_ERROR_MAX = 499;
  private static final int SERVER_ERROR_MIN = 500;
  private static final int SERVER_ERROR_MAX = 599;

  /** The endpoint that this represents a connection to. */
  private final URL endpoint;

  /** Continuations are not expected by default */
  private boolean expectContinue = false;

  /** The socket timeout, set to 5000 by default. */
  private int soTimeout = 5000;

  /**
   * The list of default graphs to use by default. Always overridden by
   * any default graphs in a Statement.
   */
  private List<URI> defaultGraphs = new ArrayList<URI>();

  /**
   * The list of named graphs to use by default. Always overridden by
   * any named graphs in a Statement.
   */
  private List<URI> namedGraphs = new ArrayList<URI>();

  /** A Connection Manager for HTTP connections. Once set, this will not change. */
  private ClientConnectionManager conManager = null;

  /** A stream responding to the server. */
  private InputStream contentStream = null;

  /** a flag to indicate if this connection is closed. */
  private boolean closed = false;

  /** A collection of key/values that can be set by the client to control the HTTP headers */
  private Properties clientParams = new Properties();

  /**
   * Creates a new virtual connection. This is called from the DriverManager.
   * @param endpoint The endpoint this is a connection for.
   */
  Connection(URL endpoint) {
    this.endpoint = endpoint;
    conManager = getConnectionManager();
  }

  /**
   * Creates a statement to be executed over the connection.
   * @return the new statement.
   */
  public Statement createStatement() {
    return new Statement(this);
  }

  /**
   * Change the expect-continue setting.
   * @param expectContinue The new value for the setting.
   */
  public void setExpectContinue(boolean expectContinue) {
    this.expectContinue = expectContinue;
  }

  /**
   * Change the socket timeout.
   * @param the new socket timeout in milliseconds.
   */
  public void setSoTimeout(int soTimeout) {
    this.soTimeout = soTimeout;
  }

  /**
   * Retrieve the expect-continue value.
   * @return The boolean flag that indicates this state.
   */
  public boolean getExpectContinue() {
    return expectContinue;
  }

  /**
   * Retrieve the current socket timeout value.
   * @return the socket timeout value in milliseconds.
   */
  public int getSoTimeout() {
    return soTimeout;
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
   * Closes any held resources.
   */
  public void close() throws SparqlException {
    try {
      if (contentStream != null) {
        contentStream.close();
        contentStream = null;
      }
    } catch (IOException e) {
      throw new SparqlException("Error closing connection", e);
    } finally {
      closed = true;
    }
  }

  /**
   * Tests if this connection has been closed.
   * @return <code>true</code> if the connection has been explicitly closed.
   */
  public boolean isClosed() {
    return closed;
  }

  /**
   * Execute a statement on the endpoint represented by this connection.
   * @param The statement to execute.
   * @return The ResultSet for the query.
   */
  ResultSet executeQuery(Statement stmt, String query) throws SparqlException, IOException {
    HttpClient client = getHttpClient();

    HttpUriRequest req;

    String params = calcParams(stmt, query);
    String u = endpoint.toString() + "?" + params;
    if (u.length() > QUERY_LIMIT) {
      // POST connection
      URL url = endpoint;
      try {
        req = new HttpPost(url.toURI());
      } catch (URISyntaxException e) {
        throw new SparqlException("Endpoint <" + url + "> not in an acceptable format", e);
      }
      ((HttpPost)req).setEntity((HttpEntity)new StringEntity(calcParams(stmt, query)));
    } else {
      // GET connection
      req = new HttpGet(u);
    }

    addHeaders(req);
    try {
      HttpResponse response = client.execute(req);
      StatusLine status = response.getStatusLine();
      int code = status.getStatusCode();

      if (code >= SUCCESS_MIN && code <= SUCCESS_MAX) {
        ResultBuilder builder = new ResultBuilder(response, stmt);
        return builder.createResult();

      } else if (code >= CLIENT_ERROR_MIN && code >= CLIENT_ERROR_MAX) {
        throw new ClientException(status.getReasonPhrase(), code);
      } else if (code >= SERVER_ERROR_MIN && code >= SERVER_ERROR_MAX) {
        throw new ServerException(status.getReasonPhrase(), code);
      } else {
        throw new UnhandledException(status.getReasonPhrase(), code);
      }
    } catch (UnsupportedEncodingException e) {
      throw new InternalException("Unabled to encode data", e);
    } catch (ClientProtocolException cpe) {
      throw new InternalException("Error in protocol", cpe);
    }
  }

  /**
   * Execute an update statement on the endpoint represented by this connection.
   * @param The statement to execute.
   * @return The number of elements affected by this operation.
   */
  int executeUpdate(Statement stmt, String operation) throws SparqlException, IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Establish a client connection in HTTP
   * @return A new client connection with parameters set for this object
   */
  private HttpClient getHttpClient() {
    HttpParams params = new BasicHttpParams();
    params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, expectContinue);
    params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
    return new DefaultHttpClient(conManager, params);
  }

  /**
   * Calculate the string that will contain all the parameters.
   */
  private String calcParams(Statement stmt, String query) {
    StringBuilder params = new StringBuilder();
    List<URI> graphURIs = stmt.getDefaultGraphs();
    if (!graphURIs.isEmpty()) {
      for (URI u: graphURIs) params.append("default-graph-uri=").append(enc(u)).append("&");
    } else if (!defaultGraphs.isEmpty()) {
      for (URI u: defaultGraphs) params.append("default-graph-uri=").append(enc(u)).append("&");
    }

    graphURIs = stmt.getNamedGraphs();
    if (!graphURIs.isEmpty()) {
      for (URI u: graphURIs) params.append("named-graph-uri=").append(enc(u)).append("&");
    } else if (!namedGraphs.isEmpty()) {
      for (URI u: namedGraphs) params.append("named-graph-uri=").append(enc(u)).append("&");
    }
    params.append("query=").append(encode(query));
    return params.toString();
  }

  /**
   * Set up some basic properties for a thread-safe client connection factory.
   * @return A new connection manager
   */
  private ClientConnectionManager getConnectionManager() {
    HttpParams params = new BasicHttpParams();
    ConnManagerParams.setMaxTotalConnections(params, 200);
    ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);

    int port = endpoint.getPort();
    if (port == -1) port = endpoint.getDefaultPort();
    HttpHost host = new HttpHost(endpoint.getHost(), port);

    connPerRoute.setMaxForRoute(new HttpRoute(host), 50);
    ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
    return new ThreadSafeClientConnManager(params, schemeRegistry);
  }

  /**
   * Adds all client headers to the request
   * @param r The request to set the headers on.
   */
  private void addHeaders(HttpRequest r) {
    for (Map.Entry<Object,Object> kv: clientParams.entrySet()) {
      r.addHeader(kv.getKey().toString(), kv.getValue().toString());
    }
  }

  /**
   * Encodes a URI if it looks like it needs it.
   * @param u The URI to encode, if needed.
   * @return a minimally encoded URI.
   */
  private static final String enc(URI u) {
    try {
      // if there is no query, then just return the unencoded URI
      String query = u.getRawQuery();
      if (query == null) return u.toString();
      // encode the query, and add it to the end of the URI
      String encQuery = encode(query);
      String encU = new URI(u.getScheme(), u.getUserInfo(), u.getHost(),
                            u.getPort(), u.getPath(), encQuery, u.getFragment()).toString();
      // if the partial encoding works, then return it
      if (decode(encU).equals(u.toString())) return encU;
      // nothing else worked, so encode it fully
      return encode(u.toString());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Unable to encode a URI", e);
    }
  }

  private static final String encode(String s) {
    try {
      return URLEncoder.encode(s, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new Error("JVM unable to handle UTF-8");
    }
  }

  private static final String decode(String s) {
    try {
      return URLDecoder.decode(s, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new Error("JVM unable to handle UTF-8");
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
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    return sql;
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    // no-op
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return true;
  }

  @Override
  public void commit() throws SQLException {
    // no-op
  }

  @Override
  public void rollback() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return new DatabaseMetaData(endpoint, clientParams);
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return true;
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCatalog() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return java.sql.Connection.TRANSACTION_NONE;
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  @Override
  public void clearWarnings() throws SQLException {
    // no-op
  }

  @Override
  public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getHoldability() throws SQLException {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  // TODO: this can be implemented with an xsd:hexBinary or xsd:base64Binary
  @Override
  public Clob createClob() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  // TODO: this can be implemented with an xsd:hexBinary or xsd:base64Binary
  @Override
  public Blob createBlob() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  // TODO: this can be implemented with an xsd:hexBinary or xsd:base64Binary
  @Override
  public NClob createNClob() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  // TODO: this can be implemented with an rdf:XMLLiteral
  @Override
  public SQLXML createSQLXML() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    return !closed && contentStream != null;
  }

  /**
   * Sets a property to be used as a header on the SPARQL connection.
   * No integrity checking is performed on the strings.
   * @param name The name of the HTTP header
   * @param value The value of the HTTP header
   */
  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    clientParams.put(name, value);
  }

  /**
   * Sets all the properties to be used as a header on the SPARQL connection.
   * @param properties A Properties object containing all required properties.
   */
  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    clientParams = properties;
  }

  /**
   * Reads a specific header that is used on SPARQL requests.
   * @param name The name of the header to read.
   * @returns The value of the requested property.
   */
  @Override
  public String getClientInfo(String name) throws SQLException {
    return clientParams.getProperty(name);
  }

  /**
   * Reads all the headers that are used on SPARQL requests.
   * @returns a Properties object containing all the headers that are used.
   */
  @Override
  public Properties getClientInfo() throws SQLException {
    return clientParams;
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

}
