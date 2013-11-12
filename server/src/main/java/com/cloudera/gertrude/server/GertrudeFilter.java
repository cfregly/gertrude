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

import com.cloudera.gertrude.ExperimentHandler;
import com.cloudera.gertrude.Experiments;
import com.google.common.collect.ImmutableList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 *  A simple servlet {@link Filter} for intercepting the current request, creating an associated
 *  {@link HttpServletExperimentStateImpl}, and diverting the request into one or more experiments using
 *  a pre-configured {@link ExperimentHandler}.
 */
public class GertrudeFilter implements Filter {

  private static final String DIVERSION_COOKIES_PARAM = "gertrude-diversion-cookies";
  private static final String DIVERSION_COOKIES_SEP = ",";

  private ExperimentHandler handler;
  private List<String> diversionCookies;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String divCookiesStr = filterConfig.getInitParameter(DIVERSION_COOKIES_PARAM);
    if (divCookiesStr == null | divCookiesStr.isEmpty()) {
      diversionCookies = ImmutableList.of();
    } else {
      diversionCookies = ImmutableList.copyOf(divCookiesStr.split(DIVERSION_COOKIES_SEP));
    }
    filterConfig.getServletContext().log("Gertrude diversion cookies: " + diversionCookies);

    // NOTE: assumption here is that the configuration of the experiment handler (deserializer, loader,
    // and condition factory) are done before the server startup.
    this.handler = Experiments.getHandler();
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest,
      ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletExperimentStateImpl state = new HttpServletExperimentStateImpl(
        (HttpServletRequest) servletRequest, diversionCookies);
    handler.handle(state);
    servletRequest.setAttribute(GertrudeUtils.GERTRUDE_EXPERIMENT_STATE_KEY, state);
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
  }
}
