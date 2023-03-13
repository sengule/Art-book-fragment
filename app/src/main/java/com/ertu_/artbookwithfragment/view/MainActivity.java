package com.ertu_.artbookwithfragment.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ertu_.artbookwithfragment.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    /*@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_art){
            // go to ImageUploadFragment
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToImageUploadFragment();
            Navigation.findNavController() //????
                    .navigate(action);
        }
        return super.onOptionsItemSelected(item);
    }
     */

}