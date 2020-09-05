package Messaging;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.circles.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import Chats.Chat;
import Profiles.Profile;
import User.User;
import de.hdodenhof.circleimageview.CircleImageView;

//adapter to display messages after clicking on person to text with
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Chat> chats;
    private String ProfilePicLink;
    User user;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> chats, String ProfilePicLink, User user) {
        this.context = context;
        this.chats = chats;
        this.ProfilePicLink = ProfilePicLink;
        this.user = user;
    }

    //uses getItemViewType() method in ViewHolder class to evaluate if message for left/right side
    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //if message is type right --> inflate with appropriate layout
        if(viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
        //if message is type left --> inflate with appropriate layout
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int i) {
        //make chat object from chat gotten from that index i
        Chat chat = chats.get(i);
        //set user's message to holder to display on screen
        holder.message.setText(chat.getMessage());

        //displays the person's profile pic on left side of screen
        ///check if person has profile pic uploaded
        //if not then upload default one
        if (ProfilePicLink != null && holder.profilePic != null) {
            if (ProfilePicLink.equals("default")) {
                holder.profilePic.setImageResource(R.drawable.ic_user2_foreground);
            }
            //else set user's profilePic using Glide framework
            else {
                Glide.with(context.getApplicationContext()).load(ProfilePicLink).into(holder.profilePic);
            }
        }

        //if user's profile on left side chat is clicked
        if (holder.profilePic != null) {
            holder.profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), Profile.class);
                    intent.putExtra("ID", user.getID());
                    intent.putExtra("Name", user.getName());
                    intent.putExtra("ProfilePicLink", ProfilePicLink);
                    v.getContext().startActivity(intent);
                }
            });
        }

        //checks for last message
        if (i == chats.size() - 1) {
            if (chat.isSeen()) {
                holder.seen.setText("Seen ✓✓");
            } else {
                holder.seen.setText("Delivered");
            }
        } else {
            holder.seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    //ViewHolder classs
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        CircleImageView profilePic;
        TextView seen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.showText);
            profilePic = itemView.findViewById(R.id.profilePic);
            seen = itemView.findViewById(R.id.seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //if sender of text is firebase user --> I am sending the message
        //thus return MSG_TYPE_RIGHT
        if(chats.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        //if not that means I am not sending any texts --> goes on left side
        //thus returns MSG_TYPE_LEFT
        else {
            return MSG_TYPE_LEFT;
        }
    }
}

