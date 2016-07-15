package com.senmonsimulator.senmonsimulator;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Meixi on 15/7/2016.
 */
public class CustomListAdapter extends ArrayAdapter<String> {

    private Switch swEnableAddRecord;
    private RadioGroup rgState;

    private RadioButton rbOff, rbNormal, rbWarning, rbCritical;

    private ArrayList<Boolean> switchStates = new ArrayList<>();
    private ArrayList<Integer> statesSelected = new ArrayList<>();

    public CustomListAdapter(Activity context, int textViewResourceId, ArrayList<String> machineIDs) {
        super(context,textViewResourceId, machineIDs);
        for(int i = 0; i < machineIDs.size(); i++)
        {
            switchStates.add(true);
            statesSelected.add(R.id.radioNormal);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //itemPosition = position;

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View v = inflater.inflate(R.layout.list_item, null, true);

        swEnableAddRecord = (Switch) v.findViewById(R.id.swEnableAddRecord);

        rgState = (RadioGroup) v.findViewById(R.id.radioState);
        rbOff = (RadioButton) v.findViewById(R.id.radioOff);
        rbNormal = (RadioButton) v.findViewById(R.id.radioNormal);
        rbWarning = (RadioButton) v.findViewById(R.id.radioWarning);
        rbCritical = (RadioButton) v.findViewById(R.id.radioCritical);

        swEnableAddRecord.setText(getItem(position));

        swEnableAddRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchStates.set(position, isChecked);
                if(isChecked)
                    enableRadioButtons(true);
                else
                    enableRadioButtons(false);
            }
        });
        rgState.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                statesSelected.set(position, checkedId);
            }
        });

        if(switchStates.size() > 0) {
            swEnableAddRecord.setChecked(switchStates.get(position));
            if (switchStates.get(position))
                enableRadioButtons(true);
            else
                enableRadioButtons(false);

            rgState.check(statesSelected.get(position));
        }
        return v;
    }

    private void enableRadioButtons(boolean setToEnable)
    {
        rbOff.setEnabled(setToEnable);
        rbNormal.setEnabled(setToEnable);
        rbWarning.setEnabled(setToEnable);
        rbCritical.setEnabled(setToEnable);
        notifyDataSetChanged();
    }

    public ArrayList<Boolean> getAddingEnabled() {
        return switchStates;
    }

    public ArrayList<Integer> getStatesSelected() {
        ArrayList<Integer> newStatesSelected = new ArrayList<>();
        for(int i = 0; i < statesSelected.size(); i++)
        {
            switch(statesSelected.get(i))
            {
                case R.id.radioOff:
                    newStatesSelected.add(0);
                    break;
                case R.id.radioNormal:
                    newStatesSelected.add(1);
                    break;
                case R.id.radioWarning:
                    newStatesSelected.add(2);
                    break;
                case R.id.radioCritical:
                    newStatesSelected.add(3);
                    break;
            }
        }
        return newStatesSelected;
    }

}
