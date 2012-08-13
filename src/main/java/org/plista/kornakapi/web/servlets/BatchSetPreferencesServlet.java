package org.plista.kornakapi.web.servlets;

import com.google.common.io.Closeables;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.mahout.cf.taste.model.Preference;
import org.plista.kornakapi.core.io.FilePreferenceIterator;
import org.plista.kornakapi.core.storage.Storage;
import org.plista.kornakapi.web.Parameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BatchSetPreferencesServlet extends BaseServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    int batchSize = getParameterAsInt(request, Parameters.BATCH_SIZE, Parameters.DEFAULT_BATCH_SIZE);

    ServletFileUpload upload = new ServletFileUpload();

    FileItemIterator fileItems;
    InputStream in = null;

    boolean fileProcessed = false;

    Storage storage = getStorage();

    try {
      fileItems = upload.getItemIterator(request);
      while (fileItems.hasNext()) {

        FileItemStream item = fileItems.next();

        if (Parameters.FILE.equals(item.getFieldName()) && !item.isFormField()) {

          in = item.openStream();
          Iterator<Preference> preferences = new FilePreferenceIterator(in);

          storage.batchSetPreferences(preferences, batchSize);

          fileProcessed = true;

          break;
        }
      }
    } catch (FileUploadException e) {
      throw new IOException(e);
    } finally {
      Closeables.closeQuietly(in);
    }

    if (!fileProcessed) {
      throw new IllegalStateException("Unable to find supplied data file!");
    }
  }
}
