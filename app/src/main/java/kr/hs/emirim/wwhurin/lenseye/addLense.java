package kr.hs.emirim.wwhurin.lenseye;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by oarum on 2017-11-21.
 */

public class addLense extends Dialog implements View.OnClickListener {


    private Context context;
    private Button closeBt;
    private Button submitBt;
    private EditText lenseName;
    private Spinner lenseTerm;

    private String name;
    private String userId = "ghen601";
    private boolean use;
    private String term;
    private String disuse;
    private int days;
    private String getTime;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


    public addLense(Context context) {

        super(context); // context 객체를 받는 생성자가 반드시 필요

    }

    public addLense(Context context, String name) {

        super(context);
        this.context = context;
        this.name = name;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_addlense);


        closeBt = (Button) findViewById(R.id.closeBt);
        submitBt = (Button) findViewById(R.id.submitBt);
        lenseName = (EditText) findViewById(R.id.lenseName);
        lenseTerm = (Spinner) findViewById(R.id.lenseTime);

        closeBt.setOnClickListener(this);
        submitBt.setOnClickListener(this);


//        submitBt.setOnClickListener(new View.OnClickListener(){
//
//
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.closeBt:
                cancel();
                break;

            case R.id.submitBt:
                sendData(userId);
                cancel();
                break;
        }

    }

    public void sendData(String userId) {

        DatabaseReference mLense = mDatabase.child("User").child("Lense").child("ghen601").child("LenseInfo");
        mLense.addValueEventListener(lenseListener);


        name = lenseName.getText().toString();
        term = lenseTerm.getSelectedItem().toString();


        //해야할 일, term 을 받아오는데 그 바다온거를 숫자로 바꿔서 데이트에 더해서 디비에 업데이트 해야하고
        //그래프 모양 바꿔야 하고
        //리스트 뿌려주면 끝

        switch (term){

            case "원데이": days=1; break;
            case "1주": days=7; break;
            case "2주": days=14; break;
            case "한달": days=30; break;
            case "1년" : days=365; break;

        }

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        getTime = sdf.format(date);

//        Calendar currentCal = Calendar.getInstance();
//        currentCal.add(Calendar.HOUR,12);

        setLenseDate();

        String use = "true";

        String key = mLense.push().getKey();


        Map<String, String> lenseValues = new HashMap<>();
        lenseValues.put("name",name);
        lenseValues.put("term",term);
        lenseValues.put("date",getTime);
        lenseValues.put("disuse",disuse);
        lenseValues.put("use", use);

        DatabaseReference keyRef = mLense.child(key);
        keyRef .setValue(lenseValues);


    }

    private void setLenseDate() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        try{

            Date date = df.parse(getTime);

            //날짜 더하기
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE,days);
            disuse = df.format(cal.getTime());

        }catch(ParseException e){

            e.printStackTrace();
        }


    }


    ValueEventListener lenseListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            //datas.clear();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                String key = snapshot.getKey();
                Lense lense = snapshot.getValue(Lense.class);
                lense.key = key;
                //  datas.add(lense);

            }

            //adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


}
