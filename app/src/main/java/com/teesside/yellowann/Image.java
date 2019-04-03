package com.teesside.yellowann;

import java.io.Serializable;

public class Image implements Serializable
{
    public String downloadUrl;

    public Image(){}

    public Image(String dUrl)
    {
        this.downloadUrl = dUrl;
    }

    public void setDownloadUrl(String dUrl)
    {
        this.downloadUrl = dUrl;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }
}
