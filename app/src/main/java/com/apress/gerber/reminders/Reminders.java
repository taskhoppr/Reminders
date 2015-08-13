package com.apress.gerber.reminders;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;


public class Reminders extends ActionBarActivity {

    // add a comment
    private ListView mListView;
    private RemindersDBAdapter mDbAdapter;
    private RemindersSimpleCursorAdapter mCursorAdapter;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        mListView = (ListView) findViewById(R.id.reminders_list_view);

        mListView.setDivider(null);
        mDbAdapter = new RemindersDBAdapter(this);
        try {
            mDbAdapter.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (savedInstanceState == null) {
            //Clear all data
            mDbAdapter.deleteAllReminders();
            //Add some data
            insertSomeReminders();
        }
        Cursor cursor = mDbAdapter.fetchAllReminders();
        String[] from = new String[]{RemindersDBAdapter.COL_CONTENT};
        int[] to = new int[] {R.id.row_text};
        mCursorAdapter = new RemindersSimpleCursorAdapter(Reminders.this, R.layout.reminders_row,cursor,from,to,0);
        mListView.setAdapter(mCursorAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Reminders.this);
                ListView modeListView = new ListView(Reminders.this);
                String[] modes = new String[] {"Edit Reminder","Delete Reminder"};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(Reminders.this,android.R.layout.simple_list_item_1,android.R.id.text1,modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //edit reminder
                        if (position == 0) {
                          int nId = getIdFromPosition(masterListPosition);
                            RemindersDAO reminders = mDbAdapter.fetchReminderById(nId);
                            fireCustomDialog(reminders);
                            //delete reminder
                        } else {
                            mDbAdapter.deleteReminderById(getIdFromPosition(masterListPosition));
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cam_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu item) {

                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_reminder:
                            for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
                                if (mListView.isItemChecked(nC)) {
                                    mDbAdapter.deleteReminderById(getIdFromPosition(nC));
                                }
                            }
                            mode.finish();
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                            return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {

                }

                @Override
                public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {

                }
            });
        }

    }

    private int getIdFromPosition(int nC) {
        return (int)mCursorAdapter.getItemId(nC);
    }

    private void fireCustomDialog(final RemindersDAO reminder){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        TextView titleView = (TextView)dialog.findViewById(R.id.custom_title);
        final EditText editCustom = (EditText) dialog.findViewById(R.id.custom_edit_reminder);
        Button commitButton = (Button) dialog.findViewById(R.id.custom_button_commit);
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.custom_check_box);
        checkBox.setChecked(true);
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.custom_root_layout);

        final boolean isEditOperation = (reminder !=null);
        if (isEditOperation) {
            titleView.setText("Edit Reminder");
            checkBox.setChecked(reminder.getmImportant() == 1);
            editCustom.setText(reminder.getmContent());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderText = editCustom.getText().toString();
                if (isEditOperation) {
                    RemindersDAO reminderEdited = new RemindersDAO(reminder.getmId(),
                            reminderText, checkBox.isChecked() ? 1 : 0);
                    mDbAdapter.updateReminder(reminderEdited);
                    //this is for new reminder
                } else {
                    mDbAdapter.createReminder(reminderText, checkBox.isChecked());
                }
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                dialog.dismiss();
            }
        });
        Button buttonCancel = (Button) dialog.findViewById(R.id.custom_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       /* int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);*/
        switch(item.getItemId()) {
            case R.id.action_new:
                Log.d(getLocalClassName(),"Create New Reminder");
                fireCustomDialog(null);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }

    }
    private void insertSomeReminders() {
        mDbAdapter.createReminder("Buy Learn Android Studio", true);
        mDbAdapter.createReminder("Send Dad birthday gift", false);
        mDbAdapter.createReminder("Dinner at the Gage on Friday", false);
        mDbAdapter.createReminder("String squash racket", false);
        mDbAdapter.createReminder("Shovel and salt walkways", false);
        mDbAdapter.createReminder("Prepare Advanced Android syllabus", true);
        mDbAdapter.createReminder("Buy new office chair", false);
        mDbAdapter.createReminder("Call Auto-body shop for quote", false);
        mDbAdapter.createReminder("Renew membership to club", false);
        mDbAdapter.createReminder("Buy new Galaxy Android phone", true);
        mDbAdapter.createReminder("Sell old Android phone - auction", false);
        mDbAdapter.createReminder("Buy new paddles for kayaks", false);
        mDbAdapter.createReminder("Call accountant about tax returns", false);
        mDbAdapter.createReminder("Buy 300,000 shares of Google", false);
        mDbAdapter.createReminder("Call the Dalai Lama back", true);
    }

}
