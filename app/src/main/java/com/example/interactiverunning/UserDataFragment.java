package com.example.interactiverunning;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class UserDataFragment extends Fragment {
    private IFragmentListener fragmentListener;
    private EditText speedField;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentListener = (IFragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_data, container, false);
        speedField = view.findViewById(R.id.speed_field);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            if (fragmentListener != null) {
                fragmentListener.notifyListeners(speedField);
            }
            NavHostFragment.findNavController(UserDataFragment.this)
                    .navigate(R.id.action_UserDataFragment_to_FirstFragment);
        });

    }
}
