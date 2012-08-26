/**
 * Copyright 2012 plista GmbH  (http://www.plista.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 */

package org.plista.kornakapi.core.recommender;

import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.recommender.IDRescorer;

public class FixedCandidatesIDRescorer implements IDRescorer {

  private final FastIDSet candidates;

  public FixedCandidatesIDRescorer(FastIDSet candidates) {
    this.candidates = candidates;
  }

  @Override
  public double rescore(long id, double originalScore) {
    return originalScore;
  }

  @Override
  public boolean isFiltered(long id) {
    return !candidates.contains(id);
  }
}
