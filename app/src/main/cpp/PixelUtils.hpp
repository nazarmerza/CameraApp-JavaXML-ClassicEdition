/*
 * PixelUtils.hpp
 *
 * Utility functions for manipulating pixels.
 *
 *  Created on: Mar 2, 2014
 *      Author: nazar
 */

#ifndef PIXELUTILS_HPP_
#define PIXELUTILS_HPP_



namespace PixelUtils {


inline int32_t toInt(char pValue) {
	return (0xff & (int32_t) pValue);
}

inline int32_t max(int32_t pValue1, int32_t pValue2) {
	if (pValue1 < pValue2) {
		return pValue2;
	} else {
		return pValue1;
	}
}

inline int32_t clamp(int32_t pValue, int32_t pLowest, int32_t pHighest) {
	if (pValue < 0) {
		return pLowest;
	} else if (pValue > pHighest) {
		return pHighest;
	} else {
		return pValue;
	}
}

inline int32_t color(int pColorR, int pColorG, int pColorB) {
	return 0xFF000000 | ((pColorB << 6) & 0x00FF0000)
			| ((pColorG >> 2) & 0x0000FF00) | ((pColorR >> 10) & 0x000000FF);
}
}



#endif /* PIXELUTILS_HPP_ */
