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
import java.net.URISyntaxException;
import java.util.List;

/**
 * Defines the methods that must be present when handling lists of default graph URIs and
 * named graph URIs.
 */
public interface GraphURILists {

  /**
   * Set the default graph for the connection to use.
   * @param u the default graph to use.
   */
  public void setDefaultGraph(String u) throws URISyntaxException;

  /**
   * Set the default graph for the connection to use.
   * @param u the default graph to use.
   */
  public void setDefaultGraph(URI u);

  /**
   * Set the default graphs for the connection to use.
   * @param u the list of default graphs to use.
   */
  public void setDefaultGraphs(List<URI> uList);

  /**
   * Adds a default graph to the existing set of graphs.
   * @param u the default graph to use.
   */
  public void addDefaultGraph(String u) throws URISyntaxException;

  /**
   * Adds a default graph to the existing set of graphs.
   * @param u the default graph to use.
   */
  public void addDefaultGraph(URI u);

  /**
   * Adds a list of default graphs to the existing set of graphs.
   * @param u the default graph to use.
   */
  public void addDefaultGraphs(List<URI> uList);

  /**
   * Clears the default graphs for the connection to use.
   */
  public void clearDefaultGraphs();

  /**
   * Return the first default graph.
   * @return The first default graph if any are set, else null.
   */
  public URI getDefaultGraph();

  /**
   * Get the default graphs.
   * @return The default graphs.
   */
  public List<URI> getDefaultGraphs();

  /**
   * Set the named graph for the connection to use.
   * @param u the named graph to use.
   */
  public void setNamedGraph(String u) throws URISyntaxException;

  /**
   * Set the named graph for the connection to use.
   * @param u the named graph to use.
   */
  public void setNamedGraph(URI u);

  /**
   * Set the named graphs for the connection to use.
   * @param u the list of named graphs to use.
   */
  public void setNamedGraphs(List<URI> uList);

  /**
   * Adds a named graph to the existing set of graphs.
   * @param u the named graph to use.
   */
  public void addNamedGraph(String u) throws URISyntaxException;

  /**
   * Adds a named graph to the existing set of graphs.
   * @param u the named graph to use.
   */
  public void addNamedGraph(URI u);

  /**
   * Adds a list of named graphs to the existing set of graphs.
   * @param u the named graph to use.
   */
  public void addNamedGraphs(List<URI> uList);

  /**
   * Clears the named graphs for the connection to use.
   */
  public void clearNamedGraphs();

  /**
   * Return the first named graph.
   * @return The first named graph if any are set, else null.
   */
  public URI getNamedGraph();

  /**
   * Get the named graphs.
   * @return The named graphs.
   */
  public List<URI> getNamedGraphs();

}
