package com.ertu_.artbookwithfragment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.ertu_.artbookwithfragment.databinding.RecyclerRowBinding;
import com.ertu_.artbookwithfragment.model.Art;
import com.ertu_.artbookwithfragment.view.HomeFragmentDirections;

import java.util.List;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    List<Art> artList;

    public ArtAdapter(List<Art> artList) {
        this.artList = artList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.recycleViewTextView.setText(artList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeFragmentDirections.ActionHomeFragmentToImageUploadFragment action = HomeFragmentDirections.actionHomeFragmentToImageUploadFragment();
                action.setId(artList.get(position).id);
                action.setInfo("old");
                Navigation.findNavController(view).navigate(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }

    class ArtHolder extends RecyclerView.ViewHolder {

        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
