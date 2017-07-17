package brownshome.scriptwars.site.servlet;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import brownshome.scriptwars.game.Game;
import brownshome.scriptwars.game.GameCreationException;
import brownshome.scriptwars.game.GameType;

@WebServlet("/requestID")
public class IDRequestServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String rawGameType = req.getParameter("type");
		String rawProtocol = req.getParameter("protocol");
		String rawGameSlot = req.getParameter("slot");
		
		int id;
		
		if(rawGameType == null) {	
			if(rawGameSlot == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No slot or type supplied.");
				return;
			}
			
			Game game;
			
			try {
				if(rawProtocol == null)
					id = Game.getGame(Integer.parseInt(rawGameSlot)).getID();
				else
					id = Game.getGame(Integer.parseInt(rawGameSlot)).getID(Integer.parseInt(rawProtocol));
			} catch(ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException ex) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid slot ID");
				return;
			}
		} else {
			try {
				if(rawProtocol == null)
					id = GameType.getGameType(rawGameType).getUserID();
				else
					id = GameType.getGameType(rawGameType).getUserID(Integer.parseInt(rawProtocol));
			} catch (GameCreationException | ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create game: " + e.getMessage());
				return;
			}
		}
		
		resp.getWriter().print(id);
		resp.getWriter().flush();
	}
}
