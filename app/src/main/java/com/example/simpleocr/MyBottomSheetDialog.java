package com.example.simpleocr;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * @author 30415
 */
public class MyBottomSheetDialog extends BottomSheetDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.about);

        BottomSheetBehavior<FrameLayout> behavior = dialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //初始化控件
        MaterialButton info = view.findViewById(R.id.about);
        MaterialButton deleteAll = view.findViewById(R.id.deleteAll);

        info.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.appInfo)
                    .setMessage(R.string.content)
                    .setPositiveButton(R.string.confirm, null)
                    .show();
            dismiss();
        });

        deleteAll.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.deleteAll))
                    .setPositiveButton(getString(R.string.confirm),
                            (dialog, which) -> {
                                Bundle result = new Bundle();
                                result.putBoolean("clear", true);
                                fragmentManager.setFragmentResult("requestKey", result);
                            })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setCancelable(false)
                    .show();
            dismiss();
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about, container, false);
    }
}
