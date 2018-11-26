package com.kalu.ocr;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import exocr.activity.OcrActivity;
import exocr.exocrengine.EXOCRDict;
import exocr.exocrengine.EXOCRModel;

public final class BankActivity extends OcrActivity {

    public static final int REQUEST_CODE_FRONT = 1000;
    public static final int REQUEST_CODE_BACK = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

//        boolean succ = EXOCRDict.InitDict(this);
//        if (!succ) return;
//
//        findViewById(R.id.card_front).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent scanIntent = new Intent(getApplicationContext(), CaptureActivity.class);
//                scanIntent.putExtra(CaptureActivity.INTNET_FRONT, true);
//                startActivityForResult(scanIntent, REQUEST_CODE_FRONT);
//            }
//        });
//
//        findViewById(R.id.card_back).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent scanIntent = new Intent(getApplicationContext(), CaptureActivity.class);
//                scanIntent.putExtra(CaptureActivity.INTNET_FRONT, false);
//                startActivityForResult(scanIntent, REQUEST_CODE_BACK);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("kalu", "requestCode = " + requestCode + ", resultCode = " + resultCode);

//        if (requestCode == REQUEST_CODE_FRONT && resultCode == CaptureActivity.RESULT_CODE && null != data) {
//
//            final Bundle extras = data.getExtras();
//            final EXOCRModel result = extras.getParcelable(CaptureActivity.EXTRA_SCAN_RESULT);
//            Log.e("kalu", "result = " + result.toString());
//
//            final TextView name = findViewById(R.id.card_name);
//            name.setText(result.name);
//
//            final TextView sex = findViewById(R.id.card_sex);
//            sex.setText(result.sex);
//
//            final TextView nation = findViewById(R.id.card_nation);
//            nation.setText(result.nation);
//
//            final TextView birth = findViewById(R.id.card_birth);
//            birth.setText(result.birth);
//
//            final TextView local = findViewById(R.id.card_local);
//            local.setText(result.address);
//
//            final TextView number = findViewById(R.id.card_number);
//            number.setText(result.cardnum);
//
//            final ImageView self = findViewById(R.id.card_self);
//            self.setImageBitmap(BitmapFactory.decodeFile(result.bitmapPath));
//
//        } else if (requestCode == REQUEST_CODE_BACK && resultCode == CaptureActivity.RESULT_CODE && null != data) {
//
//            final Bundle extras = data.getExtras();
//            final EXOCRModel result = extras.getParcelable(CaptureActivity.EXTRA_SCAN_RESULT);
//            Log.e("kalu", "result = " + result.toString());
//
//            final TextView office = findViewById(R.id.card_office);
//            office.setText(result.office);
//
//            final TextView date = findViewById(R.id.card_date);
//            date.setText(result.validdate);
//
//            final ImageView country = findViewById(R.id.card_country);
//            country.setImageBitmap(BitmapFactory.decodeFile(result.bitmapPath));
//        }
    }
}
