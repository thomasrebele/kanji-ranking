package kanjiranking.fontanalyzer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FontIdeogram {
	
	public class Image {
		public Image(BufferedImage img) {
			this.img = img;
			if(img != null) {
				pixels = (int[])img.getRaster().getDataElements(0, 0, img.getWidth(), img.getHeight(), null);
				width = img.getWidth();
				height = img.getHeight();
			}
			else {
				width = 0;
				height = 0;
				pixels = null;
			}
		}
		BufferedImage img;
		final int[] pixels;
		final int width;
		final int height;
	}
	
	public FontIdeogram(Character c, BufferedImage img) {
		this.c = c;
		this.img = new Image(img);
	}
	
	public char c;
	public Image img;
	
	
	/** 
	 * 0: pixel count
	 * 1: border left
	 * 2: border right
	 * 3: border top
	 * 4: border bottom
	 * 5: border total
	 */
	public int[] stat;
	
	public void calculateStats() {
		stat = new int[6];
		for(int y=1; y<img.height-1; y++) {
			for(int x=1; x<img.width-1; x++) {
				stat[0] += isSet(x,y) ? 1 : 0;
				stat[1] += !isSet(x-1,y) && isSet(x,y) ? 1 : 0;
				stat[2] += isSet(x-1,y) && !isSet(x,y) ? 1 : 0;
				stat[3] += !isSet(x,y-1) && isSet(x,y) ? 1 : 0;
				stat[4] += isSet(x,y-1) && !isSet(x,y) ? 1 : 0;
			}
		}
		stat[5] = stat[1] + stat[2] + stat[3] + stat[4];
	}
	
	public boolean isSet(int x, int y) {
		return img.pixels[y*img.width+x] != -1;
	}
	
	public void print() {
		for(int y=0; y<img.height; y++) {
			for(int x=0; x<img.width; x++) {
				if(img.pixels[y*img.width+x] == -1) {
					System.out.print(" ");
				}
				else {
					System.out.print("x");
				}
				
			}
			System.out.println();
		}
	}
	
	public void saveToFile(String path) {
		try {
			ImageIO.write(img.img, "png", new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}