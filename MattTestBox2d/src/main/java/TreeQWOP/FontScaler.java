

package TreeQWOP;
/**
 * Simple way to hold a range of font sizes and interpolate based on a min, max, and given value.
 * 
 * 
 * 
 */

import java.awt.Font;

@Deprecated
public class FontScaler {

	/** List which holds a bunch of fonts in evenly increasing font sizes **/
	public Font[] FontList;
	
	/** Minimum desired font size, max desired font size, and number of fonts in the range **/
	public FontScaler(int min, int max, int number) {
		FontList = new Font[number];
		
		int increment = (max-min)/number;
		for (int i = 0; i<number; i++){		
			FontList[i] = new Font("Ariel", Font.PLAIN, min+increment*i);	
		}
	}
	
	
	/** Returns the nearest size font given a range to "interpolate" on. Doesn't really interpolate (I just knew I would understand this later) **/
	public Font InterpolateFont(float low, float high, float value){
		
		int fontIndex = (int)((value-low)/(high-low)*(FontList.length-1)); //Interpolate the given range, return closest index for fonts.
		
		if (fontIndex>FontList.length-1) fontIndex = FontList.length-1; // If we're out of range, project back in.
		if (fontIndex<0) fontIndex = 0;
		
		
		return FontList[fontIndex];
		
	}

}
