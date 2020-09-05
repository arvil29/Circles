package User;

//make sure items are listed in order & name that they are found in firebase

public class User {
    private String Email;
    private String ID;
    private String Mobile;
    private String Name;
    private String ProfilePic;
    private String status;


    User() {
    }

    public User(String ID, String Name, String Email, String Mobile, String ProfilePic, String status) {
        this.Email = Email;
        this.ID = ID;
        this.Mobile = Mobile;
        this.Name = Name;
        this.ProfilePic = ProfilePic;
        this.status = status;
    }

    public String getEmail() {
        return Email;
    }

    public String getID() {
        return ID;
    }

    public String getMobile() {
        return Mobile;
    }

    public String getName() {
        return Name;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public String getStatus() {
        return status;
    }



    public void setEmail(String email) {
        this.Email = Email;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setMobile(String mobile) {
        this.Mobile = Mobile;
    }

    public void setName(String name) {
        this.Name = Name;
    }

    public void setProfilePic(String profilePic) {
        this.ProfilePic = profilePic;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
