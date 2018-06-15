package com.fibno.srinis.milkmanager.model;

public class Days
{
    private int packets;

    private int day;

    public int getPackets()
    {
        return packets;
    }

    public void setPackets(int packets)
    {
        this.packets = packets;
    }

    public int getDay()
    {
        return day;
    }

    public void setDay(int day)
    {
        this.day = day;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [packets = "+packets+", day = "+day+"]";
    }
}