package com.smenedi.metascan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by smenedi on 12/26/13.
 */
public class Result implements Parcelable {
    String file;
    String status;
    String dataid;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataid() {
        return dataid;
    }

    public void setDataid(String dataid) {
        this.dataid = dataid;
    }

    public Result(String file, String status, String dataid) {
        this.file = file;
        this.status = status;
        this.dataid = dataid;
    }
    public Result(Result res) {
        this.file = res.file;
        this.status = res.status;
        this.dataid = res.dataid;
    }

    //parcel part
    public Result(Parcel in){
        this.file = in.readString();
        this.status = in.readString();
        this.dataid = in.readString();
    }
    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeStringArray(new String[]{this.file, this.status, this.dataid});
        dest.writeString(file);
        dest.writeString(status);
        dest.writeString(dataid);
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel source) {
            return new Result(source);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

}
