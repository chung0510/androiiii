package com.example.smartbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.ViewHolder> {

    ArrayList<OptionModel> list;
    OnOptionClick listener;

    int selectedPosition = -1;

    public interface OnOptionClick{
        void onClick(OptionModel model);
    }

    public OptionAdapter(ArrayList<OptionModel> list, OnOptionClick listener) {
        this.list = list;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle,tvPrice;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            card = (MaterialCardView) itemView;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        OptionModel model = list.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvPrice.setText(model.getPrice());

        if(selectedPosition == position){
            holder.card.setCardBackgroundColor(
                    holder.itemView.getResources().getColor(R.color.green_main)
            );

            holder.tvTitle.setTextColor(
                    holder.itemView.getResources().getColor(android.R.color.white)
            );

            holder.tvPrice.setTextColor(
                    holder.itemView.getResources().getColor(android.R.color.white)
            );

        }else{

            holder.card.setCardBackgroundColor(
                    holder.itemView.getResources().getColor(android.R.color.white)
            );
        }

        holder.itemView.setOnClickListener(v -> {

            selectedPosition = position;

            notifyDataSetChanged();

            listener.onClick(model);

            Animation animation = AnimationUtils
                    .loadAnimation(holder.itemView.getContext(),
                            android.R.anim.fade_in);

            holder.itemView.startAnimation(animation);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}