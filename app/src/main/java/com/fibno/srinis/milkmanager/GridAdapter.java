package com.fibno.srinis.milkmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GridAdapter extends ArrayAdapter {
    private LayoutInflater mInflater;
    private List<Date> monthlyDates;
    private Calendar currentDate;
    private Map<Date, Integer> allEvents;
    private int selectedPosition=-1;
    public static View previousView = null;
    public static int prevViewBGColor = -1;

    GridAdapter(Context context, List<Date> monthlyDates, Calendar currentDate, Map<Date, Integer> allEvents) {
        super(context, R.layout.single_cell_layout);
        this.monthlyDates = monthlyDates;
        this.currentDate = currentDate;
        this.allEvents = allEvents;
        mInflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Date mDate = monthlyDates.get(position);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(mDate);
        int dayValue = dateCal.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCal.get(Calendar.MONTH) + 1;
        int displayYear = dateCal.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH) + 1;
        int currentYear = currentDate.get(Calendar.YEAR);
        View view = convertView;
        if(view == null){
            view = mInflater.inflate(R.layout.single_cell_layout, parent, false);
        }
        if(displayMonth == currentMonth && displayYear == currentYear){
            view.setBackgroundColor(Color.parseColor("#FAE6F1"));
        }else{
            view.setBackgroundColor(Color.parseColor("#cccccc"));
        }

        //Add day to calendar
        TextView cellNumber = view.findViewById(R.id.calendar_date_id);
        cellNumber.setText(String.valueOf(dayValue));
        //Add events to the calendar
        Calendar eventCalendar = Calendar.getInstance();
        if(dayValue == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH) + 1
                && displayYear == eventCalendar.get(Calendar.YEAR)) {
            cellNumber.setBackgroundColor(Color.CYAN);
        }

        TextView eventIndicator = view.findViewById(R.id.event_id);
        if(allEvents.size() != 0 && allEvents.get(mDate) >= 0) {
            eventIndicator.setBackgroundColor(Color.parseColor("#E6F9EE"));
            eventIndicator.setText(String.valueOf(allEvents.get(mDate)));
        }

        if(position == selectedPosition) {
            previousView = view;
            ColorDrawable cd = (ColorDrawable) view.getBackground();
            prevViewBGColor = cd.getColor();
            view.setBackgroundColor(Color.YELLOW);
            view.setSelected(true);
        }

        return view;
    }
    @Override
    public int getCount() {
        return monthlyDates.size();
    }
    @Nullable
    @Override
    public Object getItem(int position) {
        return monthlyDates.get(position);
    }
    @Override
    public int getPosition(Object item) {
        return monthlyDates.indexOf(item);
    }

    public void setPosition(int position) {
        selectedPosition = position;
    }
}