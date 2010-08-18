package org.mulgara.scon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.URI;

import org.mulgara.scon.impl.*;
import org.mulgara.scon.parser.XMLResultSetParser;
import org.mulgara.scon.parser.XMLGraphParser;
import org.mulgara.scon.parser.N3GraphParser;
import org.mulgara.mrg.Bnode;
import org.mulgara.mrg.Literal;
import org.mulgara.mrg.Uri;
import static org.mulgara.scon.ResultSet.Type.*;

/**
 * Unit test for simple App.
 */
public class ResultSetTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public ResultSetTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(ResultSetTest.class);
  }

  /**
   * Test that boolean result sets parse
   */
  public void testTrue() throws Exception {
    XMLResultSetParser parser = new XMLResultSetParser(trueDoc, null);
    ResultSet rs = parser.getResultSet();
    assertEquals(BOOLEAN, rs.getSparqlType());
    rs.next();
    assertEquals(Boolean.TRUE, rs.getObject(1));
    assertTrue(((BooleanResultSet)rs).getValue());
  }

  /**
   * Test that boolean result sets parse
   */
  public void testFalse() throws Exception {
    XMLResultSetParser parser = new XMLResultSetParser(falseDoc, null);
    ResultSet rs = parser.getResultSet();
    assertEquals(BOOLEAN, rs.getSparqlType());
    assertFalse(((BooleanResultSet)rs).getValue());
    rs.next();
    assertEquals(Boolean.FALSE, rs.getObject(1));
  }

  /**
   * Test that boolean result sets parse
   */
  public void testBoolLinks() throws Exception {
    XMLResultSetParser parser = new XMLResultSetParser(trueDoc, null);
    ResultSet rs = parser.getResultSet();
    assertTrue(rs.getLinks().isEmpty());

    parser = new XMLResultSetParser(falseDoc, null);
    rs = parser.getResultSet();
    assertEquals(1, rs.getLinks().size());
    URI u = rs.getLinks().get(0);
    assertEquals(URI.create("foo:bar"), u);
  }

  /**
   * Test that bindings result sets parse
   */
  public void testBindings() throws Exception {
    XMLResultSetParser parser = new XMLResultSetParser(bindingsDoc, null);
    ResultSet rs = parser.getResultSet();
    assertEquals(BINDINGS, rs.getSparqlType());
  }

  /**
   * Test that bindings result sets contain appropariate data
   */
  public void testBindingStructure() throws Exception {
    XMLResultSetParser parser = new XMLResultSetParser(bindingsDoc, null);
    ResultSet rs = parser.getResultSet();
    rs.last();
    assertEquals(1, rs.getRow());
    rs.first();
    assertEquals(0, rs.getRow());
    assertEquals(new Bnode("r2"), rs.getObject("x"));
    assertTrue(rs.next());
    assertEquals(new Bnode("r7"), rs.getObject("x"));
    assertFalse(rs.next());
  }

  /**
   * Test that bindings result sets contain appropariate data
   */
  public void testBindingDetails() throws Exception {
    XMLResultSetParser parser = new XMLResultSetParser(bindingsDoc, null);
    ResultSet rs = parser.getResultSet();
    rs.beforeFirst();
    assertTrue(rs.next());
    assertEquals(new Bnode("r2"), rs.getObject("x"));
    assertEquals(new Uri("http://work.example.org/bob/"), rs.getObject("hpage"));
    assertEquals(new Literal("Bob", "en"), rs.getObject("name"));
    assertEquals(new Literal("30", new URI("http://www.w3.org/2001/XMLSchema#integer")), rs.getObject("age"));
    assertEquals(new Uri("mailto:bob@work.example.org"), rs.getObject("mbox"));
    assertEquals(new Uri("http://work.example.org/fred/#me"), rs.getObject("friend"));
    assertTrue(rs.next());
    assertEquals(new Bnode("r7"), rs.getObject("x"));
    assertEquals(new Uri("http://work.example.org/fred/"), rs.getObject("hpage"));
    assertEquals(new Literal("Fred", "fr"), rs.getObject("name"));
    assertEquals(new Literal("35", new URI("http://www.w3.org/2001/XMLSchema#integer")), rs.getObject("age"));
    assertEquals(new Uri("mailto:fred@work.example.org"), rs.getObject("mbox"));
    assertEquals(null, rs.getObject("friend"));
    try {
      rs.getObject("foo");
    } catch (Exception e) { }
    assertFalse(rs.next());
    try {
      rs.getObject("x");
    } catch (Exception e) { }
  }

  public void testMetadata() throws Exception {
    XMLResultSetParser parser = new XMLResultSetParser(bindingsDoc, null);
    ResultSet rs = parser.getResultSet();
    ResultSetMetaData md = rs.getMetaData();
    assertEquals(6, md.getColumnCount());
    assertEquals("x", md.getColumnName(1));
    assertEquals("hpage", md.getColumnName(2));
    assertEquals("name", md.getColumnName(3));
    assertEquals("age", md.getColumnName(4));
    assertEquals("mbox", md.getColumnName(5));
    assertEquals("friend", md.getColumnName(6));
    try {
      md.getColumnName(0);
    } catch (SparqlException e) { }
    try {
      md.getColumnName(7);
    } catch (SparqlException e) { }
  }

  /**
   * Test that graph result sets contain appropariate data
   */
  public void testGraphStructure() throws Exception {
    Uri shoeSize = new Uri("http://biometrics.example/ns#shoeSize");
    Uri name = new Uri("http://xmlns.com/foaf/0.1/name");
    Literal sizeVal = new Literal("9.5", new URI("http://www.w3.org/2001/XMLSchema#float"));
    Literal nameVal = new Literal("Alice Smith");

    XMLGraphParser parser = new XMLGraphParser(graphDoc, null);
    ResultSet rs = parser.getResultSet();
    rs.last();
    assertEquals(1, rs.getRow());
    rs.first();
    assertEquals(0, rs.getRow());
    assertEquals(new Bnode("Unode197"), rs.getObject("subject"));
    Uri p = (Uri)rs.getObject("predicate");
    Literal v = (Literal)rs.getObject("object");
    if (p.equals(shoeSize)) {
      assertEquals(sizeVal, v);
    } else if (p.equals(name)) {
      assertEquals(nameVal, v);
    }
    assertTrue(rs.next());
    assertEquals(new Bnode("Unode197"), rs.getObject("subject"));
    p = (Uri)rs.getObject("predicate");
    v = (Literal)rs.getObject("object");
    if (p.equals(shoeSize)) {
      assertEquals(sizeVal, v);
    } else if (p.equals(name)) {
      assertEquals(nameVal, v);
    }

    assertFalse(rs.next());
  }

  /**
   * Test that N3 graph result sets contain appropariate data
   */
  public void testN3GraphStructure() throws Exception {
    Uri shoeSize = new Uri("http://biometrics.example/ns#shoeSize");
    Uri name = new Uri("http://xmlns.com/foaf/0.1/name");
    Literal sizeVal = new Literal("9.5", new URI("http://www.w3.org/2001/XMLSchema#float"));
    Literal nameVal = new Literal("Alice Smith");

    N3GraphParser parser = new N3GraphParser(n3graphDoc, null);
    ResultSet rs = parser.getResultSet();
    rs.last();
    assertEquals(1, rs.getRow());
    rs.first();
    assertEquals(0, rs.getRow());
    Bnode anon = (Bnode)rs.getObject("subject");
    Uri p = (Uri)rs.getObject("predicate");
    Literal v = (Literal)rs.getObject("object");
    if (p.equals(shoeSize)) {
      assertEquals(sizeVal, v);
    } else if (p.equals(name)) {
      assertEquals(nameVal, v);
    }
    assertTrue(rs.next());
    assertEquals(anon, rs.getObject("subject"));
    p = (Uri)rs.getObject("predicate");
    v = (Literal)rs.getObject("object");
    if (p.equals(shoeSize)) {
      assertEquals(sizeVal, v);
    } else if (p.equals(name)) {
      assertEquals(nameVal, v);
    }

    assertFalse(rs.next());
  }

  static final String trueDoc = "<?xml version=\"1.0\"?>\n" +
      "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n" +
      "  <head>\n" +
      "  </head>\n" +
      "  <boolean>true</boolean>\n" +
      "</sparql>";

  static final String falseDoc = "<?xml version=\"1.0\"?>\n" +
      "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n" +
      "  <head>\n" +
      "    <link href=\"foo:bar\"/>\n" +
      "  </head>\n" +
      "  <boolean>false</boolean>\n" +
      "</sparql>";

  static final String bindingsDoc = "<?xml version=\"1.0\"?>\n" +
      "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n" +
      "  <head>\n" +
      "    <variable name=\"x\"/>\n" +
      "    <variable name=\"hpage\"/>\n" +
      "    <variable name=\"name\"/>\n" +
      "    <variable name=\"age\"/>\n" +
      "    <variable name=\"mbox\"/>\n" +
      "    <variable name=\"friend\"/>\n" +
      "  </head>\n" +
      "\n" +
      "  <results>\n" +
      "\n" +
      "    <result> \n" +
      "      <binding name=\"x\">\n" +
      "        <bnode>r2</bnode>\n" +
      "      </binding>\n" +
      "      <binding name=\"hpage\">\n" +
      "        <uri>http://work.example.org/bob/</uri>\n" +
      "      </binding>\n" +
      "      <binding name=\"name\">\n" +
      "        <literal xml:lang=\"en\">Bob</literal>\n" +
      "      </binding>\n" +
      "      <binding name=\"age\">\n" +
      "        <literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">30</literal>\n" +
      "      </binding>\n" +
      "      <binding name=\"mbox\">\n" +
      "        <uri>mailto:bob@work.example.org</uri>\n" +
      "      </binding>\n" +
      "      <binding name=\"friend\">\n" +
      "        <uri>http://work.example.org/fred/#me</uri>\n" +
      "      </binding>\n" +
      "    </result>\n" +
      "\n" +
      "    <result> \n" +
      "      <binding name=\"x\">\n" +
      "        <bnode>r7</bnode>\n" +
      "      </binding>\n" +
      "      <binding name=\"hpage\">\n" +
      "        <uri>http://work.example.org/fred/</uri>\n" +
      "      </binding>\n" +
      "      <binding name=\"name\">\n" +
      "        <literal xml:lang=\"fr\">Fred</literal>\n" +
      "      </binding>\n" +
      "      <binding name=\"age\">\n" +
      "        <literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">35</literal>\n" +
      "      </binding>\n" +
      "      <binding name=\"mbox\">\n" +
      "        <uri>mailto:fred@work.example.org</uri>\n" +
      "      </binding>\n" +
      "    </result>\n" +
      "\n" +
      "  </results>\n" +
      "</sparql>\n";

  static final String graphDoc = "<?xml version=\"1.0\"?>\n" +
      "\n" +
      "<!DOCTYPE rdf:RDF [\n" +
      "<!ENTITY rdf 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n" +
      "<!ENTITY ns1 'http://xmlns.com/foaf/0.1/'>\n" +
      "<!ENTITY xsd 'http://www.w3.org/2001/XMLSchema#'>\n" +
      "<!ENTITY ns2 'http://biometrics.example/ns#'>]>\n" +
      "\n" +
      "<rdf:RDF\n" +
      "  xmlns:rdf=\"&rdf;\"\n" +
      "  xmlns:ns1=\"&ns1;\"\n" +
      "  xmlns:ns2=\"&ns2;\">\n" +
      "\n" +
      "  <rdf:Description rdf:nodeID=\"node197\">\n" +
      "    <ns2:shoeSize rdf:datatype=\"&xsd;float\">9.5</ns2:shoeSize>\n" +
      "    <ns1:name>Alice Smith</ns1:name>\n" +
      "  </rdf:Description>\n" +
      "</rdf:RDF>\n";

  static final String n3graphDoc = "@prefix foaf:       <http://xmlns.com/foaf/0.1/> .\n" +
      "@prefix eg:         <http://biometrics.example/ns#> .\n" +
      "@prefix xsd:        <http://www.w3.org/2001/XMLSchema#> .\n" +
      "\n" +
      "_:a  foaf:name       \"Alice Smith\".\n" +
      "_:a  eg:shoeSize     \"9.5\"^^xsd:float .\n";

}
