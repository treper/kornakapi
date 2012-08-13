package org.plista.kornakapi.web.servlets;

import org.plista.kornakapi.core.storage.Storage;
import org.plista.kornakapi.core.training.Trainer;
import org.plista.kornakapi.web.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TrainServlet extends BaseServlet {

  private static final Logger log = LoggerFactory.getLogger(TrainServlet.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String method = getParameter(request, Parameters.METHOD, true);

    Storage storage = getStorage();

    Trainer trainer = getTrainer(method);

    trainer.train(storage);
  }
}
