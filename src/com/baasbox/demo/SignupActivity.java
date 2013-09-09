package com.baasbox.demo;

import org.json.JSONException;
import org.json.JSONObject;

import com.baasbox.android.BAASBoxClientException;
import com.baasbox.android.BAASBoxException;
import com.baasbox.android.BAASBoxResult;
import com.baasbox.android.BAASBoxServerException;
import com.baasbox.demo.util.AlertUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

public class SignupActivity extends Activity {

	private SignupTask signupTask;
	
	private EditText emailEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private Button signupButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setTitle("Signup");
		
		setContentView(R.layout.activity_signup);
		
		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		emailEditText = (EditText) findViewById(R.id.email);
		signupButton = (Button) findViewById(R.id.signupButton);
		
		signupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String email = emailEditText.getText().toString();
				
				onClickSignup(email, username, password);
			}
		});
	}
	
	private void onUserSignedUp() {
		Intent intent = new Intent(this, AddressBookActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		
		finish();
	}
	
	protected void onClickSignup(String email, String username, String password) {
		signupTask = new SignupTask();
		
		JSONObject user = new JSONObject();
		JSONObject visibleByTheUser = new JSONObject();
		
		try {
			visibleByTheUser.put("email", email);
			user.put("username", username);
			user.put("password", password);
			user.put("visibleByTheUser", visibleByTheUser);
		} catch (JSONException e) {
			throw new Error(e);
		}
		
		
		signupTask.execute(user);
	}

	protected void onSignup(BAASBoxResult<Void> result) {
		try {
			result.get();
			onUserSignedUp();
		} catch (BAASBoxClientException e) {
			if (e.httpStatus == 400) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(true);
				builder.setTitle("Signup failed");
				builder.setMessage("Insert username and password");
				builder.setNegativeButton("Ok", null);
				builder.create().show();
			} else {
				AlertUtils.showErrorAlert(this, e);
			}
		} catch (BAASBoxServerException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BAASBoxException e) {
			AlertUtils.showErrorAlert(this, e);
		}
	}

	public class SignupTask extends AsyncTask<JSONObject, Void, BAASBoxResult<Void>> {
		
		@Override
		protected void onPreExecute() {
			signupButton.setEnabled(false);
		}
		
		@Override
		protected BAASBoxResult<Void> doInBackground(JSONObject... params) {
			return App.bbox.signup(params[0]);
		}

		@Override
		protected void onPostExecute(BAASBoxResult<Void> result) {
			signupButton.setEnabled(true);
			onSignup(result);
		}
	}

}
