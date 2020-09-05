package Profiles;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.circles.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import User.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfile extends AppCompatActivity {
    TextView Name;
    TextView Email;
    TextView Mobile;
    ImageView changeProfilePic;
    Toolbar toolbar;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    //we create 2 views for profile pc bc circleImageView distorts img quality for default pic
    ImageView profilePicDefault;
    CircleImageView profilePic;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask; //stores into firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        //setup toolbar w/ back button
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initialization
        Name = findViewById(R.id.name);
        Email = findViewById(R.id.email);
        Mobile = findViewById(R.id.mobile);
        profilePic = findViewById(R.id.profilePic);
        changeProfilePic = findViewById(R.id.changeProfilePic);
        profilePicDefault = findViewById(R.id.profilePicDefault);

        //get logged in user's info from firebase and display
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("USERS").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                Name.setText(user.getName());
                Email.setText("Email: " + user.getEmail());
                Mobile.setText("Mobile: " + user.getMobile());

                //check if user has profile pic uploaded
                //if not then upload default one
                if (user.getProfilePic() != null) {
                    if (user.getProfilePic().equals("default")) {
                        profilePicDefault.setImageResource(R.drawable.ic_my_profile);
                    }
                    //else set user's profilePic using Glide framework
                    else {
                        Glide.with(getApplicationContext()).load(user.getProfilePic()).into(profilePic);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        //stores uploaded profilePics in Uploads folder that gets created in firebase
        storageReference = FirebaseStorage.getInstance().getReference("Uploads");

        //when user clicks on changeProfilePic icon to change it
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
    }

    //------------------------------------------------UPLOADING PROFILE PIC----------------------------------------------------------------
    //opens gallery
    public void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    //after pic is selected this method is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            //shows us address of image gotten from phone
            imageUri = data.getData();

            //if uploadTask is in progress display toast to notify
            if(uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getApplicationContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            }
            //if not then uploadImage() is called to start the uploading process
            else {
                uploadImage();
            }
        }
    }

    private void uploadImage() {
        //the spinning dialogbox that shows when uploading
        final ProgressDialog pd = new ProgressDialog(MyProfile.this);
        pd.setMessage("Uploading...");
        pd.show();

        //if image address is not null --> convert to format that can be stored into firebase
        if(imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            //puts imageUri into firebase
            uploadTask = fileReference.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    //if task successful --> get uri and convert to string to set to "ProfilePic" in firebase
                    if(task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        //Log.d("uri", mUri);

                        //get firebase reference, update map w/ profilePic, and update map in reference
                        reference = FirebaseDatabase.getInstance().getReference("USERS").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("ProfilePic", mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    }
                    //if task not successful --> show toast
                    else {
                        Toast.makeText(getApplicationContext(), "Upload failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                //show error message for why task failed
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    //retrieves uploaded pic's MIME type and returns it
    //ex: for .jpeg files MIME type is image/jpeg
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


}
