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

package org.plista.kornakapi.core.io;

import com.google.common.base.Charsets;
import com.google.common.collect.UnmodifiableIterator;
import org.plista.kornakapi.core.MutablePreference;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.common.iterator.FileLineIterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/** an {@link java.util.Iterator} over preferences stored in a text file */
public class CSVPreferenceFileIterator extends UnmodifiableIterator<Preference> {

  private final FileLineIterator lineIterator;
  private final MutablePreference mutablePreference;

  private static final Pattern SEPARATOR = Pattern.compile("[\t,]");

  public CSVPreferenceFileIterator(InputStream in) throws IOException {
    lineIterator = new FileLineIterator(in, Charsets.UTF_8, false);
    mutablePreference = new MutablePreference();
  }

  @Override
  public boolean hasNext() {
    return lineIterator.hasNext();
  }

  @Override
  public Preference next() {
    String[] parts = SEPARATOR.split(lineIterator.next());
    mutablePreference.set(Long.parseLong(parts[0]), Long.parseLong(parts[1]), Float.parseFloat(parts[2]));
    return mutablePreference;
  }

}
