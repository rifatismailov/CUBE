package com.example.cube.draw;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.cube.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeView extends CardView {
    private TextView dateText, timeText;

    public DateTimeView(Context context) {
        super(context);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.custom_date_time_view, this);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        updateDateTime();
    }

    public void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        dateText.setText(currentDate);
        timeText.setText(currentTime);
    }

    public void setDateColor(int color) {
        dateText.setTextColor(color);
    }

    public void setTimeColor(int color) {
        timeText.setTextColor(color);
    }

    public void setCardBackgroundColor(int color) {
        super.setCardBackgroundColor(color);
    }
}
