package com.fibno.srinis.milkmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fibno.srinis.milkmanager.model.MilkAccount;
import com.fibno.srinis.milkmanager.model.Months;
import com.fibno.srinis.milkmanager.model.Years;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private MilkAccount m_dateMilkAmountMap;
    Map<Integer, Integer> mPacketsDueMap;
    private final FirebaseDatabase mFireDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference m_myRef;
    AlertDialog.Builder mAlertSettleBuilder;
    AlertDialog.Builder mAlertNotDatedBuilder;
    private Date m_currentDate;
    
    //drawer objects
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ArrayList<AccountItem> mAccountItems = new ArrayList<AccountItem>();

    //declaring constants
    private int DEFAULT_PACKETS = 2;
    private int MILK_PACKET_PRICE = 20;
    private int SERVICE_CHARGE = 30;
    private static String YEAR_PREFIX = "Y";
    private static String MONTH_PREFIX = "M";
    private static String DAY_PREFIX = "D";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // drawer operation
        mAccountItems.add(new AccountItem("D3502"));
        mAccountItems.add(new AccountItem("D3511"));
        mAccountItems.add(new AccountItem("D3701"));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mAccountItems);
        mDrawerList.setAdapter(adapter);

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String accountName = ((AccountItem)mDrawerList.getItemAtPosition(position)).mAccountName;
                Log.i(TAG, "onItemClick: " + accountName);
                if (m_dateMilkAmountMap != null) {
                    Log.i("onItemClick","UpdatePreviousAccount");
                    m_myRef.child("years").setValue(m_dateMilkAmountMap.getYears());
                }
                cacheDBData(accountName);
                invokeDateChangeListener();
                invokeButtonListeners();
                createSettleAlertDialog();
                createNotDatedAlertDialog();
            }
        });
    }

    private void createSettleAlertDialog() {
        mAlertSettleBuilder = new AlertDialog.Builder(this, R.style.SettleAlertDialogStyle);
        mAlertSettleBuilder.setTitle("Settle Amount for Month " + getCurrentCalendar().get(Calendar.MONTH));
        mAlertSettleBuilder.setMessage("Are you sure you want to settle up?");
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        Calendar calendar = getCurrentCalendar();
                        List<String> unsettledMonths = m_dateMilkAmountMap.getUnsettledMonths();
                        if (unsettledMonths == null || unsettledMonths.isEmpty()) {
                            Log.i("AlertDialog: ", "Unsettled months is empty");
                            return;
                        }
                        unsettledMonths.remove(MONTH_PREFIX + calendar.get(Calendar.MONTH));
                        m_dateMilkAmountMap.setUnsettledMonths(unsettledMonths);
                        Log.i("UnsettledMonths:::", m_dateMilkAmountMap.getUnsettledMonths().toString());
                        findViewById(R.id.textViewBalance).setVisibility(View.INVISIBLE);
                        findViewById(R.id.textViewAdvance).setVisibility(View.INVISIBLE);
                        findViewById(R.id.totalAmount).setVisibility(View.INVISIBLE);
                        findViewById(R.id.settle).setVisibility(View.INVISIBLE);
                        mPacketsDueMap.remove(calendar.get(Calendar.MONTH));
                        m_myRef.setValue(m_dateMilkAmountMap);
                        break;
                }
            }
        };
        mAlertSettleBuilder.setPositiveButton("Yes", dialogOnClickListener);
        mAlertSettleBuilder.setNegativeButton("No", dialogOnClickListener);
    }

    private void createNotDatedAlertDialog() {
        mAlertNotDatedBuilder = new AlertDialog.Builder(this, R.style.AlertNotDatedDays);
        mAlertNotDatedBuilder.setTitle("Not Updated for Month " + (getCurrentCalendar().get(Calendar.MONTH) + 1));
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        mAlertNotDatedBuilder.setPositiveButton("OK", dialogOnClickListener);
    }

    private void invokeButtonListeners() {
        ImageButton plusButton = (ImageButton) findViewById(R.id.imageButtonPlus); // get the reference of CalendarView
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView tv1 = (TextView) findViewById(R.id.textViewPackets);
                int currentPackets = Integer.parseInt(tv1.getText().toString());
                if (currentPackets < 10) {
                    tv1.setText(++currentPackets + "");
                    Log.i("CurrentValue: ", currentPackets + "");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(m_currentDate);
                    updatePackets(currentPackets, YEAR_PREFIX + calendar.get(Calendar.YEAR)
                            , DAY_PREFIX + calendar.get(Calendar.DAY_OF_MONTH), MONTH_PREFIX + (calendar.get(Calendar.MONTH) + 1));
                }
            }
        });

        ImageButton minusButton = (ImageButton) findViewById(R.id.imageButtonMinus); // get the reference of CalendarView
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv1 = (TextView) findViewById(R.id.textViewPackets);
                int currentPackets = Integer.parseInt(tv1.getText().toString());
                if (currentPackets > 0) {
                    tv1.setText(--currentPackets + "");
                    Log.i("CurrentValue: ", currentPackets + "");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(m_currentDate);
                    updatePackets(currentPackets, YEAR_PREFIX + calendar.get(Calendar.YEAR)
                            , DAY_PREFIX + calendar.get(Calendar.DAY_OF_MONTH), MONTH_PREFIX + (calendar.get(Calendar.MONTH) + 1));
                }
            }

        });

        Button settleButton = (Button) findViewById(R.id.settle);
        settleButton.setVisibility(View.INVISIBLE);
        settleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAlertSettleBuilder.show();
            }
        });

    }

    /**
     * cached FireBase DB reference to local POJO
     * @param accountName
     */
    private void cacheDBData(String accountName) {

 /*       Supplier supplier = new Supplier();

         m_myRef = mFireDatabase.getReference("Supplier");
         Months months = new Months();
         Map<String, Integer> daysMap = new HashMap<>();
         daysMap.put("D1",2);
         months.setDays(daysMap);
         months.setMonth(5);
         Years years = new Years();
         Map<String, Months> monthsMap = new HashMap<>();
         monthsMap.put("M5",months);
         years.setMonths(monthsMap);
         years.setYear(2018);
         MilkAccount accounts = new MilkAccount();
         Map<String, Years> yearsMap = new HashMap<>();
         yearsMap.put("Y2018",years);
         accounts.setYears(yearsMap);
         accounts.setAccount("D3502");
         //List<String> unsettledMonths = new ArrayList<>();
         //unsettledMonths.add("M7");
         //accounts.setUnsettledMonths(unsettledMonths);
         Map<String, MilkAccount> accountMap = new HashMap<>();
         accountMap.put("D3502", accounts);
         m_myRef.setValue(accountMap);*/
         m_myRef = mFireDatabase.getReference("Supplier").child(accountName).getRef();
        //System.out.println("--------"+m_myRef.child("D3502"));
        //DatabaseReference ref = m_myRef.child("D3502").getRef();
        m_myRef.addListenerForSingleValueEvent(attachValueEventListener());
        //mFireDatabase.setPersistenceEnabled(true);
        /*m_myRef = mFireDatabase.getReference();
        m_myRef.keepSynced(true);
        m_myRef.addListenerForSingleValueEvent(attachValueEventListener());
        if (m_dateMilkAmountMap == null) {
            Log.i("CheckNull", "Yes");
        }*/
    }

    private ValueEventListener attachValueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /** boolean connected = dataSnapshot.getValue(Boolean.class);
                 if (connected) {
                 System.out.println("connected");
                 } else {
                 System.out.println("not connected");
                 }**/
                if(dataSnapshot.exists()) {
                    m_dateMilkAmountMap =
                            dataSnapshot.getValue(MilkAccount.class);
                    if(m_dateMilkAmountMap == null) {
                        Log.i("onDataChange:", "NoInfo Found");
                        return;
                    }
                    Log.i("YearsMap:", m_dateMilkAmountMap.toString());
                    loadCurrentDate();
                    showTotalPacketsBoughtInMonth(Calendar.getInstance(),
                            Calendar.getInstance().get(Calendar.MONTH) + 1);
                    checkDue();
                    showSettlement(Calendar.getInstance().get(Calendar.MONTH) + 1);
                } else {
                    Log.i("NoSnapshot", "NoSnapShot exists");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    /**
     * calculates the due with the previous unsettled months and calculates the
     * balance
     */
    private void checkDue() {
        List<String> unsettledMonths = m_dateMilkAmountMap.getUnsettledMonths();
        if (unsettledMonths == null) {
            Log.i("Checking Due...", "No dues");
            return;
        }
        Calendar calendar = getCurrentCalendar();
        Years years = m_dateMilkAmountMap.getYears().get(YEAR_PREFIX + calendar.get(Calendar.YEAR));
        mPacketsDueMap = new HashMap<>();
/**if(unsettledMonths == null || unsettledMonths.isEmpty()) {
 unsettledMonths = new ArrayList<>();
 unsettledMonths.add("M5");
 m_dateMilkAmountMap.setUnsettledMonths(unsettledMonths);
 m_myRef.setValue(m_dateMilkAmountMap);
 }**/
        appendPreviousMonthToUnsettledMonths(MONTH_PREFIX
                + calendar.get(Calendar.MONTH), unsettledMonths);
        for (String unsettledMonth : unsettledMonths) {
            Log.i("YearsInUnsettled: ", years.toString() + unsettledMonth);
            Months month = years.getMonths().get(unsettledMonth);
            Map<String, Integer> daysPacketMap = month.getDays();
            int unsettledMonthInInt = Integer.parseInt(unsettledMonth.substring(1));

            final int totalDays = getTotalDays(unsettledMonthInInt);
            calculateMonthBalance(daysPacketMap, unsettledMonthInInt, totalDays);
        }
    }

    /**
     * add previous month if it is not present in unsettledmonths if new month started
     *
     * @param prevMonth previous month in String(For eg., May - M5)
     * @param unsettledMonths list of unsettled months in String
     */
    private void appendPreviousMonthToUnsettledMonths(String prevMonth, List<String> unsettledMonths) {
        if (!unsettledMonths.contains(prevMonth)) {
            unsettledMonths.add(prevMonth);
            m_dateMilkAmountMap.setUnsettledMonths(unsettledMonths);
            m_myRef.setValue(m_dateMilkAmountMap);
        }
    }

    /**
     * get total days of the months
     *
     * @param month month in integer
     * @return total days
     */
    private int getTotalDays(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.getInstance().get(Calendar.YEAR), month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * calculates the month balance of the unsettled month
     *
     * @param daysPacketMap       days & packets of the unsettled month
     * @param unsettledMonthInInt unsettled month in integer
     * @param totalDays           total days of the current month
     */
    private void calculateMonthBalance(Map<String, Integer> daysPacketMap, int unsettledMonthInInt, int totalDays) {
        final int totalPacketsInMonth = (totalDays * DEFAULT_PACKETS);
        int packetsInDue = totalPacketsInMonth - getTotalPacketsBought(daysPacketMap);
        mPacketsDueMap.put(unsettledMonthInInt, packetsInDue);
    }

    /**
     * updates current date's information and display it in the UI.
     * displays packets & show alert if previous days are not updated.
     */
    private void loadCurrentDate() {
        Calendar calendar = getCurrentCalendar();
        updatePackets(DEFAULT_PACKETS, YEAR_PREFIX + calendar.get(Calendar.YEAR)
                , DAY_PREFIX + calendar.get(Calendar.DAY_OF_MONTH), MONTH_PREFIX + (calendar.get(Calendar.MONTH) + 1));
        Map<String, Integer> daysMap = getDaysMapOfMonth(YEAR_PREFIX + calendar.get(Calendar.YEAR), MONTH_PREFIX + (calendar.get(Calendar.MONTH) + 1));
        updateNotDatedDays(daysMap, calendar.get(Calendar.DAY_OF_MONTH),
                YEAR_PREFIX + calendar.get(Calendar.YEAR), MONTH_PREFIX + (calendar.get(Calendar.MONTH) + 1));
    }

    /**
     * shows alert dialog if the previous days of the month before today are not updated
     *
     * @param daysMap daysmap of the month
     * @param dayOfMonth today
     * @param year year in String
     * @param month month in String
     */
    private void updateNotDatedDays(Map<String, Integer> daysMap, int dayOfMonth, String year, String month) {
        boolean foundStartDate = false;
        Log.i("DaysMap", daysMap.toString() + " ");
        int i = dayOfMonth - 1;
        for (; i > 0; i--) {
            Log.i("IIIDaysMap", String.valueOf(i));
            if (daysMap.containsKey(DAY_PREFIX + i)) {
                mAlertNotDatedBuilder.setMessage("You have not updated after " + i);
                mAlertNotDatedBuilder.show();
                foundStartDate = true;
                break;
            }
            daysMap.put(DAY_PREFIX + i, DEFAULT_PACKETS);
        }
        if (i == dayOfMonth - 1) {
            return;
        }
        Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
        Years years = yearsMap.get(year);
        Map<String, Months> monthsMap = years.getMonths();
        Months months = monthsMap.get(month);
        months.setDays(daysMap);

        if (foundStartDate) {
            return;
        }

        // first verify days in previous unsettled months are updated
        List<String> unsettledMonths = m_dateMilkAmountMap.getUnsettledMonths();
        if (unsettledMonths.isEmpty()) {
            return;
        }
        List<Integer> unsettledMonthsInInt = new ArrayList<>();
        for (String unSettledMonth : unsettledMonths) {
            unsettledMonthsInInt.add(Integer.parseInt(unSettledMonth.substring(1)));
        }
        unsettledMonthsInInt.add(Integer.parseInt(month.substring(1)));
        Collections.sort(unsettledMonthsInInt, Collections.reverseOrder());
        for (int unSettledMonth : unsettledMonthsInInt) {
            int day = findNonUpdatedDateBeginning(unSettledMonth, year);
            if (day != -1) {
                mAlertNotDatedBuilder.setMessage("You have not updated after " + day + " of" +
                        " Month " + unSettledMonth);
                mAlertNotDatedBuilder.show();
                break;
            }
        }
    }

    /**
     * finds non updated date beginning for the unsettled month in the app
     *
     * @param unSettledMonth
     * @param year
     * @return -1 if no days are updated or else return day till the update
     */
    private int findNonUpdatedDateBeginning(int unSettledMonth, String year) {
        int day = -1;
        Calendar calendar = Calendar.getInstance();
        Map<String, Integer> prevDaysMap = getDaysMapOfMonth(YEAR_PREFIX +
                calendar.get(Calendar.YEAR), MONTH_PREFIX + unSettledMonth);
        final int totalDays = getTotalDays(unSettledMonth);
        int i = totalDays;
        for (; i > 0; i--) {
            if (prevDaysMap.containsKey(DAY_PREFIX + i)) {
                day = i;
                break;
            }
            prevDaysMap.put(DAY_PREFIX + i, DEFAULT_PACKETS);
        }
        if (i != totalDays) {
            Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
            Years years = yearsMap.get(year);
            Map<String, Months> monthsMap = years.getMonths();
            Months months = monthsMap.get(MONTH_PREFIX + unSettledMonth);
            months.setDays(prevDaysMap);
        }
        return day;
    }

    /**
     * get days & packets map of the given month
     *
     * @param year year in String
     * @param month month in String
     * @return daysMap
     */
    private Map<String, Integer> getDaysMapOfMonth(String year, String month) {
        Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
        Years years = yearsMap.get(year);
        Map<String, Months> monthsMap = years.getMonths();
        Months months = monthsMap.get(month);
        return months.getDays();
    }

    private Calendar getCurrentCalendar() {
        CalendarView simpleCalendarView = (CalendarView) findViewById(R.id.calendarView); // get the reference of CalendarView
        m_currentDate = new Date(simpleCalendarView.getDate());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(m_currentDate);
        return calendar;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Start: ", "Starting");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i("Resume: ", "Resuming" + m_currentDate);
    }

    /**
     * updates packets & dates, populates unsettled months in the local cache &
     * display packets in the UI
     *
     * @param packets total packets bought
     * @param year year in String
     * @param dayOfMonth day in String
     * @param month month in String
     */
    private void updatePackets(int packets, String year, String dayOfMonth, String month) {
        if (m_dateMilkAmountMap == null) {
            Log.e("NoData", "No Data Found");
            return;
        }
        Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
        if (!yearsMap.containsKey(year)) {
            Years years = new Years();
            Map<String, Months> monthsMap = new HashMap<>();
            monthsMap.put(month, populateMonths(dayOfMonth, DEFAULT_PACKETS));
            years.setMonths(monthsMap);
            yearsMap.put(year, years);
        } else {
            Years years = yearsMap.get(year);
            Map<String, Months> monthsMap = years.getMonths();
            if (!monthsMap.containsKey(month)) {
                monthsMap.put(month, populateMonths(dayOfMonth, DEFAULT_PACKETS));
                populateUnsettledMonths(month, monthsMap);
            } else {
                Months months = monthsMap.get(month);
                Map<String, Integer> daysMap = months.getDays();
                if (!daysMap.containsKey(dayOfMonth) || daysMap.get(dayOfMonth) != packets) {
                    daysMap.put(dayOfMonth, packets);
                }
            }
        }
        ((TextView) findViewById(R.id.textViewPackets)).setText(String.valueOf(packets));
    }

    /**
     * This method will be called when new month is generated.
     * It populates unsettled month list. If previous month is not present in the list & has due,
     * then new unsettled months list will be generated.
     *
     * @param month month in String
     * @param monthsMap months map
     */
    private void populateUnsettledMonths(String month, Map<String, Months> monthsMap) {
        String prevMonth = MONTH_PREFIX + (Integer.parseInt(month.substring(1)) - 1);
        if (monthsMap.containsKey(prevMonth)) {
            List<String> unsettledMonths = m_dateMilkAmountMap.getUnsettledMonths();
            if (unsettledMonths == null) {
                m_dateMilkAmountMap.setUnsettledMonths(new ArrayList<String>());
            }
            //appendPreviousMonthToUnsettledMonths(prevMonth, unsettledMonths);
        }
    }

    /**
     * populate months object with all the days generated till the present day and packets updated.
     *
     * @param dayOfMonth day in string
     * @param packets    usually default packets
     * @return Months Months object
     */
    private Months populateMonths(String dayOfMonth, int packets) {
        Months months = new Months();
        Map<String, Integer> daysMap = new HashMap<>();
        final int[] days = getAllPreviousDays(dayOfMonth);
        for (int day : days) {
            daysMap.put(DAY_PREFIX + day, packets);
        }
        months.setDays(daysMap);
        return months;
    }

    /**
     * get all previous days of the present day in integer array
     *
     * @param dayOfMonth day in String
     * @return integer array containing days before present day
     */
    private int[] getAllPreviousDays(String dayOfMonth) {
        final int presentDay = Integer.parseInt(dayOfMonth.substring(1));
        int[] days = new int[presentDay];
        for (int i = 0; i < presentDay; i++) {
            days[i] = i + 1;
        }
        return days;
    }

    private void invokeDateChangeListener() {
        CalendarView simpleCalendarView = findViewById(R.id.calendarView); // get the reference of CalendarView

        simpleCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Log.i("DateChangeListener", "invoking-------");
                displayPackets(year, dayOfMonth, month + 1);
                showTotalPacketsBoughtInMonth(Calendar.getInstance(), month + 1);
                showSettlement(month + 1);

            }
        });
    }

    /**
     * updates the view of the UI with all UI elements of the current month
     *
     * @param currentMonth current month(For eg., May means 5)
     */
    private void showSettlement(int currentMonth) {
        if (mPacketsDueMap == null) {
            return;
        }
        //check if it is current month
        Calendar currentCalendar = Calendar.getInstance();
        if (mPacketsDueMap.containsKey(currentMonth - 1)) {
            // show advance to pay
            final int advanceToPay = getTotalDays(currentMonth) * DEFAULT_PACKETS * MILK_PACKET_PRICE;
            final String advMsg = "AdvanceToPay: " + getTotalDays(currentMonth)
                    + " X " + DEFAULT_PACKETS + " X " + MILK_PACKET_PRICE + " = " + advanceToPay;
            ((TextView) findViewById(R.id.textViewAdvance))
                    .setText(advMsg);
            findViewById(R.id.textViewAdvance).setVisibility(View.VISIBLE);

            //if it is not current month show settle button
            // Show previous month deductions/balance
            int totalDuePackets = mPacketsDueMap.get(currentMonth - 1);
            // final int totalPacketsInPrevMonth = getTotalDays(currentMonth - 1) * DEFAULT_PACKETS;
            String toDisplay;
            int totalAmt = 0;
            int totalDuePrice = 0;
            if (totalDuePackets > 0) {
                totalDuePrice = totalDuePackets * MILK_PACKET_PRICE;
                toDisplay = "Packets Less: " + Math.abs(totalDuePackets) + " X " + MILK_PACKET_PRICE
                        + " = " + totalDuePrice;
            } else if (totalDuePackets < 0) {
                totalDuePrice = totalDuePackets * MILK_PACKET_PRICE;
                toDisplay = "Packets Extra: " + Math.abs(totalDuePackets) + " X " + MILK_PACKET_PRICE
                        + " = " + +totalDuePrice;
            } else {
                toDisplay = "Packets Balance Nil";
            }
            totalAmt = advanceToPay - totalDuePrice;

            ((TextView) findViewById(R.id.textViewBalance))
                    .setText(toDisplay);
            ((TextView) findViewById(R.id.textViewBalance))
                    .setVisibility(View.VISIBLE);

            //show total amount
            final String totalAmount = "Total Amount: " + totalAmt;
            ((TextView) findViewById(R.id.totalAmount))
                    .setText(totalAmount);
            findViewById(R.id.totalAmount).setVisibility(View.VISIBLE);

            //show settle button
            findViewById(R.id.settle).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.textViewBalance).setVisibility(View.INVISIBLE);
            findViewById(R.id.textViewAdvance).setVisibility(View.INVISIBLE);
            findViewById(R.id.totalAmount).setVisibility(View.INVISIBLE);
            findViewById(R.id.settle).setVisibility(View.INVISIBLE);
        }
        showTotalPacketsBoughtInMonth(currentCalendar, currentMonth);

    }

    /**
     * show total packets bought for the month in the text area
     * @param currentCalendar current date's calendar
     * @param currentMonth current month(For eg., May - 5)
     */
    private void showTotalPacketsBoughtInMonth(Calendar currentCalendar, int currentMonth) {
        if (m_dateMilkAmountMap == null) {
            Log.e("NoData", "No Data Found");
            return;
        }
        //show totalPacketsBought
        Log.i("TotalPackets for Month:", String.valueOf(currentMonth));
        Years years = m_dateMilkAmountMap.getYears().get(YEAR_PREFIX + currentCalendar.get(Calendar.YEAR));
        Months month = years.getMonths().get(MONTH_PREFIX + currentMonth);
        if (month == null) {
            ((TextView) findViewById(R.id.textViewTotalPacketsBought))
                    .setVisibility(View.INVISIBLE);
            return;
        }
        Map<String, Integer> daysPacketMap = month.getDays();
        final int totalPacketsBought = getTotalPacketsBought(daysPacketMap);
        Log.i("TotalPackets:", String.valueOf(totalPacketsBought));
        ((TextView) findViewById(R.id.textViewTotalPacketsBought))
                .setText("TotalPacketsBought: " + totalPacketsBought);
        ((TextView) findViewById(R.id.textViewTotalPacketsBought))
                .setVisibility(View.VISIBLE);
    }

    /**
     * display packets bought of the current date
     * @param year year
     * @param dayOfMonth current day
     * @param month current month(For eg., May - 5)
     */
    private void displayPackets(int year, int dayOfMonth, int month) {
        String yearString = YEAR_PREFIX + year;
        String dayString = DAY_PREFIX + dayOfMonth;
        String monthString = MONTH_PREFIX + month;
        try {
            m_currentDate = new SimpleDateFormat("dd-MM-yyyy")
                    .parse(dayOfMonth + "-" + month + "-" + year);
            Date today = new Date(System.currentTimeMillis());
            ImageButton minusButton = (ImageButton) findViewById(R.id.imageButtonMinus); // get the reference of CalendarView
            ImageButton plusButton = (ImageButton) findViewById(R.id.imageButtonPlus); // get the reference of CalendarView
            ((TextView) findViewById(R.id.textViewPackets)).setText(String.valueOf(DEFAULT_PACKETS));
            boolean oldDate = m_dateMilkAmountMap != null &&
                    m_dateMilkAmountMap.getUnsettledMonths() != null &&
                    !m_dateMilkAmountMap.getUnsettledMonths().contains(monthString) &&
                    Calendar.getInstance().get(Calendar.MONTH) + 1 != month;
            if (m_currentDate.after(today) || oldDate) {
                minusButton.setVisibility(View.INVISIBLE);
                plusButton.setVisibility(View.INVISIBLE);
            } else {
                if (m_dateMilkAmountMap == null) {
                    Log.e("Oops No Internet", "Please check the Internet connection...");
                    return;
                }
                minusButton.setVisibility(View.VISIBLE);
                plusButton.setVisibility(View.VISIBLE);
                Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
                if (yearsMap.containsKey(yearString)) {
                    Years years = yearsMap.get(yearString);
                    Map<String, Months> monthsMap = years.getMonths();
                    if (monthsMap.containsKey(monthString)) {
                        Months months = monthsMap.get(monthString);
                        Map<String, Integer> daysMap = months.getDays();
                        if (daysMap.containsKey(dayString)) {
                            ((TextView) findViewById(R.id.textViewPackets))
                                    .setText(String.valueOf(daysMap.get(dayString)));
                            return;
                        }
                    }
                }
            }
        } catch (ParseException e) {
            Log.e("setOnDateChangeListener", e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Stop: ", "Stopping");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (m_dateMilkAmountMap != null) {
            m_myRef.child("years").setValue(m_dateMilkAmountMap.getYears());
        }
        Log.i("Pause: ", "Pausing");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_dateMilkAmountMap != null) {
            m_myRef.child("years").setValue(m_dateMilkAmountMap.getYears());
        }
        Log.i("Destroy: ", "Destroying");
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
                Log.i("YearsMap:", "" + yearsMap.get("" + calendar.get(Calendar.YEAR)));
                if (yearsMap.get("" + calendar.get(Calendar.YEAR)) instanceof java.util.Map) {
                    Log.i("YearsMap:", "" + yearsMap.get("" + calendar.get(Calendar.YEAR)));
                }

                if (yearsMap.containsKey("" + calendar.get(Calendar.YEAR))) {
                    Log.i("Debug:", calendar.get(Calendar.YEAR) + ":"
                            + (calendar.get(Calendar.MONTH) + 1) + ":" + calendar.get(Calendar.DAY_OF_MONTH));
                    Map<String, Object> monthsMap = (Map<String, Object>) yearsMap.get("" + calendar.get(Calendar.YEAR));
                    if (monthsMap.containsKey("" + (calendar.get(Calendar.MONTH) + 1))) {
                        if (monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1)) instanceof java.util.List) {
                            Log.i("Value:", "" + yearsMap.get("" + yearsMap.get("" + calendar.get(Calendar.YEAR))));
                        }
                        Log.i("MonthsMap:", monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1)) + "");
                        Map<String, Object> daysMap = (Map<String, Object>) monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1));
                        Log.i("DaysMap: ", daysMap.toString());
                        if (daysMap.containsKey("" + calendar.get(Calendar.DAY_OF_MONTH))) {
                            Log.i("ExistingValue:", dataSnapshot.getValue().toString()
                                    + "::" + daysMap.get("" + calendar.get(Calendar.DAY_OF_MONTH)) + "");
                            if (update) {
                                daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                                m_myRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                        .child("" + (calendar.get(Calendar.MONTH) + 1))
                                        .updateChildren(daysMap);
                            }
                        } else {
                            daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                            Log.i("NewValue:", dataSnapshot.getValue().toString() + "::" + yearsMap.toString() + "");
                            m_myRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                    .child("" + (calendar.get(Calendar.MONTH) + 1))
                                    .updateChildren(daysMap);
                        }
                        ((TextView) findViewById(R.id.textViewPackets)).setText(daysMap.get("" + calendar.get(Calendar.DAY_OF_MONTH)) + "");
                    } else {
                        Map<String, Object> daysMap = new HashMap<>();
                        daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                        monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1), daysMap);
                        m_myRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                .updateChildren(monthsMap);
                        Log.i("CreatingMonths", yearsMap.toString());
                    }
                } else {
                    Map<String, Object> daysMap = new HashMap<>();
                    daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                    Map<String, Object> monthsMap = new HashMap<>();
                    monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1), daysMap);
                    yearsMap.put("" + calendar.get(Calendar.YEAR), monthsMap);
                    m_myRef.child("years").updateChildren(yearsMap);
                    Log.i("CreatingYears", yearsMap.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * calculates total packets bought for the month and returns it
     * @param daysPacketMap days packets map of the month
     * @return total packets bought
     */
    public int getTotalPacketsBought(Map<String, Integer> daysPacketMap) {
        int totalPacketsBought = 0;
        for (int packet : daysPacketMap.values()) {
            totalPacketsBought += packet;
        }
        return totalPacketsBought;
    }

    class AccountItem {
        String mAccountName;
        public AccountItem(String accountName) {
            this.mAccountName = accountName;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<AccountItem> mAccountItems;

        public DrawerListAdapter(Context context, ArrayList<AccountItem> AccountItems) {
            mContext = context;
            mAccountItems = AccountItems;
        }

        @Override
        public int getCount() {
            return mAccountItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mAccountItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.account);

            titleView.setText( mAccountItems.get(position).mAccountName );

            return view;
        }
    }
}
