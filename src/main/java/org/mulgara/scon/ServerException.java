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
 * Indicates an error at the server when attempting an HTTP connection.
 */
public class ServerException extends SparqlException {

  private static final long serialVersionUID = -1660721309354155937L;

  public ServerException() { }

  public ServerException(String msg) { super(msg); }

  public ServerException(String msg, int code) { super(msg + " [" + code + "]"); }

  public ServerException(Throwable cause) { super(cause); }

  public ServerException(String msg, Throwable cause) { super(msg, cause); }

}
