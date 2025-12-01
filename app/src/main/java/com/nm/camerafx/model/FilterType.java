package com.nm.camerafx.model;

import com.nm.camerafx.R;


public class FilterType {
	public static final int 	 BLUE1	=	0	;
	public static final int 	 BLUE2	=	1	;
	public static final int 	 BLUE_SELENIUM1	=	2	;
	public static final int 	 BLUE_SELENIUM2	=	3	;
	public static final int 	 COBALT_IRON1	=	4	;
	public static final int 	 COBALT_IRON2	=	5	;
	public static final int 	 COBALT_IRON3	=	6	;
	public static final int 	 COPPER1	=	7	;
	public static final int 	 COPPER2	=	8	;
	public static final int 	 COPPER_SEPIA	=	9	;
	public static final int 	 CYAN_SELENIUM	=	10	;
	public static final int 	 CYAN_SEPIA	=	11	;
	public static final int 	 CYANOTYPE	=	12	;
	public static final int 	 GOLD1	=	13	;
	public static final int 	 GOLD2	=	14	;
	public static final int 	 GOLD_BLUE	=	15	;
	public static final int 	 GOLD_COPPER	=	16	;
	public static final int 	 GOLD_SELENIUM1	=	17	;
	public static final int 	 GOLD_SELENIUM2	=	18	;
	public static final int 	 GOLD_SEPIA	=	19	;
	public static final int 	 JK_BLUE_YELLOW	=	20	;
	public static final int 	 JK_BROWN_BROWN	=	21	;
	public static final int 	 JK_RED_YELLOW	=	22	;
	public static final int 	 PLATINUM	=	23	;
	public static final int 	 SELENIUM1	=	24	;
	public static final int 	 SELENIUM2	=	25	;
	public static final int 	 SEPIA1	=	26	;
	public static final int 	 SEPIA2	=	27	;
	public static final int 	 SEPIA3	=	28	;
	public static final int 	 SEPIA4	=	29	;
	public static final int 	 SEPIA5	=	30	;
	public static final int 	 SEPIA_ANTIQUE	=	31	;
	public static final int 	 SEPIA_BLUE1	=	32	;
	public static final int 	 SEPIA_BLUE2	=	33	;
	public static final int 	 SEPIA_CYAN	=	34	;
	public static final int 	 SEPIA_HIGHLIGHTS1	=	35	;
	public static final int 	 SEPIA_HIGHLIGHTS2	=	36	;
	public static final int 	 SEPIA_MIDTONES	=	37	;
	public static final int 	 SEPIA_SELENIUM1	=	38	;
	public static final int 	 SEPIA_SELENIUM2	=	39	;
	public static final int 	 SEPIA_SELENIUM3	=	40	;
	public static final int 	 BRANNAN	=	41	;
	public static final int 	 BW_HC_BASIC	=	42	;
	public static final int 	 BW_HC_BM40	=	43	;
	public static final int 	 NASHVILLE	=	44	;
	public static final int 	 EARLYBIRD	=	45	;
	
