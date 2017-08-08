package brownshome.scriptwars.site.servlet;

import javax.servlet.http.HttpServletRequest;

public class URLUtil {
	public static String extractMatch(HttpServletRequest request) {
		String URL = request.getRequestURL().toString();
		return URL.substring(URL.lastIndexOf('/') + 1);
	}
}
