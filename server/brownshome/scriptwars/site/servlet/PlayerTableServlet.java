package brownshome.scriptwars.site.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import brownshome.scriptwars.game.Game;

@WebServlet("/playertable/*")
public class PlayerTableServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		int gameCode = Integer.parseInt(URLUtil.extractMatch(request));
		Game game = Game.getGame(gameCode);
		
		request.setAttribute("game", game);
		getServletContext().getRequestDispatcher("/static/fragments/playerTable.jsp").forward(request, response);
	}
}
