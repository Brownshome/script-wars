package brownshome.scriptwars.site.servlet;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import brownshome.scriptwars.server.game.*;

@WebServlet("/requestID")
public class IDRequestServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String rawGameType = req.getParameter("type");
		int id;
		
		if(rawGameType == null) {
			String rawGameSlot = req.getParameter("slot");
			
			if(rawGameSlot == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No slot or type supplied.");
				return;
			}
			
			int slot = Integer.parseInt(rawGameSlot);
			Game<?> game;
			
			try {
				id = Game.getGame(slot).getDefaultConnectionHandler().getID();
			} catch(ArrayIndexOutOfBoundsException | NullPointerException ex) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid slot ID");
				return;
			}
		} else {
			try {
				id = GameType.getGameType(rawGameType).getUserID();
			} catch (GameCreationException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create game: " + e.getMessage());
				return;
			}
		}
		
		resp.getWriter().print(id);
		resp.getWriter().flush();
	}
}
