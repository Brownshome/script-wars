package brownshome.scriptwars.site.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import brownshome.scriptwars.server.game.*;

@WebServlet("/playertable/*")
public class PlayerTableServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		int gameCode = Integer.parseInt(request.getServletMapping().getMatchValue());
		Game game = Game.getGame(gameCode);
		
		request.setAttribute("game", game);
		getServletContext().getRequestDispatcher("/static/fragments/playerTable.jsp").forward(request, response);
	}
}
