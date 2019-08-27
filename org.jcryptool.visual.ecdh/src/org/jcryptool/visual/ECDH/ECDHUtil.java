package org.jcryptool.visual.ECDH;

public class ECDHUtil {
	
	/**
	 * Little helper to transform integers to bitstrings
	 * 
	 * @param i a normal positive integer
	 * @return input parameter i as bitstring
	 */
	public static String intToBitString(int i) {
		String s = ""; //$NON-NLS-1$
		int j = i;
		while (j > 1) {
			s = (j % 2) + s;
			j /= 2;
		}
		s = (j % 2) + s;
		return s;
	}
	
	/**
	 * 
	 * Convert int to bitstring and make the bitstring a certain length Not quite
	 * sure why this is needed, I guess it makes leading 0
	 * 
	 * @param i
	 * @param length
	 * @return
	 */
	public static String intToBitString(int i, int length) {
		String s = ""; //$NON-NLS-1$
		int j = i;
		for (int k = 0; k < length; k++) {
			s = (j % 2) + s;
			j /= 2;
		}
		return s;
	}
	
	/**
	 * Insert a space at every 8th character in a given String
	 * 
	 * @param input a String to be spaced
	 * @return the String with inserted spaces
	 */
	public static String spaceString(String input) {
		return input.replaceAll("(.{8})", "$1 "); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
