/*
 * ImageUtils.hpp
 *
 *  Created on: Feb 26, 2014
 *      Author: nazar
 */

#ifndef CONVERTER_HPP_
#define CONVERTER_HPP_

#include "FilterArrays.hpp"
#include "PixelUtils.hpp"
#include "Log.hpp"
#include "Utils.h"

using namespace PixelUtils;
using namespace Filters;

static bool DEBUG = false;

namespace Converter {

static const int COLOR_VALUES = 256;
static int rgbMap[COLOR_VALUES][3];


inline static void nv21_to_yuv420semiplanar(uint32_t *rgb, char *yuv, int width,
		int height);
inline static void nv21_to_yuv420Planar(uint32_t *rgb, char *yuv, int width,
		int height);
inline static void nv21_to_yuv420semiplanar_color(uint32_t *rgb, char *yuv,
		int width, int height);


inline static void setMapData2(int filterType) {
	//Map2D test2D = Filters::getFilterArray(1);
	Filters::getFilterMap2(rgbMap, filterType);
	//rgbMap = test2D;
}

inline static int mapPixel(int b, int g, int r) {

	// map rgb
	b = rgbMap[b][0];		// blue
	g = rgbMap[g][1]; 	// green
	r = rgbMap[r][2];		// red

	return 0xff000000 | (b << 16) | (g << 8) | r;
}

inline static int mapPixel2(uint8_t b, uint8_t g, uint8_t r, uint8_t* y,
		uint8_t* u, uint8_t* v) {

	// map rgb
	b = rgbMap[b][0];	// blue
	g = rgbMap[g][1]; 	// green
	r = rgbMap[r][2];	// red

	*y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
	*u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
	*v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

	return 0xff000000 | (b << 16) | (g << 8) | r;
}

inline static int mapPixel3(uint8_t b, uint8_t g, uint8_t r, uint8_t* y) {

	// map rgb
	b = rgbMap[b][0];	// blue
	g = rgbMap[g][1]; 	// green
	r = rgbMap[r][2];	// red

	*y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;

	return 0xff000000 | (b << 16) | (g << 8) | r;
}



inline static void nv21_to_yuv420semiplanar(uint32_t *rgb, char *yuv, int width,
		int height) {

	int32_t size = width * height;
	//int32_t halfSize = size / 2;

	char* y1p = yuv;			// beginning of Y-plane
	char* y2p = yuv + width;	// second row of Y-plane
	char* uvp = yuv + size;		// beginning of UV-plane

	uint8_t A = 255;
	uint8_t r, g, b;
	uint8_t y1, y2, y3, y4;
	uint8_t u, v;

	// indices
	int32_t i = 0;
	int32_t uvIndex = 0;

	// Processes each pixel and converts YUV to RGB color.

	for (int h = 0; h < height; h += 2) {

		// process 2 rows of Y plane and 1 rows of VU plane
		for (int yIndex = 0; yIndex < width; yIndex += 2) {

			// get two consecutive elements of first row
			y1 = y1p[yIndex];
			y2 = y1p[yIndex + 1];

			// get two consecutive elements of second row
			y3 = y2p[yIndex];
			y4 = y2p[yIndex + 1];

			// Convert from yuv to RGB

			// create pixels for two rows of rgb
			rgb[i] = mapPixel2(y1, y1, y1, &y1, &u, &v);
			rgb[i + 1] = mapPixel3(y2, y2, y2, &y2);
			rgb[width + i] = mapPixel3(y3, y3, y3, &y3);
			rgb[width + i + 1] = mapPixel3(y4, y4, y4, &y4);

			i += 2;

			// Convert back from RGB to yuv
			// swap VU
			uvp[uvIndex] = u;
			uvp[uvIndex + 1] = v;

			uvIndex += 2;

		}

		y1p = y1p + width * 2;
		y2p = y2p + width * 2;
		i += width;

	}

}


inline static void nv21_to_yuv420Planar(uint32_t *rgb, char *yuv, int width,
		int height) {

	int32_t size = width * height;
	//int32_t halfSize = size / 2;

	char* y1p = yuv;				// beginning of Y-plane
	char* y2p = yuv + width;		// second row of Y-plane
	char* vp = yuv + size;			// beginning of V-plane
	char* up = yuv + size + size / 4;	// beginning of U-plane

	uint8_t A = 255;
	uint8_t r, g, b;
	uint8_t y1, y2, y3, y4;
	uint8_t u, v;

	// indices
	int32_t i = 0;
	int32_t vIndex = 0;
	int32_t uIndex = 0;

	// Processes each pixel and converts YUV to RGB color.

	for (int h = 0; h < height; h += 2) {

		// process 2 rows of Y plane and 1 rows of VU plane
		for (int yIndex = 0; yIndex < width; yIndex += 2) {

			// get two consecutive elements of first row
			y1 = y1p[yIndex];
			y2 = y1p[yIndex + 1];

			// get two consecutive elements of second row
			y3 = y2p[yIndex];
			y4 = y2p[yIndex + 1];

			// Convert from yuv to RGB

			// create pixels for two rows of rgb
			rgb[i] = mapPixel2(y1, y1, y1, &y1, &u, &v);
			rgb[i + 1] = mapPixel3(y2, y2, y2, &y2);
			rgb[width + i] = mapPixel3(y3, y3, y3, &y3);
			rgb[width + i + 1] = mapPixel3(y4, y4, y4, &y4);

			i += 2;

			// Convert back from RGB to yuv
			// swap VU
			vp[vIndex] = u;
			up[uIndex] = v;

			vIndex++;
			uIndex++;

		}

		y1p = y1p + width * 2;
		y2p = y2p + width * 2;
		i += width;

	}

}


/*

inline static int clamp(int value) {
	value = value > 255 ? 255 : value < 0 ? 0 : value;
	return value;
}

inline int32_t clamp2(int32_t pValue, int32_t pLowest, int32_t pHighest) {
	if (pValue < 0) {
		return pLowest;
	} else if (pValue > pHighest) {
		return pHighest;
	} else {
		return pValue;
	}
}

inline static void yuv2rgb(int* b, int* g, int* r, uint8_t y, uint8_t u,
		uint8_t v) {
	*r = y + (1.4065 * (v - 128));
	*g = y - (0.3455 * (u - 128)) - (0.7169 * (v - 128));
	*b = y + (1.7790 * (u - 128));

	*r = clamp(*r);
	*g = clamp(*g);
	*b = clamp(*b);
}

inline static void yuv2rgb2(int* b, int* g, int* r, uint8_t y, uint8_t u,
		uint8_t v) {

	 *r = y + (((v - 128)* 1436) >> 10);
	 *g = y - (((u - 128) * 352 + (v - 128) * 731) >> 10);
	 *b = y + (((u - 128) * 1814) >> 10);

	*r = clamp(*r);
	*g = clamp(*g);
	*b = clamp(*b);


}

inline static void yuv2rgbInteger(int* b, int* g, int* r, uint8_t y, uint8_t u,
		uint8_t v) {
	// Computes R, G and B from Y, U and V.
	int y1192 = 1192 * y;
	*r = (y1192 + 1634 * v);
	*g = (y1192 - 833 * v - 400 * u);
	*b = (y1192 + 2066 * u);

	*r = clamp2(*r, 0, 262143);
	*g = clamp2(*g, 0, 262143);
	*b = clamp2(*b, 0, 262143);
}

*
 * @input	rgb
 * @output  rgb

inline static void mapRgb2Rgb(int& b, int& g, int& r) {
	b = rgbMap[b][0];	// blue
	g = rgbMap[g][1]; 	// green
	r = rgbMap[r][2];	// red
}

inline static int mapPixelColor1(uint8_t* y, uint8_t* u, uint8_t* v) {

	// convert yuv to rgb
	int r, g, b;
	yuv2rgb2(&b, &g, &r, *y, *u, *v);

	// map rgb
	mapRgb2Rgb(b, g, r);

	 b = rgbMap[b][0];	// blue
	 g = rgbMap[g][1]; 	// green
	 r = rgbMap[r][2];	// red


	// rgb to yuv
	*y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
	*u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
	*v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

	// pack abgr
	return 0xff000000 | (b << 16) | (g << 8) | r;
}

inline static int mapPixelColor2(uint8_t* y, uint8_t u, uint8_t v) {

	// convert yuv to rgb
	int r, g, b;
	yuv2rgb2(&b, &g, &r, *y, u, v);

	// map rgb
	mapRgb2Rgb(b, g, r);

	 b = rgbMap[b][0];	// blue
	 g = rgbMap[g][1]; 	// green
	 r = rgbMap[r][2];	// red


	*y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;

	return 0xff000000 | (b << 16) | (g << 8) | r;
}

inline static void nv21_to_yuv420semiplanar_color(uint32_t *rgb, char *yuv,
		int width, int height) {

	int32_t size = width * height;
	//int32_t halfSize = size / 2;

	char* y1p = yuv;			// beginning of Y-plane
	char* y2p = yuv + width;	// second row of Y-plane
	char* uvp = yuv + size;		// beginning of UV-plane

	uint8_t A = 255;
	uint8_t r, g, b;
	uint8_t y1, y2, y3, y4;
	uint8_t u, v;

	// indices
	int32_t i = 0;
	int32_t uvIndex = 0;

	// Processes each pixel and converts YUV to RGB color.

	for (int h = 0; h < height; h += 2) {

		// process 2 rows of Y plane and 1 rows of VU plane
		for (int yIndex = 0; yIndex < width; yIndex += 2) {

			// get two consecutive elements of first row
			y1 = y1p[yIndex];
			y2 = y1p[yIndex + 1];

			// get two consecutive elements of second row
			y3 = y2p[yIndex];
			y4 = y2p[yIndex + 1];

			v = uvp[uvIndex];
			u = uvp[uvIndex + 1];
			// Convert from yuv to RGB

			// create pixels for two rows of rgb
			rgb[i] = mapPixelColor1(&y1, &u, &v);

			// Convert back from RGB to yuv
			// swap VU
			uvp[uvIndex] = u;
			uvp[uvIndex + 1] = v;
			uvIndex += 2;

			rgb[i + 1] = mapPixelColor2(&y2, u, v);
			rgb[width + i] = mapPixelColor2(&y3, u, v);
			rgb[width + i + 1] = mapPixelColor2(&y4, u, v);

			i += 2;

		}

		y1p = y1p + width * 2;
		y2p = y2p + width * 2;
		i += width;

	}

}
*/

//////////////////////////////////////////////////////////

inline static void nv21_to_420sp_color3(uint32_t *lBitmapContent, char *yuv,
		int width, int height) {

	long startTime;
	if (DEBUG)
		startTime = Utils::currentTimeInMilliseconds();

	int sz;
	int i;
	int j;
	int Y;
	int Cr = 0;
	int Cb = 0;
	int pixPtr = 0;
	int jDiv2 = 0;
	int R = 0;
	int G = 0;
	int B = 0;
	int cOff;
	int w = width;
	int h = height;
	sz = w * h;

	for (j = 0; j < h; j++) {
		pixPtr = j * w;
		jDiv2 = j >> 1;

		for (i = 0; i < w; i++) {
			Y = yuv[pixPtr];
			if (Y < 0)
				Y += 255;

			if ((i & 0x1) != 1) {
				// calculate offset
				cOff = sz + jDiv2 * w + (i >> 1) * 2;

				// get
				Cb = yuv[cOff];
				if (Cb < 0)
					Cb += 127;
				else
					Cb -= 128;

				Cr = yuv[cOff + 1];
				if (Cr < 0)
					Cr += 127;
				else
					Cr -= 128;
			}

			R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
			if (R < 0)
				R = 0;
			else if (R > 255)
				R = 255;

			G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3)
					+ (Cr >> 4) + (Cr >> 5);
			if (G < 0)
				G = 0;
			else if (G > 255)
				G = 255;

			B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
			if (B < 0)
				B = 0;
			else if (B > 255)
				B = 255;

			lBitmapContent[pixPtr++] = mapPixel(R, G, B);

			//lBitmapContent[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
		}
	}
	if (DEBUG) {
		startTime = Utils::currentTimeInMilliseconds() - startTime;
		LOGD("method time = %lu", startTime);
	}

}
inline static void yuv420sp_nv21_to_rgb_420sp_yuv(uint32_t *rgb, char *yuv,
		int width, int height) {

	int32_t size = width * height;

	uint8_t A = 255;
	uint8_t R, G, B;
	uint8_t Y;
	int32_t i;

	// Processes each pixel and converts YUV to RGB color.
	for (i = 0; i < size; i++) {

		Y = yuv[i];

		R = Y;
		G = Y;
		B = Y;

		rgb[i] = 0xff000000 | (B << 16) | (G << 8) | R;
	}
}

