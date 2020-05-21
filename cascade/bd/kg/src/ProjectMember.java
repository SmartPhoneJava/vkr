import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

public class ProjectMember {
    @SerializedName("id")
    public int ID;
    @SerializedName("name")
    public String Name;
    @SerializedName("photo_title")
    public String PhotoTitle;
    @SerializedName("user")
    public UserInProject User;
    @SerializedName("token")
    public ProjectToken Token;

    ProjectMember(int id, String name, String photoTitle,  UserInProject user, ProjectToken token) {
        ID = id;
        Name = name;
        PhotoTitle = photoTitle;
        User = user;
        Token = token;
    }

    public static final Comparator<ProjectMember> COMPARE_BY_ID = new Comparator<ProjectMember>() {
        @Override
        public int compare(ProjectMember left, ProjectMember right) {
            return left.ID - right.ID;
        }
    };
}

