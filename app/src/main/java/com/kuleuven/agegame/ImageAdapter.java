package com.kuleuven.agegame;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Bitmap> images;
    private List<Integer> ageList;
    private List<Integer> yourGuessList;

    public ImageAdapter(List<Bitmap> images, List<Integer> ageList, List<Integer> yourGuessList) {
        this.images = images;
        this.ageList = ageList;
        this.yourGuessList = yourGuessList;

    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView age;
        TextView yourGuess;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            age = itemView.findViewById(R.id.text_view1);
            yourGuess = itemView.findViewById(R.id.text_view2);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Bitmap image = images.get(position);
        int age = ageList.get(position);
        int yourGuess = yourGuessList.get(position);
        holder.imageView.setImageBitmap(image);
        String ageMsg = "The actual age: " + age;
        String yourGuessMsg = "Your guess was: " + yourGuess;
        holder.age.setText(ageMsg);
        holder.yourGuess.setText(yourGuessMsg);

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}

