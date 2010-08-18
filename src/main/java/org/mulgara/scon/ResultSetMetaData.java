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


/**
 * Metadata for a result set.
 */
public interface ResultSetMetaData extends java.sql.ResultSetMetaData {

  /**
   * Get the number of columns in the result set that this metadata represents.
   * @return the number of columns.
   */
  int getColumnCount() throws SparqlException;

  /**
   * Gets the name of the column from the result set that this metadata represents.
   * @param column the 1-based index of the column.
   * @return the string name of the column (the selection variable).
   */
  String getColumnName(int column) throws SparqlException;

}
