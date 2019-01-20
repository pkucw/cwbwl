package com.example.dell.cwbwl;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.dell.cwbwl.database.MyDB;
import com.example.dell.cwbwl.enity.Record;
import com.example.dell.cwbwl.util.DateFormatType;
import com.example.dell.cwbwl.util.MyFormat;
import com.example.dell.cwbwl.util.MyTimeGetter;

import static com.example.dell.cwbwl.util.MyFormat.getTimeStr;
import static com.example.dell.cwbwl.util.MyFormat.myDateFormat;


@RequiresApi(api = Build.VERSION_CODES.N)
public class AmendActivity extends BaseActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{

    private final static String TAG = "AmendActivity";

    MyDB myDB;
    private Button btnSave;
    private Button btnBack;
    private TextView amendTime;
    private TextView amendTitle;
    private EditText amendBody;
    private Record record;
    private AlertDialog.Builder dialog;

    private DatePickerDialog dialogDate;
    private TimePickerDialog dialogTime;

    private Button btnUpcoming;
    private Button btnNotice;


    private Integer year;
    private Integer month;
    private Integer dayOfMonth;
    private Integer hour;
    private Integer minute;
    private boolean timeSetTag;

    MyTimeGetter myTimeGetter;
//    private TextView editTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.amend_linear_layout);
        init();

    }


    @Override
    public void onClick(View v) {
        String body;
        body = amendBody.getText().toString();
        switch (v.getId()){
            case R.id.button_save:
                if (updateFunction(body)){
                    intentStart();
                }
                break;
            case R.id.button_back:
                showDialog(body);
                clearDialog();
                break;
            case R.id.btn_amend_menu_notice:
                if (timeSetTag){
                    //  已经设置过提醒时间，询问是否修改
                    showAskDialog();
                } else {
                    setNoticeDate();
                }
                break;
            case R.id.btn_amend_menu_upcoming:
                Log.i("AmendActivity","这是修改页面的待办按钮被点击");
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //当返回按键被按下
            if (!isShowIng()){
                showDialog(amendBody.getText().toString());
                clearDialog();
            }
        }
        return false;
    }

    /*
     * 初始化函数
     */
    @SuppressLint("SetTextI18n")
    void init(){
        myDB = new MyDB(this);
        btnBack = findViewById(R.id.button_back);
        btnSave = findViewById(R.id.button_save);
        amendTitle = findViewById(R.id.amend_title);
        amendBody = findViewById(R.id.amend_body);
        amendTime = findViewById(R.id.amend_title_time);

        btnUpcoming = findViewById(R.id.btn_amend_menu_upcoming);
        btnNotice = findViewById(R.id.btn_amend_menu_notice);

        btnSave.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        btnNotice.setOnClickListener(this);
        btnUpcoming.setOnClickListener(this);

        Intent intent = this.getIntent();
        if (intent!=null){

            record = new Record();

            record.setId(Integer.valueOf(intent.getStringExtra(MyDB.RECORD_ID)));
            record.setTitleName(intent.getStringExtra(MyDB.RECORD_TITLE));
            record.setTextBody(intent.getStringExtra(MyDB.RECORD_BODY));
            record.setCreateTime(intent.getStringExtra(MyDB.RECORD_TIME));
            record.setNoticeTime(intent.getStringExtra(MyDB.NOTICE_TIME));

            amendTitle.setText(record.getTitleName());
            String str="";
            if (record.getNoticeTime()!=null){
                str = "    提醒时间："+record.getNoticeTime();
            }
            amendTime.setText(record.getCreateTime()+str);
            amendBody.setText(record.getTextBody());
        }
    }

    /*
     * 返回主界面
     */
    void intentStart(){
        Intent intent = new Intent(AmendActivity.this,MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    /*
     * 保存函数
     */
    boolean updateFunction(String body){

        SQLiteDatabase db;
        ContentValues values;

        boolean flag = true;
        if (body.length()>200){
            Toast.makeText(this,"内容过长",Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if(flag){
            // update
            db = myDB.getWritableDatabase();
            values = new ContentValues();
            values.put(MyDB.RECORD_BODY,body);
            values.put(MyDB.RECORD_TIME,getNowTime());
            if (timeSetTag){
                //  为当前备忘录添加提醒
                DatePicker datePicker = dialogDate.getDatePicker();
                String str = datePicker.getYear()+"-"+
                        (datePicker.getMonth()+1)+"-"+
                        datePicker.getDayOfMonth()+" "+
                        MyFormat.timeFormat(hour,minute);
                values.put(MyDB.NOTICE_TIME,str);

                //todo add notice logic
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, AlarmReceiver.class);
                Bundle bundle = new Bundle();
                bundle.putString(MyDB.RECORD_TITLE, record.getTitleName());
                bundle.putString(MyDB.RECORD_BODY, record.getTextBody());
                intent.putExtra("record", bundle);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 152, intent, 0);
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        myDateFormat(str, DateFormatType.NORMAL_TIME).getTime(),
                        pendingIntent);

            }
            db.update(MyDB.TABLE_NAME_RECORD,values,MyDB.RECORD_ID +"=?",
                    new String[]{record.getId().toString()});
            Toast.makeText(this,"修改成功",Toast.LENGTH_SHORT).show();
            db.close();
        }
        return flag;
    }

    /*
     * 弹窗函数
     * @param title
     * @param body
     * @param createDate
     */
    void showDialog(final String body){
        dialog = new AlertDialog.Builder(AmendActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage("是否保存当前编辑内容");
        dialog.setPositiveButton("保存",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateFunction(body);
                intentStart();
                    }
                });

        dialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intentStart();
                    }
                });
        dialog.show();
    }

    void clearDialog(){
        dialog = null;
    }

    boolean isShowIng(){
        if (dialog!=null){
            return true;
        }else{
            return false;
        }
    }

    /*
     * 得到当前时间
     * @return
     */
    String getNowTime(){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /*
     *  询问是否修改提醒时间的弹窗函数
     */
    void showAskDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(AmendActivity.this);
        dialog.setTitle("提示");
        DatePicker datePicker = dialogDate.getDatePicker();
        String str = datePicker.getYear()+"年"+
                (datePicker.getMonth()+1)+"月"+
                datePicker.getDayOfMonth()+"日"+
                " "+ MyFormat.timeFormat(hour,minute);
        dialog.setMessage("是否修改提醒时间？\n当前提醒时间为:"+str);
        dialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setNoticeDate();
                    }
                });

        dialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialog.show();
    }

    /*
     *  设置提醒时间函数
     */
    void setNoticeDate(){
        Calendar calendar=Calendar.getInstance();
        dialogDate = new DatePickerDialog(this,
                android.app.AlertDialog.THEME_HOLO_LIGHT,this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialogDate.getDatePicker().setCalendarViewShown(false);
        dialogDate.getDatePicker().setMinDate(calendar.getTime().getTime());
        dialogDate.setTitle("请选择日期");
        dialogDate.show();
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.i(TAG,"您选的日期为："+year+"年"+(month+1)+"月"+dayOfMonth+"日");
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;

        myTimeGetter = new MyTimeGetter(new Date(System.currentTimeMillis()));
        //  取出年月日时分
        int t_year = myTimeGetter.getYear();
        int t_month = myTimeGetter.getMonth();
        int t_dayOfMonth = myTimeGetter.getDay();
        int paramHour = 8;
        int paramMinute = 0;
        if (t_month==(this.month+1) && t_dayOfMonth==this.dayOfMonth){
            paramHour = myTimeGetter.getHour();
            //  如果是设置当天提醒，则最小时间显示默认不小于五分钟以内
            paramMinute = myTimeGetter.getMinute()+5;
        }
        dialogTime = new TimePickerDialog(this,
                android.app.AlertDialog.THEME_HOLO_LIGHT,this,
                paramHour,
                paramMinute,
                true);
        dialogTime.setTitle("请选择时间");
        if (t_year==this.year&&t_month==this.month&&t_dayOfMonth==this.dayOfMonth){
            //  设置的当天提醒，需要设置可选择的最小时间不小于当前五分钟之内
            //  暂未实现
        }
        dialogTime.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        timeSetTag = true;
        Toast.makeText(this,"提醒时间设置成功！",Toast.LENGTH_SHORT).show();
        String noticeStr = "  提醒时间："+getTimeStr(
                myDateFormat(year,(month+1),dayOfMonth,hour,minute,DateFormatType.NORMAL_TIME));
        amendTime.setText(record.getCreateTime() + noticeStr);
    }

}
