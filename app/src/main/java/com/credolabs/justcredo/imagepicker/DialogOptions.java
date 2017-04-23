package com.credolabs.justcredo.imagepicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rebus.bottomdialog.Item;

/**
 * Created by Sanjay kumar on 4/23/2017.
 */

public class DialogOptions {

    private CustomDialog customDialog;

    public DialogOptions(Context context) {
        customDialog = new CustomDialog(context);
    }

    public void title(String title) {
        customDialog.title(title);
    }

    public void title(int title) {
        customDialog.title(title);
    }

    public void inflateMenu(int menu) {
        customDialog.inflateMenu(menu);
    }

    public void addItems(List<Item> items) {
        customDialog.addItems(items);
    }

    public void addItem(Item item) {
        customDialog.addItem(item);
    }

    public void cancelable(boolean value) {
        customDialog.setCancelable(value);
    }

    public void canceledOnTouchOutside(boolean value) {
        customDialog.setCanceledOnTouchOutside(value);
    }

    public void setOnItemSelectedListener(rebus.bottomdialog.BottomDialog.OnItemSelectedListener onItemSelectedListener) {
        customDialog.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void show() {
        customDialog.show();
    }

    public void dismiss() {
        customDialog.dismiss();
    }

    public interface OnItemSelectedListener {
        boolean onItemSelected(int id);
    }

    private class CustomDialog extends Dialog implements View.OnClickListener {

        private final String TAG = CustomDialog.class.getName();

        private int padding;
        private int icon;
        private LinearLayout container;
        private rebus.bottomdialog.BottomDialog.OnItemSelectedListener onItemSelectedListener;
        private List<Item> items;

        public CustomDialog(Context context) {
            super(context);
            items = new ArrayList<>();
            icon = getContext().getResources().getDimensionPixelSize(rebus.bottomdialog.R.dimen.dimen_32_dp);
            padding = getContext().getResources().getDimensionPixelSize(rebus.bottomdialog.R.dimen.dimen_8_dp);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            container = new LinearLayout(getContext());
            container.setLayoutParams(params);
            container.setBackgroundColor(Color.WHITE);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(0, padding, 0, padding);
            ScrollView scrollView = new ScrollView(getContext());
            scrollView.addView(container);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(scrollView, params);
            setCancelable(true);
            setCanceledOnTouchOutside(true);
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getWindow().getAttributes().windowAnimations = rebus.bottomdialog.R.style.DialogAnimation;
        }

        public void cancelable(boolean value) {
            setCancelable(value);
        }

        public void canceledOnTouchOutside(boolean value) {
            setCanceledOnTouchOutside(value);
        }

        public void addItems(List<Item> itemList) {
            items.clear();
            items.addAll(itemList);
            for (Item item : items) {
                addItem(item);
            }
        }

        public void addItem(Item item) {
            int size = icon + padding + padding;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
            TextView row = new TextView(getContext());
            row.setId(item.getId());
            row.setLayoutParams(params);
            row.setMaxLines(1);
            row.setEllipsize(TextUtils.TruncateAt.END);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setText(item.getTitle());
            row.setTypeface(Typeface.DEFAULT_BOLD);
            row.setOnClickListener(this);
            row.setTextColor(rebus.bottomdialog.Utils.colorStateListText(getContext()));
            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            row.setBackgroundResource(typedValue.resourceId);
            if (item.getIcon() != null) {
                row.setCompoundDrawablesWithIntrinsicBounds(icon(item.getIcon()), null, null, null);
                row.setCompoundDrawablePadding(padding);
                row.setPadding(padding, padding, padding, padding);
            } else {
                row.setPadding(size, padding, padding, padding);
            }
            container.addView(row);
        }

        public void inflateMenu(int menu) {
            MenuInflater menuInflater = new SupportMenuInflater(getContext());
            MenuBuilder menuBuilder = new MenuBuilder(getContext());
            menuInflater.inflate(menu, menuBuilder);
            List<Item> items = new ArrayList<>();
            for (int i = 0; i < menuBuilder.size(); i++) {
                MenuItem menuItem = menuBuilder.getItem(i);
                Item item = new Item();
                item.setId(menuItem.getItemId());
                item.setIcon(menuItem.getIcon());
                item.setTitle(menuItem.getTitle().toString());
                items.add(item);
            }
            addItems(items);
        }

        /**
         * @param drawable Drawable from menu item
         * @return Drawable resized 32dp x 32dp and colored with color textColorSecondary
         */
        private Drawable icon(Drawable drawable) {
            if (drawable != null) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                Drawable resizeIcon = new BitmapDrawable(getContext().getResources(), Bitmap.createScaledBitmap(bitmap, icon, icon, true));
                Drawable.ConstantState state = resizeIcon.getConstantState();
                resizeIcon = DrawableCompat.wrap(state == null ? resizeIcon : state.newDrawable()).mutate();
                DrawableCompat.setTintList(resizeIcon, rebus.bottomdialog.Utils.colorStateListIcon(getContext()));
                return resizeIcon;
            }
            return null;
        }

        public void title(int title) {
            title(getContext().getString(title));
        }

        public void title(String title) {
            int size = getContext().getResources().getDimensionPixelSize(rebus.bottomdialog.R.dimen.dimen_16_dp) + padding;
            TextView item = new TextView(getContext());
            item.setText(title);
            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 );
            item.setPadding(size, padding, padding, padding);
            container.addView(item);
        }

        @Override
        public void onClick(View v) {
            if (onItemSelectedListener != null) {
                if (onItemSelectedListener.onItemSelected(v.getId())) {
                    dismiss();
                }
            }
        }

        public void setOnItemSelectedListener(rebus.bottomdialog.BottomDialog.OnItemSelectedListener onItemSelectedListener) {
            this.onItemSelectedListener = onItemSelectedListener;
        }

    }

}