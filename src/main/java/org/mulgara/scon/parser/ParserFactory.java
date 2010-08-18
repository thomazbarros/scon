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

/**
 * A type of class that can construct a result parser.
 */
public interface ParserFactory {

  /**
   * Creates a parser.
   * @param input The data to parse the results from.
   * @param stmt The statement used to generate the results.
   * @return a specific parser type for handling the data.
   * @throws IOException Error while the parser reads from the input stream.
   * @throws InternalException Error in the data read from the stream.
   */
  ResultParser createParser(InputStream input, Statement stmt) throws IOException, InternalException;

}
