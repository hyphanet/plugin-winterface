package freenet.winterface.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import freenet.client.FetchException;
import freenet.client.FetchResult;
import freenet.keys.FreenetURI;
import freenet.support.api.Bucket;
import freenet.support.io.BucketTools;
import freenet.winterface.core.Routes;
import freenet.winterface.core.ServerManager;
import freenet.winterface.freenet.FreenetInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Fetch USK page.
 */
public class Root extends HttpServlet {

	public Root() {
	}
	
	protected Routes getRoutes() {
		return (Routes) getServletContext().getAttribute(ServerManager.WINTERFACE_ROUTES);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("modal_key") != null) {
			response.sendRedirect("/" + request.getParameter("modal_key"));
		} else {
			doGet(request, response);
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String localPath = request.getPathInfo().substring(request.getServletPath().length() + 1);
		if(localPath.isEmpty()) {
			response.sendRedirect(getRoutes().getPathForDashboard());
		} else if (localPath.startsWith("USK@") || localPath.startsWith("KSK@") || localPath.startsWith("SSK@")) {
			FreenetInterface freenetInterface = (FreenetInterface) getServletContext().getAttribute(ServerManager.FREENET_INTERFACE);
			FetchResult result = null;
			try {
				result = freenetInterface.fetchURI(new FreenetURI(localPath));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FetchException e) {
				if (e.getMode() == FetchException.PERMANENT_REDIRECT) {
					String newURI = "/".concat(e.newURI.toString());
					response.sendRedirect(newURI);
				} else {
					e.printStackTrace();
				}
			}
			if (result != null) {
				response.setContentType(result.getMimeType());
		        response.setStatus(HttpServletResponse.SC_OK);
				OutputStream resOutStream = response.getOutputStream();

				Bucket resultBucket = result.asBucket();
				try {
					BucketTools.copyTo(resultBucket, resOutStream, Long.MAX_VALUE);
					resOutStream.flush();
					resOutStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				resultBucket.free();
			}
		} else {
			response.sendRedirect(getRoutes().getPathForErrorPage());
		}
		
	}

}