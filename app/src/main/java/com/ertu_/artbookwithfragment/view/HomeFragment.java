package com.ertu_.artbookwithfragment.view;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ertu_.artbookwithfragment.databinding.FragmentHomeBinding;
import com.ertu_.artbookwithfragment.model.Art;
import com.ertu_.artbookwithfragment.R;
import com.ertu_.artbookwithfragment.adapter.ArtAdapter;
import com.ertu_.artbookwithfragment.roomdb.ArtDao;
import com.ertu_.artbookwithfragment.roomdb.ArtDatabase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class HomeFragment extends Fragment {

    private View view;
    public ArrayList<Art> artArrayList;
    public ArtAdapter artAdapter;

    private FragmentHomeBinding binding;

    ArtDatabase artDatabase;
    ArtDao artDao;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //database processing in onCreate method
        artArrayList = new ArrayList<>();

        artDatabase = Room.databaseBuilder(requireContext(),
                                            ArtDatabase.class,"Arts")
                                            .build();
        artDao = artDatabase.artDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        getData();
    }

    private void getData(){

        mDisposable.add(artDao.getArtWithNameAndId()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(HomeFragment.this::handleResponse));

    }

    private void handleResponse(List<Art> artList) {

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        artAdapter = new ArtAdapter(artList);
        binding.recyclerView.setAdapter(artAdapter);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.add_art){
            HomeFragmentDirections.ActionHomeFragmentToImageUploadFragment action = HomeFragmentDirections.actionHomeFragmentToImageUploadFragment();
            action.setInfo("new");
            Navigation.findNavController(view).navigate(action);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mDisposable.clear();
    }
}