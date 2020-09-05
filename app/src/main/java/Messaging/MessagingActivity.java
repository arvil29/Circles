package Messaging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.circles.MainActivity;
import com.example.circles.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Chats.Chat;
import Profiles.Profile;
import User.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagingActivity extends AppCompatActivity {
    CircleImageView profilePic;
    TextView Name; //has to be written same way as on Firebase
    Intent intent;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    ImageView sendButton;
    EditText sendText;

    MessageAdapter messageAdapter;
    List<Chat> chats;
    RecyclerView recyclerView;
    User user;

    ValueEventListener seenListener;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        //set toolbar & back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessagingActivity.this, MainActivity.class).setFlags((Intent.FLAG_ACTIVITY_CLEAR_TOP)));
            }
        });

        //set toolbar contents like user's name and profilePic
        profilePic = findViewById(R.id.profilePic);
        Name = findViewById(R.id.userName);

        //set sending button and text input box
        sendButton = findViewById(R.id.sendButton);
        sendText = findViewById(R.id.sendText);


        recyclerView = findViewById(R.id.messageRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        linearLayout.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayout);


        //recieve ID passed  by intent from UserAdap class
        intent = getIntent();
        final String ID = intent.getStringExtra("ID");


        //when send button is clicked send sender's ID, receiver's ID, and text to sendText() method
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = sendText.getText().toString();
                if(!message.equals("")) {
                    sendText(firebaseUser.getUid(), ID, message);
                }
                else {
                    Toast.makeText(MessagingActivity.this, "Please type something", Toast.LENGTH_SHORT).show();
                }
                sendText.setText("");
            }
        });

        //when profilePic on toolbar is pressed
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Profile.class);
                intent.putExtra("ID", user.getID());
                intent.putExtra("Name", user.getName());
                intent.putExtra("ProfilePicLink", user.getProfilePic());
                v.getContext().startActivity(intent);
            }
        });



        //Display everything onto screen of MessagingActivity
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get clicked user's info from firebase w/ ID passed into child
        reference = FirebaseDatabase.getInstance().getReference("USERS").child(ID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //create User object of user that was clicked
                user = dataSnapshot.getValue(User.class);
                assert user != null;
                //set their name at top in textView of toolbar
                Name.setText(user.getName());


                //check if user has profile pic uploaded & display on toolbar
                //if not then upload default one
                if(user.getProfilePic() != null) {
                    if (user.getProfilePic().equals("default")) {
                        profilePic.setImageResource(R.mipmap.ic_user2);
                    }
                    //else set user's profilePic using Glide framework
                    else {
                        Glide.with(getApplicationContext()).load(user.getProfilePic()).into(profilePic);
                    }
                }
                //read the text by passing in myID, other guy's ID, & their profile pic and set message to adapter
                readText(firebaseUser.getUid(), ID, user.getProfilePic());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        //check to see if text was seen & update hashmap on firebase
        seenText(ID);
    }



    //stores parameters into hashmap and pushes it to firebase to save texts
    public void sendText(String senderID, String receiverID, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", senderID);
        hashMap.put("receiver", receiverID);
        hashMap.put("message", message);
        hashMap.put("seen", false);

        reference.child("Chats").push().setValue(hashMap);
    }

    //takes in my ID, friend'sS ID, and their profilePic
    //uses those info to display text onto screen
    private void readText(final String myID, final String userID, final String profilePic) {
        chats = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //create Chat object from chats info passed in --> senderID, receiverID, message
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getSender().equals(myID) && chat.getReceiver().equals(userID) ||
                            chat.getReceiver().equals(myID) && chat.getSender().equals(userID)) {
                        chats.add(chat);
                    }

                    //update adapter with messages being sent
                    messageAdapter = new MessageAdapter(MessagingActivity.this, chats, profilePic, user);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //update status according to app state
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }


    //get status when messaging activity is open/closed and update firebase
    private void status(String status) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("USERS").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }



    //checks to see if message was seen
    private void seenText(String ID) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    //Log.d("chat", chat.getMessage() + ", " + chat.getReceiver() + ", " + chat.getSender());
                    if (chat != null && chat.getReceiver() != null && chat.getSender() != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(ID)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("seen", true);
                            snapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
