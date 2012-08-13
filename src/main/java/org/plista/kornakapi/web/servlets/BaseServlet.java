package org.plista.kornakapi.web.servlets;

import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.storage.Storage;
import org.plista.kornakapi.core.training.Trainer;
import org.plista.kornakapi.web.Components;
import org.plista.kornakapi.web.InvalidParameterException;
import org.plista.kornakapi.web.MissingParameterException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class BaseServlet extends HttpServlet {

  protected Recommender getRecommender(String name) {
    return Components.instance().recommender(name);
  }

  protected Trainer getTrainer(String name) {
    return Components.instance().trainer(name);
  }

  protected Storage getStorage() {
    return Components.instance().storage();
  }

  protected String getParameter(HttpServletRequest request, String name, boolean required) {
    String param = request.getParameter(name);

    if (param == null && required) {
      throw new MissingParameterException("Parameter [" + name + "] must be supplied!");
    }

    return param;
  }

  protected long getParameterAsLong(HttpServletRequest request, String name, boolean required) {
    String param = getParameter(request, name, required);

    try {
      return Long.parseLong(param);
    } catch (NumberFormatException e) {
      throw new InvalidParameterException("Unable to parse parameter [" + name + "]", e);
    }
  }

  protected float getParameterAsFloat(HttpServletRequest request, String name, boolean required) {
    String param = getParameter(request, name, required);

    try {
      return Float.parseFloat(param);
    } catch (NumberFormatException e) {
      throw new InvalidParameterException("Unable to parse parameter [" + name + "]", e);
    }
  }

  protected int getParameterAsInt(HttpServletRequest request, String name, int defaultValue) {
    String param = getParameter(request, name, false);

    try {
      return param != null ? Integer.parseInt(param) : defaultValue;
    } catch (NumberFormatException e) {
      throw new InvalidParameterException("Unable to parse parameter [" + name + "]", e);
    }
  }
}
