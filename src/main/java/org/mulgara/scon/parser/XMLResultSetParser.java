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

import org.mulgara.scon.ResultSet;
import org.mulgara.scon.ResultSetHeader;
import org.mulgara.scon.Statement;
import org.mulgara.scon.InternalException;
import org.mulgara.scon.impl.BooleanResultSet;
import org.mulgara.scon.impl.BindingsResultSet;
import org.mulgara.mrg.Bnode;
import org.mulgara.mrg.Node;
import org.mulgara.mrg.Literal;
import org.mulgara.mrg.Uri;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static org.mulgara.util.Strings.toUtf8Bytes;
import static org.mulgara.scon.parser.XMLResultSetParser.ParseState.*;

/**
 * Contains the results of a query operation.
 */
public class XMLResultSetParser extends DefaultHandler implements ResultParser {

  public enum Element { SPARQL, HEAD, RESULTS, VARIABLE, LINK, RESULT, BINDING, URI, LITERAL, BNODE, BOOLEAN };

  public enum ParseState {
      STOPPED, STARTED, HEAD_SECT, AFTER_HEAD, READING_BOOLEAN, RESULTS_SECT,
      RESULT_SECT, RESULT_BINDING, URI_BINDING, BNODE_BINDING, LITERAL_BINDING
  };

  private static final String VAR_NAME = "name";
  private static final String HREF = "href";
  private static final String LANG = "xml:lang";
  private static final String DATATYPE = "datatype";

  /** The state of the parser */
  private ParseState state = STOPPED;

  /** An ordered list of variables in this result */
  private List<String> vars = new ArrayList<String>();

  /** A list of links in the header */
  private List<URI> links = new ArrayList<URI>();

  /** The header of the result set. Initialized at the end of the "header" section. */
  private ResultSetHeader header = null;

  /** The list of all the bindings for this result set. */
  private List<Node[]> results = new ArrayList<Node[]>();

  /** The ResultSet object being built by this parser. */
  private ResultSet resultSet = null;

  /** Number of rows in the results. */
  private int rows = 0;

  /** The current variable being bound. Only valid during a result section. */
  private String bindingVar = null;

  /** The language tag of the literal being scanned. Only valid during a "literal" section. */
  private String literalLang = null;

  /** The datatype of the literal being scanned. Only valid during a "literal" section. */
  private URI literalType = null;

  /** The current set of bindings being scanned. Only valid during a "result" section. */
  private Node[] currentBindings = null;

  /** The number of variables defined in this result. Initialized when the header is finalized. */
  private int width = 0;

  /** The statement used to generate the results being parsed. */
  private final Statement statement;

  /**
   * Create a result from a string.
   * @param s The string containing the result set.
   * @param statement The statement that created this data.
   */
  public XMLResultSetParser(String s, Statement statement) throws InternalException, IOException {
    this(new ByteArrayInputStream(toUtf8Bytes(s)), statement);
  }

