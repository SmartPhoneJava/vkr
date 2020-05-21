import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.ArrayList;

class Projects {
    @SerializedName("projects")
    ArrayList<ProjectWithMembers> projects;
}

public class Project {
    @SerializedName("id")
    public int ID;
    @SerializedName("name")
    public String Name;
    @SerializedName("public_access")
    public Boolean PublicAccess;
    @SerializedName("company_access")
    public Boolean CompanyAccess;
    @SerializedName("public_edit")
    public Boolean PublicEdit;
    @SerializedName("company_edit")
    public Boolean CompanyEdit;
    @SerializedName("about")
    public String About;
    @SerializedName("add")
    public Date Add;
    @SerializedName("user_confirmed")
    public Boolean UserConfirmed;
    @SerializedName("project_confirmed")
    public Boolean ProjectConfirmed;
    @SerializedName("edit")
    public Date Edit;
    @SerializedName("editor_id")
    public int EditorID;
    @SerializedName("members_amount")
    public int MembersAmount;
    @SerializedName("owners_Amount")
    public int OwnersAmount;
    @SerializedName("you_owner")
    public Boolean YouOwner;
    @SerializedName("scenes_amount")
    public int ScenesAmount;

    Project() {

    }
    Project(int id, String name, Boolean publicAccess, Boolean publicEdit, String about, Date add, Boolean userConfirmed,
            Boolean projectConfirmed, Date edit, int editorID, int membersAmount, int ownersAmount, Boolean youOwner,
            int scenesAmount) {
        ID = id;
        Name = name;
        PublicAccess = publicAccess;
        PublicEdit = publicEdit;
        About = about;
        Add = add;
        UserConfirmed = userConfirmed;
        ProjectConfirmed = projectConfirmed;
        Edit = edit;
        EditorID = editorID;
        MembersAmount = membersAmount;
        OwnersAmount = ownersAmount;
        YouOwner = youOwner;
        ScenesAmount = scenesAmount;
    }
}