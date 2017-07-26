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
		String botName = req.getParameter("name");

		try {
			int slot = Integer.parseInt(rawGameSlot);
			Game.getGame(slot).startServerBot(botName);
		} catch(NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid slot ID");
		} catch(OutOfIDsException ooide) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "That game is full");
		} catch (UnknownServerBotException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}
}
