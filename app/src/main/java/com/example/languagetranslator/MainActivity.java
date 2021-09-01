package com.example.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translatedTv;
    String[] fromLanguages = {"From", "Arabic","Bengali","Chinese","Dutch", "English","German","Greek","Gujarati","Hindi","Italian", "Japanese","Marathi","Portuguese","Romanian","Russian","Spannish","Swedish","Tamil","Telgu","Thai","Turkish","Urdu"};
    String[] toLanguages = {"To", "Arabic","Bengali","Chinese","Dutch", "English","German","Greek","Gujarati","Hindi","Italian", "Japanese","Marathi","Portuguese","Romanian","Russian","Spannish","Swedish","Tamil","Telgu","Thai","Turkish","Urdu"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromlanguageCode, tolanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.spinner1);
        toSpinner = findViewById(R.id.spinner2);
        sourceEdt = findViewById(R.id.editSource);
        micIV = findViewById(R.id.mic);
        translateBtn = findViewById(R.id.translateBtn);
        translatedTv = findViewById(R.id.translatedText);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                fromlanguageCode = getLanguageCode(fromLanguages[position]);

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter fromadapter = new ArrayAdapter(this, R.layout.spinner, fromLanguages);
        fromadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromadapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tolanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                translatedTv.setText("");
                if (sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your text to translate", Toast.LENGTH_SHORT).show();

                } else if (fromlanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select your source language", Toast.LENGTH_SHORT).show();
                } else if (tolanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select the language to make translation", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromlanguageCode, tolanguageCode, sourceEdt.getText().toString());
                }
            }


        });
        micIV.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into the text");
                try{
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);

                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data) {

    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode==REQUEST_PERMISSION_CODE){
        if (resultCode==RESULT_OK && data != null)
        {
            ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sourceEdt.setText(result.get(0));
        }
    }
}

    private void translateText(int fromlanguageCode, int tolanguageCode, String source) {
        translatedTv.setText("Downloading Model....");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromlanguageCode)
                .setTargetLanguage(tolanguageCode)
                .build();

        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions=new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess( Void unused) {
                translatedTv.setText("Translating....");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String s) {
                        translatedTv.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Fail to translate: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Fail to download Modal"+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }


    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Chinese":
                languageCode = FirebaseTranslateLanguage.ZH;
                break;
            case "Dutch":
                languageCode = FirebaseTranslateLanguage.NL;
                break;
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                break;
            case "Greek":
                languageCode = FirebaseTranslateLanguage.EL;
                break;
            case "Gujarati":
                languageCode = FirebaseTranslateLanguage.GU;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                break;
            case "Portuguese":
                languageCode = FirebaseTranslateLanguage.PT;
                break;
            case "Romanian":
                languageCode = FirebaseTranslateLanguage.RO;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "Spannish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "Sweddish":
                languageCode = FirebaseTranslateLanguage.SV;
                break;

            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                break;
            case "Telgu":
                languageCode = FirebaseTranslateLanguage.TE;
                break;
            case "Thai":
                languageCode = FirebaseTranslateLanguage.TH;
                break;
            case "Turkish":
                languageCode = FirebaseTranslateLanguage.TR;
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;

            default:
                languageCode = 0;

        }
        return languageCode;
    }
}

