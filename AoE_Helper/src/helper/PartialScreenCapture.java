package helper;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;

import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import static marvin.MarvinPluginCollection.*;

/**
 * This program demonstrates how to capture screenshot of a portion of screen.
 * From:
 * https://www.codejava.net/java-se/graphics/how-to-capture-screenshot-programmatically-in-java
 * 
 * @author www.codejava.net
 *
 */
public class PartialScreenCapture {

	public static Rectangle popRectangle, villagersRectangle;
	private static int RESOURCE_RECTANGLES_INCREASE_AMOUNT;
	private Dimension screenSize;
	private Robot robot;
	private static int counter; // used for floodfill as pixel counter

	public PartialScreenCapture() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		popRectangle = new Rectangle(453, 22, 61, 16);
		//villagersRectangle = new Rectangle(433, 39, 11, 8);
		//villagersRectangle = new Rectangle(433, 38, 14, 10);
		//villagersRectangle = new Rectangle(437, 39, 8, 8);
		
		//villagersRectangle = new Rectangle(433, 39, 12, 8);
		
		//villagersRectangle = new Rectangle(424, 39, 20, 8); // exact 100
		villagersRectangle = new Rectangle(423, 39, 22, 8); // correctly recognized 100
		
		RESOURCE_RECTANGLES_INCREASE_AMOUNT = 4;
	}
	
	public BufferedImage captureVillagersImage(Rectangle captureRect) {
		return captureImage(villagersRectangle);
		
		/*BufferedImage capturedImage = robot.createScreenCapture(captureRect);
		
		// How to handle with text length-dependendend black background:
		// Check left-most vertical line of rectangle, if not entirely black we increase the rectangle,
		// repeat this process until we have the correct size.
		// (TODO: Make if more efficient, remember last state and make rectangle smaller/larger correspondingly)
		// (TODO: Only record enlarged part, thats maybe more efficient)
		Rectangle rectangleEnlarged = new Rectangle(captureRect.x, captureRect.y, captureRect.width, captureRect.height);
		
		while (!checkIfHalfBlackLine(capturedImage)) {
			// Make rectangle bigger
			rectangleEnlarged.x -= RESOURCE_RECTANGLES_INCREASE_AMOUNT;
			rectangleEnlarged.width += RESOURCE_RECTANGLES_INCREASE_AMOUNT;
			
			// Capture image again
			capturedImage = robot.createScreenCapture(rectangleEnlarged);
		}
		
		// Lastly, post process image
		MarvinImage image = new MarvinImage(capturedImage);
		image = imagePostProcessingVillagers(image);
		MarvinImageIO.saveImage(image, "CapturedImage.png");
		
		return image.getBufferedImageNoAlpha();*/
	}
	
	private boolean checkIfHalfBlackLine(BufferedImage image) {
		Color currentColor;
		int r = 0, g = 0, b = 0;
		int height = image.getHeight();
		int pixelsBlack = 0;
		
		for (int y = height/2; y < height; y++) {
			currentColor = new Color(image.getRGB(0, y));
			
			if (currentColor.equals(Color.black)) {
				pixelsBlack++;
			}
			//r += currentColor.getRed();
			//g += currentColor.getGreen();
			//b += currentColor.getBlue();
		}
		
		int threshold = 1;
		if (pixelsBlack <= threshold) {
			return true;
		}
		return false;
		
		/*int averageRed = r / height;
		int averageGreen = g / height;
		int averageBlue = b / height;
		
		System.out.println(averageRed);
		System.out.println(averageGreen);
		System.out.println(averageBlue);
		System.out.println();
		
		int threshold = 100;
		if (averageRed + averageGreen + averageBlue < threshold) {
			return true;
		}
		return false;*/
	}

	/**
	 * This is the method that should be used to capture images
	 */
	public BufferedImage captureImage(Rectangle captureRect) {
		BufferedImage capturedImage = robot.createScreenCapture(captureRect);
		MarvinImage image = new MarvinImage(capturedImage);
		image = imagePostProcessing(image, captureRect);
		MarvinImageIO.saveImage(image, "CapturedImage.png");
		//saveImage(capturedImage);
		//System.out.println("A partial screenshot captured!");
		
		return image.getBufferedImageNoAlpha();
	}
	
	private void saveImage(BufferedImage image) {
		String format = "jpg";
        String fileName = "CapturedImage." + format;
        
		try {
			ImageIO.write(image, format, new File(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static MarvinImage imagePostProcessing(MarvinImage image, Rectangle captureRect) {
		if (captureRect == popRectangle) {
			return imagePostProcessingPop(image);
		}
		else if (captureRect == villagersRectangle) {
			return imagePostProcessingVillagers(image);
		}
		return null;
	}

	/**
	 * This method applies some post processing methods to the image for better
	 * recognition later
	 */
	private static MarvinImage imagePostProcessingPop(MarvinImage image) {
		//image = Binarization.GetBmp(image);
		//image = grayScaleImage(image, 95); // making the image darker seems to help differentiating between 0 and 9
		//image = grayScaleImage(image, 50);
		
		//image = Binarization.GetBmp(image);
		//image = equalize(image);
		//image = scaleImage(image, 2.0f, AffineTransformOp.TYPE_BILINEAR);
		//MarvinImagePlugin pluginImage = MarvinPluginLoader.loadImagePlugin("org.marvinproject.image.color.grayScale.jar");
		
		brightnessAndContrast(image, 50, 100);
		thresholding(image, 40);
		image = scaleImage(image, 2.0f, 4.0f, AffineTransformOp.TYPE_BILINEAR);
		
		//scale(image.clone(), image, (int) (image.getWidth() * 2.0f), (int) (image.getHeight() * 4.0f));
		
		//sobel(image.clone(), image);
		//brightnessAndContrast(image, 0, 64);
		//blackAndWhite(image, 30);
		//thresholding(image, 30);
		//thresholding(image, 100);
		//invertColors(image);
		//scale(image.clone(), image, (int) (image.getWidth() * scaleFactor), (int) (image.getHeight() * scaleFactor));
		
		/*MarvinImage image2 = new MarvinImage(image);*/
		/*boundaryFill(image.clone(), image, 0, 0, Color.white, 150);
		image.setAlphaByColor(0, 0xFFFFFFFF);
		alphaBoundary(image, 5);*/
		//image = Binarization.GetBmp(image);

		return image;
	}
	
	private static MarvinImage imagePostProcessingVillagers(MarvinImage image) {
		//brightnessAndContrast(image, -100, 100);
		//thresholding(image, 40);
		//image = scaleImage(image, 4.0f, 8.0f, AffineTransformOp.TYPE_BILINEAR);
		//grayScale(image);
		//morphologicalBoundary(image.clone(), image);
		//int pixels = 10;
		//crop(image.clone(), image, -pixels, -pixels, image.getWidth() + pixels, image.getHeight() + pixels);
		
		//Check: 1,2,9,10,11,12,19,40,90,91,99,100,101,102
		// avoid to change 90 threshold in removeSmall regions before scaling, it's good to recognize 1
		
		image = borderImage(image, 3);
		image = removeColorfulPixels(image, 90f / 255, Color.black);
		//brightnessAndContrast(image, 50, 0);
		//image = removeSmallRegions(image, 2, 90, Color.black);
		//image = removeColorfulPixels(image, 100f / 255, Color.black);
		
		//image = removeColorfulPixels(image, 244f / 255, Color.black);
		//brightnessAndContrast(image, 240, 0);
		//brightnessAndContrast(image, 50, 100);
		//brightnessAndContrast(image, 50, 200);
		//thresholding(image, 200);
		//thresholding(image, 180);
		//thresholding(image, 50);
		//thresholding(image, 3);
		image = scaleImage(image, 4.0f, 4.0f, AffineTransformOp.TYPE_BILINEAR);
		//brightnessAndContrast(image, 240, 0);
		
		thresholding(image, 100); // makes sourrounding bigger/white and helps to make regions
		image = removeSmallRegions(image, 90, 150, Color.black);
		
		//image = removeSmallRegions(image, 90, 150, Color.black);
		
		//image = removeColorfulPixels(image, 90f / 255, Color.black);
		//image = removeSmallRegions(image, 80, 90, Color.black);
		//brightnessAndContrast(image, 200, 0);

		return image;
	}

	/**
	 * https://stackoverflow.com/questions/9131678/convert-a-rgb-image-to-grayscale-image-reducing-the-memory-in-java
	 * grayness is between 0 (white) and 100 (black)
	 */
	public static BufferedImage grayScaleImage(BufferedImage image, int grayness) {
		ImageFilter filter = new GrayFilter(true, grayness);
		ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
		return ImageToBufferedImage(Toolkit.getDefaultToolkit().createImage(producer));
	}

	/**
	 * https://stackoverflow.com/questions/665406/how-to-make-a-color-transparent-in-a-bufferedimage-and-save-as-png
	 */
	private static BufferedImage ImageToBufferedImage(Image image) {
		BufferedImage dest = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return dest;
	}
	
	private static MarvinImage scaleImage(MarvinImage before, float scaleFactorX, float scaleFactorY, int type) {
		BufferedImage bufferedImage = before.getBufferedImageNoAlpha();
		bufferedImage = scaleImage(bufferedImage, scaleFactorX, scaleFactorY, type);
		return new MarvinImage(bufferedImage);
	}

	/**
	 * From:
	 * https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
	 */
	private static BufferedImage scaleImage(BufferedImage before, float scaleFactorX, float scaleFactorY, int type) {
		int w = (int) (before.getWidth() * scaleFactorX);
		int h = (int) (before.getHeight() * scaleFactorY);
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		AffineTransform at = new AffineTransform();
		at.scale(scaleFactorX, scaleFactorY);
		AffineTransformOp scaleOp = new AffineTransformOp(at, type);
		return scaleOp.filter(before, after);
	}
	
	private static MarvinImage borderImage(MarvinImage before, int pixels) {
		BufferedImage bufferedImage = before.getBufferedImageNoAlpha();
		bufferedImage = borderImage(bufferedImage, pixels);
		return new MarvinImage(bufferedImage);
	}
	
	private static BufferedImage borderImage(BufferedImage before, int pixels) {
		int w_old = before.getWidth();
		int h_old = before.getHeight();
		int w = w_old + 2*pixels;
		int h = h_old + 2*pixels;
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		
		for (int y = 0; y < h_old; y++) {
			for (int x = 0; x < w_old; x++) {
				after.setRGB(x + pixels, y + pixels, before.getRGB(x, y));
			}
		}
		
		return after;
	}
	
	private static MarvinImage removeColorfulPixels(MarvinImage before, float threshold, Color color) {
		BufferedImage bufferedImage = before.getBufferedImageNoAlpha();
		bufferedImage = removeColorfulPixels(bufferedImage, threshold, color);
		return new MarvinImage(bufferedImage);
	}
	
	/**
	 * Use this method to remove colors where rgb are not the same
	 * (that means colors inequal to white, gray or black)
	 * 
	 * @param threshold should be between 0 and 1
	 * Making threshold near to 1 means that the pixels that have high hue will be removed,
	 * if threshold is near to 0 also pixels are removed that have not so high hue.
	 */
	private static BufferedImage removeColorfulPixels(BufferedImage image, float threshold, Color color) {
		Color currentColor;
		int removeColor = color.getRGB();
		int w = image.getWidth();
		int h = image.getHeight();
		int sum_rgb = 0;
		int r, g, b;
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				currentColor = new Color(image.getRGB(x, y));
				
				r = currentColor.getRed();
				g = currentColor.getGreen();
				b = currentColor.getBlue();
				sum_rgb = r + g + b;
				
				// If there is a (great) hue difference
				if (r >= sum_rgb*threshold || g >= sum_rgb*threshold || b >= sum_rgb*threshold) {
					// Change color
					image.setRGB(x, y, removeColor);
				}
			}
		}
		return image;
	}
	
	private static MarvinImage removeSmallRegions(MarvinImage before, int minimalPixels, int threshold, Color color) {
		BufferedImage bufferedImage = before.getBufferedImageNoAlpha();
		bufferedImage = removeSmallRegions(bufferedImage, minimalPixels, threshold, color);
		return new MarvinImage(bufferedImage);
		
		// todo: return: for each region: boundaryFill(color black)
	}
	
	private static BufferedImage removeSmallRegions(BufferedImage image, int minimalPixels, int threshold, Color color) {
		int w = image.getWidth();
		int h = image.getHeight();
		int removeColor = color.getRGB();
		
		int[][] regions = new int[w][h]; // stores "counter" values (counter indicates how much pixels nearby have the same color)
		// Initialize regions with -1
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				regions[x][y] = -1;
			}
		}
		
		// Apply floodfill on every (not visited) pixel
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (regions[x][y] == -1) {
					Color c = new Color(image.getRGB(x, y));
					//flood(regions, image, x, y, c, Color.black, 30);
					floodFillImage(image, x, y, Color.black, regions, threshold);
				}
			}
		}
		
		// Debug: Print regions
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// If connected with at least "minimalPixels" pixels remove color
				if (regions[x][y] <= minimalPixels) {
					image.setRGB(x, y, removeColor);
				}
				//System.out.print(regions[x][y] + "\t");
			}
			//System.out.println();
		}
		//System.out.println();
		
		return image;
	}
	
	/**
	 * Floodfill method using a queue, slightly changed to fit the task
	 * From: https://stackoverflow.com/questions/2783204/flood-fill-using-a-stack
	 * Threshold idea from: https://www.geeksforgeeks.org/java-applet-implementing-flood-fill-algorithm/
	 * 
	 * Threshold: How different a color should be to be painted (0 = color equal, 85 = total different color)
	 */
	public static void floodFillImage(BufferedImage image,int x, int y, Color color, int[][] array, int threshold) 
	{
	    int srcColor = image.getRGB(x, y);
	    boolean[][] hits = new boolean[image.getHeight()][image.getWidth()];

	    List<Point> pointsVisited = new ArrayList<>();
	    counter = 0;
	    
	    Queue<Point> queue = new LinkedList<Point>();
	    queue.add(new Point(x, y));

	    while (!queue.isEmpty()) 
	    {
	        Point p = queue.remove();

	        if(floodFillImageDo(image,hits,p.x,p.y, srcColor, color.getRGB(), array, pointsVisited, threshold))
	        {     
	            queue.add(new Point(p.x,p.y - 1)); 
	            queue.add(new Point(p.x,p.y + 1)); 
	            queue.add(new Point(p.x - 1,p.y)); 
	            queue.add(new Point(p.x + 1,p.y));
	            
	            queue.add(new Point(p.x - 1,p.y - 1));
	            queue.add(new Point(p.x - 1,p.y + 1));
	            queue.add(new Point(p.x + 1,p.y - 1));
	            queue.add(new Point(p.x + 1,p.y + 1));
	        }
	    }
	    
	    // Mark all visited points with counter value
        for (int i = 0; i < pointsVisited.size(); i++) {
        	Point pV = pointsVisited.get(i);
        	array[pV.x][pV.y] = counter;
		}
	}

	private static boolean floodFillImageDo(BufferedImage image, boolean[][] hits,int x, int y, int srcColor, int tgtColor,
			int[][] array, List<Point> pointsVisited, int threshold) 
	{
	    if (y < 0) return false;
	    if (x < 0) return false;
	    if (y > image.getHeight()-1) return false;
	    if (x > image.getWidth()-1) return false;
	    if (array[x][y] != -1) return false;

	    if (hits[y][x]) return false;
	    
	    // if there is no boundary (the color is almost
		// same as the color of the point where
		// floodfill is to be applied
	    Color currentColor = new Color(image.getRGB(x, y));
	    Color startColor = new Color(srcColor);
	    
		if (!(Math.abs(currentColor.getGreen() - startColor.getGreen()) < threshold
			&& Math.abs(currentColor.getRed() - startColor.getRed()) < threshold
			&& Math.abs(currentColor.getBlue() - startColor.getBlue()) < threshold)) {
			return false;
		}
	    
		// If pixel colors are not equal, stop
		//if (image.getRGB(x, y)!=srcColor)
	    //    return false;

	    // valid, paint it
	    //image.setRGB(x, y, tgtColor);
	    counter++;
	    pointsVisited.add(new Point(x, y));
	    hits[y][x] = true;
	    return true;
	}
}