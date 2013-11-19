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
package com.cloudera.gertrude;

import com.google.common.base.Optional;

import java.util.Set;

/**
 * Container for all of the information about how to divert a request into experiments
 * and what the values of experiment flags are for the current request.
 *
 * <p>Most of the logic for managing experiment ids and flag value calculations is defined in the
 * {@link AbstractExperimentState} class, which clients should extend in order to provide a
 * definition of the {@link #getDiversionIdentifier(int)} method that is appropriate for their
 * application.
 *
 * <p>{@code ExperimentState} is an interface so that clients may create re-usable families of
 * {@link Condition} functions that expect a specific sub-interface of ExperimentState and are
 * aware of how to process them. For example, there could be a {@code HttpServletRequestExperimentState}
 * sub-interface that provided a {@code getServletRequest()} method, and an associated set of
 * {@code Condition} functions that could process the {@code HttpServletRequest} and make decisions
 * based on its values. Then any client could access and use those {@code Condition} functions simply
 * by extending the {@code AbstractExperimentState} and implementing the {@code HttpServletRequestExperimentState}
 * interface, without having to re-write their own {@code Condition} implementations for every application.
 */
public interface ExperimentState {

  /**
   * Return the identifier to use for this instance with the diversion criterion that
   * has the given identifer.
   *
   * <p>Clients must override this method in order to specify the identifiers that they
   * support for a given request, and what the values of those identifiers are for each
   * request.
   *
   * <p>Remember that these identifiers are used for randomly diverting requests into
   * experiments, so they should be unique for each entity that we are experimenting on.
   * For example, using the country that a request comes from is a relatively poor choice
   * of a diversion identifier compared to a random account ID or browser cookie.
   *
   * @param diversionId The unique id of the requested diversion criterion
   * @return an identifier to use for diverting this request into an experiment or domain
   */
  Optional<String> getDiversionIdentifier(int diversionId);

  /**
   * Indicate a set of experiment IDs that this {@code ExperimentState} should be forced
   * into by the {@link ExperimentHandler#handle(AbstractExperimentState)} method.
   *
   * <p>By default, this method returns an empty set. Subclasses may override this
   * method to provide clients with a way to force a request to be in a certain experiment
   * in order to test or debug a scenario.
   *
   * @return the ids of the experiments that this request should be in
   */
  Set<Integer> forceExperimentIds();

  /**
   * Returns the value of the given {@code ExperimentFlag} for this {@code ExperimentState}.
   *
   * <p>If the {@link FlagValue} that was calculated for this flag can be cached on a per-request
   * basis, then this instance will keep the calculated value in a cache so that it is not
   * re-computed unnecessarily during the same request.
   *
   * <p>If this state has not yet been diverted by the {@code ExperimentHandler}, then the
   * default value of the flag is returned, but not stored in the cache.
   *
   * @param flag the flag whose value is returned
   * @return the value of the flag for this instance
   */
  <T> T get(ExperimentFlag<T> flag);

  /**
   * A convenience method for acccessing the value of a {@code ExperimentFlag<Long>} as an {@code int}.
   *
   * @param longFlag the flag
   * @return the integer point value of the flag
   */
  int getInt(ExperimentFlag<Long> longFlag);

  /**
   * A convenience method for acccessing the value of a {@code ExperimentFlag<Double>} as a {@code float}.
   *
   * @param doubleFlag the flag
   * @return the floating point value of the flag
   */
  float getFloat(ExperimentFlag<Double> doubleFlag);

  /**
   * Returns the integer identifiers for the experiments that this state was diverted into.
   *
   * @return the integer identifiers for the experiments that this state was diverted into
   */
  Set<Integer> getExperimentIds();

  /**
   * Indicates whether or not this {@code ExperimentState} has been passed to {@link ExperimentHandler#handle}.
   *
   * @return true if this instance has already been diverted, false otherwise
   */
  boolean isDiverted();

  /**
   * Returns the time that this {@code ExperimentState} instance was created in milliseconds. Used for
   * determining whether a particular experiment should be active or inactive for this request.
   *
   * @return the time that this object was created in milliseconds
   */
  long getRequestTimeMsec();
}
