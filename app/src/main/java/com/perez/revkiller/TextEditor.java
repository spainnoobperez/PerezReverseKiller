package com.perez.revkiller;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.perez.res.*;
import com.perez.util.StringUtils;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ScrollView;
import android.widget.CheckBox;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.KeyEvent;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TextEditor extends AppCompatActivity {

    public static final int SETTEXT = 1;
    public static final int TOAST = 2;
    public static final String PLUGIN = "plugin";
    public static final String DATA = "data";

    Edit edit;
    EditText text;
    private SharedPreferences mPreferences;
    private TextSettings mSettings;
    private boolean isViewText = true;
    private boolean isChanged = false;
    private boolean noText = false;

    public static String searchString = "";
    public static String replaceString = "";
    public static byte[] data;// single
    private ScrollView scroll;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case SETTEXT:
                text.setText(msg.obj != null ? msg.obj.toString() : "");
                isChanged = false;
                break;
            case TOAST:
                toast(msg.obj.toString());
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            handlerIntent();
            if(isViewText)
                setContentView(R.layout.view_text);
            else
                setContentView(R.layout.text_editor);
            text = (EditText) findViewById(R.id.txtEdit);
            scroll = (ScrollView) findViewById(R.id.scroll);
            mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            mSettings = new TextSettings(mPreferences);
            updatePrefs();
            open();
            TextWatcher watch = new TextWatcher() {
                public void beforeTextChanged(CharSequence c, int start,
                                              int count, int after) {
                }
                public void onTextChanged(CharSequence c, int start, int count,
                                          int after) {
                }
                public void afterTextChanged(Editable edit) {
                    if(!isChanged)
                        isChanged = true;
                }
            };
            text.addTextChangedListener(watch);
        } catch(Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void showDialog() {
        PerezReverseKillerMain.prompt(this, getString(R.string.prompt),
                               getString(R.string.is_save),
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dailog, int which) {
                if(which == AlertDialog.BUTTON_POSITIVE) {
                    write();
                    result();
                } else if(which == AlertDialog.BUTTON_NEGATIVE)
                    finish();
            }
        });
    }

    public void open() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    List<String> list = new ArrayList<String>();
                    edit.read(list, data);
                    // set text
                    Message msg = new Message();
                    msg.what = SETTEXT;
                    msg.obj = StringUtils.join(list, "\n");
                    mHandler.sendMessage(msg);
                } catch(Exception e) {
                    noText = true;
                    Message msg = new Message();
                    msg.what = TOAST;
                    msg.obj = e.getMessage();
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void updatePrefs() {
        mSettings.readPrefs(mPreferences);
        // line wrap
        text.setHorizontallyScrolling(!mSettings.mLineWrap);
        // font type
        String font = mSettings.mFontType;
        if(font.equals("Serif"))
            text.setTypeface(Typeface.SERIF);
        else if(font.equals("Sans Serif"))
            text.setTypeface(Typeface.SANS_SERIF);
        else
            text.setTypeface(Typeface.MONOSPACE);
        text.setTextSize(mSettings.mFontSize);
        text.setTextColor(mSettings.mFontColor);
        // toast("fontcolor "+mSettings.mFontColor);
        text.setBackgroundColor(mSettings.mBgColor);
        scroll.setBackgroundColor(mSettings.mBgColor);
        // toast("bgcolor "+mSettings.mBgColor);
    }

    public void onResume() {
        super.onResume();
        updatePrefs();
    }

    private void searchString() {
        LayoutInflater inflate = getLayoutInflater();
        ScrollView scroll = (ScrollView) inflate.inflate(
                                R.layout.alert_dialog_search_or_replace, null);
        final CheckBox from_start = (CheckBox) scroll
                                    .findViewById(R.id.from_start);
        final EditText srcName = (EditText) scroll.findViewById(R.id.src_edit);
        final CheckBox isReplace = (CheckBox) scroll.findViewById(R.id.replace);
        final EditText dstName = (EditText) scroll
                                 .findViewById(R.id.replace_edit);
        srcName.setText(searchString);
        dstName.setText(replaceString);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.search_string);
        alert.setView(scroll);
        alert.setPositiveButton(R.string.btn_ok,
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                searchString = srcName.getText().toString();
                replaceString = dstName.getText().toString();
                if(searchString.length() == 0) {
                    toast(getString(R.string.search_name_empty));
                    return;
                }
                int index = from_start.isChecked() ? 0 : text
                            .getSelectionStart();
                if(isReplace.isChecked()) {
                    if(!replace(searchString, replaceString, index)) {
                        toast(String.format(
                                  getString(R.string.search_not_found),
                                  searchString));
                    }
                    return;
                }
                if(!searchString(searchString, index)) {
                    toast(String.format(
                              getString(R.string.search_not_found),
                              searchString));
                }
            }
        });
        alert.setNegativeButton(R.string.btn_cancel, null);
        alert.show();
    }

    private boolean searchString(String src, int index) {
        CharSequence seq = text.getText();
        index = seq.toString().indexOf(src, index + 1);
        if(index != -1) {
            text.setSelection(index, index + src.length());
            return true;
        }
        return false;
    }

    private boolean replace(String src, String dst, int index) {
        Editable editable = text.getEditableText();
        String s = text.getText().toString();
        if((index = s.indexOf(src, index + 1)) != -1) {
            editable.replace(index, index + src.length(), dst);
            text.setSelection(index, index + dst.length());
            return true;
        }
        return false;
    }

    private void handlerIntent() {
        Intent intent = getIntent();
        String plugin = intent.getStringExtra(PLUGIN);
        load(plugin);
    }

    private void load(String name) {
        // toast(name);
        if("ARSCEditor".equals(name)) {
            isViewText = false;
            edit = new ARSCEditor();
        } else if("TextEditor".equals(name)) {
            isViewText = true;
            edit = new Text();
        } else if("AXmlEditor".equals(name)) {
            isViewText = false;
            edit = new AXmlEditor();
        } else if("StringIdsEditor".equals(name)) {
            isViewText = false;
            edit = new StringIdsEditor();
        } else if("TypeIdsEditor".equals(name)) {
            isViewText = false;
            edit = new TypeIdsEditor();
        }
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void write() {
        String data = text.getText().toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            edit.write(data, baos);
            TextEditor.data = baos.toByteArray();
        } catch(IOException io) {
        }
    }

    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater in = getMenuInflater();
        in.inflate(R.menu.text_editor_menu, m);
        if(noText)
            m.removeItem(R.id.save);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        edit = null;
        scroll = null;
        mSettings = null;
        mPreferences = null;
        System.gc();
    }

    private void result() {
        Intent intent = getIntent();
        setResult(ActResConstant.text_editor, intent);
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(!noText && isChanged) {
                showDialog();
                return true;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        switch(mi.getItemId()) {
        case R.id.save:
            write();
            result();
            isChanged = false;
            break;
        case R.id.exit:
            if(noText) {
                finish();
                return true;
            }
            if(isChanged)
                showDialog();
            else
                finish();
            break;
        case R.id.search_string:
            searchString();
            break;
        case R.id.preferences: {
            Intent intent = new Intent(this, TextPreferences.class);
            startActivity(intent);
            break;
        }
        }
        return true;
    }

    class Text implements Edit {

        public void read(List<String> data, byte[] input) throws IOException {
            String s = new String(input, "UTF-8");
            String[] strs = s.split("\n");
            for(String str : strs)
                data.add(str);
        }

        public void write(String data, OutputStream out) throws IOException {
            out.write(data.getBytes("UTF-8"));
        }
    }
}
