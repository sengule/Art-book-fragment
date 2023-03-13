package com.ertu_.artbookwithfragment.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.room.Room;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ertu_.artbookwithfragment.R;
import com.ertu_.artbookwithfragment.databinding.FragmentImageUploadBinding;
import com.ertu_.artbookwithfragment.model.Art;
import com.ertu_.artbookwithfragment.roomdb.ArtDao;
import com.ertu_.artbookwithfragment.roomdb.ArtDatabase;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class ImageUploadFragment extends Fragment {

    ActivityResultLauncher<Intent> activityResultLauncher; //to pick something from any other applications
    ActivityResultLauncher<String> permissionLauncher;
    ImageView view1;
    Bitmap selectedImage;
    SQLiteDatabase db;
    ArtDatabase artDatabase;
    ArtDao artDao;
    Art artFromMain;

    private EditText nameText;
    private EditText authorText;
    private EditText yearText;

    private FragmentImageUploadBinding binding;
    private final CompositeDisposable mDisposable = new CompositeDisposable();


    public ImageUploadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //database builder and register launcher is initialize at oncreate method
        registerLauncher();

        artDatabase = Room.databaseBuilder(requireContext(),
                        ArtDatabase.class, "Arts")
                .build();

        artDao = artDatabase.artDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentImageUploadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = requireActivity().openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null);


        nameText = view.findViewById(R.id.nameText);
        authorText = view.findViewById(R.id.artistText);
        yearText = view.findViewById(R.id.yearText);

        Button saveButton = view.findViewById(R.id.save_button);

        view1 = view.findViewById(R.id.imageView);

        String information = ImageUploadFragmentArgs.fromBundle(getArguments()).getInfo();

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(view);
            }
        });

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(v);
            }
        });


        if(information.equals("new")){
            nameText.setText("");
            authorText.setText("");
            yearText.setText("");
            view1.setImageResource(R.drawable.selectimage);
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);
        }else if(information.equals("old")){
            saveButton.setVisibility(View.INVISIBLE);

            int id = ImageUploadFragmentArgs.fromBundle(getArguments()).getId();
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

            mDisposable.add(artDao.getArtById(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ImageUploadFragment.this::handleResponseWithOldArt));
        }

    }

    private void handleResponseWithOldArt(Art art) {
        artFromMain = art;
        binding.nameText.setText(art.name);
        binding.artistText.setText(art.artistName);
        binding.yearText.setText(art.year);

        Bitmap bitmap = BitmapFactory.decodeByteArray(art.image,0,art.image.length);
        binding.imageView.setImageBitmap(bitmap);
    }

    private void save(View view){

        String name = nameText.getText().toString();
        String artistName = authorText.getText().toString();
        String year = yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);
        int artId = ImageUploadFragmentArgs.fromBundle(getArguments()).getId();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        Art art = new Art(name, artistName, year, byteArray);

        mDisposable.add(artDao.insert(art)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ImageUploadFragment.this::handleResponse));

    }

    private void delete(View view){
        mDisposable.add(artDao.delete(artFromMain)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ImageUploadFragment.this::handleResponse));
    }

    private void handleResponse() {
        NavDirections action = ImageUploadFragmentDirections.actionImageUploadFragmentToHomeFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    private void selectImage(View view) {
        //context compat method is used for "any android version" can be used by user
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view, "Permission needed for galery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            //request permission

        }else {
            //galery
            openGallery();
        }
    }

    private void openGallery(){
        Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // this intent uses the gallery uri to go there
        activityResultLauncher.launch(intentToGallery);
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if(bitmapRatio > 1){
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }else{
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return image.createScaledBitmap(image,width,height,true);
    }


    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(     new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == -1){
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null){
                        Uri imageData = intentFromResult.getData();
                        //binding.imageView.setImageURI(imageData);
                        //the image must be converted into bitmap to put the image data to database
                        try {

                            if (Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                view1.setImageBitmap(selectedImage);
                            }else{
                                selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageData);
                                view1.setImageBitmap(selectedImage);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    //permission denied
                    Toast.makeText(getActivity(),"Permission needed",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mDisposable.clear();
    }


}