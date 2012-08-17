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

package org.plista.kornakapi.web.servlets;

import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.storage.Storage;
import org.plista.kornakapi.core.training.Trainer;
import org.plista.kornakapi.web.Components;
import org.plista.kornakapi.web.InvalidParameterException;
import org.plista.kornakapi.web.MissingParameterException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

public abstract class BaseServlet extends HttpServlet {

  private Components getComponents() {
    return Components.instance();
  }

  protected File getModelDirectory() {
    return new File(getComponents().getConfiguration().getModelDirectory());
  }

  protected Recommender getRecommender(String name) {
    return getComponents().recommender(name);
  }

  protected Trainer getTrainer(String name) {
    return getComponents().trainer(name);
  }

  protected Storage getStorage() {
    return getComponents().storage();
  }

  protected boolean hasParameter(HttpServletRequest request, String name) {
    return request.getParameter(name) != null;
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
