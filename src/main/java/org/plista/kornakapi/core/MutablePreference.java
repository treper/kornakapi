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

package org.plista.kornakapi.core;

import org.apache.mahout.cf.taste.model.Preference;

/** implementation of {@link Preference}, the framework might reuse this instance */
public class MutablePreference implements Preference {

  private long userID;
  private long itemID;
  private float value;

  public void set(long userID, long itemID, float value) {
    this.userID = userID;
    this.itemID = itemID;
    setValue(value);
  }

  @Override
  public long getUserID() {
    return userID;
  }

  @Override
  public long getItemID() {
    return itemID;
  }

  @Override
  public float getValue() {
    return value;
  }

  @Override
  public void setValue(float value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "MutablePreference{" + "userID=" + userID + ", itemID=" + itemID + ", value=" + value + '}';
  }
}
