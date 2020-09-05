package User;

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

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<User> users;
    private UsersAdapter userRecyclerAdap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        //create array to store all users from firebase
        users = new ArrayList<>();

        //readUsers() to store them into users array and update on UsersAdapter
        readUsers();

        return view;
    }

    //reads all users in firebase to display onto Users fragment
    public void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("USERS");

        //goes thru everything stored in firebase database
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //gets every user snapshot from "USERS" children in firebase and make User obj
                    User user = snapshot.getValue(User.class);

                    //Display other users with an account
                    assert user != null;
                    assert firebaseUser != null;
                    //find all IDs in firebase that don't match currently logged in user & add to users arraylist defined above
                    if(!user.getID().equals(firebaseUser.getUid())) {
                        //Log.d("UserID", "matched!");
                        users.add(user);

                        //print users
//                        for(int i = 0; i < users.size(); i++) {
//                            Log.d("users", users.get(i).getName() + ", " + users.get(i).getEmail());
//                        }
                    }
                }
                //update UsersAdapter w/ all users available
                userRecyclerAdap = new UsersAdapter(getContext(), users, true);
                recyclerView.setAdapter(userRecyclerAdap);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
