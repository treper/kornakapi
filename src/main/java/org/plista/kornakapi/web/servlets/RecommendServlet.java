package org.plista.kornakapi.web.servlets;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.web.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class RecommendServlet extends BaseServlet {

  private static final Logger log = LoggerFactory.getLogger(RecommendServlet.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String method = getParameter(request, Parameters.METHOD, true);
    long userID = getParameterAsLong(request, Parameters.USER_ID, false);
    int howMany = getParameterAsInt(request, Parameters.HOW_MANY, Parameters.DEFAULT_HOW_MANY);

    Recommender recommender = getRecommender(method);

    try {
      List<RecommendedItem> recommendedItems = recommender.recommend(userID, howMany);

      PrintWriter writer = response.getWriter();

      response.setContentType("application/json");

      String separator = "";
      writer.write("[");
      for (RecommendedItem recommendedItem : recommendedItems) {
        writer.write(separator);
        writer.write("{itemID:");
        writer.write(String.valueOf(recommendedItem.getItemID()));
        writer.write(",value:");
        writer.write(String.valueOf(recommendedItem.getValue()));
        writer.write("}");
        separator = ",";
      }
      writer.write("]");

      if (log.isDebugEnabled()) {
        log.debug(recommendedItems.size() + " recommendations for user " + userID + " using method " + method);
      }

    } catch (TasteException e) {
      throw new ServletException(e);
    }

  }
}
