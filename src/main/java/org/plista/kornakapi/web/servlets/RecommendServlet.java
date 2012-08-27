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

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.plista.kornakapi.core.recommender.FixedCandidatesIDRescorer;
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

    String recommenderName = getParameter(request, Parameters.RECOMMENDER, true);
    long userID = getParameterAsLong(request, Parameters.USER_ID, false);
    int howMany = getParameterAsInt(request, Parameters.HOW_MANY, Parameters.DEFAULT_HOW_MANY);

    IDRescorer rescorer = null;

    if (hasParameter(request, Parameters.LABEL)) {
      String label = getParameter(request, Parameters.LABEL, false);
      FastIDSet candidates = storage().getCandidates(label);

      if (!candidates.isEmpty()) {
        rescorer = new FixedCandidatesIDRescorer(candidates);
      }
    }

    Recommender recommender = recommender(recommenderName);

    try {
      List<RecommendedItem> recommendedItems = recommender.recommend(userID, howMany, rescorer);

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
        log.debug("{} recommendations for user {} using recommender {}",
            new Object[] { recommendedItems.size(), userID, recommenderName });
      }

    } catch (TasteException e) {
      throw new ServletException(e);
    }

  }
}
