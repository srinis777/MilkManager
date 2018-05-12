package com.fibno.srinis.milkmanager.model;

public class Days
{
    private String packets;

    private String day;

    public String getPackets ()
    {
        return packets;
    }

    public void setPackets (String packets)
    {
        this.packets = packets;
    }

    public String getDay ()
    {
        return day;
    }

    public void setDay (String day)
    {
        this.day = day;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [packets = "+packets+", day = "+day+"]";
    }
}