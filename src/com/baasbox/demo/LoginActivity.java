package com.baasbox.demo;

import com.baasbox.android.BaasClientException;
import com.baasbox.android.BaasException;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasServerException;
import com.baasbox.android.BaasUser;
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

public class LoginActivity extends Activity {

	private LoginTask loginTask;
	
	private EditText usernameEditText;
	private EditText passwordEditText;
	private Button loginButton;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if ( BaasUser.current().isAuthentcated()) {
			onUserLogged();
			return;
		}
		
		setTitle("Login");
		
		setContentView(R.layout.activity_login);
		
		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		loginButton = (Button) findViewById(R.id.loginButton);
		
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				
				onClickLogin(username, password);
			}
		});
		
		findViewById(R.id.signupLink).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickSignup();
			}
		});
	}
	
	private void onUserLogged() {
		Intent intent = new Intent(this, AddressBookActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		
		finish();
	}
	
	protected void onClickSignup() {
		Intent intent = new Intent(this, SignupActivity.class);
		startActivity(intent);
	}
	
	protected void onClickLogin(String username, String password) {
		loginTask = new LoginTask();
		loginTask.execute(username, password);
	}

	protected void onLogin(BaasResult<BaasUser> result) {
		try {
			result.get();
			onUserLogged();
		} catch (BaasClientException e) {
			if (e.httpStatus == 401) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(true);
				builder.setTitle("Login failed");
				builder.setMessage("Invalid username or password");
				builder.setNegativeButton("Cancel", null);
				builder.create().show();
			} else {
				AlertUtils.showErrorAlert(this, e);
			}
		} catch (BaasServerException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BaasException e) {
			AlertUtils.showErrorAlert(this, e);
		}
	}

	public class LoginTask extends AsyncTask<String, Void, BaasResult<BaasUser>> {
		
		@Override
		protected void onPreExecute() {
			loginButton.setEnabled(false);
		}
		
		@Override
		protected BaasResult<BaasUser> doInBackground(String... params) {
			BaasUser user = BaasUser.withUserName(params[0]);
			user.setPassword(params[1]);
			return user.loginSync();
		}

		@Override
		protected void onPostExecute(BaasResult<BaasUser> result) {
			loginButton.setEnabled(true);
			onLogin(result);
		}
	}

}
