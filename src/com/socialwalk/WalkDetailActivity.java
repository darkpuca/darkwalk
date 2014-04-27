package com.socialwalk;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.socialwalk.dataclass.WalkHistory;

public class WalkDetailActivity extends Activity implements OnClickListener
{
	private WalkHistory history = null;
	private String targetFileName = null;
	private Button facebookButton;
	
    
    private final String PENDING_ACTION_BUNDLE_KEY = "com.socialwalk:PendingAction";
    private static final String PERMISSION = "publish_actions";
    
    private enum PendingAction
    {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    private UiLifecycleHelper uiHelper;
    private PendingAction pendingAction = PendingAction.NONE;
    private boolean canPresentShareDialog;
    private GraphUser user;

    private Session.StatusCallback callback = new Session.StatusCallback()
    {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

		setContentView(R.layout.activity_walk_detail);
		
		this.targetFileName = getIntent().getStringExtra(Globals.EXTRA_KEY_FILENAME);
		this.history = Utils.GetDefaultTool().WalkHistoryFromFile(this, this.targetFileName);
		
		updateDetails();
		
		RelativeLayout routeButtonLayout = (RelativeLayout)findViewById(R.id.routeButtonLayout);
		routeButtonLayout.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), WalkRouteActivity.class);
				i.putExtra(Globals.EXTRA_KEY_FILENAME, targetFileName);
				startActivity(i);
			}
		});
		
		Button btnClose = (Button)findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
		facebookButton = (Button)findViewById(R.id.btnFacebook);
		facebookButton.setOnClickListener(this);
	
        canPresentShareDialog = FacebookDialog.canPresentShareDialog(this, FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
	}

    @Override
	protected void onResume()
    {
		super.onResume();
        uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() 
	    {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	        }
	    });
	
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		uiHelper.onPause();
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException))
        {
        	// TODO: 등록 실패 메세지 표시.
            pendingAction = PendingAction.NONE;
        }
        else if (state == SessionState.OPENED_TOKEN_UPDATED)
        {
            handlePendingAction();
        }
    }
    
	private void updateDetails()
	{
		if (null == this.history) return;
		
		TextView distance = (TextView)findViewById(R.id.distance);
		TextView walkTime = (TextView)findViewById(R.id.walkTime);
		TextView walkSpeed = (TextView)findViewById(R.id.walkSpeed);
		TextView startTime = (TextView)findViewById(R.id.startTime);
		TextView endTime = (TextView)findViewById(R.id.endTime);
		TextView calories = (TextView)findViewById(R.id.calories);
		
		distance.setText(this.history.TotalDistanceString());
		walkTime.setText(this.history.TotalWalkingTimeString());
		walkSpeed.setText(this.history.AverageSpeed());

		SimpleDateFormat formatter = new SimpleDateFormat(Globals.DATETIME_FORMAT_FOR_UI, Locale.US);
		startTime.setText(formatter.format(this.history.StartTime));
		endTime.setText(formatter.format(this.history.EndTime));
		calories.setText(this.history.TotalCalories() + " " + getResources().getString(R.string.CALORIES_UNIT));
	}

	@Override
	public void onClick(View view)
	{
		if (view.equals(facebookButton))
		{
			if (FacebookDialog.canPresentShareDialog(getApplicationContext(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) 
			{
				// Publish the post using the Share Dialog
				FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this).setLink("https://developers.facebook.com/android").build();
				uiHelper.trackPendingDialogCall(shareDialog.present());
			}
			else
			{
				// 	Fallback. For example, publish the post using the Feed Dialog
			}
		}
	}
	
    private boolean hasPublishPermission()
    {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }
    
    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction()
    {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction)
        {
            case POST_PHOTO:
                postPhoto();
                break;
            case POST_STATUS_UPDATE:
                postStatusUpdate();
                break;
        }
    }

    private void performPublish(PendingAction action, boolean allowNoSession)
    {
        Session session = Session.getActiveSession();
        if (session != null) {
            pendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSION));
                return;
            }
        }

        if (allowNoSession) {
            pendingAction = action;
            handlePendingAction();
        }
    }

    private void postPhoto() {
        if (hasPublishPermission()) {
            Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon);
            Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                    showPublishResult(getString(R.string.MSG_FACEBOOK_POST_PHOTO), response.getGraphObject(), response.getError());
                }
            });
            request.executeAsync();
        } else {
            pendingAction = PendingAction.POST_PHOTO;
        }
    }
    
    private FacebookDialog.ShareDialogBuilder createShareDialogBuilder()
    {
        return new FacebookDialog.ShareDialogBuilder(this)
                .setName("Hello Facebook")
                .setDescription("The 'Hello Facebook' sample application showcases simple Facebook integration")
                .setLink("http://developers.facebook.com/android");
    }

    private void postStatusUpdate()
    {
        if (canPresentShareDialog)
        {
            FacebookDialog shareDialog = createShareDialogBuilder().build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        }
        else if (user != null && hasPublishPermission())
        {
            final String message = "socialwalk test message.";
            Request request = Request.newStatusUpdateRequest(Session.getActiveSession(), message, null, null, new Request.Callback()
            {
            	@Override
            	public void onCompleted(Response response)
            	{
            		showPublishResult(message, response.getGraphObject(), response.getError());
            	}
            });
            request.executeAsync();
        } 
        else
        {
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }
    

    private interface GraphObjectWithId extends GraphObject
    {
        String getId();
    }

    private void showPublishResult(String message, GraphObject result, FacebookRequestError error)
    {
        String title = null;
        String alertMessage = null;
        if (error == null) {
            title = getString(R.string.SUCCESS);
            String id = result.cast(GraphObjectWithId.class).getId();
            alertMessage = getString(R.string.MSG_FACEBOOK_PUBLISHED, message, id);
        } else {
            title = getString(R.string.FAIL);
            alertMessage = error.getErrorMessage();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton(R.string.OK, null)
                .show();
    }
}
