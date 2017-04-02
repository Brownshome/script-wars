package brownshome.scriptwars.site;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import brownshome.scriptwars.server.game.GameCreationException;
import brownshome.scriptwars.server.game.GameType;

@WebServlet("/requestID")
public class IDRequestServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String rawGameType = req.getParameter("type");
		
		if(rawGameType == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No game id supplied.");
			return;
		}
		
		int id;
		
		try {
			id = GameType.getGameType(rawGameType).getUserID();
		} catch (GameCreationException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create game: " + e.getMessage());
			return;
		}
		
		resp.getWriter().print(id);
		resp.getWriter().flush();
	}
}