	public static int getFilterType(int id) {
		int filterType;

		if (id == R.drawable.filter_blue1) {
			filterType = FilterType.BLUE1;
		} else if (id == R.drawable.filter_blue2) {
			filterType = FilterType.BLUE2;
		} else if (id == R.drawable.filter_blue_selenium1) {
			filterType = FilterType.BLUE_SELENIUM1;
		} else if (id == R.drawable.filter_blue_selenium2) {
			filterType = FilterType.BLUE_SELENIUM2;
		} else if (id == R.drawable.filter_cobalt_iron1) {
			filterType = FilterType.COBALT_IRON1;
		} else if (id == R.drawable.filter_cobalt_iron2) {
			filterType = FilterType.COBALT_IRON2;
		} else if (id == R.drawable.filter_cobalt_iron3) {
			filterType = FilterType.COBALT_IRON3;
		} else if (id == R.drawable.filter_copper1) {
			filterType = FilterType.COPPER1;
		} else if (id == R.drawable.filter_copper2) {
			filterType = FilterType.COPPER2;
		} else if (id == R.drawable.filter_copper_sepia) {
			filterType = FilterType.COPPER_SEPIA;
		} else if (id == R.drawable.filter_cyan_selenium) {
			filterType = FilterType.CYAN_SELENIUM;
		} else if (id == R.drawable.filter_cyan_sepia) {
			filterType = FilterType.CYAN_SEPIA;
		} else if (id == R.drawable.filter_cyanotype) {
			filterType = FilterType.CYANOTYPE;
		} else if (id == R.drawable.filter_gold1) {
			filterType = FilterType.GOLD1;
		} else if (id == R.drawable.filter_gold2) {
			filterType = FilterType.GOLD2;
		} else if (id == R.drawable.filter_gold_blue) {
			filterType = FilterType.GOLD_BLUE;
		} else if (id == R.drawable.filter_gold_copper) {
			filterType = FilterType.GOLD_COPPER;
		} else if (id == R.drawable.filter_gold_selenium1) {
			filterType = FilterType.GOLD_SELENIUM1;
		} else if (id == R.drawable.filter_gold_selenium2) {
			filterType = FilterType.GOLD_SELENIUM2;
		} else if (id == R.drawable.filter_gold_sepia) {
			filterType = FilterType.GOLD_SEPIA;
		} else if (id == R.drawable.filter_jk_blue_yellow) {
			filterType = FilterType.JK_BLUE_YELLOW;
		} else if (id == R.drawable.filter_jk_brown_brown) {
			filterType = FilterType.JK_BROWN_BROWN;
		} else if (id == R.drawable.filter_jk_red_yellow) {
			filterType = FilterType.JK_RED_YELLOW;
		} else if (id == R.drawable.filter_platinum) {
			filterType = FilterType.PLATINUM;
		} else if (id == R.drawable.filter_selenium1) {
			filterType = FilterType.SELENIUM1;
		} else if (id == R.drawable.filter_selenium2) {
			filterType = FilterType.SELENIUM2;
		} else if (id == R.drawable.filter_sepia1) {
			filterType = FilterType.SEPIA1;
		} else if (id == R.drawable.filter_sepia2) {
			filterType = FilterType.SEPIA2;
		} else if (id == R.drawable.filter_sepia3) {
			filterType = FilterType.SEPIA3;
		} else if (id == R.drawable.filter_sepia4) {
			filterType = FilterType.SEPIA4;
		} else if (id == R.drawable.filter_sepia5) {
			filterType = FilterType.SEPIA5;
		} else if (id == R.drawable.filter_sepia_antique) {
			filterType = FilterType.SEPIA_ANTIQUE;
		} else if (id == R.drawable.filter_sepia_blue1) {
			filterType = FilterType.SEPIA_BLUE1;
		} else if (id == R.drawable.filter_sepia_blue2) {
			filterType = FilterType.SEPIA_BLUE2;
		} else if (id == R.drawable.filter_sepia_cyan) {
			filterType = FilterType.SEPIA_CYAN;
		} else if (id == R.drawable.filter_sepia_highlights1) {
			filterType = FilterType.SEPIA_HIGHLIGHTS1;
		} else if (id == R.drawable.filter_sepia_highlights2) {
			filterType = FilterType.SEPIA_HIGHLIGHTS2;
		} else if (id == R.drawable.filter_sepia_midtones) {
			filterType = FilterType.SEPIA_MIDTONES;
		} else if (id == R.drawable.filter_sepia_selenium1) {
			filterType = FilterType.SEPIA_SELENIUM1;
		} else if (id == R.drawable.filter_sepia_selenium2) {
			filterType = FilterType.SEPIA_SELENIUM2;
		} else if (id == R.drawable.filter_sepia_selenium3) {
			filterType = FilterType.SEPIA_SELENIUM3;
		} else {
			filterType = FilterType.BLUE1;
		}

		/*
		 * case R.drawable.filter_brannan: filterType = FilterType. BRANNAN;
		 * break; case R.drawable.filter_bw_hc_basic: filterType = FilterType.
		 * BW_HC_BASIC; break; case R.drawable.filter_bw_hc_bm40: filterType =
		 * FilterType. BW_HC_BM40; break; case R.drawable.filter_nashville:
		 * filterType = FilterType. NASHVILLE; break; case
		 * R.drawable.filter_earlybird: filterType = FilterType. EARLYBIRD;
		 * break;
		 */

		return filterType;
		

	}




}
