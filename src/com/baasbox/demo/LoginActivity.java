package com.baasbox.demo;

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

public class LoginActivity extends Activity {

	private LoginTask loginTask;
	
	private EditText usernameEditText;
	private EditText passwordEditText;
	private Button loginButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (App.bbox.isUserLoggedIn()) {
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

	protected void onLogin(BAASBoxResult<Void> result) {
		try {
			result.get();
			onUserLogged();
		} catch (BAASBoxClientException e) {
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
		} catch (BAASBoxServerException e) {
			AlertUtils.showErrorAlert(this, e);
		} catch (BAASBoxException e) {
			AlertUtils.showErrorAlert(this, e);
		}
	}

	public class LoginTask extends AsyncTask<String, Void, BAASBoxResult<Void>> {
		
		@Override
		protected void onPreExecute() {
			loginButton.setEnabled(false);
		}
		
		@Override
		protected BAASBoxResult<Void> doInBackground(String... params) {
			return App.bbox.login(params[0], params[1]);
		}

		@Override
		protected void onPostExecute(BAASBoxResult<Void> result) {
			loginButton.setEnabled(true);
			onLogin(result);
		}
	}

}