inline static void nv21_to_rgb_to_420sp_yvu(uint32_t *rgb, char *yuv, int width,
		int height) {

	int32_t size = width * height;
	//int32_t halfSize = size / 2;

	char* y1p = yuv;			// beginning of Y-plane
	char* y2p = yuv + width;	// second row of Y-plane
	char* uvp = yuv + size;		// beginning of UV-plane

	uint8_t A = 255;
	uint8_t r, g, b;
	uint8_t y1, y2, y3, y4;
	char u, v;

	// indices
	int32_t i = 0;
	int32_t uvIndex = 0;

	// Processes each pixel and converts YUV to RGB color.

	for (int h = 0; h < height; h += 2) {

		// process 2 rows of Y plane and 1 rows of VU plane
		for (int yIndex = 0; yIndex < width; yIndex += 2) {

			// get two consecutive elements of first row
			y1 = y1p[yIndex];
			y2 = y1p[yIndex + 1];

			// get two consecutive elements of second row
			y3 = y2p[yIndex];
			y4 = y2p[yIndex + 1];

			// get u, v elements
			v = uvp[uvIndex];
			u = uvp[uvIndex + 1];

			// swap VU
			uvp[uvIndex] = u;
			uvp[uvIndex + 1] = v;

			uvIndex += 2;

			// create pixels for two rows of rgb
			rgb[i] = 0xff000000 | (y1 << 16) | (y1 << 8) | y1;
			rgb[i + 1] = 0xff000000 | (y2 << 16) | (y2 << 8) | y2;
			rgb[width + i] = 0xff000000 | (y3 << 16) | (y3 << 8) | y3;
			rgb[width + i + 1] = 0xff000000 | (y4 << 16) | (y4 << 8) | y4;
			i += 2;

		}

		y1p = y1p + width * 2;
		y2p = y2p + width * 2;
		i += width;

	}

}

}

#endif /* CONVERTER_HPP_ */
