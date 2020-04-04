package com.rajendraprasad.videoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rajendraprasad.videoplayer.databinding.PartialAllVideosRowItemBinding;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AllVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> allVideoPathList = new ArrayList<>();

    public AllVideosAdapter(Context context, ArrayList<String> allVideoPath) {
        this.mContext = context;
        this.allVideoPathList = allVideoPath;
        mInflater = LayoutInflater.from(mContext);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        PartialAllVideosRowItemBinding itemBinding = PartialAllVideosRowItemBinding.inflate(mInflater, viewGroup, false);
        return new ItemHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final ItemHolder itemHolder = (ItemHolder) viewHolder;

        String videoName=allVideoPathList.get(i);
        int index=videoName.lastIndexOf('/');
//        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(allVideoPathList.get(i), MediaStore.Video.Thumbnails.MICRO_KIND);
//        itemHolder.binding.ivThumbnail.setImageBitmap(bMap);

        File file = new File(videoName);
        Date lastModDate = new Date(file.lastModified());
        String date = lastModDate.toString();

        SimpleDateFormat spf=new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        Date newDate= null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf= new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss");
        date = spf.format(newDate);
        itemHolder.binding.tvDate.setText(mContext.getString(R.string.created_on)+" : "+date);

        itemHolder.binding.tvVideoName.setText((i+1)+" . "+videoName.substring(index).replace("/",""));
    }

    @Override
    public int getItemCount() {
        return allVideoPathList.size();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

        public PartialAllVideosRowItemBinding binding;

        public ItemHolder(final PartialAllVideosRowItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
