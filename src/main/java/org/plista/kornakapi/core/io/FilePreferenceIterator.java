package org.plista.kornakapi.core.io;

import com.google.common.base.Charsets;
import com.google.common.collect.UnmodifiableIterator;
import org.plista.kornakapi.core.ReusablePreference;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.common.iterator.FileLineIterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;


public class FilePreferenceIterator extends UnmodifiableIterator<Preference> {

  private final FileLineIterator lineIterator;
  private final ReusablePreference preference;

  private static final Pattern SEPARATOR = Pattern.compile("[\t,]");

  public FilePreferenceIterator(InputStream in) throws IOException {
    lineIterator = new FileLineIterator(in, Charsets.UTF_8, false);
    preference = new ReusablePreference();
  }

  @Override
  public boolean hasNext() {
    return lineIterator.hasNext();
  }

  @Override
  public Preference next() {
    String[] parts = SEPARATOR.split(lineIterator.next());
    preference.set(Long.parseLong(parts[0]), Long.parseLong(parts[1]), Float.parseFloat(parts[2]));
    return preference;
  }

}
