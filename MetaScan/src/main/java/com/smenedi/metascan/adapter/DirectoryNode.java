package com.smenedi.metascan.adapter;

/**
 * Created by smenedi on 12/20/13.
 */

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class DirectoryNode {
    public File file;
    public FileState state;
    public DirectoryNode parentNode;
    //public boolean isDir;
    public HashMap<File, DirectoryNode> map;

    public DirectoryNode(File file, FileState fileState, DirectoryNode parent) {
        this.file = file;
        parentNode = parent;
        state = fileState;
        map = new HashMap<File, DirectoryNode>();
        Log.e("CURRENT DIR : ", this.file.toString());
        //for(File eachFile : this.getAllFilesInFolder()){
        // this.insert(eachFile, FileState.CHECK_NONE);
        //}
    }

    public void insert(File insertFile, FileState fileState) {
        if (this.map.containsKey(insertFile)) {
            this.map.get(insertFile).state = fileState;
        } else {
            this.map.put(insertFile, new DirectoryNode(insertFile, state, this));
        }
    }

    public File find(File findFile) {
        if (this.map.containsKey(findFile)) {
            return this.map.get(findFile).file;
        }
        return null;
    }

    public int countOfReadableFiles() {
        return ((new FileExplorer(this.file)).getFileList()).length;
    }

    public File[] getAllFilesInFolder() {
        return (new FileExplorer(this.file)).getFileList();
    }

    public ArrayList<DirectoryNode> getAllSelectedDirNodesInFolder() {

        ArrayList<DirectoryNode> result = new ArrayList<DirectoryNode>();
        if (this.state == FileState.CHECK_FULL) {
            result.add(this);
            return result;
        }

        Set<File> files = this.map.keySet();
        //File[] files = (new FileExplorer(this.file)).getFileList();
        for (File file : files) {
            FileState state = this.map.get(file).state;
            if (state == FileState.CHECK_FULL) {
                result.add(this.map.get(file));
            } else if (state == FileState.CHECK_HALF) {
                result.addAll(this.map.get(file).getAllSelectedDirNodesInFolder());
            }
        }
        return result;
    }

    public int countOfSelectedFiles() {
        int counter = 0;
        for (DirectoryNode node : this.map.values()) {
            if (node.state == FileState.CHECK_FULL)
                counter++;
        }
        return counter;
    }

    public boolean isAllFilesSelected() {
        for (DirectoryNode node : this.map.values()) {
            if (node.state != FileState.CHECK_FULL)
                return false;
        }
        return true;
    }

    public boolean isAllFilesDeSelected() {
        for (DirectoryNode node : this.map.values()) {
            if (node.state == FileState.CHECK_FULL || node.state == FileState.CHECK_HALF)
                return false;
        }
        return true;
    }


    private DirectoryNode currentParent(DirectoryNode node) {
        return node.parentNode;
    }

    public DirectoryNode getParentNode() {
        return parentNode;
    }

    public void setAllAncestorsState() {
        DirectoryNode parent = currentParent(this);
        while (parent != null) {
            if (parent.isAllFilesDeSelected()) {
                parent.state = FileState.CHECK_NONE;
                parent = currentParent(parent);
            } else if (parent.isAllFilesSelected()) {
                parent.state = FileState.CHECK_FULL;
                parent = currentParent(parent);
            } else {
                parent.state = FileState.CHECK_HALF;
                parent = currentParent(parent);
            }
        }
    }


    public Set<File> getRecursiveFiles() {
        File currentFile = this.file;
        TreeSet<File> allFiles = new TreeSet<File>();
        allFiles.addAll((new FileExplorer(currentFile)).getAllFileList());
        return allFiles;
    }
}
