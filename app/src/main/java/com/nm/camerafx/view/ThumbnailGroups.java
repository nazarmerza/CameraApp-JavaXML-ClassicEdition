package com.nm.camerafx.view;

import java.util.ArrayList;
import java.util.List;

import com.nm.camerafx.R;




public class ThumbnailGroups {

	private static List<Thumbnail> filterMetalic = new ArrayList<Thumbnail>();
	private static List<Thumbnail> filterPreset = new ArrayList<Thumbnail>();
	private static List<Thumbnail> filtersRetro = new ArrayList<Thumbnail>();
	private static List<Thumbnail> filtersLomo = new ArrayList<Thumbnail>();
	private static List<Thumbnail> filtersBw = new ArrayList<Thumbnail>();
	private static List<Thumbnail> filtersGrunge = new ArrayList<Thumbnail>();
	private static List<Thumbnail> filtersBlur = new ArrayList<Thumbnail>();

	private static List<Thumbnail> overlay = new ArrayList<Thumbnail>();
	private static List<Thumbnail> frames = new ArrayList<Thumbnail>();

	/*
	public static List<Thumbnail> getThumbnails() {
		List<Thumbnail> thumbNails = new ArrayList<Thumbnail>(
				ThumbnailGroups.getFiltersBw());
		thumbNails.addAll(ThumbnailGroups.getFiltersRetro());
		return thumbNails;
	}

*/
	public static List<Thumbnail> getFiltersMetalic() {
		
		// close to black and white
		
		filterMetalic.add(new Thumbnail(R.drawable.filter_platinum,"PLATINUM" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_selenium1,"SELENIUM1" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_selenium2,"SELENIUM2" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_jk_brown_brown,"BROWN BROWN" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_sepia1,"SEPIA1" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_sepia2,"SEPIA2" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_sepia3,"SEPIA3" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_sepia4,"SEPIA4" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_sepia5,"SEPIA5" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_blue1,"SEPIA_BLUE1" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_blue2,"SEPIA_BLUE2" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_cyan,"SEPIA_CYAN" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_highlights1,"SEPIA HL 1" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_highlights2,"SEPIA HL 2" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_midtones,"SEPIA MID" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_selenium1,"SEPIA_SELENIUM1" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_selenium2,"SEPIA_SEL 2" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_selenium3,"SEPIA_SELENIUM3" ));

		
		filterMetalic.add(new Thumbnail(R.drawable.filter_jk_blue_yellow," BLUE YELLOW" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_blue_selenium1,"BLUE_SELENIUM1" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_blue_selenium2,"BLUE SEL 2" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_blue1,"BLUE1" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_blue2,"BLUE2" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_cobalt_iron1,"COBALT_IRON1" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_cobalt_iron2,"COBALT_IRON2" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_cobalt_iron3,"COBALT_IRON3" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_jk_red_yellow,"RED_YELLOW" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_copper1,"COPPER1" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_copper2,"COPPER2" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_copper_sepia,"COPPER_SEPIA" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_cyan_selenium,"CYAN_SELENIUM" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_cyan_sepia,"CYAN_SEPIA" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_cyanotype,"CYANOTYPE" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_sepia_antique,"SEPIA_ANTIQUE" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_gold1,"GOLD1" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_gold2,"GOLD2" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_gold_blue,"GOLD_BLUE" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_gold_copper,"GOLD_COPPER" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_gold_selenium1,"GOLD_SELENIUM1" ));
		//filterMetalic.add(new Thumbnail(R.drawable.filter_gold_selenium2,"GOLD_SELENIUM2" ));
		filterMetalic.add(new Thumbnail(R.drawable.filter_gold_sepia,"GOLD_SEPIA" ));

		




		return filterMetalic;
	}

	/*
	 * public static List<Thumbnail> getFiltersBw() { filtersBw.add(new
	 * Thumbnail(R.drawable.filter_bw, "BW")); filtersBw.add(new
	 * Thumbnail(R.drawable.filter_bw_hc_basic, "HC Basic")); filtersBw.add(new
	 * Thumbnail(R.drawable.filter_bw_hc_bm40, "HC2")); return filtersBw;
	 * 
	 * }
	 * 
	 * public static List<Thumbnail> getFiltersPreset() { // preset
	 * filterPreset.add(new Thumbnail(R.drawable.filter_earlybird,
	 * "Earlybird")); filterPreset.add(new Thumbnail(R.drawable.filter_brannan,
	 * "Brannan")); filterPreset.add(new Thumbnail(R.drawable.filter_nashville,
	 * "Nash")); filterPreset.add(new Thumbnail(R.drawable.filter_gingham,
	 * "Gotham")); return filterPreset; }
	 * 
	 * public static List<Thumbnail> getFiltersRetro() { // retro group
	 * filtersRetro.add(new Thumbnail(R.drawable.filter_earlybird,
	 * "Earlybird")); filtersRetro.add(new Thumbnail(R.drawable.filter_brannan,
	 * "Brannan")); filtersRetro.add(new Thumbnail(R.drawable.filter_nashville,
	 * "Nash")); filtersRetro.add(new Thumbnail(R.drawable.filter_gingham,
	 * "Gotham")); return filtersRetro; }
	 * 
	 * public static List<Thumbnail> getFiltersLomo() { // lomo group
	 * filtersLomo.add(new Thumbnail(R.drawable.filter_xproii, "X Pro II"));
	 * return filtersLomo; }
	 */

	public static List<Thumbnail> getFiltersGrunge() {
		return filtersGrunge;
	}

	public static List<Thumbnail> getFiltersBlur() {
		return filtersBlur;
	}

	public static List<Thumbnail> getOverlay() {
		return overlay;
	}

	public static List<Thumbnail> getFrames() {
		return frames;
	}

}
