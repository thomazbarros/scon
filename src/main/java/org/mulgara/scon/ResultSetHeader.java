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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains header information for a result set.
 */
public class ResultSetHeader {

  /** The list of variables, in order of definition. */
  private final List<String> variables;

  /** A mapping from variables back to column numbers. */
  private final Map<String,Integer> columns;

  /** The metadata links in a result set document. */
  private final List<URI> links;

  /**
   * Construct a header with a list of variables and metadata.
   * @param variables The variables defined in the header.
   * @param links The links defined in the header.
   */
  public ResultSetHeader(List<String> variables, List<URI> links) {
    this.variables = Collections.unmodifiableList(variables);
    this.links = Collections.unmodifiableList(links);
    columns = new HashMap<String,Integer>();
    for (int i = 0; i < variables.size(); i++) {
      columns.put(variables.get(i), i);
    }
  }

  /**
   * Get the list of links for metadata for these results.
   */
  public List<URI> getLinks() {
    return links;
  }

  public String[] getVariables() {
    return variables.toArray(new String[variables.size()]);
  }

  public int getColumnIndex(String var) {
    return columns.get(var);
  }

  public String getColumnName(int index) {
    return variables.get(index);
  }

  /**
   * Indicates if this header contains a given variable.
   * @param var The variable to test for.
   * @return <code>true</code> only if the variable is defined in this header.
   */
  public boolean defines(String var) {
    return columns.containsKey(var);
  }

}
