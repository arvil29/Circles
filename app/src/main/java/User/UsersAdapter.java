package User;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.circles.R;

import java.util.List;

import Profiles.Profile;

//adapter to display list of friends that are accessible to chat w/ in their circle
//displays their name & activeStatus
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private Context context;
    private List<User> users;
    private boolean activeStatus;

    public UsersAdapter(Context context, List<User> users, boolean activeStatus) {
        this.context = context;
        this.users = users;
        this.activeStatus = activeStatus;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_list_users, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int i) {
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


        //if user's name is pressed go to Profile
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Profile.class);
                //send intent with ID of user that was clicked on
                intent.putExtra("ID", user.getID());
                intent.putExtra("Name", user.getName());
                intent.putExtra("Email", user.getEmail());
                intent.putExtra("ProfilePicLink", user.getProfilePic());

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
        private ImageView on;
        private ImageView off;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            on = itemView.findViewById(R.id.on);
            off = itemView.findViewById(R.id.off);
            profilePic = itemView.findViewById(R.id.profilePic);
        }
    }
}
