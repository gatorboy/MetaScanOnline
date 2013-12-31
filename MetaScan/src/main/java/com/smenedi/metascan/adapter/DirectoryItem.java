package com.smenedi.metascan.adapter;

/**
 * Created by smenedi on 12/19/13.
 */
public class DirectoryItem {
    public int checkBoxIcon;
    public int icon;
    public String title;
    public DirectoryItem(){
        super();
    }
    public DirectoryItem(int checkBoxIcon, int icon, String title) {
        super();
        this.checkBoxIcon = checkBoxIcon;
        this.icon = icon;
        this.title = title;
    }
}
