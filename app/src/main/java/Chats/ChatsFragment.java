package Chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.circles.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import User.User;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatsAdapter chatRecyclerAdap;
    private List<User> users;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private List<String> textTransacRec;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //get user currently logged in
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        textTransacRec = new ArrayList<>();

        //get database reference to chat data
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textTransacRec.clear();

                //get every chat snapshot of "Chats" children and make Chat obj
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    //if the chat's senderID is equal to the logged in user's ID
                    //that means logged in user has sent a message to someone
                    //add receiver's ID to list
                    if(chat.getSender().equals(firebaseUser.getUid())) {
                        textTransacRec.add(chat.getReceiver());
                    }

                    //if chat's receiverID is equal to the logged in user's ID
                    //that means logged in user has been sent a message from someone else
                    //add sender's ID to list
                    if(chat.getReceiver().equals(firebaseUser.getUid())) {
                        textTransacRec.add(chat.getSender());
                    }
                }
                //call method to evaluate who user has chatted w/ using textTransacRec
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return view;
    }

    //method to evaluate who you have chatted with and display on Chats fragment accordingly
    private void readChats() {
        users = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("USERS");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();

                //gets every user snapshot from "USERS" children in firebase and make User obj
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    //for every ID that was stored in textTransacRec list
                    for (String ID : textTransacRec) {
                        //out of all existing users in firebase if a user's ID matches any of the ID's in textTransacRec
                        if(user.getID().equals(ID)) {
                            //if there are any User objects inside users list
                            if(users.size() != 0) {
                                //check to see if the user already exists in list or not by matching w/ existing user ID's
                                //if there is no ID match then user does not exist --> add to users list to display
                                //also makes sure user is added to chats fragment only once
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                            boolean ifExists = users.stream().anyMatch(u -> u.getID().equals(user.getID()));
                                            if(!ifExists) {
                                                users.add(user);
                                            }
                                        }
                            }
                            //if no User objects inside users list --> add to users list
                            else {
                                users.add(user);
                            }
                        }
                    }
                }
                //update list to adapter to display in recyclerView
                chatRecyclerAdap = new ChatsAdapter(getContext(), users, true);
                recyclerView.setAdapter(chatRecyclerAdap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
