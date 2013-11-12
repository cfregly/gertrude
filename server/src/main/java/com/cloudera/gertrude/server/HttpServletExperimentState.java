package com.cloudera.gertrude.server;

import com.cloudera.gertrude.ExperimentState;

import javax.servlet.http.HttpServletRequest;

/**
 * An extension to the {@link ExperimentState} interface that can be implemented by any concrete subclass of
 * {@link com.cloudera.gertrude.AbstractExperimentState} that can return a {@link HttpServletRequest} object.
 *
 * <p>Separating the concrete implementation in {@link HttpServletExperimentStateImpl} from the
 * {@code HttpServletExperimentState} interface allows clients to re-use any {@link com.cloudera.gertrude.Condition}
 * instances that only need to know about the {@code HttpServletRequest}, not the details of how experiments are
 * diverted for that state.
 */
public interface HttpServletExperimentState extends ExperimentState {

  /**
   * Returns the {@code request} associated with this instance.
   */
  HttpServletRequest getRequest();
}
