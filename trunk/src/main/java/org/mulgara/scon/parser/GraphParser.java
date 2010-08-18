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

package org.mulgara.scon.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.mulgara.scon.ResultSet;
import org.mulgara.scon.Statement;
import org.mulgara.scon.InternalException;
import org.mulgara.scon.impl.GraphResultSet;
import org.mulgara.mrg.Graph;
import org.mulgara.mrg.parser.ParseException;

import static org.mulgara.util.Strings.toUtf8Bytes;

/**
 * Parses the contents of a data stream into a graph representation.
 */
public abstract class GraphParser implements ResultParser {

  /** Logging for this class. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(XMLGraphParser.class.getName());

  /** The statement used to generate the results being parsed. */
  private final Statement statement;

  /** The graph that is parsed from the input data. */
  private Graph graph;

  /** The number of triples parsed. */
  private long triples = 0;

  /**
   * Create a graph from a string.
   * A parser factory is provided here instead of a parser, since the implementing
   * class should only worry about the type, and not have to do the InputStream setup.
   * @param s The string containing the RDF/XML.
   * @param statement The statement that created this data.
   * @param parserFactory A factory for creating the correct type of parser.
   */
  protected GraphParser(String s, Statement statement, RdfParserFactory parserFactory) throws InternalException, IOException {
    this(new ByteArrayInputStream(toUtf8Bytes(s)), statement, parserFactory);
  }

  /**
   * Create a graph from an InputStream.
   * A parser factory is provided here instead of a parser, since the implementing
   * class should only worry about the type, and not have to do the InputStream setup.
   * @param is The input stream with the graph data.
   * @param statement The statement that created this data.
   * @param parserFactory A factory for creating the correct type of parser.
   */
  protected GraphParser(InputStream is, Statement statement, RdfParserFactory parserFactory) throws InternalException, IOException {
    this.statement = statement;

    try {
      org.mulgara.mrg.parser.GraphParser parser = parserFactory.createParser(is);
      graph = parser.getGraph();
      triples = parser.getProcessedRows();
    } catch (ParseException e) {
      throw new InternalException("Error parsing graph result", e);
    }
  }

  /**
   * Retrieves the ResultSet that this parser built.
   */
  public ResultSet getResultSet() {
    return new GraphResultSet(graph, statement);
  }

  /**
   * Return the number of rows parsed.
   */
  public int getProcessedRows() {
    return (int)triples;
  }

  /**
   * Describes a function for creating a graph parser.
   */
  protected interface RdfParserFactory {
    org.mulgara.mrg.parser.GraphParser createParser(InputStream is) throws ParseException, IOException;
  }
}
