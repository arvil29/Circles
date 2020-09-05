package Chats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.circles.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import Messaging.MessagingActivity;
import User.User;

//adapter to display list of friends that user is chatting with currently
//displays their name, activeStatus, and last text sent/received
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder>{
    private Context context;
    private List<User> users;
    private boolean activeStatus;

    String theLastText;

    public ChatsAdapter(Context context, List<User> users, boolean activeStatus) {
        this.context = context;
        this.users = users;
        this.activeStatus = activeStatus;
    }

    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_list_chats, parent, false);
        return new ChatsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.ViewHolder holder, int i) {
        String userName = users.get(i).getName();
        holder.name.setText(userName);
        final User user = users.get(i);

        ///check if user has profile pic uploaded
        //if not then upload default one
        if(user.getProfilePic() != null) {
            if (user.getProfilePic().equals("default")) {
                holder.profilePic.setImageResource(R.drawable.ic_user2_foreground);
            }
            //else set user's profilePic using Glide framework
            else {
                Glide.with(context).load(user.getProfilePic()).into(holder.profilePic);
            }
        }


        //if activeStatus == true --> lastMessage() to display last text
        if(activeStatus) {
            lastMessage(user.getID(), holder.lastText);
        }
        else {
            holder.lastText.setVisibility(View.GONE);
        }


        //if activeStatus == true & user's status == online --> green online button visible
        if(activeStatus) {
            if(user.getStatus().equals("online")) {
                holder.on.setVisibility(View.VISIBLE);
                holder.off.setVisibility(View.GONE);
            }
            else {
                holder.off.setVisibility(View.VISIBLE);
                holder.on.setVisibility(View.GONE);
            }
        }
        else {
            holder.on.setVisibility(View.GONE);
            holder.off.setVisibility(View.GONE);
        }


        //if user's name is pressed go to MessagingActivity to start texting
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessagingActivity.class);
                //send intent with ID of user that was clicked on
                intent.putExtra("ID", user.getID());
                intent.putExtra("ProfilePic", user.getProfilePic());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView profilePic;
        ImageView on;
        ImageView off;
        TextView lastText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            on = itemView.findViewById(R.id.on);
            off = itemView.findViewById(R.id.off);
            lastText = itemView.findViewById(R.id.lastText);
            profilePic = itemView.findViewById(R.id.profilePic);
        }
    }

    //check for last text
    private void lastMessage(String userID, TextView lastText) {
        theLastText = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        //get reference to "Chats" in firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //create 1 chat obj from reference for each chat
                    Chat chat = snapshot.getValue(Chat.class);
                    //check w/ conditions to see if text was last text or not
                    if (chat != null && chat.getReceiver() != null && chat.getSender() != null && firebaseUser != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userID) ||
                                chat.getReceiver().equals(userID) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastText = chat.getMessage();

                            //change lastText color if new text was received by user and unseen
                            if(chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isSeen()) {
                                lastText.setTextColor(Color.parseColor("#0000FF"));
                            }
                        }

                    }
                }
                //if was able to connect to firebase and get messages then display messages accordingly
                switch(theLastText) {
                    case "default":
                        lastText.setText("No Message");
                        break;
                    default:
                        lastText.setText(theLastText);
                        break;
                }
                theLastText = "default";

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
