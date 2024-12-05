package com.example.cube;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ShowPopupWindow {

    private Context context;
    private PopupWindow popupWindow;

    // Конструктор класу
    public ShowPopupWindow(Context context) {
        this.context = context;
    }

    // Метод для показу спливаючого вікна 20240409_172704.mp4
    public void showPopupWindow(View anchorView, String message) {
        // Створюємо LayoutInflater для інфляції макету
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        // Створюємо об'єкт PopupWindow
        popupWindow = new PopupWindow(popupView,
                900,
                LinearLayout.LayoutParams.MATCH_PARENT,
                true); // true означає, що вікно можна закрити натисканням поза межами

        // Встановлюємо текст повідомлення у спливаючому вікні
        TextView textView = popupView.findViewById(R.id.textView);
        textView.setText(message);

        // Встановлюємо місце показу вікна зліва зверху екрану
        // Значення 0 по вертикалі (ось Y), щоб вікно з'являлося зверху екрану
        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, 0, 0);

        // Закриваємо вікно при натисканні на текстове поле
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
}
