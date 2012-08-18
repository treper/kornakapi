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
import org.apache.mahout.common.iterator.FileLineIterator;
import org.plista.kornakapi.core.Candidate;
import org.plista.kornakapi.core.MutableCandidate;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class CSVCandidateFileIterator extends UnmodifiableIterator<Candidate> {

  private final FileLineIterator lineIterator;
  private final MutableCandidate mutableCandidate;

  private static final Pattern SEPARATOR = Pattern.compile("[\t,]");

  public CSVCandidateFileIterator(InputStream in) throws IOException {
    lineIterator = new FileLineIterator(in, Charsets.UTF_8, false);
    mutableCandidate = new MutableCandidate();
  }

  @Override
  public boolean hasNext() {
    return lineIterator.hasNext();
  }

  @Override
  public Candidate next() {
    String[] parts = SEPARATOR.split(lineIterator.next());
    mutableCandidate.set(parts[0], Long.parseLong(parts[1]));
    return mutableCandidate;
  }
}
