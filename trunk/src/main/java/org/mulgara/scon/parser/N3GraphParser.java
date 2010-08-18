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

import java.io.InputStream;
import java.io.IOException;

import org.mulgara.scon.Statement;
import org.mulgara.scon.InternalException;
import org.mulgara.mrg.parser.ParseException;

/**
 * Parses the contents of an N3 file into a graph representation.
 */
public class N3GraphParser extends GraphParser {

  /**
   * Create a graph from a string.
   * @param s The string containing the RDF/XML.
   * @param statement The statement that created this data.
   */
  public N3GraphParser(String s, Statement statement) throws InternalException, IOException {
    super(s, statement, createFactory());
  }

  /**
   * Create a graph from an InputStream.
   * @param is The input stream with the graph data.
   * @param statement The statement that created this data.
   */
  public N3GraphParser(InputStream is, Statement statement) throws InternalException, IOException {
    super(is, statement, createFactory());
  }

  /**
   * Creates a function for creating a graph parser.
   */
  private static RdfParserFactory createFactory() {
    return new RdfParserFactory() {
      public org.mulgara.mrg.parser.GraphParser createParser(InputStream is) throws ParseException, IOException {
        return new org.mulgara.mrg.parser.N3GraphParser(is);
      }
    };
  }
}
