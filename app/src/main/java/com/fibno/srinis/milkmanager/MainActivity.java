package com.fibno.srinis.milkmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fibno.srinis.milkmanager.model.CloseAppActivity;
import com.fibno.srinis.milkmanager.model.MilkAccount;
import com.fibno.srinis.milkmanager.model.Months;
import com.fibno.srinis.milkmanager.model.Supplier;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.fibno.srinis.milkmanager.R.*;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private MilkAccount m_dateMilkAmountMap;
    private Map<String, MilkAccount> milkAccountMap = new HashMap<>();
    SparseIntArray mPacketsDueMap;
    private final FirebaseDatabase mFireDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference m_dateMilkAccountMapRef;
    private DatabaseReference m_supplierAccountsListRef;
    AlertDialog.Builder mAlertSettleBuilder;
    AlertDialog.Builder mAlertNotDatedBuilder;

    //drawer objects
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ArrayList<AccountItem> mAccountItems = new ArrayList<>();
    private Date mCurrentDate;

    //declaring constants
    private int DEFAULT_PACKETS = 2;
    private int SERVICE_CHARGE = 30;
    private static String YEAR_PREFIX = "Y";
    private static String MONTH_PREFIX = "M";
    private static String DAY_PREFIX = "D";
    private int mGridPosition;
    private List<String> mAccounts;
    private String mUserId;
    boolean doubleBackToExitPressedOnce = false;

    public void setGridPosition(int gridPosition) {
        mGridPosition = gridPosition;
    }

    public void setmCurrentDate(Date mCurrentDate) {
        this.mCurrentDate = mCurrentDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "InsideOncreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layout.activity_main);

        // DrawerLayout
        mDrawerLayout = findViewById(id.drawerLayout);

        // Populate the Navigtion Drawer with options
        mDrawerPane = findViewById(id.drawerPane);
        mDrawerList = findViewById(id.navList);

        cacheAccounts();
        TextView userName = findViewById(id.userName);
        userName.setText(mUserId);
        // drawer operation
        ImageButton addButton = findViewById(id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.i("add", "adddddd");
                showAddDialog();
            }
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder editDialog = new AlertDialog.Builder(this, style.AddAccountDialogStyle);
        editDialog.setTitle("Add account ");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(this);
        input.setHint("Enter Account Name");
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        input.setLayoutParams(lp);
        layout.addView(input);
        final EditText prize = new EditText(this);
        prize.setHint("Set Packet Prize");
//        lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        pwd.setLayoutParams(lp);
        layout.addView(prize);

        editDialog.setView(layout);
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        // add account name in accounts, MilkAccount key, account in MilkAccount
                             /*       Supplier supplier = new Supplier();

         m_dateMilkAccountMapRef = mFireDatabase.getReference("Supplier");

         Months months = new Months();
         Map<String, Integer> daysMap = new HashMap<>();
         daysMap.put("D1",2);
         months.setDays(daysMap);
         months.setMonth(8);
         Years years = new Years();
         Map<String, Months> monthsMap = new HashMap<>();
         monthsMap.put("M8",months);
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
         m_dateMilkAccountMapRef.setValue(accountMap);*/
                        String accountName = input.getText().toString();
                        Months months = new Months();
                        Map<String, Integer> daysMap = new HashMap<>();
                        daysMap.put(DAY_PREFIX + getCurrentCalendar().get(Calendar.DAY_OF_MONTH),0);
                        months.setDays(daysMap);
                        months.setMonth(getCurrentCalendar().get(Calendar.MONTH ) + 1);
                        Years years = new Years();
                        Map<String, Months> monthsMap = new HashMap<>();
                        monthsMap.put(MONTH_PREFIX + (getCurrentCalendar().get(Calendar.MONTH) + 1), months);
                        years.setMonths(monthsMap);
                        years.setYear(getCurrentCalendar().get(Calendar.YEAR));
                        MilkAccount accounts = new MilkAccount();
                        Map<String, Years> yearsMap = new HashMap<>();

                        yearsMap.put(YEAR_PREFIX + getCurrentCalendar().get(Calendar.YEAR), years);
                        accounts.setYears(yearsMap);
                        accounts.setAccount(accountName);
                        milkAccountMap.put(accountName, accounts);
                        mAccountItems.add(new AccountItem(accountName));
                        ((DrawerListAdapter)mDrawerList.getAdapter()).updateList(mAccountItems);
                        Map<String, MilkAccount> accountMap = new HashMap<>();
                        accountMap.put(accountName, accounts);
                        m_supplierAccountsListRef.child("accounts").setValue(milkAccountMap);
                        break;
                }
            }
        };
        editDialog.setPositiveButton("Save", dialogOnClickListener);
        editDialog.setNegativeButton("Cancel", dialogOnClickListener);
        editDialog.show();
    }

    private void createSettleAlertDialog() {
        mAlertSettleBuilder = new AlertDialog.Builder(this, style.SettleAlertDialogStyle);
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
                        if ( unsettledMonths == null || unsettledMonths.isEmpty() ) {
                            Log.i("AlertDialog: ", "Unsettled months is empty");
                            return;
                        }
                        unsettledMonths.remove(MONTH_PREFIX + calendar.get(Calendar.MONTH));
                        m_dateMilkAmountMap.setUnsettledMonths(unsettledMonths);
                        Log.i("UnsettledMonths:::", m_dateMilkAmountMap.getUnsettledMonths().toString());
                        findViewById(id.textViewBalance).setVisibility(View.INVISIBLE);
                        findViewById(id.textViewAdvance).setVisibility(View.INVISIBLE);
                        findViewById(id.totalAmount).setVisibility(View.INVISIBLE);
                        findViewById(id.settle).setVisibility(View.INVISIBLE);
                        mPacketsDueMap.delete(calendar.get(Calendar.MONTH));
                        m_dateMilkAccountMapRef.setValue(m_dateMilkAmountMap);
                        break;
                }
            }
        };
        mAlertSettleBuilder.setPositiveButton("Yes", dialogOnClickListener);
        mAlertSettleBuilder.setNegativeButton("No", dialogOnClickListener);
    }

    private void invokeButtonListeners() {
        ImageButton plusButton = findViewById(id.imageButtonPlus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv1 = findViewById(id.textViewPackets);
                int currentPackets = Integer.parseInt(tv1.getText().toString());
                if ( currentPackets < 10 ) {
                    tv1.setText(++currentPackets + "");
                    Log.i("CurrentValue: ", currentPackets + "");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mCurrentDate);
                    updatePackets(currentPackets, YEAR_PREFIX + calendar.get(Calendar.YEAR)
                            , DAY_PREFIX + calendar.get(Calendar.DAY_OF_MONTH), MONTH_PREFIX + (calendar.get(Calendar.MONTH) + 1));
                    CalendarCustomView mView = findViewById(id.custom_calendar);
                    mView.setUpCalendarAdapter(mGridPosition);
                    Log.i("PLUSS", "Gridddpsosss " + mGridPosition);
                }
            }
        });

        ImageButton minusButton = findViewById(id.imageButtonMinus);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv1 = findViewById(id.textViewPackets);
                int currentPackets = Integer.parseInt(tv1.getText().toString());
                if ( currentPackets > 0 ) {
                    tv1.setText(--currentPackets + "");
                    Log.i("CurrentValue: ", currentPackets + "");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mCurrentDate);
                    updatePackets(currentPackets, YEAR_PREFIX + calendar.get(Calendar.YEAR)
                            , DAY_PREFIX + calendar.get(Calendar.DAY_OF_MONTH), MONTH_PREFIX + (calendar.get(Calendar.MONTH) + 1));
                    CalendarCustomView mView = findViewById(id.custom_calendar);
                    mView.setUpCalendarAdapter();
                    GridView calendarGridView = mView.findViewById(id.calendar_grid);
                    mView.setUpCalendarAdapter(mGridPosition);
                    Log.i("PLUSS", "Gridddpsosss " + mGridPosition + " : " + calendarGridView.getAdapter().getItem(mGridPosition).getClass());
                }
            }

        });

        FloatingActionButton settleButton = findViewById(id.settle);
        settleButton.setVisibility(View.INVISIBLE);
        settleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAlertSettleBuilder.show();
            }
        });

    }

    private void cacheAccounts() {
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        mUserId = intent.getStringExtra(LauncherActivity.EXTRA_MESSAGE);
        String key = intent.getStringExtra(LauncherActivity.EXTRA_MESSAGE_KEY);

        m_supplierAccountsListRef = FirebaseDatabase.getInstance()
                .getReference("Supplier").child(key).child(mUserId);
        Log.i("LLLL" , "" + m_supplierAccountsListRef.getDatabase());
        m_supplierAccountsListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //mAccounts = (ArrayList<String>) dataSnapshot.child("accounts").getValue();
                Log.i(",ccccc,", " " + dataSnapshot.child("accounts").getChildrenCount());
                mAccounts = new ArrayList<>();

                Iterator<DataSnapshot> itr = dataSnapshot.child("accounts").getChildren().iterator();
                while(itr.hasNext()) {
                    DataSnapshot i = itr.next();
                    if ( i.getValue() instanceof HashMap) {
                        mAccounts.add(i.getKey());
                        milkAccountMap.put(i.getKey(), i.getValue(MilkAccount.class));
                        Log.i(",iiiii,", " " + i.getValue().getClass() + " "
                                + i.getValue());
                    }
                }

                Log.i("pppppp,", " " + mAccounts);
                for(String account : mAccounts) {
                    mAccountItems.add(new AccountItem(account));
                }
                DrawerListAdapter adapter = new DrawerListAdapter(MainActivity.this, mAccountItems);
                mDrawerList.setAdapter(adapter);

                // Drawer Item click listeners
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long iid) {
                        String accountName = ((AccountItem) mDrawerList.getItemAtPosition(position)).mAccountName;
                        Log.i(TAG, "onItemClick: " + accountName);
                        CalendarCustomView mView = findViewById(id.custom_calendar);
                        TextView accountNameView = mView.findViewById(id.account_name);
                        accountNameView.setText(accountName);
                        if ( m_dateMilkAmountMap != null ) {
                            Log.i("onItemClick", "UpdatePreviousAccount" + m_dateMilkAmountMap);
                            //m_supplierAccountsListRef.child("accounts").setValue(milkAccountMap);
                            m_dateMilkAccountMapRef.child("years").setValue(m_dateMilkAmountMap.getYears());
                        }
                        m_dateMilkAccountMapRef = m_supplierAccountsListRef.child("accounts").child(accountName).getRef();
                        m_dateMilkAmountMap = milkAccountMap.get(accountName);
                        Log.i("oooododd", m_dateMilkAmountMap.toString());
                        loadCurrentDate();
                        invokeButtonListeners();
                        showTotalPacketsBoughtInMonth(Calendar.getInstance(),
                                Calendar.getInstance().get(Calendar.MONTH) + 1);
                        checkDue();
                        showSettlement(Calendar.getInstance().get(Calendar.MONTH) + 1);
                        mView.setUpCalendarAdapter();
                        //cacheDBData(accountName);
                        createSettleAlertDialog();
                        mDrawerLayout.closeDrawer(mDrawerPane);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        Log.i("DEED", mAccounts.toString());
    }
    /**
     * cached FireBase DB reference to local POJO
     *
     * @param accountName account name
     */
    private void cacheDBData(String accountName) {

 /*       Supplier supplier = new Supplier();

         m_dateMilkAccountMapRef = mFireDatabase.getReference("Supplier");

         Months months = new Months();
         Map<String, Integer> daysMap = new HashMap<>();
         daysMap.put("D1",2);
         months.setDays(daysMap);
         months.setMonth(8);
         Years years = new Years();
         Map<String, Months> monthsMap = new HashMap<>();
         monthsMap.put("M8",months);
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
         m_dateMilkAccountMapRef.setValue(accountMap);*/
        m_dateMilkAccountMapRef = m_supplierAccountsListRef.child("accounts").child(accountName).getRef();

        //System.out.println("--------"+m_dateMilkAccountMapRef.child("D3502"));
        //DatabaseReference ref = m_dateMilkAccountMapRef.child("D3502").getRef();
        m_dateMilkAccountMapRef.addListenerForSingleValueEvent(attachValueEventListener());
        //mFireDatabase.setPersistenceEnabled(true);
        /*m_dateMilkAccountMapRef = mFireDatabase.getReference();
        m_dateMilkAccountMapRef.keepSynced(true);
        m_dateMilkAccountMapRef.addListenerForSingleValueEvent(attachValueEventListener());
        if (m_dateMilkAmountMap == null) {
            Log.i("CheckNull", "Yes");
        }*/
    }

    public MilkAccount getDateMilkAmountMap() {
        return m_dateMilkAmountMap;
    }

    private ValueEventListener attachValueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                m_dateMilkAmountMap =
                        dataSnapshot.getValue(MilkAccount.class);
                if ( m_dateMilkAmountMap != null )
                    Log.i("YearsMap:", m_dateMilkAmountMap.toString());
                loadCurrentDate();
                showTotalPacketsBoughtInMonth(Calendar.getInstance(),
                        Calendar.getInstance().get(Calendar.MONTH) + 1);
                checkDue();
                showSettlement(Calendar.getInstance().get(Calendar.MONTH) + 1);
                CalendarCustomView mView = findViewById(id.custom_calendar);
                mView.setUpCalendarAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    /**
     * calculates the due with the previous unsettled months and calculates the
     * balance
     */
    public void checkDue() {
        List<String> unsettledMonths = m_dateMilkAmountMap.getUnsettledMonths();
        if ( unsettledMonths == null ) {
            Log.i("Checking Due...", "No dues");
            return;
        }
        Calendar calendar = getCurrentCalendar();
        Years years = m_dateMilkAmountMap.getYears().get(YEAR_PREFIX + calendar.get(Calendar.YEAR));
        mPacketsDueMap = new SparseIntArray();
/**if(unsettledMonths == null || unsettledMonths.isEmpty()) {
 unsettledMonths = new ArrayList<>();
 unsettledMonths.add("M5");
 m_dateMilkAmountMap.setUnsettledMonths(unsettledMonths);
 m_dateMilkAccountMapRef.setValue(m_dateMilkAmountMap);
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
     * @param prevMonth       previous month in String(For eg., May - M5)
     * @param unsettledMonths list of unsettled months in String
     */
    private void appendPreviousMonthToUnsettledMonths(String prevMonth, List<String> unsettledMonths) {
        if ( !unsettledMonths.contains(prevMonth) ) {
            unsettledMonths.add(prevMonth);
            m_dateMilkAmountMap.setUnsettledMonths(unsettledMonths);
            m_dateMilkAccountMapRef.setValue(m_dateMilkAmountMap);
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
    public void loadCurrentDate() {
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
     * @param daysMap    daysmap of the month
     * @param dayOfMonth today
     * @param year       year in String
     * @param month      month in String
     */
    private void updateNotDatedDays(Map<String, Integer> daysMap, int dayOfMonth, String year, String month) {
        if ( daysMap.containsKey(DAY_PREFIX + dayOfMonth) ) {
            return;
        }
        boolean foundStartDate = false;
        int i = dayOfMonth - 1;
        for (; i > 0; i--) {
            Log.i("IIIDaysMap", String.valueOf(i));
            if ( daysMap.containsKey(DAY_PREFIX + i) ) {
                mAlertNotDatedBuilder.setMessage("You have not updated after " + i);
                mAlertNotDatedBuilder.show();
                foundStartDate = true;
                break;
            }
            daysMap.put(DAY_PREFIX + i, DEFAULT_PACKETS);
        }
        if ( i == dayOfMonth - 1 ) {
            return;
        }
        Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
        Years years = yearsMap.get(year);
        Map<String, Months> monthsMap = years.getMonths();
        Months months = monthsMap.get(month);
        months.setDays(daysMap);

        if ( foundStartDate ) {
            return;
        }

        // first verify days in previous unsettled months are updated
        List<String> unsettledMonths = m_dateMilkAmountMap.getUnsettledMonths();
        if ( unsettledMonths.isEmpty() ) {
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
            if ( day != -1 ) {
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
     * @param unSettledMonth unsettle month in int
     * @param year           year in string(2018)
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
            if ( prevDaysMap.containsKey(DAY_PREFIX + i) ) {
                day = i;
                break;
            }
            prevDaysMap.put(DAY_PREFIX + i, DEFAULT_PACKETS);
        }
        if ( i != totalDays ) {
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
     * @param year  year in String
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
        mCurrentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mCurrentDate);
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
        Log.i("Resume: ", "Resuming" + mCurrentDate);
    }

    /**
     * updates packets & dates, populates unsettled months in the local cache &
     * display packets in the UI
     *
     * @param packets    total packets bought
     * @param year       year in String
     * @param dayOfMonth day in String
     * @param month      month in String
     */
    private void updatePackets(int packets, String year, String dayOfMonth, String month) {
        if ( m_dateMilkAmountMap == null ) {
            Log.e("NoData", "No Data Found");
            return;
        }
        Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
        if ( !yearsMap.containsKey(year) ) {
            Years years = new Years();
            Map<String, Months> monthsMap = new HashMap<>();
            monthsMap.put(month, populateMonths(dayOfMonth, DEFAULT_PACKETS));
            years.setMonths(monthsMap);
            yearsMap.put(year, years);
        } else {
            Years years = yearsMap.get(year);
            Map<String, Months> monthsMap = years.getMonths();
            if ( !monthsMap.containsKey(month) ) {
                monthsMap.put(month, populateMonths(dayOfMonth, DEFAULT_PACKETS));
                populateUnsettledMonths(month, monthsMap);
            } else {
                Months months = monthsMap.get(month);
                Map<String, Integer> daysMap = months.getDays();
                if ( !daysMap.containsKey(dayOfMonth) || daysMap.get(dayOfMonth) != packets ) {
                    daysMap.put(dayOfMonth, packets);
                }
            }
        }
        ((TextView) findViewById(id.textViewPackets)).setText(String.valueOf(packets));
    }

    /**
     * This method will be called when new month is generated.
     * It populates unsettled month list. If previous month is not present in the list & has due,
     * then new unsettled months list will be generated.
     *
     * @param month     month in String
     * @param monthsMap months map
     */
    private void populateUnsettledMonths(String month, Map<String, Months> monthsMap) {
        String prevMonth = MONTH_PREFIX + (Integer.parseInt(month.substring(1)) - 1);
        if ( monthsMap.containsKey(prevMonth) ) {
            List<String> unsettledMonths = m_dateMilkAmountMap.getUnsettledMonths();
            if ( unsettledMonths == null ) {
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

    /**
     * updates the view of the UI with all UI elements of the current month
     *
     * @param currentMonth current month(For eg., May means 5)
     */
    public void showSettlement(int currentMonth) {
        int milkPacketPrice = 20;
        if ( mPacketsDueMap == null ) {
            findViewById(id.textViewBalance).setVisibility(View.INVISIBLE);
            findViewById(id.textViewAdvance).setVisibility(View.INVISIBLE);
            findViewById(id.totalAmount).setVisibility(View.INVISIBLE);
            findViewById(id.settle).setVisibility(View.INVISIBLE);
            return;
        }
        int totalDuePackets = 0;
        int advanceDue = 0;
        StringBuilder advMsg = new StringBuilder("");
        for(int i = 0 ; i < mPacketsDueMap.size(); i++) {
            final int month = mPacketsDueMap.keyAt(i);
            Log.i("SummingUp Packets Due", "Packets Due for month: " + month + " : "
            + mPacketsDueMap.valueAt(i));
            totalDuePackets += mPacketsDueMap.valueAt(i);
            int advance = getTotalDays(month+1) * DEFAULT_PACKETS * milkPacketPrice;
            advanceDue += advance;
            advMsg.append("AdvanceToPay for month ").append(month + 1).append(": ").append(getTotalDays(month+1))
                    .append(" X ").append(DEFAULT_PACKETS).append(" X ").append(milkPacketPrice).append(" = ").append(advance).append("\n");
        }
        //check if it is current month
        if ( totalDuePackets != 0 ) {
            // show advance to pay

            ((TextView) findViewById(id.textViewAdvance))
                    .setText(advMsg);
            findViewById(id.textViewAdvance).setVisibility(View.VISIBLE);

            //if it is not current month show settle button
            // Show previous month deductions/balance
            // final int totalPacketsInPrevMonth = getTotalDays(currentMonth - 1) * DEFAULT_PACKETS;
            String toDisplay;
            int totalAmt;
            int totalDuePrice = 0;
            if ( totalDuePackets > 0 ) {
                totalDuePrice = totalDuePackets * milkPacketPrice;
                toDisplay = "Packets Less: " + Math.abs(totalDuePackets) + " X " + milkPacketPrice
                        + " = " + totalDuePrice;
            } else if ( totalDuePackets < 0 ) {
                totalDuePrice = totalDuePackets * milkPacketPrice;
                toDisplay = "Packets Extra: " + Math.abs(totalDuePackets) + " X " + milkPacketPrice
                        + " = " + +totalDuePrice;
            } else {
                toDisplay = "Packets Balance Nil";
            }
            totalAmt = advanceDue - totalDuePrice;

            ((TextView) findViewById(id.textViewBalance)).setText(toDisplay);
            (findViewById(id.textViewBalance)).setVisibility(View.VISIBLE);

            //show total amount
            final String totalAmount = new StringBuilder().append("Total Amount: ").
                    append(totalAmt).toString();
            ((TextView) findViewById(id.totalAmount))
                    .setText(totalAmount);
            findViewById(id.totalAmount).setVisibility(View.VISIBLE);

            //show settle button
            findViewById(id.settle).setVisibility(View.VISIBLE);
//            Intent i=new Intent(android.content.Intent.ACTION_SEND);
//            i.setType("text/plain");
//            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Subject test");
//            i.putExtra(android.content.Intent.EXTRA_TEXT, "extra text that you want to put");
//            startActivity(Intent.createChooser(i,"Share via"));
        } else {
            findViewById(id.textViewBalance).setVisibility(View.INVISIBLE);
            findViewById(id.textViewAdvance).setVisibility(View.INVISIBLE);
            findViewById(id.totalAmount).setVisibility(View.INVISIBLE);
            findViewById(id.settle).setVisibility(View.INVISIBLE);
        }
        showTotalPacketsBoughtInMonth(Calendar.getInstance(), currentMonth);

    }

    /**
     * show total packets bought for the month in the text area
     *
     * @param currentCalendar current date's calendar
     * @param currentMonth    current month(For eg., May - 5)
     */
    public void showTotalPacketsBoughtInMonth(Calendar currentCalendar, int currentMonth) {
        if ( m_dateMilkAmountMap == null ) {
            Log.e("NoData", "No Data Found");
            return;
        }
        //show totalPacketsBought
        Log.i("TotalPackets for Month:", String.valueOf(currentMonth));
        Years years = m_dateMilkAmountMap.getYears().get(YEAR_PREFIX + currentCalendar.get(Calendar.YEAR));
        Months month = years.getMonths().get(MONTH_PREFIX + currentMonth);
        if ( month == null ) {
            (findViewById(id.textViewTotalPacketsBought)).setVisibility(View.INVISIBLE);
            return;
        }
        Map<String, Integer> daysPacketMap = month.getDays();
        final int totalPacketsBought = getTotalPacketsBought(daysPacketMap);
        Log.i("TotalPackets:", String.valueOf(totalPacketsBought));
        ((TextView) findViewById(id.textViewTotalPacketsBought))
                .setText("TotalPacketsBought: " + totalPacketsBought);
        (findViewById(id.textViewTotalPacketsBought)).setVisibility(View.VISIBLE);
    }

    /**
     * display packets bought of the current date
     *
     * @param year       year
     * @param dayOfMonth current day
     * @param month      current month(For eg., May - 5)
     */
    public int getPackets(int year, int dayOfMonth, int month) {
        String yearString = YEAR_PREFIX + year;
        String dayString = DAY_PREFIX + dayOfMonth;
        String monthString = MONTH_PREFIX + month;
        Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();
        if ( yearsMap.containsKey(yearString) ) {
            Years years = yearsMap.get(yearString);
            Map<String, Months> monthsMap = years.getMonths();
            Log.i("MONTHDebugddd", monthString);
            if ( monthsMap.containsKey(monthString) ) {
                Months months = monthsMap.get(monthString);
                Map<String, Integer> daysMap = months.getDays();

                if ( daysMap.containsKey(dayString) ) {
                    Log.i("Debugddd", dayString);
                    return daysMap.get(dayString);
                }
            }
        }
        return -1;

    }

    /**
     * display packets bought of the current date
     *
     * @param year       year
     * @param dayOfMonth current day
     * @param month      current month(For eg., May - 5)
     */
    public void displayPackets(int year, int dayOfMonth, int month) {
        String yearString = YEAR_PREFIX + year;
        String dayString = DAY_PREFIX + dayOfMonth;
        String monthString = MONTH_PREFIX + month;
        try {
            mCurrentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .parse(dayOfMonth + "-" + month + "-" + year);
            Date today = new Date(System.currentTimeMillis());
            ImageButton minusButton = findViewById(id.imageButtonMinus);
            ImageButton plusButton = findViewById(id.imageButtonPlus);
            ((TextView) findViewById(id.textViewPackets))
                    .setText(String.valueOf(DEFAULT_PACKETS));
            boolean oldDate = m_dateMilkAmountMap != null &&
                    (m_dateMilkAmountMap.getUnsettledMonths() == null ||
                            !m_dateMilkAmountMap.getUnsettledMonths().contains(monthString)) &&
                    Calendar.getInstance().get(Calendar.MONTH) + 1 != month;
            if ( mCurrentDate.after(today) || oldDate ) {
                minusButton.setVisibility(View.INVISIBLE);
                plusButton.setVisibility(View.INVISIBLE);
            } else {
                if ( m_dateMilkAmountMap == null ) {
                    Log.e("Oops No Internet", "Please check the Internet connection...");
                    return;
                }
                minusButton.setVisibility(View.VISIBLE);
                plusButton.setVisibility(View.VISIBLE);
                Map<String, Years> yearsMap = m_dateMilkAmountMap.getYears();

                if ( yearsMap.containsKey(yearString) ) {
                    Years years = yearsMap.get(yearString);
                    Map<String, Months> monthsMap = years.getMonths();

                    if ( monthsMap.containsKey(monthString) ) {
                        Months months = monthsMap.get(monthString);
                        Map<String, Integer> daysMap = months.getDays();

                        if ( daysMap.containsKey(dayString) ) {
                            ((TextView) findViewById(id.textViewPackets))
                                    .setText(String.valueOf(daysMap.get(dayString)));
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
//        if(milkAccountMap != null) {
//            m_supplierAccountsListRef.child("accounts").setValue(milkAccountMap);
//        }
        if ( m_dateMilkAmountMap != null ) {
            m_dateMilkAccountMapRef.child("years").setValue(m_dateMilkAmountMap.getYears());
        }
        Log.i("Pause: ", "Pausing");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(milkAccountMap != null) {
//            m_supplierAccountsListRef.child("accounts").setValue(milkAccountMap);
//        }
        if ( m_dateMilkAmountMap != null ) {
            m_dateMilkAccountMapRef.child("years").setValue(m_dateMilkAmountMap.getYears());
        }
        Log.i("Destroy: ", "Destroying");
    }

    private void updateDBReference(final int packets, final boolean update,
                                   final String month, final String year) {
        /** Log.i("Root:",m_dateMilkAccountMapRef.getRoot().toString());
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(mCurrentDate);
         Map<String, Object> daysMap = new HashMap<>();
         daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH) ,packets);
         Map<String, Object> monthsMap = new HashMap<>();
         monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1) ,daysMap);
         Map<String, Object> yearsMap = new HashMap<>();
         yearsMap.put("" + calendar.get(Calendar.YEAR), monthsMap);

         Map<String, Object> accountsMap = new HashMap<>();
         accountsMap.put("years",yearsMap);
         m_dateMilkAccountMapRef.setValue(accountsMap);**/

        m_dateMilkAccountMapRef.child("years").child(year).child(month).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> yearsMap = (Map<String, Object>) dataSnapshot.getValue();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mCurrentDate);
                if ( yearsMap == null || yearsMap.get("" + calendar.get(Calendar.YEAR)) == null
                        || !(yearsMap.get("" + calendar.get(Calendar.YEAR)) instanceof java.util.Map) ) {
                    return;
                }
                Log.i("YearsMap:", "" + yearsMap.get("" + calendar.get(Calendar.YEAR)));
                if ( yearsMap.containsKey("" + calendar.get(Calendar.YEAR)) ) {
                    Log.i("Debug:", calendar.get(Calendar.YEAR) + ":"
                            + (calendar.get(Calendar.MONTH) + 1) + ":" + calendar.get(Calendar.DAY_OF_MONTH));
                    Map<String, Object> monthsMap = (Map<String, Object>) yearsMap.get("" + calendar.get(Calendar.YEAR));
                    if ( monthsMap.containsKey("" + (calendar.get(Calendar.MONTH) + 1)) ) {
                        if ( monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1)) instanceof java.util.List ) {
                            Log.i("Value:", "" + yearsMap.get("" + yearsMap.get("" + calendar.get(Calendar.YEAR))));
                        }
                        Log.i("MonthsMap:", monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1)) + "");
                        Map<String, Object> daysMap = (Map<String, Object>) monthsMap.get("" + (calendar.get(Calendar.MONTH) + 1));
                        Log.i("DaysMap: ", daysMap.toString());
                        if ( daysMap.containsKey("" + calendar.get(Calendar.DAY_OF_MONTH)) ) {
                            Log.i("ExistingValue:", Objects
                                    .requireNonNull(dataSnapshot.getValue()).toString()
                                    + "::" + daysMap.get("" + calendar.get(Calendar.DAY_OF_MONTH)) + "");
                            if ( update ) {
                                daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                                m_dateMilkAccountMapRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                        .child("" + (calendar.get(Calendar.MONTH) + 1))
                                        .updateChildren(daysMap);
                            }
                        } else {
                            daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                            Log.i("NewValue:", Objects.requireNonNull(dataSnapshot.getValue()).toString() + "::" + yearsMap.toString() + "");
                            m_dateMilkAccountMapRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                    .child("" + (calendar.get(Calendar.MONTH) + 1))
                                    .updateChildren(daysMap);
                        }
                        ((TextView) findViewById(id.textViewPackets))
                                .setText(daysMap.get("" + calendar.get(Calendar.DAY_OF_MONTH)) + "");
                    } else {
                        Map<String, Object> daysMap = new HashMap<>();
                        daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                        monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1), daysMap);
                        m_dateMilkAccountMapRef.child("years").child("" + calendar.get(Calendar.YEAR))
                                .updateChildren(monthsMap);
                        Log.i("CreatingMonths", yearsMap.toString());
                    }
                } else {
                    Map<String, Object> daysMap = new HashMap<>();
                    daysMap.put("" + calendar.get(Calendar.DAY_OF_MONTH), packets);
                    Map<String, Object> monthsMap = new HashMap<>();
                    monthsMap.put("" + (calendar.get(Calendar.MONTH) + 1), daysMap);
                    yearsMap.put("" + calendar.get(Calendar.YEAR), monthsMap);
                    m_dateMilkAccountMapRef.child("years").updateChildren(yearsMap);
                    Log.i("CreatingYears", yearsMap.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * calculates total packets bought for the month and returns it
     *
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

        private AccountItem(String accountName) {
            this.mAccountName = accountName;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<AccountItem> mAccountItems;
        String mCurrentAccountName;
        String mEditDialogAccountName;

        private DrawerListAdapter(Context context, ArrayList<AccountItem> AccountItems) {
            mContext = context;
            mAccountItems = AccountItems;
        }

        public void updateList(ArrayList<AccountItem> AccountItems) {
            mAccountItems = AccountItems;
            notifyDataSetChanged();
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

            if ( convertView == null ) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                view = inflater.inflate(layout.drawer_item, null);
            } else {
                view = convertView;
            }

            TextView titleView = view.findViewById(id.account);
            Log.i("ppppp", "possi" + position + ":" + mAccountItems.get(position).mAccountName);
            mCurrentAccountName = mAccountItems.get(position).mAccountName;
            titleView.setText(mCurrentAccountName);

            ImageButton editButton = view.findViewById(id.editButton);
            editButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    View parentRow = (View) view.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);

                    mCurrentAccountName = mAccountItems.get(position).mAccountName;
                    Log.i("edit", "editeeee" + position);
                    showEditDialog();
                }
            });

            ImageButton deleteButton = view.findViewById(id.deleteButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    View parentRow = (View) view.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);
                    mCurrentAccountName = mAccountItems.get(position).mAccountName;
                    Log.i("delete", "delete" + position);
                    showDeleteDialog();
                }
            });
            return view;
        }

        private void showDeleteDialog() {
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(mContext, style.DeleteAccountDialogStyle);
            deleteDialog.setTitle("Delete account " + mCurrentAccountName);
            deleteDialog.setMessage("Are you sure you want to delete the account?");
            DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                        case DialogInterface.BUTTON_POSITIVE:
                            // delete account name in accounts, MilkAccount key, account in MilkAccount
                            milkAccountMap.remove(mCurrentAccountName);
                            for(int i = 0; i < mAccountItems.size(); i++) {
                                if( mAccountItems.get(i).mAccountName.equals(mCurrentAccountName)) {
                                    mAccountItems.remove(i);
                                    break;
                                }
                            }
                            ((DrawerListAdapter)mDrawerList.getAdapter()).updateList(mAccountItems);
                            m_supplierAccountsListRef.child("accounts").setValue(milkAccountMap);
                            break;
                    }
                }
            };
            deleteDialog.setPositiveButton("Delete", dialogOnClickListener);
            deleteDialog.setNegativeButton("Cancel", dialogOnClickListener);
            deleteDialog.show();
        }

        private void showEditDialog() {
            AlertDialog.Builder editDialog = new AlertDialog.Builder(mContext, style.EditAccountDialogStyle);
            editDialog.setTitle("Editing account " + mCurrentAccountName);
            mEditDialogAccountName = mCurrentAccountName;
            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            editDialog.setView(input);
            Log.i("bbbedited", "Editedd account " + mCurrentAccountName);

            DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_NEGATIVE:
                            Log.i("ccccedited", "Editedd account " + mEditDialogAccountName);
                            dialog.dismiss();
                            break;
                        case DialogInterface.BUTTON_POSITIVE:
                            // update account name in accounts, MilkAccount key, account in MilkAccount
                            String accountName = input.getText().toString();
                            Log.i("edited", "Editedd account " + mEditDialogAccountName);
                            MilkAccount accounts = milkAccountMap.get(mEditDialogAccountName);
                            accounts.setAccount(accountName);
                            milkAccountMap.remove(mEditDialogAccountName);
                            for(int i = 0; i < mAccountItems.size(); i++) {
                                if( mAccountItems.get(i).mAccountName.equals(mEditDialogAccountName)) {
                                    mAccountItems.remove(i);
                                    break;
                                }
                            }
                            milkAccountMap.put(accountName, accounts);
                            mAccountItems.add(new AccountItem(accountName));
                            ((DrawerListAdapter)mDrawerList.getAdapter()).updateList(mAccountItems);
                            m_supplierAccountsListRef.child("accounts").setValue(milkAccountMap);
                            break;
                    }
                }
            };
            editDialog.setPositiveButton("Save", dialogOnClickListener);
            editDialog.setNegativeButton("Cancel", dialogOnClickListener);
            editDialog.show();
        }
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Intent intent = new Intent(this, CloseAppActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
