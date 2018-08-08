package com.mydrawingsv2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class BrushSizeChooseFragment extends DialogFragment {

    private OnNewBrushSizeListener mListener;
    private SeekBar seekBar;
    private TextView minValue, maxValue, currentValue;
    private int currentSize;
    private static int mSize;

    public void setOnNewBrushSizeListener(OnNewBrushSizeListener listener){
        mListener = listener;
    }


    public static  BrushSizeChooseFragment NewInstance(int size){
        BrushSizeChooseFragment fragment = new BrushSizeChooseFragment();
        Bundle bundle = new Bundle();
        mSize = size;
        if (size>0){
            bundle.putInt("current_brush_size", size);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle!=null && bundle.containsKey("current_brush_size")){
            int brushSize = bundle.getInt("current_brush_size",10);
            if (brushSize>0)
                currentSize = brushSize;
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.brush_chooser, null);
        if (dialogView !=null){
            int minSize = getResources().getInteger(R.integer.min_size);
            minValue = dialogView.findViewById(R.id.min_valueTV);
            minValue.setText(minSize+"");

            int maxSize = getResources().getInteger(R.integer.max_size);
            maxValue = dialogView.findViewById(R.id.max_valueTV);
            maxValue.setText(maxSize+"");

            currentValue = dialogView.findViewById(R.id.brush_sizeTV);

            if (currentSize>0)
                //currentValue.setText(R.string.brush_size+currentSize);
                currentValue.append(" "+currentSize);

            seekBar = dialogView.findViewById(R.id.brushSizeSB);
            seekBar.setProgress(mSize);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progressChanged = 0;
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChanged = progress;
                    //currentValue.setText(R.string.brush_size+progressChanged);
                    currentValue.setText(R.string.brush_size);
                    currentValue.append(" "+progressChanged);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mListener.OnNewBrushListener(progressChanged);
                }
            });
        }

        builder.setTitle(R.string.choose_brush)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(dialogView);
        return builder.create();
    }


}
