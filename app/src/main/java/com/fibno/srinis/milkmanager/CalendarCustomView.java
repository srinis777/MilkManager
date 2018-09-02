package com.fibno.srinis.milkmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarCustomView extends LinearLayout {
    private static final String TAG = CalendarCustomView.class.getSimpleName();
    private ImageView previousButton, nextButton;
    private TextView currentDate;
    private GridView calendarGridView;
    private static final int MAX_CALENDAR_COLUMN = 42;
    private SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    private Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    private Context context;
    private GridAdapter mAdapter;
    private View previousView;
    private int prevViewBGColor = -1;
    List<Date> monthlyDates = new ArrayList<>();
    public CalendarCustomView(Context context) {
        super(context);
    }
    public CalendarCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initializeUILayout();
        Log.i("CalendarView","InsideCalendarView");
        setUpCalendarAdapter();
        setPreviousButtonClickEvent();
        setNextButtonClickEvent();
        setGridCellClickEvents();
        Log.d(TAG, "I need to call this method");
    }
    public CalendarCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private void initializeUILayout(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calendar_layout, this);
        previousButton = view.findViewById(R.id.previous_month);
        nextButton = view.findViewById(R.id.next_month);
        currentDate = view.findViewById(R.id.display_current_date);
        calendarGridView = view.findViewById(R.id.calendar_grid);
        inflater.inflate(R.layout.single_cell_layout, this);
    }
    public void setPreviousButtonClickEvent(){
        previousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.MONTH, -1);
                setUpCalendarAdapter();
            }
        });
    }
    private void setNextButtonClickEvent(){
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.MONTH, 1);
                setUpCalendarAdapter();
            }
        });
    }
    private void setGridCellClickEvents(){
        calendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(previousView != null) {
                    previousView.setBackgroundColor(prevViewBGColor);
                }
                if(GridAdapter.previousView != null) {
                    GridAdapter.previousView.setBackgroundColor(GridAdapter.prevViewBGColor);
                    GridAdapter.previousView = null;
                    GridAdapter.prevViewBGColor = -1;
                }

                TextView cellNumber = view.findViewById(R.id.calendar_date_id);
                    ColorDrawable cd = (ColorDrawable) view.getBackground();
                    prevViewBGColor = cd.getColor();
                    Log.d("dddd", prevViewBGColor + "");
                previousView = view;
                Date mDate = monthlyDates.get(position);
                Log.i("DateChangeListener", "invoking-------" + mDate);
                Calendar dateCal = Calendar.getInstance();
                dateCal.setTime(mDate);
                view.setBackgroundColor(Color.YELLOW);
                TextView eventIndicator = view.findViewById(R.id.event_id);
                Log.i(TAG, "EVentTEXXXX:" + eventIndicator.getText());
                ((MainActivity)context).setGridPosition(position);
                ((MainActivity) context).setmCurrentDate(mDate);

                ((MainActivity)context).displayPackets(dateCal.get(Calendar.YEAR), Integer.parseInt(cellNumber.getText().toString()),
                        dateCal.get(Calendar.MONTH) + 1);
            }
        });
    }

    public void setUpCalendarAdapter(int position){
        List<Date> dayValueInCells = new ArrayList<>();
        Map<Date, Integer> mEvents = new HashMap<>();
        Calendar mCal = (Calendar)cal.clone();
        mCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfTheMonth = mCal.get(Calendar.DAY_OF_WEEK) - 1;
        Log.i(TAG, "setUpCalendarAdapter: " + firstDayOfTheMonth + " day: " + mCal.get(Calendar.DAY_OF_MONTH));
        mCal.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while(dayValueInCells.size() < MAX_CALENDAR_COLUMN){
            dayValueInCells.add(mCal.getTime());
            if(((MainActivity) context).getDateMilkAmountMap() != null) {
                Log.i("Adaptter","DM: " + mCal.get(Calendar.DAY_OF_MONTH) + "MM " + mCal.get(Calendar.MONTH));
                int packets = ((MainActivity) context).getPackets(mCal.get(Calendar.YEAR), mCal.get(Calendar.DAY_OF_MONTH),
                        mCal.get(Calendar.MONTH) + 1);
                Log.i(TAG, "PACKETSSS:" + packets);
                mEvents.put(mCal.getTime(), packets);
            }
            mCal.add(Calendar.DAY_OF_MONTH, 1);

        }
        String sDate = formatter.format(cal.getTime());
        Log.d(TAG, "Number of date " + dayValueInCells.size() + " " + sDate);
        currentDate.setText(sDate);
        mAdapter = new GridAdapter(context, dayValueInCells, cal, mEvents);
        calendarGridView.setAdapter(mAdapter);
        mAdapter.setPosition(position);
        mAdapter.notifyDataSetChanged();
        ((MainActivity) context).checkDue();
        ((MainActivity) context).showSettlement(Calendar.getInstance().get(Calendar.MONTH) + 1);
        ((MainActivity) context).setmCurrentDate(dayValueInCells.get(position));
    }

    public void setUpCalendarAdapter(){
        List<Date> dayValueInCells = new ArrayList<>();
        Map<Date, Integer> mEvents = new HashMap<>();
        Calendar mCal = (Calendar)cal.clone();
        mCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfTheMonth = mCal.get(Calendar.DAY_OF_WEEK) - 1;
        Log.i(TAG, "setUpCalendarAdapter: " + firstDayOfTheMonth + " day: " + mCal.get(Calendar.DAY_OF_MONTH));
        mCal.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while(dayValueInCells.size() < MAX_CALENDAR_COLUMN){
            dayValueInCells.add(mCal.getTime());
            if(((MainActivity) context).getDateMilkAmountMap() != null) {
                Log.i("Adaptter","DM: " + mCal.get(Calendar.DAY_OF_MONTH) + "MM " + mCal.get(Calendar.MONTH));
                int packets = ((MainActivity) context).getPackets(mCal.get(Calendar.YEAR), mCal.get(Calendar.DAY_OF_MONTH),
                        mCal.get(Calendar.MONTH) + 1);
                Log.i(TAG, "PACKETSSS:" + packets);
                mEvents.put(mCal.getTime(), packets);
                Log.i(TAG, "MEEEE:" + mEvents.get(mCal.getTime()));
            }
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        monthlyDates = dayValueInCells;
        String sDate = formatter.format(cal.getTime());
        Log.d(TAG, "Number of date " + dayValueInCells.size() + " " + sDate);
        currentDate.setText(sDate);
        mAdapter = new GridAdapter(context, dayValueInCells, cal, mEvents);
        calendarGridView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }
}
