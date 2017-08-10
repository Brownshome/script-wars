package brownshome.scriptwars.site.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import brownshome.scriptwars.game.GameType;

@WebServlet("/gameinfo/*")
public class GameInfoServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String gameName = URLUtil.extractMatch(request);
		GameType type = GameType.getGameType(gameName);

		if(type == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid game type: " + gameName);
			return;
		}
		
		request.setAttribute("gameType", type);
		getServletContext().getRequestDispatcher("/static/fragments/gameInfo.jsp").forward(request, response);
	}
}
