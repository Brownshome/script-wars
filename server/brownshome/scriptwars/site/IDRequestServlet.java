package brownshome.scriptwars.site;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import brownshome.scriptwars.server.Server;

@WebServlet("/requestID")
public class IDRequestServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String rawID = req.getParameter("id");
		if(rawID == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No game id supplied.");
			return;
		}
		
		int gameID;
		try {
			gameID = Integer.parseInt(rawID);
		} catch(NumberFormatException nfe) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, nfe.getMessage());
			return;
		}
		
		//TODO game is full?
		resp.getWriter().print(Server.getUserID(gameID));
		resp.getWriter().flush();
	}
}
