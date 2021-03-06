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
package com.cloudera.gertrude.server;

import com.cloudera.gertrude.ExperimentFlag;
import com.cloudera.gertrude.Experiments;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An example of using experiment flags to control response behavior in a servlet.
 */
public class ExampleServlet extends HttpServlet {

  private static ExperimentFlag<Long> MAX_RESPONSE_LENGTH = Experiments.declare("max_response_length", -1L);
  private static ExperimentFlag<Boolean> SKIP_RESPONSE = Experiments.declare("skip_response", false);
  private static ExperimentFlag<String> RESPONSE_TEXT = Experiments.declare("response_text", "Hello World!");

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (GertrudeUtils.getFlagValue(request, SKIP_RESPONSE)) {
      String responseText = GertrudeUtils.getFlagValue(request, RESPONSE_TEXT);
      int maxLength = GertrudeUtils.getFlagValue(request, MAX_RESPONSE_LENGTH).intValue();
      if (maxLength > 0) {
        responseText = responseText.substring(0, maxLength);
      }
      response.setContentType("text/plain");
      response.getWriter().println(responseText);
    }
  }
}
