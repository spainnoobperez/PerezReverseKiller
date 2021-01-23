package es.perez.netdiagnosis.Activity;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.perez.netdiagnosis.Adapter.ContentFilterAdapter;
import es.perez.netdiagnosis.Bean.ResponseFilterRule;
import com.perez.revkiller.R;
import com.perez.catchexception.CrashApp;
import es.perez.netdiagnosis.Utils.SharedPreferenceUtils;

public class ChangeFilterActivity extends AppCompatActivity {
    @BindView(R.id.activity_change_filter)
    public RelativeLayout relativeLayout;

    @BindView(R.id.lv_filter)
    public ListView listView;

    @BindView(R.id.fab_add)
    public FloatingActionButton floatingActionButton;

    ContentFilterAdapter contentFilterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_filter);
        ButterKnife.bind(this);
        setupActionBar();

        if(((CrashApp)getApplication()).ruleList == null){
            contentFilterAdapter = new ContentFilterAdapter(this,new ArrayList<ResponseFilterRule>());
        }else{
            contentFilterAdapter = new ContentFilterAdapter(this,((CrashApp)getApplication()).ruleList);
        }

        listView.setAdapter(contentFilterAdapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(null);
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        setTitle("Return packet injection item");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void showDialog(final ResponseFilterRule responseFilterRule){
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeFilterActivity.this);

        View textEntryView = LayoutInflater.from(ChangeFilterActivity.this).inflate(R.layout.alert_resp_filter, null);
        final EditText urlEditText = (EditText) textEntryView.findViewById(R.id.et_origin_url);
        final EditText regexEditText = (EditText) textEntryView.findViewById(R.id.et_regex);
        final EditText contentEditText = (EditText) textEntryView.findViewById(R.id.et_replace_result);
        if(responseFilterRule!=null){
            urlEditText.setText(responseFilterRule.getUrl());
            regexEditText.setText(responseFilterRule.getReplaceRegex());
            contentEditText.setText(responseFilterRule.getReplaceContent());
            builder.setTitle("Modify injection item");
        }else{
            builder.setTitle("Add injection item");
        }

        builder.setCancelable(true);
        builder.setView(textEntryView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(responseFilterRule!=null){
                    responseFilterRule.setUrl(urlEditText.getText().toString());
                    responseFilterRule.setReplaceRegex(regexEditText.getText().toString());
                    responseFilterRule.setReplaceContent(contentEditText.getText().toString());
                }else {
                    if(urlEditText.getText().length()>0 && regexEditText.getText().length()>0
                            && contentEditText.getText().length()>0) {
                        ResponseFilterRule responseFilterRule = new ResponseFilterRule();
                        responseFilterRule.setUrl(urlEditText.getText().toString());
                        responseFilterRule.setReplaceRegex(regexEditText.getText().toString());
                        responseFilterRule.setReplaceContent(contentEditText.getText().toString());
                        ((CrashApp) getApplication()).ruleList.add(responseFilterRule);
                    }
                }
                contentFilterAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.show();
    }

    @Override
    protected void onStop() {
        SharedPreferenceUtils.save(getApplicationContext(),
                "response_filter",((CrashApp) getApplication()).ruleList);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
