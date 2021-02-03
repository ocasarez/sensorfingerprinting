package com.oscarcasarezruiz.sensorfingerprinting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.oscarcasarezruiz.sensorfingerprinting.presenter.DisclaimerActivityPresenter;

import java.util.UUID;

public class DisclaimerActivity extends AppCompatActivity implements DisclaimerActivityPresenter.View, View.OnClickListener {

    private DisclaimerActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);
        initViews();

        // Disclaimer Presenter
        presenter = new DisclaimerActivityPresenter(this);
    }

    private void initViews(){
        updateActionBarTitle();
        findViewById(R.id.disclaimer_activity_btn_accept).setOnClickListener(this);
    }

    @Override
    public void updateActionBarTitle() {
        getSupportActionBar().setTitle(R.string.disclaimer_title);
    }

    @Override
    public void navigateToFeatures() {
        Intent intent = new Intent(this, FeatureActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.disclaimer_activity_btn_accept){
            presenter.acceptButtonClicked();
        }
    }
}