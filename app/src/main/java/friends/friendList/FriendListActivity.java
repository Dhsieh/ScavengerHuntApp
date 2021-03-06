package friends.friendList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Utils.Constants;
import Utils.RetroFit.RetroFitInterface;
import Utils.RetroFit.ToGet;
import mainPage.MainPageActivity;
import derekhsieh.derekhsiehapp.R;
import retrofit2.Call;
import retrofit2.Callback;


public class FriendListActivity extends ListActivity {
    private List<String> friendList;
    private String user;
    private FriendListAdapter adapter;

    //Just to store number of FriendRequests when going back to main page
    private int numFriendRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            this.friendList = savedInstanceState.getStringArrayList("friend_list");
            this.user = savedInstanceState.getString(Constants.username);
        } else {
            this.user = extras.getString(Constants.username);
        }

        ToGet toGet = RetroFitInterface.createToGet();
        Call<List<String>> call = toGet.getListForUser("GetFriends", user);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, retrofit2.Response<List<String>> response) {
                if(response.isSuccessful()){
                    friendList = response.body();
                    setContentView(R.layout.activity_friend_list);
                    TextView view = (TextView) findViewById(R.id.Username);
                    view.setText(user);
                    if(friendList != null)
                        adapter = new FriendListAdapter(FriendListActivity.this, user,  friendList);
                    else
                        adapter = new FriendListAdapter(FriendListActivity.this, user,
                                new ArrayList<>(Arrays.asList(new String[]{"quiz", "help"})));
                    setListAdapter(adapter);
                }else{

                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("friend_list", (ArrayList<String>) this.friendList);
        savedInstanceState.putString("username", this.user);
    }

    public void onBackPressed(View v){
        Intent goBackToMainPage = new Intent(this, MainPageActivity.class);
        goBackToMainPage.putExtra(Constants.username, user);
        goBackToMainPage.putExtra(Constants.friendRequests, numFriendRequest);
    }
}
