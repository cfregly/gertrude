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

import com.google.common.base.Preconditions;

/**
 * A rule for diverting an {@link ExperimentState} into a {@link Segment} defined within the
 * set of {@link Layer} instances in the current {@link ExperimentSpace}.
 *
 * <p>For a web application, there may be multiple diversion criteria available, such as
 * a user's login ID, a cookie stored in a browser, or some other attribute of the request,
 * such as a search query. The ids of the diversion critera imply a priority ordering of the
 * criteria (from lowest to highest), so that the diversion rules for a request that contain
 * multiple identifiers (such as both a login ID and a browser cookie) are unambiguous.
 *
 * <p>It is also possible to define a random diversion criterion that is independent of the
 * request. If one is defined, it should be the lowest priority criteria.
 *
 * <p>Each criterion (either fixed or random) must have an associated number of buckets available.
 * {@code Segment} instances claim buckets for a diversion criterion, and {@code ExperimentState}
 * instances are assigned to {@code Segment} instances based on how their identifiers are mapped into
 * buckets.
 *
 * <p>See the comments on the {@link ExperimentState#getDiversionIdentifier(int)} method for more
 * information about working with {@code DiversionCriterion}.
 */
public final class DiversionCriterion implements Comparable<DiversionCriterion> {
  private final int id;
  private final int numBuckets;
  private final boolean random;

  public DiversionCriterion(int id, int numBuckets, boolean random) {
    Preconditions.checkArgument(numBuckets > 0, String.format("Non-positive bucket count for diversion id %d: %d",
        id, numBuckets));
    this.id = id;
    this.numBuckets = numBuckets;
    this.random = random;
  }

  /**
   * Returns the unique id of this diversion criteria that is referenced in
   * the definition of a {@code Segment}.
   *
   * @return the id of this diversion criteria
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the number of buckets for this criteria that may be allocated to {@code Segment} instances.
   * @return the number of buckets for this criteria
   */
  public int getNumBuckets() {
    return numBuckets;
  }

  /**
   * Returns true if this is a random criteria that is independent of any information about
   * the request contained in the {@code ExperimentState}.
   *
   * @return true for request-independent diversion
   */
  public boolean isRandom() {
    return random;
  }

  @Override
  public int compareTo(DiversionCriterion other) {
    return id - other.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DiversionCriterion that = (DiversionCriterion) o;

    if (id != that.id) return false;
    if (numBuckets != that.numBuckets) return false;
    if (random != that.random) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + numBuckets;
    result = 31 * result + (random ? 1 : 0);
    return result;
  }
}
