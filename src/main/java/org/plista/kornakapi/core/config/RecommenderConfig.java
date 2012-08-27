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

package org.plista.kornakapi.core.config;

public abstract class RecommenderConfig {

  private String name;

  private String retrainCronExpression;

  private int retrainAfterPreferenceChanges = DONT_RETRAIN_ON_PREFERENCE_CHANGES;

  public static final int DONT_RETRAIN_ON_PREFERENCE_CHANGES = 0;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRetrainCronExpression() {
    return retrainCronExpression;
  }

  public void setRetrainCronExpression(String retrainCronExpression) {
    this.retrainCronExpression = retrainCronExpression;
  }

  public int getRetrainAfterPreferenceChanges() {
    return retrainAfterPreferenceChanges;
  }

  public void setRetrainAfterPreferenceChanges(int retrainAfterPreferenceChanges) {
    this.retrainAfterPreferenceChanges = retrainAfterPreferenceChanges;
  }
}
