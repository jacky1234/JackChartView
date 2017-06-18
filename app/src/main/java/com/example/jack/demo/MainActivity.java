package com.example.jack.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.jack.piechart.ChartData;
import com.example.jack.piechart.JerryChartView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    JerryChartView jcv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jcv = (JerryChartView) findViewById(R.id.jcv);

        List<ChartData> datas = new ArrayList<>();
        ChartData cd0 = new ChartData(0xffF0868A, 0.70f, 0xffffffff, false);
        ChartData cd1 = new ChartData(0xffF48533, 0.20f, 0xffffffff, false);
        ChartData cd2 = new ChartData(0xffE0E4CD, 0.10f, 0xff898989, true);
        datas.add(cd0);
        datas.add(cd1);
        datas.add(cd2);

        jcv.setData(datas);
    }
}
