package tool.xfy9326.floatpicture.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import tool.xfy9326.floatpicture.Methods.ImageMethods;
import tool.xfy9326.floatpicture.Methods.WindowsMethods;
import tool.xfy9326.floatpicture.R;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;

public class PictureSettingsFragment extends PreferenceFragmentCompat {
    private final static String WINDOW_CREATED = "WINDOW_CREATED";
    private boolean Edit_Mode;
    private boolean Window_Created;
    private boolean onUseEditPicture = false;
    private LayoutInflater inflater;
    private PictureData pictureData;
    private String PictureId;
    private String PictureName;
    private WindowManager windowManager;
    private FloatImageView floatImageView;
    private Bitmap bitmap;
    private Bitmap bitmap_Edit;
    private FloatImageView floatImageView_Edit;
    private boolean touch_and_move;
    private float default_zoom;
    private float zoom;
    private float zoom_temp;
    private float picture_degree;
    private float picture_degree_temp;
    private float picture_alpha;
    private float picture_alpha_temp;
    private float picture_darken;
    private float picture_darken_temp;
    private int position_x;
    private int position_y;
    private int position_x_temp;
    private int position_y_temp;
    private boolean allow_picture_over_layout;
    private boolean fill_screen;
    private boolean filter_app_enabled;
    private String filter_app_package;
    private String filter_app_name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window_Created = false;
        Edit_Mode = false;
        pictureData = new PictureData();
        inflater = LayoutInflater.from(getActivity());
        windowManager = WindowsMethods.getWindowManager(requireActivity());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_picture_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restoreData(savedInstanceState);
        setMode();
        PreferenceSet();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(WINDOW_CREATED, true);
        super.onSaveInstanceState(outState);
    }

    private void restoreData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Window_Created = savedInstanceState.getBoolean(WINDOW_CREATED, false);
            windowManager = WindowsMethods.getWindowManager(requireActivity());
        }
    }

    private void setMode() {
        Intent intent = Objects.requireNonNull(requireActivity().getIntent());
        Edit_Mode = intent.getBooleanExtra(Config.INTENT_PICTURE_EDIT_MODE, false);
        AlertDialog.Builder loading = new AlertDialog.Builder(requireActivity());
        loading.setCancelable(false);
        if (!Edit_Mode) {
            loading.setOnCancelListener(dialog -> WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y));
        }
        View mView = inflater.inflate(R.layout.dialog_loading, requireActivity().findViewById(R.id.layout_dialog_loading));
        loading.setView(mView);
        final AlertDialog alertDialog = loading.show();
        new Thread(() -> {
            if (!Window_Created) {
                if (Edit_Mode) {
                    //Edit
                    PictureId = intent.getStringExtra(Config.INTENT_PICTURE_EDIT_ID);
                    pictureData.setDataControl(PictureId);
                    PictureName = pictureData.getListArray().get(PictureId);
                    position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
                    position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);
                    picture_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, Config.DATA_DEFAULT_PICTURE_DEGREE);
                    picture_alpha = pictureData.getFloat(Config.DATA_PICTURE_ALPHA, Config.DATA_DEFAULT_PICTURE_ALPHA);
                    picture_darken = pictureData.getFloat(Config.DATA_PICTURE_DARKEN, Config.DATA_DEFAULT_PICTURE_DARKEN);
                    touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
                    allow_picture_over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT);
                    fill_screen = pictureData.getBoolean(Config.DATA_PICTURE_FILL_SCREEN, Config.DATA_DEFAULT_PICTURE_FILL_SCREEN);
                    filter_app_enabled = pictureData.getBoolean(Config.DATA_PICTURE_FILTER_APP_ENABLED, Config.DATA_DEFAULT_PICTURE_FILTER_APP_ENABLED);
                    filter_app_package = pictureData.getString(Config.DATA_PICTURE_FILTER_APP_PACKAGE, Config.DATA_DEFAULT_PICTURE_FILTER_APP_PACKAGE);
                    filter_app_name = pictureData.getString(Config.DATA_PICTURE_FILTER_APP_NAME, Config.DATA_DEFAULT_PICTURE_FILTER_APP_NAME);
                    bitmap = ImageMethods.getShowBitmap(requireContext(), PictureId);
                    default_zoom = ImageMethods.getDefaultZoom(requireContext(), bitmap, false);
                    zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, default_zoom);
                    floatImageView = ImageMethods.getFloatImageViewById(requireContext(), PictureId);
                    floatImageView.setDarken(picture_darken);
                } else {
                    //New
                    PictureId = ImageMethods.setNewImage(getActivity(), intent.getData());
                    pictureData.setDataControl(PictureId);
                    PictureName = getString(R.string.new_picture_name);
                    position_x = Config.DATA_DEFAULT_PICTURE_POSITION_X;
                    position_y = Config.DATA_DEFAULT_PICTURE_POSITION_Y;
                    picture_alpha = Config.DATA_DEFAULT_PICTURE_ALPHA;
                    picture_darken = Config.DATA_DEFAULT_PICTURE_DARKEN;
                    picture_degree = Config.DATA_DEFAULT_PICTURE_DEGREE;
                    touch_and_move = Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE;
                    allow_picture_over_layout = Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT;
                    fill_screen = Config.DATA_DEFAULT_PICTURE_FILL_SCREEN;
                    filter_app_enabled = Config.DATA_DEFAULT_PICTURE_FILTER_APP_ENABLED;
                    filter_app_package = Config.DATA_DEFAULT_PICTURE_FILTER_APP_PACKAGE;
                    filter_app_name = Config.DATA_DEFAULT_PICTURE_FILTER_APP_NAME;
                    bitmap = ImageMethods.getShowBitmap(requireContext(), PictureId);
                    default_zoom = ImageMethods.getDefaultZoom(requireContext(), bitmap, false);
                    zoom = default_zoom;
                    floatImageView = ImageMethods.createPictureView(requireContext(), bitmap, touch_and_move, allow_picture_over_layout, zoom, picture_degree);
                    floatImageView.setAlpha(picture_alpha);
                    floatImageView.setDarken(picture_darken);
                    floatImageView.setPictureId(PictureId);
                }
                alertDialog.cancel();
            }
        }).start();
    }

    @NonNull
    private Preference requirePreference(CharSequence key) {
        return Objects.requireNonNull(findPreference(key));
    }

    private void PreferenceSet() {
        requirePreference(Config.PREFERENCE_PICTURE_NAME).setOnPreferenceClickListener(preference -> {
            setPictureName();
            return true;
        });
        requirePreference(Config.PREFERENCE_PICTURE_RESIZE).setOnPreferenceClickListener(preference -> {
            setPictureSize();
            return true;
        });
        requirePreference(Config.PREFERENCE_PICTURE_DEGREE).setOnPreferenceClickListener(preference -> {
            setPictureDegree();
            return true;
        });
        requirePreference(Config.PREFERENCE_PICTURE_ALPHA).setOnPreferenceClickListener(preference -> {
            setPictureAlpha();
            return true;
        });
        requirePreference(Config.PREFERENCE_PICTURE_DARKEN).setOnPreferenceClickListener(preference -> {
            setPictureDarken();
            return true;
        });
        CheckBoxPreference preference_touch_and_move = findPreference(Config.PREFERENCE_PICTURE_TOUCH_AND_MOVE);
        assert preference_touch_and_move != null;
        preference_touch_and_move.setChecked(touch_and_move);
        preference_touch_and_move.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                PictureTouchAndMoveAlert();
                return false;
            } else {
                setPictureTouchAndMove(false);
                return true;
            }
        });
        CheckBoxPreference preference_over_layout = findPreference(Config.PREFERENCE_ALLOW_PICTURE_OVER_LAYOUT);
        assert preference_over_layout != null;
        preference_over_layout.setChecked(allow_picture_over_layout);
        preference_over_layout.setOnPreferenceChangeListener((preference, newValue) -> {
            setAllowPictureOverLayout((boolean) newValue);
            return true;
        });
        CheckBoxPreference preference_fill_screen = findPreference(Config.PREFERENCE_PICTURE_FILL_SCREEN);
        assert preference_fill_screen != null;
        preference_fill_screen.setChecked(fill_screen);
        preference_fill_screen.setOnPreferenceChangeListener((preference, newValue) -> {
            setPictureFillScreen((boolean) newValue);
            return true;
        });
        requirePreference(Config.PREFERENCE_PICTURE_POSITION).setOnPreferenceClickListener(preference -> {
            setPicturePosition();
            return true;
        });
        CheckBoxPreference preference_filter_app = findPreference(Config.PREFERENCE_PICTURE_FILTER_APP_ENABLED);
        assert preference_filter_app != null;
        preference_filter_app.setChecked(filter_app_enabled);
        preference_filter_app.setOnPreferenceChangeListener((preference, newValue) -> {
            setPictureFilterAppEnabled((boolean) newValue);
            return true;
        });
        Preference preference_select_app = findPreference(Config.PREFERENCE_PICTURE_FILTER_APP);
        assert preference_select_app != null;
        updateFilterAppSummary(preference_select_app);
        preference_select_app.setOnPreferenceClickListener(preference -> {
            selectFilterApp(preference);
            return true;
        });
    }

    private void setAllowPictureOverLayout(boolean allow) {
        allow_picture_over_layout = allow;
        WindowsMethods.safeRemoveView(windowManager, floatImageView);
        floatImageView.setOverLayout(allow_picture_over_layout);
        WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow, position_x, position_y);
    }

    private void setPictureFillScreen(boolean fill) {
        fill_screen = fill;
        floatImageView.setFillScreen(fill);
        WindowsMethods.safeRemoveView(windowManager, floatImageView);
        if (fill) {
            floatImageView.setImageBitmap(bitmap);
        } else {
            floatImageView.setImageBitmap(ImageMethods.resizeBitmap(bitmap, zoom, picture_degree));
        }
        WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
    }

    private void setPictureFilterAppEnabled(boolean enabled) {
        filter_app_enabled = enabled;
        floatImageView.setFilterAppEnabled(enabled);
        if (!enabled) {
            filter_app_package = "";
            filter_app_name = "";
            floatImageView.setFilterAppPackage("");
        }
        Preference preference_select_app = findPreference(Config.PREFERENCE_PICTURE_FILTER_APP);
        if (preference_select_app != null) {
            updateFilterAppSummary(preference_select_app);
        }
    }

    private void updateFilterAppSummary(Preference preference) {
        if (filter_app_enabled && !filter_app_package.isEmpty()) {
            preference.setSummary(filter_app_name);
        } else if (filter_app_enabled) {
            preference.setSummary(getString(R.string.settings_picture_filter_app_sum));
        } else {
            preference.setSummary(getString(R.string.settings_picture_filter_app_none));
        }
    }

    private void selectFilterApp(Preference preference) {
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        java.util.List<android.content.pm.ResolveInfo> apps = requireActivity().getPackageManager().queryIntentActivities(intent, 0);
        java.util.ArrayList<AppItem> appList = new java.util.ArrayList<>();
        for (android.content.pm.ResolveInfo resolveInfo : apps) {
            String pkg = resolveInfo.activityInfo.packageName;
            String name = resolveInfo.loadLabel(requireActivity().getPackageManager()).toString();
            if (!pkg.equals(requireActivity().getPackageName())) {
                appList.add(new AppItem(pkg, name));
            }
        }
        java.util.Collections.sort(appList, (a, b) -> a.name.compareToIgnoreCase(b.name));
        String[] names = new String[appList.size()];
        for (int i = 0; i < appList.size(); i++) {
            names[i] = appList.get(i).name;
        }
        int checkedIndex = -1;
        if (!filter_app_package.isEmpty()) {
            for (int i = 0; i < appList.size(); i++) {
                if (appList.get(i).packageName.equals(filter_app_package)) {
                    checkedIndex = i;
                    break;
                }
            }
        }
        final int[] selectedIndex = {checkedIndex};
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.settings_picture_filter_app_dialog_title)
                .setSingleChoiceItems(names, checkedIndex, (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton(R.string.done, (dialog, which) -> {
                    if (selectedIndex[0] >= 0) {
                        filter_app_package = appList.get(selectedIndex[0]).packageName;
                        filter_app_name = appList.get(selectedIndex[0]).name;
                        floatImageView.setFilterAppPackage(filter_app_package);
                        updateFilterAppSummary(preference);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean hasUsageStatsPermission() {
        android.app.AppOpsManager appOps = (android.app.AppOpsManager) requireActivity().getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), requireActivity().getPackageName());
        return mode == android.app.AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.settings_usage_stats_permission_title)
                .setMessage(R.string.settings_usage_stats_permission_message)
                .setPositiveButton(R.string.settings_usage_stats_permission_grant, (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static class AppItem {
        final String packageName;
        final String name;
        AppItem(String packageName, String name) {
            this.packageName = packageName;
            this.name = name;
        }
    }

    private void setPictureTouchAndMove(boolean touchable_and_moveable) {
        touch_and_move = touchable_and_moveable;
        WindowsMethods.safeRemoveView(windowManager, floatImageView);
        floatImageView.setMoveable(touchable_and_moveable);
        WindowsMethods.createWindow(windowManager, floatImageView, touchable_and_moveable, allow_picture_over_layout, position_x, position_y);
    }

    private void PictureTouchAndMoveAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.settings_picture_touchable_and_moveable);
        builder.setMessage(R.string.settings_picture_touchable_and_moveable_warn);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.done, (dialog, which) -> {
            ((CheckBoxPreference) Objects.requireNonNull(findPreference(Config.PREFERENCE_PICTURE_TOUCH_AND_MOVE))).setChecked(true);
            setPictureTouchAndMove(true);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void setPictureName() {
        View mView = inflater.inflate(R.layout.dialog_edit_text, requireActivity().findViewById(R.id.layout_dialog_edit_text));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_name);
        final EditText editText = mView.findViewById(R.id.edittext_dialog);
        editText.setText(PictureName);
        dialog.setPositiveButton(R.string.done, (dialog12, which) -> {
            if (editText.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), R.string.settings_picture_name_warn, Toast.LENGTH_SHORT).show();
            } else {
                PictureName = editText.getText().toString();
            }
        });
        dialog.setNegativeButton(R.string.cancel, (dialog1, which) -> {
            if (editText.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), R.string.settings_picture_name_warn, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setView(mView);
        dialog.show();
    }

    private void setPictureSize() {
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom, picture_degree);
        onEditPicture(floatImageView_Edit);

        View mView = inflater.inflate(R.layout.dialog_set_size, requireActivity().findViewById(R.id.layout_dialog_set_size));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_resize);
        dialog.setCancelable(false);
        final float Max_Size = ImageMethods.getDefaultZoom(requireContext(), bitmap, true) * 100;
        TextView name = mView.findViewById(R.id.textview_set_size);
        name.setText(R.string.settings_picture_resize_size);
        final SeekBar seekBar = mView.findViewById(R.id.seekbar_set_size);
        seekBar.setMax((int) Max_Size);
        seekBar.setProgress((int) (zoom * 100));
        final EditText editText = mView.findViewById(R.id.edittext_set_size);
        editText.setText(String.valueOf(zoom));
        zoom_temp = zoom;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0) {
                    zoom_temp = ((float) progress) / 100;
                    editText.setText(String.valueOf(zoom_temp));
                    WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom_temp, picture_degree, position_x, position_y);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            float edittext_temp = Float.parseFloat(v.getText().toString());
            if (edittext_temp > 0 && (allow_picture_over_layout || edittext_temp <= Max_Size)) {
                zoom_temp = edittext_temp;
                if (!allow_picture_over_layout) {
                    seekBar.setProgress((int) (zoom_temp * 100));
                }
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom_temp, picture_degree, position_x, position_y);
            } else {
                Toast.makeText(getActivity(), R.string.settings_picture_resize_warn, Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        dialog.setPositiveButton(R.string.done, (__, which) -> {
            if (allow_picture_over_layout) {
                try {
                    zoom = Float.parseFloat(editText.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    zoom = zoom_temp;
                }
            } else {
                zoom = zoom_temp;
            }
            onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> onFailedEditPicture(floatImageView_Edit, bitmap_Edit));
        dialog.setView(mView);
        dialog.show();
    }

    private void setPictureDegree() {
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom, picture_degree);
        onEditPicture(floatImageView_Edit);

        View mView = inflater.inflate(R.layout.dialog_set_size, requireActivity().findViewById(R.id.layout_dialog_set_size));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_degree);
        dialog.setCancelable(false);
        TextView name = mView.findViewById(R.id.textview_set_size);
        name.setText(R.string.degree);
        final SeekBar seekBar = mView.findViewById(R.id.seekbar_set_size);
        seekBar.setMax(3600);
        seekBar.setProgress((int) (picture_degree * 10));
        final EditText editText = mView.findViewById(R.id.edittext_set_size);
        editText.setText(String.valueOf(((float) Math.round(picture_degree * 10)) / 10));
        picture_degree_temp = picture_degree;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                picture_degree_temp = ((float) progress) / 10;
                editText.setText(String.valueOf(((float) Math.round(picture_degree_temp * 10)) / 10));
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom, picture_degree_temp, position_x, position_y);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            float edittext_temp = Float.parseFloat(v.getText().toString());
            if (edittext_temp >= 0 && edittext_temp <= 360) {
                picture_degree_temp = edittext_temp;
                seekBar.setProgress((int) (picture_degree_temp * 10));
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touch_and_move, allow_picture_over_layout, zoom, picture_degree_temp, position_x, position_y);
            } else {
                Toast.makeText(getActivity(), R.string.settings_number_warn, Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        dialog.setPositiveButton(R.string.done, (__, which) -> {
            picture_degree = picture_degree_temp;
            onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> onFailedEditPicture(floatImageView_Edit, bitmap_Edit));
        dialog.setView(mView);
        dialog.show();
    }

    private void setPictureAlpha() {
        View mView = inflater.inflate(R.layout.dialog_set_size, requireActivity().findViewById(R.id.layout_dialog_set_size));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_alpha);
        dialog.setCancelable(false);
        TextView name = mView.findViewById(R.id.textview_set_size);
        name.setText(R.string.transparency);
        final SeekBar seekBar = mView.findViewById(R.id.seekbar_set_size);
        seekBar.setMax(100);
        seekBar.setProgress((int) (picture_alpha * 100));
        final EditText editText = mView.findViewById(R.id.edittext_set_size);
        editText.setText(String.valueOf(picture_alpha));
        picture_alpha_temp = picture_alpha;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                picture_alpha_temp = ((float) progress) / 100;
                editText.setText(String.valueOf(picture_alpha_temp));
                floatImageView.setAlpha(picture_alpha_temp);
                WindowsMethods.updateWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            float edittext_temp = Float.parseFloat(v.getText().toString());
            if (edittext_temp >= 0 && edittext_temp <= 100) {
                picture_alpha_temp = edittext_temp;
                seekBar.setProgress((int) (picture_alpha_temp * 100));
                floatImageView.setAlpha(picture_alpha_temp);
                WindowsMethods.updateWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
            } else {
                Toast.makeText(getActivity(), R.string.settings_number_warn, Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        dialog.setPositiveButton(R.string.done, (__, which) -> {
            picture_alpha = picture_alpha_temp;
            floatImageView.setAlpha(picture_alpha);
            WindowsMethods.updateWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> {
            floatImageView.setAlpha(picture_alpha);
            WindowsMethods.updateWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
        });
        dialog.setView(mView);
        dialog.show();
    }

    private void setPictureDarken() {
        View mView = inflater.inflate(R.layout.dialog_set_size, requireActivity().findViewById(R.id.layout_dialog_set_size));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_darken);
        dialog.setCancelable(false);
        TextView name = mView.findViewById(R.id.textview_set_size);
        name.setText(R.string.darken_amount);
        final SeekBar seekBar = mView.findViewById(R.id.seekbar_set_size);
        seekBar.setMax(100);
        seekBar.setProgress((int) (picture_darken * 100));
        final EditText editText = mView.findViewById(R.id.edittext_set_size);
        editText.setText(String.valueOf(picture_darken));
        picture_darken_temp = picture_darken;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                picture_darken_temp = ((float) progress) / 100;
                editText.setText(String.valueOf(picture_darken_temp));
                floatImageView.setDarken(picture_darken_temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            float edittext_temp = Float.parseFloat(v.getText().toString());
            if (edittext_temp >= 0 && edittext_temp <= 1) {
                picture_darken_temp = edittext_temp;
                seekBar.setProgress((int) (picture_darken_temp * 100));
                floatImageView.setDarken(picture_darken_temp);
            } else {
                Toast.makeText(getActivity(), R.string.settings_number_warn, Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        dialog.setPositiveButton(R.string.done, (__, which) -> {
            picture_darken = picture_darken_temp;
            floatImageView.setDarken(picture_darken);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> floatImageView.setDarken(picture_darken));
        dialog.setView(mView);
        dialog.show();
    }

    private void setPicturePosition() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        final boolean touchable_edit = (touch_and_move || sharedPreferences.getBoolean(Config.PREFERENCE_TOUCHABLE_POSITION_EDIT, false));
        bitmap_Edit = ImageMethods.getEditBitmap(getActivity(), bitmap);
        floatImageView_Edit = ImageMethods.createPictureView(getActivity(), bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree);
        onEditPicture(floatImageView_Edit);
        if (touchable_edit) {
            WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, true, allow_picture_over_layout, zoom, picture_degree, position_x, position_y);
        }

        View mView = inflater.inflate(R.layout.dialog_set_position, requireActivity().findViewById(R.id.layout_dialog_set_position));
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(R.string.settings_picture_position);
        dialog.setCancelable(false);
        Point size = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(size);
        final int Max_X = size.x;
        final int Max_Y = size.y;
        final SeekBar seekBar_x = mView.findViewById(R.id.seekbar_set_position_x);
        if (!allow_picture_over_layout) {
            seekBar_x.setMax(Max_X);
            seekBar_x.setProgress(position_x);
        }
        final EditText editText_x = mView.findViewById(R.id.edittext_set_position_x);
        editText_x.setText(String.valueOf(position_x));
        final SeekBar seekBar_y = mView.findViewById(R.id.seekbar_set_position_y);
        if (!allow_picture_over_layout) {
            seekBar_y.setMax(Max_Y);
            seekBar_y.setProgress(position_y);
        }
        final EditText editText_y = mView.findViewById(R.id.edittext_set_position_y);
        editText_y.setText(String.valueOf(position_y));
        if (allow_picture_over_layout) {
            editText_x.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            editText_y.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        position_x_temp = position_x;
        position_y_temp = position_y;
        seekBar_x.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position_x_temp = progress;
                editText_x.setText(String.valueOf(progress));
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText_x.setOnEditorActionListener((v, actionId, event) -> {
            try {
                int edittext_temp = Integer.parseInt(v.getText().toString());
                if (allow_picture_over_layout || (edittext_temp >= 0 && edittext_temp <= Max_X)) {
                    position_x_temp = edittext_temp;
                    if (!allow_picture_over_layout) {
                        seekBar_x.setProgress(edittext_temp);
                    }
                    WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
                } else {
                    Toast.makeText(getActivity(), R.string.settings_picture_position_warn, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        seekBar_y.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position_y_temp = progress;
                editText_y.setText(String.valueOf(progress));
                WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editText_y.setOnEditorActionListener((v, actionId, event) -> {
            try {
                int edittext_temp = Integer.parseInt(v.getText().toString());
                if (allow_picture_over_layout || (edittext_temp >= 0 && edittext_temp <= Max_Y)) {
                    position_y_temp = edittext_temp;
                    if (!allow_picture_over_layout) {
                        seekBar_y.setProgress(edittext_temp);
                    }
                    WindowsMethods.updateWindow(windowManager, floatImageView_Edit, bitmap_Edit, touchable_edit, allow_picture_over_layout, zoom, picture_degree, position_x_temp, position_y_temp);
                } else {
                    Toast.makeText(getActivity(), R.string.settings_picture_position_warn, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        if (allow_picture_over_layout) {
            seekBar_x.setEnabled(false);
            seekBar_y.setEnabled(false);
        }
        if (touchable_edit) {
            dialog.setNeutralButton(R.string.save_moved_position, (dialog1, which) -> {
                position_x = (int) floatImageView_Edit.getMovedPositionX();
                position_y = (int) floatImageView_Edit.getMovedPositionY();
                onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
            });
        }
        dialog.setPositiveButton(R.string.done, (__, which) -> {
            if (allow_picture_over_layout) {
                try {
                    position_x = Integer.parseInt(editText_x.getText().toString());
                    position_y = Integer.parseInt(editText_y.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    position_x = position_x_temp;
                    position_y = position_y_temp;
                }
            } else {
                position_x = position_x_temp;
                position_y = position_y_temp;
            }
            onSuccessEditPicture(floatImageView_Edit, bitmap_Edit);
        });
        dialog.setNegativeButton(R.string.cancel, (__, which) -> onFailedEditPicture(floatImageView_Edit, bitmap_Edit));
        dialog.setView(mView);
        dialog.show();
    }

    private void onEditPicture(FloatImageView FloatImageView_Edit) {
        if (!onUseEditPicture) {
            WindowsMethods.safeRemoveView(windowManager, floatImageView);
            floatImageView.refreshDrawableState();
            WindowsMethods.createWindow(windowManager, FloatImageView_Edit, touch_and_move, allow_picture_over_layout, position_x, position_y);
            onUseEditPicture = true;
        }
    }

    private void onSuccessEditPicture(FloatImageView floatImageView_Edit, Bitmap bitmap_Edit) {
        if (onUseEditPicture) {
            WindowsMethods.safeRemoveView(windowManager, floatImageView_Edit);
            floatImageView_Edit.refreshDrawableState();
            bitmap_Edit.recycle();
            if (fill_screen) {
                floatImageView.setImageBitmap(bitmap);
            } else {
                floatImageView.setImageBitmap(ImageMethods.resizeBitmap(bitmap, zoom, picture_degree));
            }
            WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
            onUseEditPicture = false;
        }
    }

    private void onFailedEditPicture(FloatImageView floatImageView_Edit, Bitmap bitmap_Edit) {
        if (onUseEditPicture) {
            WindowsMethods.safeRemoveView(windowManager, floatImageView_Edit);
            floatImageView_Edit.refreshDrawableState();
            bitmap_Edit.recycle();
            WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, allow_picture_over_layout, position_x, position_y);
            onUseEditPicture = false;
        }
    }

    public void saveAllData() {
        pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
        pictureData.put(Config.DATA_PICTURE_ZOOM, zoom);
        pictureData.put(Config.DATA_PICTURE_DEFAULT_ZOOM, default_zoom);
        pictureData.put(Config.DATA_PICTURE_ALPHA, picture_alpha);
        pictureData.put(Config.DATA_PICTURE_DARKEN, picture_darken);
        if (touch_and_move) {
            position_x = (int) floatImageView.getMovedPositionX();
            position_y = (int) floatImageView.getMovedPositionY();
        }
        pictureData.put(Config.DATA_PICTURE_POSITION_X, position_x);
        pictureData.put(Config.DATA_PICTURE_POSITION_Y, position_y);
        pictureData.put(Config.DATA_PICTURE_DEGREE, picture_degree);
        pictureData.put(Config.DATA_PICTURE_TOUCH_AND_MOVE, touch_and_move);
        pictureData.put(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, allow_picture_over_layout);
        pictureData.put(Config.DATA_PICTURE_FILL_SCREEN, fill_screen);
        pictureData.put(Config.DATA_PICTURE_FILTER_APP_ENABLED, filter_app_enabled);
        pictureData.put(Config.DATA_PICTURE_FILTER_APP_PACKAGE, filter_app_package);
        pictureData.put(Config.DATA_PICTURE_FILTER_APP_NAME, filter_app_name);
        pictureData.commit(PictureName);
        WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, touch_and_move, allow_picture_over_layout, zoom, picture_degree, position_x, position_y);
        ImageMethods.saveFloatImageViewById(requireActivity(), PictureId, floatImageView);
    }

    public void clearEditView() {
        if (onUseEditPicture) {
            if (floatImageView_Edit != null && bitmap_Edit != null) {
                onFailedEditPicture(floatImageView_Edit, bitmap_Edit);
            }
        }
    }

    public void exit() {
        if (!Edit_Mode) {
            if (floatImageView != null) {
                WindowsMethods.safeRemoveView(windowManager, floatImageView);
                bitmap.recycle();
                floatImageView = null;
            }
            ImageMethods.clearAllTemp(requireActivity(), PictureId);
        } else {
            float original_zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, zoom);
            float original_alpha = pictureData.getFloat(Config.DATA_PICTURE_ALPHA, picture_alpha);
            float original_darken = pictureData.getFloat(Config.DATA_PICTURE_DARKEN, picture_darken);
            float original_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, picture_degree);
            int original_position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, position_x);
            int original_position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, position_y);
            boolean original_allow_picture_over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, allow_picture_over_layout);
            boolean original_touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
            boolean original_fill_screen = pictureData.getBoolean(Config.DATA_PICTURE_FILL_SCREEN, Config.DATA_DEFAULT_PICTURE_FILL_SCREEN);
            boolean original_filter_app_enabled = pictureData.getBoolean(Config.DATA_PICTURE_FILTER_APP_ENABLED, Config.DATA_DEFAULT_PICTURE_FILTER_APP_ENABLED);
            String original_filter_app_package = pictureData.getString(Config.DATA_PICTURE_FILTER_APP_PACKAGE, Config.DATA_DEFAULT_PICTURE_FILTER_APP_PACKAGE);
            String original_filter_app_name = pictureData.getString(Config.DATA_PICTURE_FILTER_APP_NAME, Config.DATA_DEFAULT_PICTURE_FILTER_APP_NAME);
            floatImageView.setAlpha(original_alpha);
            floatImageView.setDarken(original_darken);
            floatImageView.setOverLayout(original_allow_picture_over_layout);
            floatImageView.setMoveable(original_touch_and_move);
            floatImageView.setFillScreen(original_fill_screen);
            floatImageView.setFilterAppEnabled(original_filter_app_enabled);
            floatImageView.setFilterAppPackage(original_filter_app_package);
            WindowsMethods.updateWindow(windowManager, floatImageView, bitmap, original_touch_and_move, original_allow_picture_over_layout, original_zoom, original_degree, original_position_x, original_position_y);
        }

    }

}
