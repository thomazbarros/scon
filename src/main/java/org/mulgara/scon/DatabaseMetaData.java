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

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author pag
 *
 */
public class DatabaseMetaData implements java.sql.DatabaseMetaData {

  private final URL endpoint;

  private final Properties clientParams;

  static final String PRODUCT_NAME = "scon. Java SPARQL Connectivity";

  static final int VERSION_MAJOR = 1;

  static final int VERSION_MINOR = 0;

  static final String VERSION = Integer.toString(VERSION_MAJOR) + "." + VERSION_MINOR;

  static final String USERNAME = "username";

  static final String KEYWORDS = "BASE,SELECT,ORDER BY,FROM,GRAPH,STR,isURI," +
                                 "PREFIX,CONSTRUCT,LIMIT,FROM NAMED,OPTIONAL,LANG,isIRI," +
                                 "DESCRIBE,OFFSET,WHERE,UNION,LANGMATCHES,isLITERAL," +
                                 "ASK,DISTINCT,FILTER,DATATYPE,REGEX," +
                                 "REDUCED,a,BOUND,true,sameTERM,false,isBLANK";

  static String extraVarnameChars;

  static final int[][] EXTRA_VARNAME_RANGES = new int[][] {
    new int[] {0x00C0, 0x00D6},
    new int[] {0x00D8, 0x00F6},
    new int[] {0x00F8, 0x02FF},
    new int[] {0x0370, 0x037D},
    new int[] {0x037F, 0x1FFF},
    new int[] {0x200C, 0x200D},
    new int[] {0x2070, 0x218F},
    new int[] {0x2C00, 0x2FEF},
    new int[] {0x3001, 0xD7FF},
    new int[] {0xF900, 0xFDCF},
    new int[] {0xFDF0, 0xFFFD},
    new int[] {0x10000, 0xEFFFF}
  };


  DatabaseMetaData(URL endpoint, Properties clientParams) {
    this.endpoint = endpoint;
    this.clientParams = (Properties)clientParams.clone();
  }

