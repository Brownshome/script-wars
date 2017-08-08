package brownshome.scriptwars.site.servlet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import brownshome.scriptwars.game.GameType;

@WebServlet("/gameicons/*")
public class GameIconServlet extends HttpServlet {	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String gameName = URLUtil.extractMatch(request);
		GameType type = GameType.getGameType(gameName);
		
		if(type == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid game type: " + gameName);
			return;
		}
		
		String path = "/static/games/" + gameName + "/icon.png";
		if(!type.isBetaGame()) {
			getServletContext().getRequestDispatcher(path).forward(request, response);
		} else {
			BufferedImage result;
			
			try {
				result = readAndAnnotateImage(new File(getServletContext().getRealPath(path)), 
						new File(getServletContext().getRealPath("/static/beta.png"))); //TODO use path instead?
			} catch(IOException ioex) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No file found at " + path);
				return;
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(result, "PNG", baos);
			response.setContentLength(baos.size());
			response.setContentType("image/png");
			baos.writeTo(response.getOutputStream());
		}
	}

	private BufferedImage readAndAnnotateImage(File file, File beta) throws IOException {
		BufferedImage image = ImageIO.read(file);
		int height = image.getHeight();
		int width = image.getWidth();
		
		Graphics2D graphics = image.createGraphics();
		BufferedImage overlay = ImageIO.read(beta);
		graphics.drawImage(overlay, 0, 0, null);
		
		return image;
	}
}
