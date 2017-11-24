package kr.hs.emirim.wwhurin.lenseye;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private LineChart chart;
    private Thread thread;
    private Month month;

    TextView MText, countValue;
    addLense add;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    DateFormat dateFormat;
    Button button;
    int Month;

    String userId = "ghen601";
    int value;
    String mon[] = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
    String Label[] = {"m01", "m02", "m03", "m04", "m05", "m06", "m07", "m08",
            "m09", "m10", "m11", "m12"};
    //int cnt[] = new int[12];
    int cnt[] = {0,5,7,8,9,4,1,0,30,20,22,15};

    View view;
    // private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment



        MText = (TextView) view.findViewById(R.id.MText);
        countValue = (TextView) view.findViewById(R.id.countValue);
        button = (Button) view.findViewById(R.id.addLens);


        checkMonth();



        add = new addLense(view.getContext());
        add.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        chart = (LineChart) view.findViewById(R.id.chart);


        Paint p = chart.getPaint(chart.PAINT_DESCRIPTION);
        p.setColor(Color.rgb(110,173,220));

        chart.getAxisLeft().setTextColor(Color.rgb(110,173,220)); // left y-axis
        chart.getXAxis().setTextColor(Color.rgb(110,173,220));

        chart.getLegend().setTextColor(Color.rgb(110,173,220));
        chart.setBorderColor(Color.rgb(110,173,220));


        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(15f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextSize(15f);
        leftAxis.setDrawGridLines(false);


        readData(userId);
        //Toast.makeText(this, String.valueOf(cnt[Month - 1]+1), Toast.LENGTH_SHORT).show();
        MText.setText(mon[Month - 1]);

        LineData data = new LineData();
        chart.setData(data);
        //chart.setVisibleXRangeMaximum(12);
        feedMultiple();

        chart.notifyDataSetChanged(); // let the chart know it's data changed
        chart.invalidate(); // refresh






        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //버튼을 클릭하면 현재 월을 가져오고, 그 월에 들어있는 값을 가져와서 1을 증가 시킨후
                //다시 넣기

                add.show();
                writeLense(userId);
                readData(userId);
                LineData data = new LineData();
                chart.setData(data);
                //chart.setVisibleXRangeMaximum(12);
                feedMultiple();
                chart.notifyDataSetChanged(); // let the chart know it's data changed
                chart.invalidate(); // refresh

            }
        };

        button.setOnClickListener(listener);


        return view;
    }


    void show(){

        final EditText editName = new EditText(view.getContext());
        final EditText editDate = new EditText(view.getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("렌즈 착용하기");
        builder.setMessage("착용할 렌즈를 추가하거나, 선택해주세요");

        builder.setView(editName);
        builder.setView(editDate);

        builder.setPositiveButton("추가",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.show();

    }

    private void readData(String userId){


        DatabaseReference mLense = mDatabase.child("User").child("Lense").child("ghen601").child("Month");
        ValueEventListener LenseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Month month = dataSnapshot.getValue(Month.class);


                cnt[0] = month.getM01();
                cnt[1] = month.getM02();
                cnt[2] = month.getM03();

                cnt[3] = month.getM04();
                cnt[4] = month.getM05();
                cnt[5] = month.getM06();

                cnt[6] = month.getM07();
                cnt[7] = month.getM08();
                cnt[8] = month.getM09();

                cnt[9] = month.getM10();
                cnt[10] = month.getM11();
                cnt[11] = month.getM12();

                countValue.setText(String.valueOf(cnt[Month - 1])+"회");

//                String num = String.valueOf(month.getM09());
//
                //Toast.makeText(getApplicationContext(),num, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mLense.addValueEventListener(LenseListener);

    }


    public void checkMonth(){

        //  현재 월(Month) 추출하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM");
        Month = Integer.parseInt(CurMonthFormat.format(date));



    }

    private void addEntry(){

        LineData data = chart.getData();
        if(data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if(set == null ){

                set = createSet();
                data.addDataSet(set);

            }

            for(int i = 0 ; i < 12 ; i++) {
                data.addEntry(new Entry(set.getEntryCount(), cnt[i]), 0);
            }

            //
            data.notifyDataChanged();

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(10);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null,"Your lens"); //데이터셋의 이름 정하고, 기본 값은 null
        set.setAxisDependency(YAxis.AxisDependency.LEFT); // YAxis의 LEFT를 기본으로 설정
        set.setColor(Color.rgb(199,217,238)); //데이터 라인색 : HoloBule()
        set.setLineWidth(8f); //라인의 두께를 2f로 설정
        set.setCircleRadius(15f); // 라인의 점의 반지름을 4f 로 설정
        set.setFillAlpha(65); // 투명도 채우기를 65로 설정
        set.setFillColor(Color.rgb(199,217,238)); // 채우기 색은 HoloBlue
        set.setHighLightColor(Color.rgb(199,217,238)); // 하이라이트 컬러
        set.setDrawValues(true); // 값을 텍스르로 나타내지 않음
        set.setDrawFilled(true);
        set.setValueTextColor(Color.rgb(142,180,222));
        set.setValueTextSize(15);
        set.setCircleColor(Color.rgb(199,217,238));
        set.setColors(Color.rgb(199,217,238));
        return set; // 이렇게 생성한 set을 반환

    }

    private void feedMultiple(){

        if(thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //for(int i = 1 ; i<= 12 ;i++ ){

               getActivity().runOnUiThread(runnable);

                //}
            }


        });

        thread.start();

    }

    private void writeLense(String userId){

        if(cnt[Month - 1] < 31) {
            mDatabase.child("User").child("Lense").child("ghen601").child("Month").child(Label[Month - 1]).setValue(cnt[Month - 1] + 1);
            readData(userId);
        }else{

            Toast.makeText(getContext(), "한 달에 착용할 수 있는 범위를 넘어섰습니다", Toast.LENGTH_SHORT).show();

        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
