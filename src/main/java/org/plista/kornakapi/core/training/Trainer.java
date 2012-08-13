package org.plista.kornakapi.core.training;

import org.plista.kornakapi.core.storage.Storage;

import java.io.IOException;

public interface Trainer {

  void train(Storage storage) throws IOException;
}
