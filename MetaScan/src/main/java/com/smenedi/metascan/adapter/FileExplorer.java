package com.smenedi.metascan.adapter;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by smenedi on 12/19/13.
 */
public class FileExplorer  {
    //private String root;
    private static final String TAG = "F_PATH";
    private File path;

    File[] fileList;

    public FileExplorer() {
        fileList = null;

    }

    public FileExplorer(File root) {
        fileList = null;
        path = root;
    }

    public File[] getFileList() {
        /*try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card " + e.toString());
        }*/
        if (path.exists() && path.isDirectory()) {
            Log.e("PATH in EXPLORER: &&&&&&&&&&", path.getAbsolutePath());
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return (sel.isFile() || sel.isDirectory());
                }
            };
            Log.e(TAG, "at right place " + path.getAbsolutePath() + "abc");
            fileList = path.listFiles(filter);
            int length = fileList.length;
            Log.e(TAG, "at right place " + length);

        } else {
            Log.e(TAG, "path does not exist");
        }
        return fileList;
    }

    public Set<File> getAllFileList() {
        Set<File> result = new TreeSet<File>();
        File currentFile = this.path;
        if (currentFile.exists() && currentFile.isFile() && currentFile.canRead()) {
            result.add(this.path);
            return result;
        }

        if (currentFile.exists() && currentFile.isDirectory() && currentFile.canRead()) {
            File[] files = currentFile.listFiles();
            for (File file : files) {
                if (file.exists() && file.isFile() && file.canRead()) {
                    result.add(file);
                } else if (file.exists() && file.isDirectory() && file.canRead()) {
                    result.addAll(new FileExplorer(file).getAllFileList());
                }
            }
        }
        return result;
    }
}
