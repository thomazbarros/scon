# Introduction #
ResultSets provide a cursor based interface to data returned from a query. While ResultSet extends JDBC ResultSets, it also provides some extra functionality more specifically related to SPARQL results.

SPARQL results can take one of 3 forms:
  * A list of rows, each of equal arbitrary width. These are returned from SELECT queries.
  * A list of rows, each 3 columns wide. These are returned from CONSTRUCT and DESCRIBE queries, and are actually RDF graphs.
  * A boolean value.