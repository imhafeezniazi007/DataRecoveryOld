package com.example.datarecoverynew.storage.entities


import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.database.PropertyName

@Keep
data class BPAds(
    var adIds: AdsIds? = AdsIds(),
    var addSettings: AdSettings? = AdSettings()
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readParcelable<AdsIds>(AdsIds::class.java.classLoader),
        source.readParcelable<AdSettings>(AdSettings::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(adIds, 0)
        writeParcelable(addSettings, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BPAds> = object : Parcelable.Creator<BPAds> {
            override fun createFromParcel(source: Parcel): BPAds = BPAds(source)
            override fun newArray(size: Int): Array<BPAds?> = arrayOfNulls(size)
        }
    }
}

@Keep
data class AdsIds(
    //@PropertyName("AdmobBanner")
    val AdmobBanner: String? = "",
    //@PropertyName("AdmobInt")
    val AdmobInt: String? = "",
    val AdmobAppOpen: String? = "",
    val AdNativeAd: String? = "",
    val FacebookBanner: String? = "",
    //@PropertyName("FacebookInt")
    val FacebookInt: String? = "",
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(AdmobBanner)
        writeString(AdmobInt)
        writeString(AdmobAppOpen)
        writeString(AdNativeAd)
        writeString(FacebookBanner)
        writeString(FacebookInt)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AdsIds> = object : Parcelable.Creator<AdsIds> {
            override fun createFromParcel(source: Parcel): AdsIds = AdsIds(source)
            override fun newArray(size: Int): Array<AdsIds?> = arrayOfNulls(size)
        }
    }
}

@Keep
data class AdSettings(
    @PropertyName("AdmobBanner")
    val AdmobBanner: Boolean? = false,
    @PropertyName("AdmobInt")
    val AdmobInt: Boolean? = false,
    @PropertyName("FacebookBanner")
    val FacebookBanner: Boolean? = false,
    @PropertyName("AdmobAppOpen")
    val AdmobAppOpen: Boolean? = false,
    @PropertyName("AdNativeAd")
    val AdNativeAd: Boolean? = false,
    @PropertyName("FacebookInt")
    val FacebookInt: Boolean? = false
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readValue(Boolean::class.java.classLoader) as Boolean?,
        source.readValue(Boolean::class.java.classLoader) as Boolean?,
        source.readValue(Boolean::class.java.classLoader) as Boolean?,
        source.readValue(Boolean::class.java.classLoader) as Boolean?,
        source.readValue(Boolean::class.java.classLoader) as Boolean?,
        source.readValue(Boolean::class.java.classLoader) as Boolean?
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeValue(AdmobBanner)
        writeValue(AdmobInt)
        writeValue(FacebookBanner)
        writeValue(AdmobAppOpen)
        writeValue(AdNativeAd)
        writeValue(FacebookInt)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AdSettings> = object : Parcelable.Creator<AdSettings> {
            override fun createFromParcel(source: Parcel): AdSettings = AdSettings(source)
            override fun newArray(size: Int): Array<AdSettings?> = arrayOfNulls(size)
        }
    }
}