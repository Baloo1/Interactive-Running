package com.example.interactiverunning;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class FirstFragment extends Fragment {
    private FragmentListener fragmentListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        fragmentListener = (FragmentListener) activity;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_first, container, false);
        Button startButton = view.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {
            if (fragmentListener != null) {
                fragmentListener.notifyListeners(v);
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));
    }

    public interface FragmentListener {
        void notifyListeners(View button);
    }
}