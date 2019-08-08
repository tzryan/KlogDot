package com.tzlog.dotlib

import android.os.Parcel
import android.os.Parcelable
import java.io.File

/**
 * @ComputerCode: tianzhen
 * @Author: TianZhen
 * @QQ: 959699751
 * @CreateTime: Created on 2019/8/8 17:53
 * @Package: com.tzlog.dotlib
 * @Description:
 **/
data class IData constructor(var file: File, var checked:Boolean) : Parcelable {

    constructor(parcel: Parcel) : this( TODO("file"), parcel.readByte() != 0.toByte() )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (checked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IData> {
        override fun createFromParcel(parcel: Parcel): IData {
            return IData(parcel)
        }

        override fun newArray(size: Int): Array<IData?> {
            return arrayOfNulls(size)
        }
    }
}