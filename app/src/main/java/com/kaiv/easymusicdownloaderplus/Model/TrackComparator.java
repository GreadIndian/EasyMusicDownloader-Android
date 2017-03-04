package com.kaiv.easymusicdownloaderplus.Model;

import java.util.Comparator;

public class TrackComparator implements Comparator<Track> {

    @Override
    public int compare(Track t1, Track t2) {
        return t1.id - t2.id;
    }
}
