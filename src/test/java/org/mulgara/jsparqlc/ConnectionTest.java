package org.mulgara.scon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.Arrays;

import org.mulgara.mrg.Bnode;
import org.mulgara.mrg.Literal;
import org.mulgara.mrg.Uri;
import org.mulgara.mrg.Graph;
import org.mulgara.mrg.SubjectNode;
import org.mulgara.mrg.ObjectNode;

/**
 * Unit test for simple App.
 */
public class ConnectionTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public ConnectionTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(ConnectionTest.class);
  }


  public static final String ENDPOINT = "http://localhost:8080/sparql/";

  public void testConnection() throws Exception {
    @SuppressWarnings("unused")
    Connection c = DriverManager.getConnection(ENDPOINT);
  }

  public void testStatement() throws Exception {
    Connection c = DriverManager.getConnection(ENDPOINT);
    Statement s = c.createStatement();
    s.setDefaultGraph("test:data");
  }

  public void testQuery() throws Exception {
    Connection c = DriverManager.getConnection(ENDPOINT);
    Statement s = c.createStatement();
    s.setDefaultGraph("test:data");
    ResultSet rs = s.executeQuery("SELECT * WHERE { ?s ?p ?o }");
    rs.close();
    c.close();
  }

  public void testQueryResults() throws Exception {
    Connection c = DriverManager.getConnection(ENDPOINT);
    Statement s = c.createStatement();
    s.setDefaultGraph("test:data");
    ResultSet rs = s.executeQuery("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\nSELECT * WHERE { ?s foaf:name ?o }\nORDER BY ?o");
    rs.beforeFirst();
    int i = 0;
    while (rs.next()) {
      assertTrue(rs.getObject(1) instanceof Bnode);
      assertEquals(EXPECTED[i++], ((Literal)rs.getObject(2)).getText());
    }
    assertEquals(EXPECTED.length, i);
    rs.close();
    c.close();
  }

  public void testConstructResults() throws Exception {
    Uri foafName = new Uri("http://xmlns.com/foaf/0.1/name");
    Connection c = DriverManager.getConnection(ENDPOINT);
    Statement s = c.createStatement();
    s.setDefaultGraph("test:data");
    ResultSet rs = s.executeQuery("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\nCONSTRUCT { ?s foaf:name ?o } WHERE { ?s foaf:name ?o }\nORDER BY ?o");
    rs.beforeFirst();
    int i = 0;
    List<String> expected = Arrays.asList(EXPECTED);
    while (rs.next()) {
      assertTrue(rs.getObject(1) instanceof Bnode);
      assertEquals(foafName, rs.getObject(2));
      assertTrue(expected.contains(((Literal)rs.getObject(3)).getText()));
      i++;
    }
    assertEquals(EXPECTED.length, i);

    assertTrue(rs instanceof Graph);
    Graph g = (Graph)rs;
    List<SubjectNode> people = g.getSubjects(foafName, new Literal("Bruce Campbell"));
    assertEquals(1, people.size());
    Bnode person = (Bnode)people.get(0);
    List<ObjectNode> names = g.getValues(person, foafName);
    assertEquals(2, names.size());
    assertTrue(names.contains(new Literal("Bruce Campbell")));
    assertTrue(names.contains(new Literal("Bob Smith")));

    rs.close();
    c.close();
  }

  private final String[] EXPECTED = { "Alice Smith", "Bob Collins", "Bob Smith", "Bruce Campbell" };
}

