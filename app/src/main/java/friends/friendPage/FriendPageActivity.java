package friends.friendPage;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import Utils.RetroFit.RetroFitInterface;
import Utils.Constants;
import Utils.RetroFit.ToGet;
import Utils.RetroFit.ToPost;
import derekhsieh.derekhsiehapp.R;
import friends.friendList.FriendListActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by phoenix on 12/23/15.
 * Activity where the user can take a new picture, send a new topic, or rate and image
 */
// TODO: I could not come up with a better name for the activity for the life of me, so I'm open to any suggestions.
public class FriendPageActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Context context = this;
    private Bitmap imageBitmap = null;
    private ImageView cameraImageView;
    private String user;
    private String friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        this.user = extras.getString(Constants.username);
        this.friend = extras.getString("friend");
        String friendsImage = friend + "'s image";

        setContentView(R.layout.activity_friend_page);

        TextView friendNameTextView = (TextView) findViewById(R.id.friendNameTextView);
        friendNameTextView.setText(friendsImage);

        ToGet postMethod = RetroFitInterface.createToGet();
        Call<FriendPageResponse> call = postMethod.getFriendPageResponse("GetFriendPage", user, friend);
        call.enqueue(new Callback<FriendPageResponse>() {
            @Override
            public void onResponse(Call<FriendPageResponse> call, Response<FriendPageResponse> response) {
                if (response.isSuccessful()) {
                    FriendPageResponse friendPageResponse = response.body();
                    TextView avgScoreValue = (TextView) findViewById(R.id.avgScoreValue);
                    avgScoreValue.setText(String.valueOf(friendPageResponse.getAvgHuntScore()));
                    TextView huntPlayedValued = (TextView) findViewById(R.id.HuntsPlayedValue);
                    huntPlayedValued.setText(String.valueOf(friendPageResponse.getHuntsPlayed()));
                }else{
                    Log.e("Error", "Could not login in properly");
                }
            }

            @Override
            public void onFailure(Call<FriendPageResponse> call, Throwable t) {
                Log.e("Error", "FAILURE");
            }
        });
        cameraImageView = (ImageView) findViewById(R.id.imageView);
       // getImage();

        Button takeImageButton = (Button) findViewById(R.id.takeImageButton);
        takeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starts default android camera app
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        Button sendNewTopicButton = (Button) findViewById(R.id.sendNewTopicButton);
        sendNewTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opens a dialog where the user can enter a new topic
                NewTopicDialog topicDialog = new NewTopicDialog(context);
                topicDialog.show();
                topicDialog.setDialogResult(new NewTopicDialog.OnNewTopicDialogResult() {
                    @Override
                    public void finish(String result) {
                        ToPost toPost = RetroFitInterface.createToPost();
                        Call<Boolean> call = toPost.postTopic("AddTopic", new TopicRequest(user, friend, result, System.currentTimeMillis()));
                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "SENT NEW TOPIC. YISSSS!!! ", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(getApplicationContext(), "Could not send topic ", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {

                            }
                        });
                    }
                });
            }
        });

        Button rateImageButton = (Button) findViewById(R.id.rateImageButton);
        rateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Opens a dialog where the user can select a rating
                ImageRatingDialog ratingDialog = new ImageRatingDialog(context);
                ratingDialog.show();
                ratingDialog.setDialogResult(new ImageRatingDialog.OnImageRatingDialogResult() {
                    @Override
                    public void finish(Float result) {
                        // TODO: Implement on server side before uncommenting
                        ToPost toPost = RetroFitInterface.createToPost();
                        Call<Boolean> call = toPost.postRating("AddRating", new RatingRequest(user, friend, result, System.currentTimeMillis()));
                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "SENT RATING. AWWWWWW YEAAA!!! ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Could not send rating ", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {

                            }
                        });
                    }
                });
            }
        });
    }

    private void saveImage() {
        if (imageBitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            String bitmapString = Base64.encodeToString(byteArray, Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
        }
    }

    private void getImage() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
        Handles the return from the android default camera app
         */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            saveImage();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("image", imageBitmap);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        /*
        Ensures that when you rotate the device, the image will still be shown
        */
        super.onRestoreInstanceState(savedInstanceState);
        imageBitmap = (Bitmap) savedInstanceState.get("image");
        cameraImageView.setImageBitmap(imageBitmap);
    }

    public void onBackPressed(View v) {
        Intent backToFriendsList = new Intent(this, FriendListActivity.class);
        backToFriendsList.putExtra(Constants.username, user);
        context.startActivity(backToFriendsList);
    }


    public static void longInfo(String str) {
        if (str.length() > 4000) {
            System.out.println(str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            System.out.println(str);
    }

}
