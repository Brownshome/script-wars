package brownshome.scriptwars.site.servlet;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import brownshome.scriptwars.connection.*;
import brownshome.scriptwars.game.*;

@WebServlet("/playericon/*")
public class PlayerIconServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Player<?> player;
		String rawID = "Unset";
		
		try {
			rawID = URLUtil.extractMatch(request);
			int playerID = Integer.parseInt(rawID);
			player = Player.getPlayerFromID(playerID);
			
			if(player == null)
				throw new InvalidIDException();
		} catch(NumberFormatException | InvalidIDException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid player ID: " + rawID);
			return;
		}
		
		
		BufferedImage image;

		try {
			ServletContext context = getServletContext();
			String header = "/static/games/";
			image = player.getIcon(s -> new File(context.getRealPath(header + s)));
		} catch(IOException ioex) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ioex.getMessage());
			return;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", baos);
		response.setContentLength(baos.size());
		response.setContentType("image/png");
		baos.writeTo(response.getOutputStream());
	}
}
