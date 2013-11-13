/**
 * Copyright (c) 2013, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.gertrude.space;

/**
 * An exception for validation errors that occur when attempting to build an
 * {@link com.cloudera.gertrude.ExperimentSpace}.
 */
public class ValidationException extends Exception {
  public ValidationException(String msg) {
    super(msg);
  }

  public ValidationException(String msg, Throwable cause) {
    super(msg, cause);
  }

  @Override
  public String toString() {
    if (getCause() == null) {
      return getLocalizedMessage();
    } else {
      return getLocalizedMessage() + " caused by: " + getCause().getLocalizedMessage();
    }
  }
}
