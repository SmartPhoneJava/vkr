import com.google.gson.annotations.SerializedName;

import java.util.Comparator;
import java.util.Date;

public class UserInProject {
    @SerializedName("id")
    public int ID;
    @SerializedName("position")
    public String Position;
    @SerializedName("user_id")
    public int UserID;
    @SerializedName("token_id")
    public int TokenID;
    @SerializedName("project_id")
    public int ProjectID;
    @SerializedName("from")
    public Date From;
    @SerializedName("to")
    public Date To;
    @SerializedName("user_confirmed")
    public Boolean UserConfirmed;
    @SerializedName("project_confirmed")
    public Boolean ProjectConfirmed;

    UserInProject(int id, String position, int userID, int tokenID, int projectID,
                  Date from, Date to, Boolean userConfirmed, Boolean projectConfirmed) {
        ID = id;
        Position = position;
        UserID = userID;
        TokenID = tokenID;
        ProjectID = projectID;
        From = from;
        To = to;
        UserConfirmed = userConfirmed;
        ProjectConfirmed = projectConfirmed;
    }

    Boolean confirmed() {
        return UserConfirmed && ProjectConfirmed;
    }
}
