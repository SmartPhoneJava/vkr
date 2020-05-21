

import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bitmap
{
	/** The width, in pixels, of the image */
	private final int  width_;
	/** The height, in pixels, of the image */
	private final int  height_;
	/** Every pixel component in the image */
	private final byte components_[];

	/** Basic getter */
	public int GetWidth() { return width_; }
	/** Basic getter */
	public int GetHeight() { return height_; }

	public byte GetComponent(int index) { if (index > 0 && index < components_.length)return components_[index];return components_[0]; }

	/**
	 * Creates and initializes a Bitmap.
	 *
	 * @param width The width, in pixels, of the image.
	 * @param height The height, in pixels, of the image.
	 */
	public Bitmap(int width, int height)
	{
		width_      = width;
		height_     = height;
		components_ = new byte[width_ * height_ * 4];
	}

	public Bitmap(String fileName) throws IOException
	{
		int width = 0;
		int height = 0;
		byte[] components = null;

		BufferedImage image = ImageIO.read(new File(fileName));

		width = image.getWidth();
		height = image.getHeight();

		int imgPixels[] = new int[width * height];
		image.getRGB(0, 0, width, height, imgPixels, 0, width);
		components = new byte[width * height * 4];

		for(int i = 0; i < width * height; i++)
		{
			int pixel = imgPixels[i];

			components[i * 4]     = (byte)((pixel >> 24) & 0xFF); // A
			components[i * 4 + 1] = (byte)((pixel      ) & 0xFF); // B
			components[i * 4 + 2] = (byte)((pixel >> 8 ) & 0xFF); // G
			components[i * 4 + 3] = (byte)((pixel >> 16) & 0xFF); // R
		}

		width_ = width;
		height_ = height;
		components_ = components;
	}

	/**
	 * Sets every pixel in the bitmap to a specific shade of grey.
	 *
	 * @param shade The shade of grey to use. 0 is black, 255 is white.
	 */
	public void Clear(byte shade)
	{
		Arrays.fill(components_, shade);
	}

	/**
	 * Sets the pixel at (x, y) to the color specified by (a,b,g,r).
	 *
	 * @param x Pixel location in X
	 * @param y Pixel location in Y
	 * @param a Alpha component
	 * @param b Blue component
	 * @param g Green component
	 * @param r Red component
	 */
	public void DrawPixel(int x, int y, byte a, byte b, byte g, byte r)
	{
		int index = (x + y * width_) * 4;
		components_[index    ] = a;
		components_[index + 1] = b;
		components_[index + 2] = g;
		components_[index + 3] = r;
	}

	public void CopyPixel(int destX, int destY, int srcX,
			int srcY, Bitmap src, float lightAmt, int offset, float dark, int lighter)
	{
		int destIndex = (destX + destY * width_) * 4;
		int srcIndex = (srcX + srcY * src.GetWidth()) * 4;
		
		//lightAmt *= (float)Math.sin(0.01 * destX);
		//dark = 0.8f;
		float depth_light =  Math.abs((float)Math.sin(0.01f * ((float)offset)));
		float light = lightAmt * (dark + depth_light);
		if (light > 0.98f) {
			light = 0.98f;
		}
		
		int blue = src.GetComponent(srcIndex + 1) + 120 - lighter ;
		int green = src.GetComponent(srcIndex + 2) + 120 - lighter;
		int red = src.GetComponent(srcIndex + 3) + 120 - lighter;
		
		/*
		if (blue < 0) {
			blue = 255;
		}
		if (green < 0) {
			green = 255;
		}
		if (red < 0) {
			red = 255;
		}
		*/
		if (blue > 255) {
			blue = 255;
		}
		if (green > 255) {
			green = 255;
		}
		if (red > 255) {
			red = 255;
		}
		
		//if ((blue < 0)||(red < 0)||(green < 0)||(green > 255)||(red > 255)||(blue > 255)) {
		//	System.out.println("color:" + red + "," + green + "," + blue + "; add="+(120 - lighter));
		//}
		
		components_[destIndex    ] = (byte)((src.GetComponent(srcIndex) & 0xFF) * light);
		components_[destIndex + 1] = (byte)(((byte)(blue) & 0xFF) * light);
		components_[destIndex + 2] = (byte)(((byte)(green) & 0xFF) * light);
		components_[destIndex + 3] = (byte)(((byte)(red) & 0xFF) * light);
		
		//System.out.println("something:" + components_[destIndex    ] + "," +
		//		components_[destIndex + 1] + "," + components_[destIndex + 2]);
	}

	/**
	 * Copies the Bitmap into a BGR byte array.
	 *
	 * @param dest The byte array to copy into.
	 */
	public void CopyToByteArray(byte[] dest)
	{
		for(int i = 0; i < width_ * height_; i++)
		{
			dest[i * 3    ] = components_[i * 4 + 1];
			dest[i * 3 + 1] = components_[i * 4 + 2];
			dest[i * 3 + 2] = components_[i * 4 + 3];
		}
	}
}
