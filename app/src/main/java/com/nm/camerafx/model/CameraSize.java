package com.nm.camerafx.model;

import java.util.Comparator;

public class CameraSize implements Comparator<CameraSize>{

    public int width;
    public int height;

    public CameraSize() {}

    public CameraSize(int w, int h) {
        width = w;
        height = h;
    }

    public CameraSize(CameraSize p) {
        this.width = p.width;
        this.height = p.height;
    }

    public final void set(int w, int h) {
        width = w;
        height = h;
    }

    public final void set(CameraSize d) {
        this.width = d.width;
        this.height = d.height;
    }

    public final boolean equals(int w, int h) {
        return this.width == w && this.height == h;
    }

    public final boolean equals(Object o) {
        return o instanceof CameraSize && (o == this || equals(((CameraSize)o).width, ((CameraSize)o).height));
    }

	@Override
	public int compare(CameraSize a, CameraSize b) {
	
			return a.width * a.height - b.width * b.height;
		
	}


    
}
