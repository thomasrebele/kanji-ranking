package kanjiranking.fontanalyzer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class FontAnalyzer {
	
	private float fontSize = 25;
	
	public FontAnalyzer(float fontSize) {
		this.fontSize = fontSize;
	}

	public int contourLength(BufferedImage img) {
		return 0;
	}
	
	static Font defaultFont = null;
	public Font getDefaultFont() {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
		return img.getGraphics().getFont();
	}
	
	public FontIdeogram fontIdeogram(Character c) {
		BufferedImage img0 = textToImage("" + c);
		FontIdeogram i0 = new FontIdeogram(c, img0);
		return i0;
	}
	
	public BufferedImage textToImage(String text) {
		//Font f = new Font("HanaMinA", Font.PLAIN, size);
		if(defaultFont == null) {
			defaultFont = getDefaultFont();
		}
		Font f = defaultFont.deriveFont(fontSize);
		FontRenderContext frc = new FontRenderContext(null, true, true);
		Rectangle2D r2d = f.getStringBounds(text, frc);
		//System.out.println(r2d.getWidth() + "x" + r2d.getHeight());

		int border = 1;
		int width = (int)Math.ceil(r2d.getWidth())+2*border;
		int height = (int)Math.ceil(r2d.getHeight())+2*border;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
		g2d.setColor(Color.BLACK);
		g2d.setFont(f);
		FontMetrics fm = g2d.getFontMetrics();
		g2d.drawString(text, border, fm.getAscent()+border);
		g2d.dispose();
		return img;
	}
}
