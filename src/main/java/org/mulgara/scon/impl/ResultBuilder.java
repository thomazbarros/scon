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

package org.mulgara.scon.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.mulgara.scon.Statement;
import org.mulgara.scon.ResultSet;
import org.mulgara.scon.UnhandledException;
import org.mulgara.scon.InternalException;

import org.mulgara.scon.parser.ParserFactory;
import org.mulgara.scon.parser.ResultParser;
import org.mulgara.scon.parser.N3Factory;
import org.mulgara.scon.parser.RdfXmlFactory;
import org.mulgara.scon.parser.SparqlXmlFactory;
import org.mulgara.scon.parser.SparqlJsonFactory;

/**
 * A class for building a result out of an HTTP response.
 */
public class ResultBuilder {

  /** The response from the server to build a result from. */
  private final HttpResponse response;

  /** The Statement used to create the result. */
  private final Statement statement;

  /**
   * Create a new result builder/
   * @param response The HTTP response that the result will be constructed from.
   */
  public ResultBuilder(HttpResponse response, Statement statement) {
    this.response = response;
    this.statement = statement;
  }


  /**
   * Determine the format of the result and create an appropriate header out of it.
   * @return A new result object.
   * @throws IOException If there is a communications fault while getting data.
   * @throws UnhandledException If the data is in a format that is not understood.
   * @throws InternalException If there was some kind of problem parsing the data.
   */
  public ResultSet createResult() throws IOException, UnhandledException, InternalException {
    HttpEntity entity = response.getEntity();
    if (entity == null) throw new UnhandledException("No data in response from server");
    Header header = entity.getContentType();

    ResponseType type = ResponseType.forMime(stripParams(header.getValue()));
    if (type == null) throw new UnhandledException("Unable to deal with a response of: " + header.getValue());
    ResultParser parser = type.getFactory().createParser(entity.getContent(), statement);
    return parser.getResultSet();
  }

  /**
   * Strips the parameters from the end of a mediaType description.
   * @param mediaType The text in a Content-Type header.
   * @return The content type string without any parameters.
   */
  private static final String stripParams(String mediaType) {
    int sc = mediaType.indexOf(';');
    if (sc >= 0) mediaType = mediaType.substring(0, sc);
    return mediaType;
  }


  /**
   * An enumeration describing the types of responses that can come back from a SPARQL endpoint.
   * Each member also holds a factory for a parser that can deal with that format.
   */
  enum ResponseType {
    SPARQL_XML("application/sparql-results+xml", new SparqlXmlFactory()),
    SPARQL_JSON("application/sparql-results+json", new SparqlJsonFactory()),
    RDF_XML("application/rdf+xml", new RdfXmlFactory()),
    RDF_N3("text/rdf+n3", new N3Factory());

    private String mimeText;
    private ParserFactory factory;

    private ResponseType(String mimeText, ParserFactory factory) {
      this.mimeText = mimeText;
      this.factory = factory;
    }
    public ParserFactory getFactory() { return factory; }

    static private Map<String,ResponseType> types = new HashMap<String,ResponseType>();
    static { for (ResponseType t: ResponseType.values()) types.put(t.mimeText, t); }
    static ResponseType forMime(String mimeText) { return types.get(mimeText); }
  }

}
