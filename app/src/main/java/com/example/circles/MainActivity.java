package com.example.circles;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import Chats.Chat;
import Chats.ChatsFragment;
import Profiles.MyProfile;
import User.User;
import User.UsersFragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FirebaseAuth firebaseAuth;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    CircleImageView profilePic;
    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //tabLayout sets up the tabs to switch from chats <--> user
        //viewPager allows swiping motion between tabs
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);

        //counts how many unread messages there are & displays
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //we need to create adapter using ViewPager --> create ViewPageAdapter
                ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
                //counter for # of unread messages
                int unread = 0;

                //create chat objects from firebase data
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    Chat chat = snapshot.getValue(Chat.class);
                    //if firebaseUser is the receiver of text and they have not opened text yet --> increment unread
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isSeen()) {
                            unread++;
                        }
                }
                //set on viewPager accordingly
                if(unread == 0) {
                    //make ChatsFragment obj inside adapter
                    viewPageAdapter.addFragment(new ChatsFragment(), "Chats");
                }
                else {
                    //make ChatsFragment obj inside adapter
                    viewPageAdapter.addFragment(new ChatsFragment(), "(" + unread + ") Chats");
                }

                //make UsersFragment obj inside adapter
                viewPageAdapter.addFragment(new UsersFragment(), "Users");
                //set adapter to ViewPager
                viewPager.setAdapter(viewPageAdapter);
                //set ViewPager to tabLayout
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //get logged in user's name & profilePic and display it on toolbar
        name = findViewById(R.id.userName);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("USERS").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                name.setText(user.getName());

                //check if user has profile pic uploaded
                //if not then upload default one
                if(user.getProfilePic() != null) {
                    Log.d("ProfilePic", user.getProfilePic());
                    if (user.getProfilePic().equals("default")) {
                        profilePic.setImageResource(R.drawable.ic_my_profile);
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

        //when profilePic is clicked on --> MyProfile
        profilePic = findViewById(R.id.profilePic);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MyProfile.class);
                v.getContext().startActivity(intent);
            }
        });

    }

    //inflates toolbar with items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items, menu);
        return true;
    }


    //when item is selected from toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            firebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, Login.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //handle onResume and onPause conditions
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    //get status when main activity is running or mimimized and update database
    private void status(String status) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            reference = FirebaseDatabase.getInstance().getReference("USERS").child(firebaseUser.getUid());
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    //override onBackPressed() to exit whole activity when back button pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
