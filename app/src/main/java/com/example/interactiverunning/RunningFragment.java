package com.example.interactiverunning;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class RunningFragment extends Fragment {
    private IFragmentListener fragmentListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentListener = (IFragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_running_view, container, false);

        Button startButton = view.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {
            if (fragmentListener != null) {
                fragmentListener.notifyListeners(v);
            }
        });

        Button stopButton = view.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> {
            if (fragmentListener != null) {
                fragmentListener.notifyListeners(v);
            }
        });
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
