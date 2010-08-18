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
 * Indicates a condition described by the HTTP response that we don't know how to handle.
 */
public class UnhandledException extends SparqlException {

  private static final long serialVersionUID = 7024395693086586575L;

  public UnhandledException() { }

  public UnhandledException(String msg) { super(msg); }

  public UnhandledException(String msg, int code) { super(msg + " [" + code + "]"); }

  public UnhandledException(Throwable cause) { super(cause); }

  public UnhandledException(String msg, Throwable cause) { super(msg, cause); }

}
