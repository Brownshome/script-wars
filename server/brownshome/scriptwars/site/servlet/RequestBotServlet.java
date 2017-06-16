package brownshome.scriptwars.site.servlet;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import brownshome.scriptwars.connection.MemoryConnectionHandler;
import brownshome.scriptwars.game.*;

@WebServlet("/requestBot")
public class RequestBotServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String rawGameSlot = req.getParameter("slot");
		String gameName = req.getParameter("name");

		try {
			int slot = Integer.parseInt(rawGameSlot);
			MemoryConnectionHandler.runAI(gameName, Game.getGame(slot));
		} catch(NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid slot ID");
		}
	}
}
