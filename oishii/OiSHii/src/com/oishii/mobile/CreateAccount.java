package com.oishii.mobile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.madrobot.di.plist.NSDictionary;
import com.madrobot.di.plist.NSNumber;
import com.madrobot.di.plist.NSObject;
import com.madrobot.di.plist.PropertyListParser;
import com.oishii.mobile.beans.RegistrationResult;
import com.oishii.mobile.util.HttpSettings;
import com.oishii.mobile.util.HttpSettings.HttpMethod;
import com.oishii.mobile.util.TextUtils;
import com.oishii.mobile.util.tasks.HttpRequestTask;
import com.oishii.mobile.util.tasks.HttpRequestWrapper;
import com.oishii.mobile.util.tasks.IHttpCallback;

public class CreateAccount extends OishiiBaseActivity {

	private EditText title;
	private EditText fName;
	private EditText lName;
	private EditText email;
	private EditText pwd;
	private EditText repPwd;

	private final int OPERATION_REGISTER = 54;

	@Override
	protected void hookInChildViews() {
		findViewById(R.id.btnCreateAcc).setOnClickListener(createAccListener);
		title = (EditText) findViewById(R.id.field_Title);
		fName = (EditText) findViewById(R.id.fld_FirstName);
		lName = (EditText) findViewById(R.id.fld_LastName);
		email = (EditText) findViewById(R.id.fld_Email);
		pwd = (EditText) findViewById(R.id.fld_Password);
		repPwd = (EditText) findViewById(R.id.fld_RepPassword);
	}

	View.OnClickListener createAccListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			boolean hasErrors = checkForErrors();
			if (!hasErrors) {
				createNewAccount();
			}
		}
	};

	private void createNewAccount() {
		HttpRequestWrapper requestWrapper = new HttpRequestWrapper();
		requestWrapper.requestURI = ApplicationConstants.API_REGISTRATION;
		requestWrapper.callback = createAccountCallback;
		requestWrapper.operationID = OPERATION_REGISTER;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair param = new BasicNameValuePair("title", title.getText()
				.toString());
		params.add(param);
		param = new BasicNameValuePair("fname", fName.getText().toString());
		params.add(param);
		param = new BasicNameValuePair("lname", lName.getText().toString());
		params.add(param);
		param = new BasicNameValuePair("email", email.getText().toString());
		params.add(param);
		param = new BasicNameValuePair("pswd", pwd.getText().toString());
		params.add(param);
		param = new BasicNameValuePair("is_subscribed", "1");
		params.add(param);
		HttpSettings settings = new HttpSettings();
		settings.setHttpMethod(HttpMethod.HTTP_POST);
		requestWrapper.httpParams = params;
		requestWrapper.httpSettings = settings;
		showDialog(getString(R.string.loading_register));
		new HttpRequestTask().execute(requestWrapper);
	}




	

	private RegistrationResult getResult(NSObject object) {
		NSDictionary dict = (NSDictionary) object;
		RegistrationResult res=new RegistrationResult();
		NSNumber sucessFalg=(NSNumber) dict.objectForKey("success");
		res.setSucess(sucessFalg.boolValue());
		res.setErrorMessage( dict.objectForKey("message").toString());
		return res;
	}

	IHttpCallback createAccountCallback = new IHttpCallback() {

		@Override
		public Object populateBean(InputStream is, int operationId) {
			NSObject object = null;
			try {
				object = PropertyListParser.parse(is);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (object != null) {
				RegistrationResult result = getResult(object);
				return result;
			} else {
				return null;
			}

		}

		@Override
		public void onFailure(int message, int operationID) {
		}

		@Override
		public void bindUI(Object t, int operationId) {
			hideDialog();
			RegistrationResult result=(RegistrationResult) t;
			if(!result.isSucess()){
				showErrorDialog(result.getErrorMessage());
			}else{
				//TODO launch login screen  . and set the src screen
				Intent intent=new Intent(CreateAccount.this,Login.class);
				intent.putExtra(OutOfSession.SRC_KEY, getIntent().getIntExtra(OutOfSession.SRC_KEY,0));
				startActivity(intent);
			}
		}
	};

	@Override
	protected int getChildViewLayout() {
		return R.layout.create_account;
	}

	@Override
	protected String getTitleString() {
		return getString(R.string.create_acc_title);
	}

	@Override
	protected int getSreenID() {
		return R.id.myacc;
	}

	private boolean checkForErrors() {
		String errors = validate();
		if (errors.length() > 0) {

			Toast toast = Toast.makeText(CreateAccount.this, errors, 8000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return true;
		}
		return false;
	}

	

	private String validate() {
		// Level 1 validation
		String newline = System.getProperty("line.separator");
		StringBuilder errors = new StringBuilder();
		if (!hasValidText(title)) {
			errors.append(getString(R.string.error_no_title));
			errors.append(newline);
		}
		if (!hasValidText(fName)) {
			errors.append(getString(R.string.error_no_fn));
			errors.append(newline);
		}
		if (!hasValidText(lName)) {
			errors.append(getString(R.string.error_no_ln));
			errors.append(newline);
		}
		if (!hasValidText(email)) {
			errors.append(getString(R.string.error_no_email));
			errors.append(newline);
		}
		if (!hasValidText(pwd)) {
			errors.append(getString(R.string.error_no_pwd));
			errors.append(newline);
		}

		if (!hasValidText(repPwd)) {
			errors.append(getString(R.string.error_no_rpwd));
			errors.append(newline);
		}
		if (errors.length() > 0)
			return errors.toString();

		// level 2 validation
		if (!TextUtils.isValidEmailAddress(email.getText().toString())) {
			errors.append(getString(R.string.error_invalid_email));
			errors.append(newline);
		}
		if (!(pwd.getText().toString().equals(repPwd.getText().toString()))) {
			errors.append(getString(R.string.error_pwd_match));
		}

		return errors.toString();
	}
}