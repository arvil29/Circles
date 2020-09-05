package Profiles;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.circles.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import Messaging.MessagingActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    TextView name;
    ImageView sendMessage;
    ImageView sendEmail;
    Intent intent;
    Toolbar toolbar;
    CircleImageView profilePic;
    ImageView profilePicDefault;

    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initialization
        toolbar = findViewById(R.id.toolbar);
        name = findViewById(R.id.name);
        sendMessage = findViewById(R.id.sendMessage);
        sendEmail = findViewById(R.id.sendEmail);
        profilePic = findViewById(R.id.profilePic);
        profilePicDefault = findViewById(R.id.profilePicDefault);

        storageReference = FirebaseStorage.getInstance().getReference("Uploads");

        //setup toolbar w/ back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //get ID, name, and profilePic passed from UsersAdapter
        intent = getIntent();
        final String ID = intent.getStringExtra("ID");
        final String Name = intent.getStringExtra("Name");
        final String Email = intent.getStringExtra("Email");
        final String ProfilePicLink = intent.getStringExtra("ProfilePicLink");

        //set friend's name at top
        name.setText(Name);

        //check if friend has profile pic uploaded
        //if not then upload default one
        if(ProfilePicLink != null) {
            if (ProfilePicLink.equals("default")) {
                profilePicDefault.setImageResource(R.mipmap.ic_user2_round);
            }
            //else set user's profilePic using Glide framework
            else {
                Glide.with(getApplicationContext()).load(ProfilePicLink).into(profilePic);
            }
        }

        //open MessageActivity once message button is clicked
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MessagingActivity.class);
                //send intent with ID of user that was clicked on
                intent.putExtra("ID", ID);
                v.getContext().startActivity(intent);
            }
        });

        //open email once email button is clicked
        //open email w/ receiver's email already put into "Recipient" blank
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] {Email});
                startActivity(Intent.createChooser(intent, "Choose an email client"));
            }
        });

    }


}
