import com.google.gson.annotations.SerializedName;

public class ProjectToken {
    @SerializedName("id")
    public int ID;
    @SerializedName("owner")
    public Boolean Owner;
    @SerializedName("edit_name")
    public Boolean EditName;
    @SerializedName("edit_info")
    public Boolean EditInfo;
    @SerializedName("edit_access")
    public Boolean EditAccess;
    @SerializedName("edit_scene")
    public Boolean EditScene;
    @SerializedName("edit_members_list")
    public Boolean EditMembersList;
    @SerializedName("edit_members_token")
    public Boolean EditMembersToken;

    ProjectToken(int id, Boolean owner, Boolean editName, Boolean editInfo, Boolean editAccess,
                 Boolean editScene, Boolean editMembersList, Boolean editMembersToken) {
        ID = id;
        Owner = owner;
        EditName = editName;
        EditInfo = editInfo;
        EditAccess = editAccess;
        EditScene = editScene;
        EditMembersList = editMembersList;
        EditMembersToken = editMembersToken;
    }
}