  /**
   * Create a result set from an InputStream.
   * @param is The input stream with the results.
   * @param statement The statement that created this data.
   */
  public XMLResultSetParser(InputStream is, Statement statement) throws InternalException, IOException {
    this.statement = statement;
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(is, this);
    } catch (SAXException e) {
      throw new InternalException(e.getMessage(), e);
    } catch (ParserConfigurationException e) {
      throw new InternalError("Internal configuration of XML Parser failed: " + e.getMessage());
    }
  }

  /**
   * Retrieves the ResultSet that this parser built.
   */
  public ResultSet getResultSet() {
    return resultSet;
  }

  /**
   * Retrieves the variables for a result set.
   */
  public List<String> getVariables() {
    return Collections.unmodifiableList(vars);
  }

  /**
   * Return the number of rows parsed.
   */
  public int getProcessedRows() {
    return rows;
  }

  /**
   * Detects new elements as they come in.
   * This keeps a state machine to figure out what should be happening next.
   * @param uri The URI of the element. Usually empty.
   * @param localName The local name of the element. Usually empty.
   * @param qName The qName of the element. This is usually where the information is found.
   * @param attr The attributes of the element.
   */
  public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
    Element e = Enum.valueOf(Element.class, qName.toUpperCase());
    switch (e) {
      case SPARQL:
        if (state != STOPPED) throw new SAXException("SPARQL document embedded in another document.");
        state = STARTED;
        break;
      case HEAD:
        if (state != STARTED) throw new SAXException("Entered HEAD section without first initializing document");
        state = HEAD_SECT;
        break;
      case VARIABLE:
        if (state != HEAD_SECT) throw new SAXException("Variable definitions only permitted in the document header");
        if (!links.isEmpty()) throw new SAXException("Metadata links must appear after all variable definitions");
        vars.add(attr.getValue(VAR_NAME));
        break;
      case LINK:
        if (state != HEAD_SECT) throw new SAXException("Metadata links only permitted in the document header");
        try {
          links.add(new URI(attr.getValue(HREF)));
        } catch (URISyntaxException ue) {
          throw new SAXException("Bad Metadata link", ue);
        }
        break;
      case BOOLEAN:
        if (state != AFTER_HEAD && state != STARTED) throw new SAXException("Boolean result found in the wrong place in the document");
        state = READING_BOOLEAN;
        break;
      case RESULTS:
        if (state != AFTER_HEAD) throw new SAXException("Entered RESULTS without completing the header");
        state = RESULTS_SECT;
        break;
      case RESULT:
        if (state != RESULTS_SECT) throw new SAXException("Result outside of a results section.");
        rows++;
        currentBindings = new Node[width];
        state = RESULT_SECT;
        break;
      case BINDING:
        if (state != RESULT_SECT) throw new SAXException("Binding declared outside of a result.");
        bindingVar = attr.getValue(VAR_NAME);
        if (!header.defines(bindingVar)) throw new SAXException("Binding variable not present in header: " + bindingVar);
        state = RESULT_BINDING;
        break;
      case URI:
        if (state != RESULT_BINDING) throw new SAXException("URI declared outside of a result binding.");
        state = URI_BINDING;
        break;
      case BNODE:
        if (state != RESULT_BINDING) throw new SAXException("Blank Node declared outside of a result binding.");
        state = BNODE_BINDING;
        break;
      case LITERAL:
        if (state != RESULT_BINDING) throw new SAXException("Literal declared outside of a result binding.");
        literalLang = attr.getValue(LANG);
        String typeStr = attr.getValue(DATATYPE);
        try {
          if (typeStr != null) literalType = new URI(typeStr);
        } catch (URISyntaxException ue) {
          throw new SAXException("Bad datatype for literal in '" + bindingVar + "': " + typeStr);
        }
        state = LITERAL_BINDING;
        break;
      default:
        throw new SAXException("Encountered unknown element: " + qName);
    }
  }

  /**
   * This doesn't have to do any work except keep the state machine humming along.
   * @param uri The URI of the element. Usually empty.
   * @param localName The local name of the element. Usually empty.
   * @param qName The qName of the element. This is usually where the information is found.
   */
  public void endElement(String uri, String localName, String qName) throws SAXException {
    Element e = Enum.valueOf(Element.class, qName.toUpperCase());
    switch (e) {
      case SPARQL:
        if (state != STARTED) throw new SAXException("SPARQL document ended without starting.");
        state = STOPPED;
        break;
      case HEAD:
        if (state != HEAD_SECT) throw new SAXException("Ended a HEAD section without starting.");
        header = new ResultSetHeader(vars, links);
        width = vars.size();
        state = AFTER_HEAD;
        break;
      case BOOLEAN:
        if (state != READING_BOOLEAN) throw new SAXException("Ended a boolean result without starting.");
        assert resultSet != null;
        state = STARTED;
        break;
      case RESULTS:
        if (state != RESULTS_SECT) throw new SAXException("Ended a RESULTS section without starting.");
        resultSet = new BindingsResultSet(header, results, statement);
        state = STARTED;
        break;
      case RESULT:
        if (state != RESULT_SECT) throw new SAXException("Ended a RESULT sub-section without starting.");
        results.add(currentBindings);
        currentBindings = null;
        state = RESULTS_SECT;
        break;
      case BINDING:
        if (state != RESULT_BINDING) throw new SAXException("Ended a binding without starting.");
        bindingVar = null;
        state = RESULT_SECT;
        break;
      case URI:
        if (state != URI_BINDING) throw new SAXException("Ended a URI without defining it.");
        state = RESULT_BINDING;
        break;
      case BNODE:
        if (state != BNODE_BINDING) throw new SAXException("Ended a Blank Node without defining it.");
        state = RESULT_BINDING;
        break;
      case LITERAL:
        if (state != LITERAL_BINDING) throw new SAXException("Ended a Literal without defining it.");
        state = RESULT_BINDING;
        break;
      case LINK:
        if (state != HEAD_SECT) throw new SAXException("Closing of link outside of the document header");
        break;
      case VARIABLE:
        if (state != HEAD_SECT) throw new SAXException("Closing of variable definition outside of the document header");
        break;
      default:
        throw new SAXException("Closing unknown element: " + qName);
    }
  }

  /**
   * This reads the text between element tags.
   * @param ch A character array containing the text.
   * @param start The beginning of the characters containing the text.
   * @param length The size of the characters containing the text.
   */
  public void characters(char[] ch, int start, int length) throws SAXException {
    String data = new String(ch, start, length).trim();
    switch (state) {
      case READING_BOOLEAN:
        resultSet = new BooleanResultSet(header, Boolean.valueOf(data), statement);
        break;
      case URI_BINDING:
        try {
          currentBindings[header.getColumnIndex(bindingVar)] = new Uri(new URI(data));
        } catch (URISyntaxException e) {
          throw new SAXException("Invalid URI found in result: <" + data + ">");
        }
        break;
      case BNODE_BINDING:
        currentBindings[header.getColumnIndex(bindingVar)] = new Bnode(data);
        break;
      case LITERAL_BINDING:
        assert literalType == null || literalLang == null : "Literals cannot have a language code and datatype";
        Literal literal = null;
        if (literalType != null) {
          literal = new Literal(data, literalType);
        } else if (literalLang != null) {
          literal = new Literal(data, literalLang);
        } else {
          literal = new Literal(data);
        }
        currentBindings[header.getColumnIndex(bindingVar)] = literal;
        literalType = null;
        literalLang = null;
        break;
      default:
        // ignore whitespace
    }
  }

}