  /** {@inheritDoc */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) return (T)this;
    throw new SQLException("scon does not implement: " + iface.getName());
  }

  /** {@inheritDoc */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isInstance(this);
  }

  /**
   * {@inheritDoc}
   * Don't expect SPARQL endpoints to have callable procedures.
   */
  @Override
  public boolean allProceduresAreCallable() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean allTablesAreSelectable() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getURL() throws SQLException {
    return endpoint.toString();
  }

  /**
   * {@inheritDoc}
   * This is done on a connection-by-connection basis.
   */
  @Override
  public String getUserName() throws SQLException {
    return clientParams.getProperty(USERNAME, "").toString();
  }

  /**
   * {@inheritDoc}
   * Read-only for the moment.
   */
  @Override
  public boolean isReadOnly() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean nullsAreSortedHigh() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean nullsAreSortedLow() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean nullsAreSortedAtStart() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean nullsAreSortedAtEnd() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDatabaseProductName() throws SQLException {
    return PRODUCT_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDatabaseProductVersion() throws SQLException {
    return VERSION;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDriverName() throws SQLException {
    return PRODUCT_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDriverVersion() throws SQLException {
    return VERSION;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getDriverMajorVersion() {
    return VERSION_MAJOR;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getDriverMinorVersion() {
    return VERSION_MINOR;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean usesLocalFiles() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean usesLocalFilePerTable() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsMixedCaseIdentifiers() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean storesUpperCaseIdentifiers() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean storesLowerCaseIdentifiers() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean storesMixedCaseIdentifiers() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getIdentifierQuoteString() throws SQLException {
    return " ";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSQLKeywords() throws SQLException {
    return KEYWORDS;
  }

  /**
   * {@inheritDoc}
   * TODO: track standard functions in SPARQL
   */
  @Override
  public String getNumericFunctions() throws SQLException {
    return "";
  }

  /**
   * {@inheritDoc}
   * TODO: track standard functions in SPARQL
   */
  @Override
  public String getStringFunctions() throws SQLException {
    return "";
  }

  /**
   * {@inheritDoc}
   * TODO: track standard functions in SPARQL
   */
  @Override
  public String getSystemFunctions() throws SQLException {
    return "";
  }

  /**
   * {@inheritDoc}
   * TODO: track standard functions in SPARQL
   */
  @Override
  public String getTimeDateFunctions() throws SQLException {
    return "";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSearchStringEscape() throws SQLException {
    return "\\";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getExtraNameCharacters() throws SQLException {
    if (extraVarnameChars == null) extraVarnameChars = createExtraVarnameChars();
    return extraVarnameChars;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsAlterTableWithAddColumn() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsAlterTableWithDropColumn() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   * TODO: This is sort of supported in SPARQL 1.1
   */
  @Override
  public boolean supportsColumnAliasing() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   * Treating NULL as UNBOUND
   */
  @Override
  public boolean nullPlusNonNullIsNull() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsConvert() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsConvert(int fromType, int toType) throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsTableCorrelationNames() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsDifferentTableCorrelationNames() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsExpressionsInOrderBy() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsOrderByUnrelated() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   * TODO: supported in SPARQL 1.1
   */
  @Override
  public boolean supportsGroupBy() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsGroupByUnrelated() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsGroupByBeyondSelect() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsLikeEscapeClause() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsMultipleResultSets() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsMultipleTransactions() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsNonNullableColumns() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsMinimumSQLGrammar() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsCoreSQLGrammar() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsExtendedSQLGrammar() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsANSI92EntryLevelSQL() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsANSI92IntermediateSQL() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsANSI92FullSQL() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsIntegrityEnhancementFacility() throws SQLException {
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsOuterJoins()
   */
  @Override
  public boolean supportsOuterJoins() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsFullOuterJoins()
   */
  @Override
  public boolean supportsFullOuterJoins() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsLimitedOuterJoins()
   */
  @Override
  public boolean supportsLimitedOuterJoins() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getSchemaTerm()
   */
  @Override
  public String getSchemaTerm() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getProcedureTerm()
   */
  @Override
  public String getProcedureTerm() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getCatalogTerm()
   */
  @Override
  public String getCatalogTerm() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#isCatalogAtStart()
   */
  @Override
  public boolean isCatalogAtStart() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getCatalogSeparator()
   */
  @Override
  public String getCatalogSeparator() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()
   */
  @Override
  public boolean supportsSchemasInDataManipulation() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()
   */
  @Override
  public boolean supportsSchemasInProcedureCalls() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()
   */
  @Override
  public boolean supportsSchemasInTableDefinitions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()
   */
  @Override
  public boolean supportsSchemasInIndexDefinitions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()
   */
  @Override
  public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()
   */
  @Override
  public boolean supportsCatalogsInDataManipulation() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()
   */
  @Override
  public boolean supportsCatalogsInProcedureCalls() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()
   */
  @Override
  public boolean supportsCatalogsInTableDefinitions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()
   */
  @Override
  public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()
   */
  @Override
  public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsPositionedDelete()
   */
  @Override
  public boolean supportsPositionedDelete() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsPositionedUpdate()
   */
  @Override
  public boolean supportsPositionedUpdate() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSelectForUpdate()
   */
  @Override
  public boolean supportsSelectForUpdate() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsStoredProcedures()
   */
  @Override
  public boolean supportsStoredProcedures() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()
   */
  @Override
  public boolean supportsSubqueriesInComparisons() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInExists()
   */
  @Override
  public boolean supportsSubqueriesInExists() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInIns()
   */
  @Override
  public boolean supportsSubqueriesInIns() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()
   */
  @Override
  public boolean supportsSubqueriesInQuantifieds() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()
   */
  @Override
  public boolean supportsCorrelatedSubqueries() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsUnion()
   */
  @Override
  public boolean supportsUnion() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsUnionAll()
   */
  @Override
  public boolean supportsUnionAll() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()
   */
  @Override
  public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()
   */
  @Override
  public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()
   */
  @Override
  public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()
   */
  @Override
  public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxBinaryLiteralLength()
   */
  @Override
  public int getMaxBinaryLiteralLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxCharLiteralLength()
   */
  @Override
  public int getMaxCharLiteralLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxColumnNameLength()
   */
  @Override
  public int getMaxColumnNameLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxColumnsInGroupBy()
   */
  @Override
  public int getMaxColumnsInGroupBy() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxColumnsInIndex()
   */
  @Override
  public int getMaxColumnsInIndex() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxColumnsInOrderBy()
   */
  @Override
  public int getMaxColumnsInOrderBy() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxColumnsInSelect()
   */
  @Override
  public int getMaxColumnsInSelect() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxColumnsInTable()
   */
  @Override
  public int getMaxColumnsInTable() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxConnections()
   */
  @Override
  public int getMaxConnections() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxCursorNameLength()
   */
  @Override
  public int getMaxCursorNameLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxIndexLength()
   */
  @Override
  public int getMaxIndexLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxSchemaNameLength()
   */
  @Override
  public int getMaxSchemaNameLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxProcedureNameLength()
   */
  @Override
  public int getMaxProcedureNameLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxCatalogNameLength()
   */
  @Override
  public int getMaxCatalogNameLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxRowSize()
   */
  @Override
  public int getMaxRowSize() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()
   */
  @Override
  public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxStatementLength()
   */
  @Override
  public int getMaxStatementLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxStatements()
   */
  @Override
  public int getMaxStatements() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxTableNameLength()
   */
  @Override
  public int getMaxTableNameLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxTablesInSelect()
   */
  @Override
  public int getMaxTablesInSelect() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getMaxUserNameLength()
   */
  @Override
  public int getMaxUserNameLength() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getDefaultTransactionIsolation()
   */
  @Override
  public int getDefaultTransactionIsolation() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsTransactions()
   */
  @Override
  public boolean supportsTransactions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsTransactionIsolationLevel(int)
   */
  @Override
  public boolean supportsTransactionIsolationLevel(int level)
      throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsDataDefinitionAndDataManipulationTransactions()
   */
  @Override
  public boolean supportsDataDefinitionAndDataManipulationTransactions()
      throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()
   */
  @Override
  public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()
   */
  @Override
  public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()
   */
  @Override
  public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getProcedures(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getProcedures(String catalog, String schemaPattern,
      String procedureNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getProcedureColumns(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getProcedureColumns(String catalog, String schemaPattern,
      String procedureNamePattern, String columnNamePattern)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getTables(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public ResultSet getTables(String catalog, String schemaPattern,
      String tableNamePattern, String[] types) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getSchemas()
   */
  @Override
  public ResultSet getSchemas() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getCatalogs()
   */
  @Override
  public ResultSet getCatalogs() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getTableTypes()
   */
  @Override
  public ResultSet getTableTypes() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getColumns(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getColumns(String catalog, String schemaPattern,
      String tableNamePattern, String columnNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getColumnPrivileges(String catalog, String schema,
      String table, String columnNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getTablePrivileges(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getTablePrivileges(String catalog, String schemaPattern,
      String tableNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String, java.lang.String, java.lang.String, int, boolean)
   */
  @Override
  public ResultSet getBestRowIdentifier(String catalog, String schema,
      String table, int scope, boolean nullable) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getVersionColumns(String catalog, String schema, String table)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getPrimaryKeys(String catalog, String schema, String table)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getImportedKeys(String catalog, String schema, String table)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getExportedKeys(String catalog, String schema, String table)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getCrossReference(String parentCatalog, String parentSchema,
      String parentTable, String foreignCatalog, String foreignSchema,
      String foreignTable) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getTypeInfo()
   */
  @Override
  public ResultSet getTypeInfo() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
   */
  @Override
  public ResultSet getIndexInfo(String catalog, String schema, String table,
      boolean unique, boolean approximate) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsResultSetType(int)
   */
  @Override
  public boolean supportsResultSetType(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
   */
  @Override
  public boolean supportsResultSetConcurrency(int type, int concurrency)
      throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)
   */
  @Override
  public boolean ownUpdatesAreVisible(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#ownDeletesAreVisible(int)
   */
  @Override
  public boolean ownDeletesAreVisible(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#ownInsertsAreVisible(int)
   */
  @Override
  public boolean ownInsertsAreVisible(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#othersUpdatesAreVisible(int)
   */
  @Override
  public boolean othersUpdatesAreVisible(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#othersDeletesAreVisible(int)
   */
  @Override
  public boolean othersDeletesAreVisible(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#othersInsertsAreVisible(int)
   */
  @Override
  public boolean othersInsertsAreVisible(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#updatesAreDetected(int)
   */
  @Override
  public boolean updatesAreDetected(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#deletesAreDetected(int)
   */
  @Override
  public boolean deletesAreDetected(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#insertsAreDetected(int)
   */
  @Override
  public boolean insertsAreDetected(int type) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsBatchUpdates()
   */
  @Override
  public boolean supportsBatchUpdates() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String, java.lang.String, int[])
   */
  @Override
  public ResultSet getUDTs(String catalog, String schemaPattern,
      String typeNamePattern, int[] types) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getConnection()
   */
  @Override
  public Connection getConnection() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsSavepoints()
   */
  @Override
  public boolean supportsSavepoints() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsNamedParameters()
   */
  @Override
  public boolean supportsNamedParameters() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsMultipleOpenResults()
   */
  @Override
  public boolean supportsMultipleOpenResults() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
   */
  @Override
  public boolean supportsGetGeneratedKeys() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getSuperTypes(String catalog, String schemaPattern,
      String typeNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getSuperTables(String catalog, String schemaPattern,
      String tableNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getAttributes(String catalog, String schemaPattern,
      String typeNamePattern, String attributeNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
   */
  @Override
  public boolean supportsResultSetHoldability(int holdability)
      throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getResultSetHoldability()
   */
  @Override
  public int getResultSetHoldability() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getDatabaseMajorVersion()
   */
  @Override
  public int getDatabaseMajorVersion() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getDatabaseMinorVersion()
   */
  @Override
  public int getDatabaseMinorVersion() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getJDBCMajorVersion()
   */
  @Override
  public int getJDBCMajorVersion() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getJDBCMinorVersion()
   */
  @Override
  public int getJDBCMinorVersion() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getSQLStateType()
   */
  @Override
  public int getSQLStateType() throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#locatorsUpdateCopy()
   */
  @Override
  public boolean locatorsUpdateCopy() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsStatementPooling()
   */
  @Override
  public boolean supportsStatementPooling() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getRowIdLifetime()
   */
  @Override
  public RowIdLifetime getRowIdLifetime() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getSchemas(java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getSchemas(String catalog, String schemaPattern)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#supportsStoredFunctionsUsingCallSyntax()
   */
  @Override
  public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#autoCommitFailureClosesAllResultSets()
   */
  @Override
  public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getClientInfoProperties()
   */
  @Override
  public ResultSet getClientInfoProperties() throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getFunctions(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getFunctions(String catalog, String schemaPattern,
      String functionNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.sql.DatabaseMetaData#getFunctionColumns(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getFunctionColumns(String catalog, String schemaPattern,
      String functionNamePattern, String columnNamePattern) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Creates a list of all the characters that SPARQL permits in its identifiers
   * beyond the standard [a-zA-Z0-9_]
   * @return A String containing all of the extra permitted SPARQL characters.
   */
  private static final String createExtraVarnameChars() {
    StringBuilder chars = new StringBuilder();
    for (int[] range: EXTRA_VARNAME_RANGES) {
      assert range.length == 2;
      assert range[0] <= range[1];
      for (int c = range[0]; c <= range[1]; c++) {
        if (Character.charCount(c) == 1) chars.append((char)c);
        else chars.append(new String(new int[] {c}, 0, 1));
      }
    }
    return chars.toString();
  }
}
