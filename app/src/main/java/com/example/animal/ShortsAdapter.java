    package com.example.animal;


    import static androidx.core.app.ActivityCompat.startActivityForResult;

    import android.content.Context;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.media.Image;
    import android.provider.MediaStore;
    import android.util.Base64;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import androidx.recyclerview.widget.RecyclerView;

    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Random;

        public class ShortsAdapter extends RecyclerView.Adapter<ShortsAdapter.ImageViewHolder> {


            private Context context;
            private List<String> imageUrls; // 이미지 URL 리스트

                private FirebaseFirestore db;

                public ShortsAdapter(Context context) {
                    this.context = context;
                    this.db = FirebaseFirestore.getInstance();
                    imageUrls = new ArrayList<>();

                    // Firestore에서 이미지를 가져오는 비동기 작업
                    loadImagesFromFirestore();
                }

                private void loadImagesFromFirestore() {
                    db.collection("shorts")
                            .get()  // 컬렉션의 모든 문서를 가져옴
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                imageUrls.clear();  // 기존 데이터를 초기화
                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                    String encodedImage = document.getString("image");  // 각 문서에서 이미지 데이터 가져오기
                                    if (encodedImage != null) {
                                        imageUrls.add(encodedImage);  // 리스트에 추가
                                    }
                                }
                                notifyDataSetChanged();  // RecyclerView 업데이트
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "이미지 로드 실패", e);
                            });
                }

                @Override
                public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(context).inflate(R.layout.item_shorts, parent, false);
                    return new ImageViewHolder(view);
                }

                @Override
                public void onBindViewHolder(ImageViewHolder holder, int position) {
                    if (position < imageUrls.size()) {
                        String encodedImage = imageUrls.get(position);
                        // Base64로 인코딩된 이미지를 디코딩하여 비트맵으로 변환
                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.imageView.setImageBitmap(decodedBitmap);
                    }
                }

                @Override
                public int getItemCount() {
                    return imageUrls.size();
                }

                public static class ImageViewHolder extends RecyclerView.ViewHolder {
                    ImageView imageView;

                    public ImageViewHolder(View itemView) {
                        super(itemView);
                        imageView = itemView.findViewById(R.id.imageView);
                    }
                }
            }
