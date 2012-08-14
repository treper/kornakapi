package org.plista.kornakapi.web.servlets;

import org.plista.kornakapi.core.training.Trainer;
import org.plista.kornakapi.web.Parameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TrainServlet extends BaseServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String method = getParameter(request, Parameters.METHOD, true);

    Trainer trainer = getTrainer(method);

    trainer.train(getModelDirectory(), getStorage(), getRecommender(method));
  }
}
