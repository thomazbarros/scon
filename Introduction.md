SCON: Java SPARQL Connectivity API

# Introduction #

The purpose of this library is to provide a SPARQL API in a framework that will appear familiar to users of JDBC.

Since RDF does not have the same structure as data in a relational database, some elements of JDBC cannot be duplicated in SPARQL. However, some of the basic ideas are kept. The initial goal of this project was to make the following JDBC code execute a SPARQL query on a SPARQL endpoint:

```
    Connection c = DriverManager.getConnection(ENDPOINT);
    Statement s = c.createStatement();
    ResultSet rs = s.executeQuery(THE_QUERY);
    rs.beforeFirst();
    int columns = rs.getMetadata().getColumnCount();
    while (rs.next()) {
      for (int c = 1; c < columns; c++) {
        System.out.print(rs.getObject() + " ");
      }
      System.out.println();
    }
    rs.close();
    c.close();
```


# Details #

This library uses HTTP for communication, and XML as the default data serialization. Much of the protocol code is boilerplate to avoid communicating with HTTP, and parsers for converting results into ResultSet objects. The code for this is all to be found in org.mulgara.jsparqlc. All the classes you need to access for Scon are to be found in this package.

Because data being returned represents RDF, the library also makes heavy use of the [MRG](http://code.google.com/p/mrg/) API. This is a complete [Graph API](http://code.google.com/p/mrg/wiki/GraphReading) for programmatic handling of RDF data, and is developed in conjunction with this project.