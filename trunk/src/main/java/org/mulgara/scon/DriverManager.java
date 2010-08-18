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

import java.net.URL;
import java.net.MalformedURLException;

public class DriverManager {

  public static URL endpoint = null;

  /**
   * Sets a new endpoint to use.
   * @param endpoint the new endpoint.
   * @return The previous endpoint.
   */
  public static URL setEndpoint(String endpoint) {
    try {
      return setEndpoint(new URL(endpoint));
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Unable to locate endpoint: " + endpoint);
    }
  }

  /**
   * Sets a new endpoint to use.
   * @param endpoint the new endpoint.
   * @return The previous endpoint.
   */
  public static URL setEndpoint(URL endpoint) {
    URL tmp = DriverManager.endpoint;
    DriverManager.endpoint = endpoint;
    return tmp;
  }

  public static Connection getConnection(String endpoint) {
    try {
      return new Connection(new URL(endpoint));
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Unable to locate endpoint: " + endpoint);
    }
  }

  public static Connection getConnection(URL endpoint) {
    return new Connection(endpoint);
  }

  public static Connection getConnection() {
    return new Connection(endpoint);
  }

}
