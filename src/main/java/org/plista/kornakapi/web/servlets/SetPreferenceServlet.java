package org.plista.kornakapi.web.servlets;

import org.plista.kornakapi.web.Parameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SetPreferenceServlet extends BaseServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    long userID = getParameterAsLong(request, Parameters.USER_ID, true);
    long itemID = getParameterAsLong(request, Parameters.ITEM_ID, true);
    float value = getParameterAsFloat(request, Parameters.VALUE, true);

    getStorage().setPreference(userID, itemID, value);
  }
}
