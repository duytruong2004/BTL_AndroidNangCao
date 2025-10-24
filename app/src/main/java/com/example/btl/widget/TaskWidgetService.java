package com.example.btl.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class TaskWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        // Trả về Factory, nơi chứa logic đổ dữ liệu
        return new TaskWidgetFactory(this.getApplicationContext());
    }
}