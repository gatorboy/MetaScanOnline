package com.smenedi.metascan.results;
import com.smenedi.metascan.R;
/**
 * Created by smenedi on 12/21/13.
 */
public class ResultsItem {
    public String fileName;
    public String scanResult;
    public String scanStatus;
    public int overflowMenu;
    public ResultsItem(){
        super();
    }
    public ResultsItem(String fileName, String scanResult, String scanStatus) {
        super();
        this.fileName = fileName;
        this.scanResult = scanResult;
        this.scanStatus = scanStatus;
        this.overflowMenu = R.drawable.listitem_overflow_menu;
    }
}
