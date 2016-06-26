package com.rahim.metawaregraph.utils;

import com.mbientlab.metawear.MetaWearBoard;

/**
 * Created by rahim on 23-Jun-16.
 */
public class Board {
    private static MetaWearBoard metaWearBoard;
    private static Board board;

    private Board(){

    }

    public static Board getInstance(){
        if(board==null) board = new Board();
        return board;
    }

    public MetaWearBoard getMetaWearBoard(){
        return metaWearBoard;
    }

    public void setMetaWearBoard(MetaWearBoard metaWearBoard){
        this.metaWearBoard = metaWearBoard;
    }

}
