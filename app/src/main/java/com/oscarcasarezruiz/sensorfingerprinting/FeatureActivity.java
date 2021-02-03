package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.oscarcasarezruiz.sensorfingerprinting.presenter.FeatureActivityPresenter;

public class FeatureActivity extends AppCompatActivity implements FeatureActivityPresenter.View, View.OnClickListener {

    private FeatureActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);
        initViews();

        //Features Presenter
        presenter = new FeatureActivityPresenter(this);
    }

    private void initViews(){
        updateActionBarTitle();
        findViewById(R.id.feature_activity_btn_sensordata).setOnClickListener(this);
        findViewById(R.id.feature_activity_btn_sensorfingerprint_3trace).setOnClickListener(this);
        findViewById(R.id.feature_activity_btn_sensorfingerprint_2trace).setOnClickListener(this);
        findViewById(R.id.feature_activity_btn_sensorfingerprint_1trace).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.feature_activity_btn_sensordata:
                presenter.sensorDataButtonClicked();
                break;
            case R.id.feature_activity_btn_sensorfingerprint_3trace:
                presenter.sensorFingerprintButtonClicked("Three");
                break;
            case R.id.feature_activity_btn_sensorfingerprint_2trace:
                presenter.sensorFingerprintButtonClicked("Two");
                break;
            case R.id.feature_activity_btn_sensorfingerprint_1trace:
                presenter.sensorFingerprintButtonClicked("One");
                break;
        }
    }

    @Override
    public void updateActionBarTitle() {
        getSupportActionBar().setTitle(R.string.features_title);
    }

    @Override
    public void navigateToSensorDate() {
        Intent intent = new Intent(this, SensorDataActivity.class);
        startActivity(intent);
    }

    @Override
    public void navigateToSensorFingerprint(String traces) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        intent.putExtra("TraceCount", traces);
        startActivity(intent);
    }
}