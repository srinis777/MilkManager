package com.fibno.srinis.milkmanager;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fibno.srinis.milkmanager.model.Days;
import com.fibno.srinis.milkmanager.model.Months;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private  final Map m_dateMilkAmountMap = new HashMap<String, Integer>();
    private final FirebaseDatabase m_fireDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference m_myRef;
    private String m_val = "2";


   // private String m_currentDate;
    private Date m_currentDate;
    static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalendarView simpleCalendarView = (CalendarView) findViewById(R.id.calendarView); // get the reference of CalendarView
        m_currentDate = new Date(simpleCalendarView.getDate());

    //    m_currentDate = calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + 1
      //          + "-" + calendar.get(Calendar.YEAR);
        m_myRef = m_fireDatabase.getReference();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(m_currentDate);
        updateDBReference(2, false,
                (calendar.get(Calendar.MONTH) + 1) + "", calendar.get(Calendar.YEAR)+"");

        ImageButton plusButton = (ImageButton) findViewById(R.id.imageButton2); // get the reference of CalendarView
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv1 = (TextView)findViewById(R.id.textView);
                int currentPackets = Integer.parseInt(tv1.getText().toString());
                if(currentPackets < 10) {
                    tv1.setText(++currentPackets + "");
                    Log.i("CurrentValue: ",currentPackets + "");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(m_currentDate);
                    updateDBReference(currentPackets, true,
                            (calendar.get(Calendar.MONTH) + 1) + "", calendar.get(Calendar.YEAR)+"");                    //  m_dateMilkAmountMap.put(m_currentDate, currentPackets);
                  //  Calendar calendar = Calendar.getInstance();
                  /**  try {
                        calendar.setTime(new SimpleDateFormat("dd-MM-yyyy").parse(m_currentDate));
                    } catch (ParseException e) {
                        Log.e("MinusButtonOnClick: ",e.getMessage());
                    }**/
                   /** m_myRef.child(calendar.get(Calendar.YEAR)+"")
                            .child(calendar.get(Calendar.MONTH) + 1 +"")
                            .child(calendar.get(Calendar.DAY_OF_MONTH)+"").setValue(currentPackets);
                    **/
                }
            }
        });

        ImageButton minusButton = (ImageButton) findViewById(R.id.imageButton); // get the reference of CalendarView
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv1 = (TextView)findViewById(R.id.textView);
                int currentPackets = Integer.parseInt(tv1.getText().toString());
                if(currentPackets > 0) {
                    tv1.setText(--currentPackets + "");
                    Log.i("CurrentValue: ",currentPackets + "");
                    CalendarView simpleCalendarView = (CalendarView) findViewById(R.id.calendarView); // get the reference of CalendarView
                   // m_dateMilkAmountMap.put(m_currentDate, currentPackets);
                    Calendar calendar = Calendar.getInstance();
                        calendar.setTime(m_currentDate);
                    updateDBReference(currentPackets, true,
                            (calendar.get(Calendar.MONTH) + 1) + "", calendar.get(Calendar.YEAR)+"");
                  /**  m_myRef.child("years").child(calendar.get(Calendar.YEAR)+"")
                            .child((calendar.get(Calendar.MONTH) + 1) + "")
                            .child(calendar.get(Calendar.DAY_OF_MONTH)+ "").setValue(currentPackets);**/
                }
            }
        });

        simpleCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                try {
                    m_currentDate = new SimpleDateFormat("dd-MM-yyyy").parse(dayOfMonth + "-" + (month + 1) + "-" + year);
                } catch (ParseException e) {
                    Log.e("setOnDateChangeListener",e.getMessage());
                }
                Log.i("CurrentDate: ",dayOfMonth + "-" + (month + 1) + "-" + year);
                updateDBReference(2, false, (month + 1) + "", year + "");
                /**if(!m_dateMilkAmountMap.containsKey(m_currentDate)) {
                    m_dateMilkAmountMap.put(m_currentDate, 2);
                    storeMilkDetails(year, month + 1, dayOfMonth,2 );
                }
                m_myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i("DataChangevalue:",""+dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
               // Log.i("Value: " , m_val);
                ((TextView)findViewById(R.id.textView)).setText(m_dateMilkAmountMap.get(m_currentDate) + "");
                 **/
            }
        });
    }

    private void updateDBReference(final int packets, final boolean update,
                                   final String month, final String year) {
       /** Log.i("Root:",m_myRef.getRoot().toString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(m_currentDate);
        Map<String, Object> daysMap = new HashMap<>();
        daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH) ,packets);
        Map<String, Object> monthsMap = new HashMap<>();
        monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1) ,daysMap);
        Map<String, Object> yearsMap = new HashMap<>();
        yearsMap.put("" + calendar.get(Calendar.YEAR), monthsMap);

        Map<String, Object> accountsMap = new HashMap<>();
        accountsMap.put("years",yearsMap);
        m_myRef.setValue(accountsMap);**/

        m_myRef.child("years").child(year).child(month).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> yearsMap = (Map<String, Object>) dataSnapshot.getValue();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(m_currentDate);
                Log.i("YearsMap:",""+yearsMap.get("" + calendar.get(Calendar.YEAR)));
                if(yearsMap.get("" + calendar.get(Calendar.YEAR)) instanceof java.util.Map) {
                    Log.i("YearsMap:",""+yearsMap.get("" + calendar.get(Calendar.YEAR)));
                }

                if(yearsMap.containsKey("" + calendar.get(Calendar.YEAR))) {
                    Log.i("Debug:", calendar.get(Calendar.YEAR) + ":"
                            + (calendar.get(Calendar.MONTH) + 1) + ":" + calendar.get(Calendar.DAY_OF_MONTH));
                    Map<String, Object> monthsMap = (Map<String, Object>) yearsMap.get("" + calendar.get(Calendar.YEAR));
                    if(monthsMap.containsKey("" + (calendar.get(Calendar.MONTH) + 1))) {
                        if(monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1)) instanceof java.util.List) {
                            Log.i("Value:",""+yearsMap.get("" + yearsMap.get("" + calendar.get(Calendar.YEAR))));
                        }
                        Log.i("MonthsMap:", monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1)) + "");
                        Map<String, Object> daysMap = (Map<String, Object>) monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1));
                        Log.i("DaysMap: ",daysMap.toString());
                        if(daysMap.containsKey("" + calendar.get(Calendar.DAY_OF_MONTH))) {
                            Log.i("ExistingValue:",dataSnapshot.getValue().toString()
                                    + "::" + daysMap.get("" + calendar.get(Calendar.DAY_OF_MONTH)) + "");
                            if(update) {
                                daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                                m_myRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                        .child("" + (calendar.get(Calendar.MONTH) + 1))
                                        .updateChildren(daysMap);
                            }
                        } else {
                            daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH) ,packets);
                            Log.i("NewValue:",dataSnapshot.getValue().toString()+"::"+yearsMap.toString()+"");
                            m_myRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                    .child("" + (calendar.get(Calendar.MONTH) + 1))
                                    .updateChildren(daysMap);                        }
                        ((TextView)findViewById(R.id.textView)).setText(daysMap.get("" + calendar.get(Calendar.DAY_OF_MONTH)) + "");
                    } else {
                        Map<String, Object> daysMap = new HashMap<>();
                        daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH) ,packets);
                        monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1) ,daysMap);
                        m_myRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                .updateChildren(monthsMap);
                        Log.i("CreatingMonths",yearsMap.toString());
                    }
                } else {
                    Map<String, Object> daysMap = new HashMap<>();
                    daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH) ,packets);
                    Map<String, Object> monthsMap = new HashMap<>();
                    monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1) ,daysMap);
                    yearsMap.put("" + calendar.get(Calendar.YEAR),monthsMap);
                    m_myRef.child("years").updateChildren(yearsMap) ;
                    Log.i("CreatingYears",yearsMap.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
