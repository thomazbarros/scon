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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The Driver interface is the main entry point for a compliant JDBC implementation.
 */
public class Driver implements java.sql.Driver {

  /** The main protocol for SPARQL. */
  private static final String HTTP = "http";

  /**
   * Creates a new connection. When username/passwords are supported then they
   * may be included in the info field.
   * @param url The URL of the endpoint.
   * @param info The connection parameters. Currently unused.
   */
  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    try {
      return new Connection(new URL(url));
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Unable to locate endpoint: " + url);
    }
  }

  /**
   * Tests to see if this driver can handle the given URL.
   * Since SPARQL is over http, then this is what is tested for.
   * HTTPS is possible, but currently unsupported.
   * @param url The potential URL.
   * @returns <code>true</code> iff the URL is HTTP.
   */
  @Override
  public boolean acceptsURL(String url) throws SQLException {
    try {
      String protocol = new URL(url).getProtocol();
      // TODO: HTTPS
      return protocol.equals(HTTP);
    } catch (MalformedURLException e) {
      return false;
    }
  }

  /**
   * Enquires about possible properties that may be needed for a successful
   * connection to an endpoint. No properties are needed at the moment
   * so an empty array is returned. The most obvious to consider will be
   * username/password.
   * @param url The endpoint that will be queried.
   * @param info The proposed properties.
   */
  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
    return new DriverPropertyInfo[0];
  }

  /**
   * The major version number.
   */
  @Override
  public int getMajorVersion() {
    return 1;
  }

  /**
   * The minor version number.
   */
  @Override
  public int getMinorVersion() {
    return 0;
  }

  /**
   * SPARQL can never pass the SQL92 tests, so this method must return false.
   */
  @Override
  public boolean jdbcCompliant() {
    return false;
  }

}
